package com.jigen.practicse.ui.screens.exam

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.entity.QuestionEntity
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.ExamConfigStore
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.dao.ErrorReportDao
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.QuestionDao
import com.jigen.practicse.data.local.dao.SessionDao
import com.jigen.practicse.data.local.entity.ErrorReportEntity
import com.jigen.practicse.data.local.entity.SessionEntity
import com.jigen.practicse.data.local.entity.UserProgressEntity
import com.jigen.practicse.data.local.normalizeTrackKey
import com.jigen.practicse.data.local.trackKeyToLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class ExamViewModelNew(
	private val questionDao: QuestionDao,
	private val sessionDao: SessionDao,
	private val progressDao: ProgressDao,
	private val errorReportDao: ErrorReportDao,
	private val context: Context,
	private val sessionMode: String = "new"
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
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					if (modelClass.isAssignableFrom(ExamViewModelNew::class.java)) {
						val database = PractiCSEDatabase.getInstance(context)
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
		viewModelScope.launch { loadExam() }
	}

	private suspend fun loadExam() {
		try {
			ensureQuestionsSeeded()

			val (mode, categoryKey) = parseSessionMode(sessionMode)
			val existingSession = if (mode == "resume") sessionDao.getSession() else null
			val activeCategoryKey = categoryKey ?: existingSession?.lastCategory?.let(::categoryNameToKey)
			val activeTrackKey = normalizeTrackKey(existingSession?.lastTrack ?: appPreferencesStore.getActiveTrackKey())
			val sessionSeed = existingSession?.sessionSeed?.takeIf { it != 0L } ?: System.currentTimeMillis()
			val examConfig = examConfigStore.getConfig()

			val questions = if (mode == "resume" && !existingSession?.questionIdsJson.isNullOrBlank()) {
				loadQuestionsFromSavedOrder(existingSession.questionIdsJson)
			} else {
				buildQuestionSet(questionDao.getAllQuestions(), activeCategoryKey, examConfig, sessionSeed)
			}

			if (questions.isEmpty()) {
				_uiState.value = ExamUiState.Error("No questions available")
				return
			}

			val sessionCategory = activeCategoryKey?.let { displayCategoryName(it) }
				?: existingSession?.lastCategory
				?: "All Categories"
			val sessionTrackLabel = trackKeyToLabel(activeTrackKey)
			val uiQuestions = questions.map { entity ->
				QuestionUiState(
					id = entity.id,
					category = entity.category,
					text = entity.questionText,
					referenceText = entity.referenceText,
					shuffledOptions = entity.getShuffledOptions(sessionSeed),
					correctAnswer = entity.correctAnswer
				)
			}

			val restoredSelections = if (mode == "resume") buildSelectionState(uiQuestions) else emptyMap<Int, String>() to emptySet<Int>()
			val startingTime = existingSession?.remainingTimeMillis?.takeIf { it > 0L } ?: DEFAULT_EXAM_DURATION_MILLIS
			val startingIndex = if (mode == "resume" && existingSession != null && existingSession.examEndTimeMillis == null) {
				existingSession.lastQuestionIndex.coerceIn(0, uiQuestions.lastIndex.coerceAtLeast(0))
			} else {
				0
			}
			val startingScore = if (mode == "resume" && existingSession != null && existingSession.examEndTimeMillis == null) {
				existingSession.lastScore
			} else {
				0
			}
			val startingFlags = if (mode == "resume") existingSession?.flaggedQuestionIdsJson.toIntSet() else emptySet()

			if (mode == "new") {
				createNewSession(sessionCategory, activeTrackKey, questions, sessionSeed, overwriteProgress = true)
			} else if (existingSession == null || existingSession.examEndTimeMillis != null) {
				createNewSession(sessionCategory, activeTrackKey, questions, sessionSeed, overwriteProgress = false)
			}

			_uiState.value = ExamUiState.Success(
				questions = uiQuestions,
				currentIndex = startingIndex,
				currentScore = startingScore,
				remainingTimeMillis = startingTime,
				sessionTrack = sessionTrackLabel,
				sessionCategory = sessionCategory,
				selectedAnswers = restoredSelections.first,
				evaluatedQuestions = restoredSelections.second,
				flaggedQuestionIds = startingFlags
			)

			startTimer()
		} catch (e: Exception) {
			_uiState.value = ExamUiState.Error(e.message ?: "Unknown error")
		}
	}

	private suspend fun createNewSession(
		sessionCategory: String,
		trackKey: String,
		questions: List<QuestionEntity>,
		sessionSeed: Long,
		overwriteProgress: Boolean
	) {
		if (overwriteProgress) {
			progressDao.clearAllProgress()
		}
		sessionDao.clearCompletedSessions()
		sessionDao.upsert(
			SessionEntity(
				id = 1,
				lastScore = 0,
				lastTrack = normalizeTrackKey(trackKey),
				lastCategory = sessionCategory,
				lastQuestionIndex = 0,
				remainingTimeMillis = DEFAULT_EXAM_DURATION_MILLIS,
				sessionSeed = sessionSeed,
				questionIdsJson = questions.map { it.id }.toJsonArrayString(),
				flaggedQuestionIdsJson = null,
				examEndTimeMillis = null
			)
		)
	}

	private suspend fun buildSelectionState(uiQuestions: List<QuestionUiState>): Pair<Map<Int, String>, Set<Int>> {
		val progressMap = progressDao.getAllProgress().associateBy { it.questionId }
		val selectedAnswers = mutableMapOf<Int, String>()
		val evaluatedQuestions = mutableSetOf<Int>()

		uiQuestions.forEach { question ->
			val progress = progressMap[question.id] ?: return@forEach
			val selectedText = question.shuffledOptions.getOrNull(progress.selectedIndex)
			if (!selectedText.isNullOrBlank()) {
				selectedAnswers[question.id] = selectedText
				evaluatedQuestions.add(question.id)
			}
		}

		return selectedAnswers to evaluatedQuestions
	}

	private fun buildQuestionSet(
		allQuestions: List<QuestionEntity>,
		activeCategoryKey: String?,
		examConfig: com.jigen.practicse.data.local.ExamQuestionConfig,
		sessionSeed: Long
	): List<QuestionEntity> {
		return if (activeCategoryKey != null) {
			val filtered = allQuestions.filter { matchesCategory(it, activeCategoryKey) }
			filtered.shuffled(Random(sessionSeed)).take(examConfig.countForCategoryKey(activeCategoryKey))
		} else {
			val requested = examConfig.allExamCount.coerceAtLeast(1)
			allQuestions.shuffled(Random(sessionSeed)).take(minOf(requested, allQuestions.size))
		}
	}

	private suspend fun loadQuestionsFromSavedOrder(questionIdsJson: String?): List<QuestionEntity> {
		val ids = questionIdsJson.toIntList()
		if (ids.isEmpty()) return emptyList()
		return buildList {
			ids.forEach { questionId ->
				questionDao.getQuestionById(questionId)?.let(::add)
			}
		}
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
			val updatedAnswers = currentState.selectedAnswers.toMutableMap().apply { put(question.id, selectedText) }
			val updatedEvaluated = currentState.evaluatedQuestions.toMutableSet().apply { add(question.id) }

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
				saveSessionSnapshot(currentState.copy(currentScore = newScore, selectedAnswers = updatedAnswers, evaluatedQuestions = updatedEvaluated))
			} catch (_: Exception) {
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
			val updatedState = currentState.copy(currentIndex = clampedIndex)
			_uiState.value = updatedState
			saveSessionSnapshot(updatedState)
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
		val updatedState = currentState.copy(flaggedQuestionIds = updatedFlags)
		_uiState.value = updatedState
		viewModelScope.launch { saveSessionSnapshot(updatedState) }
	}

	private fun completeExam(state: ExamUiState.Success) {
		viewModelScope.launch {
			stopTimer()
			saveSessionSnapshot(state, examEndTimeMillis = System.currentTimeMillis())

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

				val updatedVoided = currentState.voidedQuestions.toMutableSet().apply { add(currentQuestion.id) }
				val updatedState = currentState.copy(voidedQuestions = updatedVoided)
				_uiState.value = updatedState
				saveSessionSnapshot(updatedState)

				if (currentState.isLastQuestion) {
					completeExam(updatedState)
				} else {
					goToQuestion(currentState.currentIndex + 1)
				}
			} catch (_: Exception) {
				// Log but don't interrupt exam
			}
		}
	}

	private fun startTimer() {
		timerJob = viewModelScope.launch {
			var remaining = (_uiState.value as? ExamUiState.Success)?.remainingTimeMillis ?: DEFAULT_EXAM_DURATION_MILLIS
			while (remaining > 0) {
				delay(1000)
				remaining -= 1000
				val state = _uiState.value
				if (state is ExamUiState.Success) {
					val updatedState = state.copy(remainingTimeMillis = remaining)
					_uiState.value = updatedState
					saveSessionSnapshot(updatedState)
				}
			}
			val state = _uiState.value as? ExamUiState.Success ?: return@launch
			_effects.tryEmit(ExamEffect.TimeExpired)
			completeExam(state)
		}
	}

	private suspend fun saveSessionSnapshot(state: ExamUiState.Success, examEndTimeMillis: Long? = null) {
		sessionDao.getSession()?.let { session ->
			sessionDao.upsert(
				session.copy(
					lastScore = state.currentScore,
					lastQuestionIndex = state.currentIndex,
					lastTrack = normalizeTrackKey(state.sessionTrack),
					lastCategory = state.sessionCategory,
					remainingTimeMillis = state.remainingTimeMillis,
					questionIdsJson = state.questions.map { it.id }.toJsonArrayString(),
					flaggedQuestionIdsJson = state.flaggedQuestionIds.toJsonArrayString(),
					examEndTimeMillis = examEndTimeMillis
				)
			)
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
			else -> "All Categories"
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

	private fun List<Int>.toJsonArrayString(): String = JSONArray(this).toString()

	private fun Set<Int>.toJsonArrayString(): String = JSONArray(this.toList()).toString()

	private fun String?.toIntList(): List<Int> {
		if (this.isNullOrBlank()) return emptyList()
		val array = runCatching { JSONArray(this) }.getOrNull() ?: return emptyList()
		return buildList {
			for (index in 0 until array.length()) {
				val value = array.optInt(index, Int.MIN_VALUE)
				if (value != Int.MIN_VALUE) add(value)
			}
		}
	}

	private fun String?.toIntSet(): Set<Int> = toIntList().toSet()

	sealed class ExamEffect {
		data class NavigateToPage(val index: Int) : ExamEffect()
		object ExamCompleted : ExamEffect()
		object TimeExpired : ExamEffect()
		object QuestionReported : ExamEffect()
	}
}
