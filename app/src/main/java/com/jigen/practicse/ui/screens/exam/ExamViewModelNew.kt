package com.jigen.practicse.ui.screens.exam

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.normalizeTrackKey
import com.jigen.practicse.data.local.trackKeyToLabel
import com.jigen.practicse.data.entity.QuestionEntity
import com.jigen.practicse.data.local.ExamConfigStore
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.dao.ErrorReportDao
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.QuestionDao
import com.jigen.practicse.data.local.dao.SessionDao
import com.jigen.practicse.data.local.entity.ErrorReportEntity
import com.jigen.practicse.data.local.entity.SessionEntity
import com.jigen.practicse.data.local.entity.UserProgressEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ExamViewModelNew(
	private val questionDao: QuestionDao,
	private val sessionDao: SessionDao,
	private val progressDao: ProgressDao,
	private val errorReportDao: ErrorReportDao,
	private val context: Context,
	private val sessionMode: String = "new" // "new" or "resume"
) : ViewModel() {

	private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
	val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

	private val _effects = MutableSharedFlow<ExamEffect>(extraBufferCapacity = 1)
	val effects: SharedFlow<ExamEffect> = _effects.asSharedFlow()

	private var timerJob: Job? = null
	private val examConfigStore = ExamConfigStore(context)
	private val appPreferencesStore = AppPreferencesStore(context)

	companion object {
		private const val DEFAULT_EXAM_DURATION_MILLIS = 165L * 60_000L

		fun factory(context: Context, sessionMode: String = "new"): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					if (modelClass.isAssignableFrom(ExamViewModelNew::class.java)) {
						val database = PractiCSEDatabase.getInstance(context)
						@Suppress("UNCHECKED_CAST")
						return ExamViewModelNew(
							database.questionDao(),
							database.sessionDao(),
							database.progressDao(),
							database.errorReportDao(),
							context,
							sessionMode = sessionMode
						) as T
					}
					throw IllegalArgumentException("Unknown ViewModel class")
				}
			}
		}
	}

	init {
		viewModelScope.launch {
			loadExam()
		}
	}

	private suspend fun loadExam() {
		try {
			ensureQuestionsSeeded()

			val (mode, categoryKey) = parseSessionMode(sessionMode)
			val existingSession = if (mode == "resume") sessionDao.getSession() else null
			val resumeCategoryKey = existingSession?.lastCategory?.let(::categoryNameToKey)
			val activeCategoryKey = categoryKey ?: resumeCategoryKey
			val activeTrackKey = normalizeTrackKey(existingSession?.lastTrack ?: appPreferencesStore.getActiveTrackKey())
			val examConfig = examConfigStore.getConfig()
			val allQuestions = questionDao.getAllQuestions()
			val questions = buildQuestionSet(allQuestions, activeCategoryKey, examConfig)
			val sessionCategory = activeCategoryKey?.let { displayCategoryName(it) }
				?: existingSession?.lastCategory
				?: "All Categories"
			val sessionTrackLabel = trackKeyToLabel(activeTrackKey)

			if (questions.isEmpty()) {
				_uiState.value = ExamUiState.Error("No questions available")
				return
			}

			val uiQuestions = questions.map { entity ->
				QuestionUiState(
					id = entity.id,
					category = entity.category,
					text = entity.questionText,
					referenceText = entity.referenceText,
					shuffledOptions = entity.getShuffledOptions(),
					correctAnswer = entity.correctAnswer
				)
			}

			// Determine starting point based on sessionMode
			var startingIndex = 0
			var startingScore = 0

			if (mode == "resume") {
				if (existingSession != null && existingSession.examEndTimeMillis == null) {
					// Only resume if session is active (not completed)
					startingIndex = existingSession.lastQuestionIndex.coerceIn(0, uiQuestions.lastIndex.coerceAtLeast(0))
					startingScore = computeCurrentScore(uiQuestions)
				} else {
					// Session is completed or doesn't exist, create a new one
					createNewSession(sessionCategory, activeTrackKey)
				}
			} else {
				// Create new session for a fresh exam
				createNewSession(sessionCategory, activeTrackKey)
			}

			_uiState.value = ExamUiState.Success(
				questions = uiQuestions,
				currentIndex = startingIndex,
				currentScore = startingScore,
				remainingTimeMillis = DEFAULT_EXAM_DURATION_MILLIS,
				sessionTrack = sessionTrackLabel,
				sessionCategory = sessionCategory
			)

			startTimer()
		} catch (e: Exception) {
			_uiState.value = ExamUiState.Error(e.message ?: "Unknown error")
		}
	}

	private suspend fun createNewSession(sessionCategory: String, trackKey: String) {
		// Clear any existing completed sessions or old sessions
		sessionDao.clearCompletedSessions()

		val newSession = SessionEntity(
			id = 1,
			lastScore = 0,
			lastQuestionIndex = 0,
			lastTrack = normalizeTrackKey(trackKey),
			lastCategory = sessionCategory,
			examEndTimeMillis = null
		)
		sessionDao.upsert(newSession)
	}

	private suspend fun computeCurrentScore(uiQuestions: List<QuestionUiState>): Int {
		var score = 0
		for (question in uiQuestions) {
			val progress = progressDao.getProgress(question.id)
			if (progress?.isCorrect == true) {
				score += 1
			}
		}
		return score
	}

	private fun buildQuestionSet(
		allQuestions: List<QuestionEntity>,
		activeCategoryKey: String?,
		examConfig: com.jigen.practicse.data.local.ExamQuestionConfig
	): List<QuestionEntity> {
		if (activeCategoryKey != null) {
			val filtered = allQuestions.filter { matchesCategory(it, activeCategoryKey) }
			return filtered.shuffled().take(examConfig.countForCategoryKey(activeCategoryKey))
		}

		val requested = examConfig.allExamCount.coerceAtLeast(1)
		return allQuestions.shuffled().take(minOf(requested, allQuestions.size))
	}

	private suspend fun ensureQuestionsSeeded() {
		if (questionDao.countQuestions() > 0) return

		val seedQuestions = loadSeedQuestions()
		if (seedQuestions.isNotEmpty()) {
			questionDao.insertAll(seedQuestions)
		}
	}

	private fun loadSeedQuestions(): List<QuestionEntity> {
		val rawJson = runCatching {
			context.assets.open("question_bank/questions.json").bufferedReader().use { it.readText() }
		}.getOrNull()?.trim().orEmpty()

		if (rawJson.isBlank()) return emptyList()

		val questionsArray = JSONArray(rawJson)
		return buildList {
			for (index in 0 until questionsArray.length()) {
				val item = questionsArray.optJSONObject(index) ?: continue
				item.toQuestionEntity(index + 1)?.let(::add)
			}
		}
	}

	private fun JSONObject.toQuestionEntity(assignedId: Int): QuestionEntity? {
		val questionText = optString("questionText").takeIf { it.isNotBlank() } ?: return null
		val correctAnswer = optString("correctAnswer").takeIf { it.isNotBlank() } ?: return null

		val wrongChoicesArray = optJSONArray("wrongChoices") ?: JSONArray()
		val wrongChoices = buildList {
			for (index in 0 until wrongChoicesArray.length()) {
				wrongChoicesArray.optString(index).takeIf { it.isNotBlank() }?.let(::add)
			}
		}

		// Require at least 3 wrong choices so every question has 4+ total options
		if (wrongChoices.size < 3) return null

		val refText = optString("referenceText")

		return QuestionEntity(
			id = assignedId,
			category = optString("category", "General"),
			subCategory = optString("subCategory", "General"),
			questionText = questionText,
			referenceText = if (refText == "null" || refText.isBlank()) null else refText,
			correctAnswer = correctAnswer,
			wrongChoices = wrongChoices
		)
	}

	fun selectAnswer(selectedText: String) {
		val currentState = _uiState.value
		if (currentState !is ExamUiState.Success) return

		val question = currentState.currentQuestion ?: return
		if (question.id in currentState.evaluatedQuestions) return

		viewModelScope.launch {
			val isCorrect = selectedText.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
			val newScore = if (isCorrect) currentState.currentScore + 1 else currentState.currentScore
			val updatedAnswers = currentState.selectedAnswers.toMutableMap().apply {
				put(question.id, selectedText)
			}
			val updatedEvaluated = currentState.evaluatedQuestions.toMutableSet().apply {
				add(question.id)
			}

			try {
				progressDao.upsert(
					UserProgressEntity(
						questionId = question.id,
						selectedIndex = question.shuffledOptions.indexOf(selectedText).coerceAtLeast(-1),
						isCorrect = isCorrect,
						answeredAtMillis = System.currentTimeMillis(),
						track = normalizeTrackKey(currentState.sessionTrack),
						category = question.category
					)
				)
				sessionDao.getSession()?.let { session ->
					sessionDao.upsert(
						session.copy(
							lastScore = newScore,
							lastQuestionIndex = currentState.currentIndex,
							lastTrack = normalizeTrackKey(currentState.sessionTrack),
							examEndTimeMillis = null
						)
					)
				}
			} catch (e: Exception) {
				// Log but continue
			}

			_uiState.value = currentState.copy(
				currentScore = newScore,
				selectedAnswers = updatedAnswers,
				evaluatedQuestions = updatedEvaluated
			)
		}
	}

	fun goToQuestion(index: Int) {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		val clampedIndex = index.coerceIn(0, currentState.totalQuestions - 1)
		if (clampedIndex == currentState.currentIndex) return

		viewModelScope.launch {
			_uiState.value = currentState.copy(currentIndex = clampedIndex)
			sessionDao.getSession()?.let { session ->
				sessionDao.upsert(
					session.copy(
						lastScore = currentState.currentScore,
						lastQuestionIndex = clampedIndex,
						lastCategory = currentState.sessionCategory,
						lastTrack = normalizeTrackKey(currentState.sessionTrack),
						examEndTimeMillis = null
					)
				)
			}
		}
	}

	fun previousQuestion() {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		goToQuestion(currentState.currentIndex - 1)
	}

	fun nextQuestion() {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		if (currentState.isLastQuestion) {
			completeExam(currentState)
		} else {
			goToQuestion(currentState.currentIndex + 1)
		}
	}

	fun toggleFlagCurrentQuestion() {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		val questionId = currentState.currentQuestion?.id ?: return
		val updatedFlags = currentState.flaggedQuestionIds.toMutableSet().apply {
			if (contains(questionId)) remove(questionId) else add(questionId)
		}
		_uiState.value = currentState.copy(flaggedQuestionIds = updatedFlags)
	}

	private fun completeExam(state: ExamUiState.Success) {
		viewModelScope.launch {
			stopTimer()
			sessionDao.getSession()?.let { session ->
				sessionDao.upsert(
					session.copy(
						lastScore = state.currentScore,
						lastQuestionIndex = state.currentIndex,
						lastTrack = normalizeTrackKey(state.sessionTrack),
						examEndTimeMillis = System.currentTimeMillis()
					)
				)
			}

			val displayName = appPreferencesStore.getDisplayName()
			PractiCSEDatabase.getInstance(context).leaderboardDao().upsert(
				com.jigen.practicse.data.local.entity.LeaderboardEntryEntity(
					userName = displayName,
					totalScore = state.currentScore,
					lastUpdatedMillis = System.currentTimeMillis()
				)
			)

			val scorableQuestions = state.totalQuestions - state.voidedQuestions.size

			_uiState.value = ExamUiState.Completed(
				totalScore = state.currentScore,
				totalQuestions = scorableQuestions
			)
			_effects.tryEmit(ExamEffect.ExamCompleted)
		}
	}

	fun reportCurrentQuestion() {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		val currentQuestion = currentState.currentQuestion ?: return

		viewModelScope.launch {
			try {
				errorReportDao.insert(
					ErrorReportEntity(
						questionId = currentQuestion.id,
						reportedAtMillis = System.currentTimeMillis()
					)
				)

				val updatedVoided = currentState.voidedQuestions.toMutableSet().apply {
					add(currentQuestion.id)
				}
				_uiState.value = currentState.copy(voidedQuestions = updatedVoided)

				// Auto-advance logic (glides via existing LaunchedEffect in UI)
				if (currentState.isLastQuestion) {
					completeExam(currentState.copy(voidedQuestions = updatedVoided))
				} else {
					goToQuestion(currentState.currentIndex + 1)
				}

			} catch (e: Exception) {
				// Log but don't interrupt exam
			}
		}
	}

	private fun startTimer() {
		timerJob = viewModelScope.launch {
			var remaining = 165L * 60_000L
			while (remaining > 0) {
				delay(1000)
				remaining -= 1000
				val state = _uiState.value
				if (state is ExamUiState.Success) {
					_uiState.value = state.copy(remainingTimeMillis = remaining)
				}
			}
			val state = _uiState.value as? ExamUiState.Success ?: return@launch
			_effects.tryEmit(ExamEffect.TimeExpired)
			completeExam(state)
		}
	}

	private fun stopTimer() {
		timerJob?.cancel()
		timerJob = null
	}

	override fun onCleared() {
		super.onCleared()
		stopTimer()
	}

	private fun parseSessionMode(raw: String): Pair<String, String?> {
		if (raw.startsWith("new@")) {
			return "new" to raw.substringAfter("@").ifBlank { null }
		}
		return raw to null
	}

	private fun displayCategoryName(categoryKey: String): String {
		return when (categoryKey) {
			"numerical_ability" -> "Numerical Ability"
			"verbal_ability" -> "Verbal Ability"
			"general_information" -> "General Information"
			else -> "Professional"
		}
	}

	private fun categoryNameToKey(categoryName: String?): String? {
		return when (categoryName?.lowercase()?.trim()) {
			"numerical ability" -> "numerical_ability"
			"verbal ability" -> "verbal_ability"
			"general information" -> "general_information"
			else -> null
		}
	}

	private fun matchesCategory(question: QuestionEntity, categoryKey: String): Boolean {
		val value = question.category.lowercase()
		return when (categoryKey) {
			"numerical_ability" -> "numerical" in value
			"verbal_ability" -> "verbal" in value
			"general_information" -> "general" in value
			else -> true
		}
	}

	sealed class ExamEffect {
		data class NavigateToPage(val index: Int) : ExamEffect()
		object ExamCompleted : ExamEffect()
		object TimeExpired : ExamEffect()
		object QuestionReported : ExamEffect()
	}
}