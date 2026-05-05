package com.jigen.practicse.ui.screens.exam

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.entity.QuestionEntity
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

class ExamViewModelNew(
	private val questionDao: QuestionDao,
	private val sessionDao: SessionDao,
	private val progressDao: ProgressDao,
	private val errorReportDao: ErrorReportDao,
	private val sessionMode: String = "new" // "new" or "resume"
) : ViewModel() {

	private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
	val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

	private val _effects = MutableSharedFlow<ExamEffect>(extraBufferCapacity = 1)
	val effects: SharedFlow<ExamEffect> = _effects.asSharedFlow()

	private var timerJob: Job? = null

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
			val (mode, categoryKey) = parseSessionMode(sessionMode)
			val allQuestions = questionDao.getAllQuestions()
			val questions = if (mode == "new" && categoryKey != null) {
				allQuestions.filter { matchesCategory(it, categoryKey) }
			} else {
				allQuestions
			}
			val sessionCategory = categoryKey?.let { displayCategoryName(it) } ?: "Professional"

			if (questions.isEmpty()) {
				_uiState.value = ExamUiState.Error("No questions available")
				return
			}

			val uiQuestions = questions.map { entity ->
				QuestionUiState(
					id = entity.id,
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
				// Load existing session
				val existingSession = sessionDao.getSession()
				if (existingSession != null) {
					startingIndex = existingSession.lastQuestionIndex
					// Calculate score from progress entries for questions up to startingIndex
					var score = 0
					for (i in 0 until startingIndex.coerceAtMost(uiQuestions.size)) {
						val qId = uiQuestions.getOrNull(i)?.id ?: continue
						val prog = progressDao.getProgress(qId)
						if (prog?.isCorrect == true) score += 1
					}
					startingScore = score
				} else {
					// No session to resume, treat as new
					createNewSession(sessionCategory)
				}
			} else {
				// Create new session
				createNewSession(sessionCategory)
			}

			_uiState.value = ExamUiState.Success(
				questions = uiQuestions,
				currentIndex = startingIndex,
				currentScore = startingScore,
				remainingTimeMillis = DEFAULT_EXAM_DURATION_MILLIS,
				sessionCategory = sessionCategory
			)

			startTimer()
		} catch (e: Exception) {
			_uiState.value = ExamUiState.Error(e.message ?: "Unknown error")
		}
	}

	private suspend fun createNewSession(sessionCategory: String) {
		val newSession = SessionEntity(
			id = 1,
			lastQuestionIndex = 0,
			lastTrack = "Professional",
			lastCategory = sessionCategory
		)
		sessionDao.upsert(newSession)
	}

	fun selectAnswer(selectedText: String) {
		val currentState = _uiState.value
		if (currentState !is ExamUiState.Success) return

		val question = currentState.currentQuestion ?: return

		viewModelScope.launch {
			val isCorrect = selectedText.equals(question.correctAnswer, ignoreCase = true)
			val newScore = if (isCorrect) currentState.currentScore + 1 else currentState.currentScore

			try {
				progressDao.upsert(
					UserProgressEntity(
						questionId = question.id,
						selectedIndex = -1,
						isCorrect = isCorrect,
						answeredAtMillis = System.currentTimeMillis(),
						track = "Professional",
						category = currentState.sessionCategory ?: "General"
					)
				)
			} catch (e: Exception) {
				// Log but continue
			}

			if (currentState.isLastQuestion) {
				stopTimer()
				_uiState.value = ExamUiState.Completed(
					totalScore = newScore,
					totalQuestions = currentState.totalQuestions
				)

				// Update session with final score
				val session = sessionDao.getSession()
				if (session != null) {
					sessionDao.upsert(session.copy(lastQuestionIndex = currentState.totalQuestions))
				}

				_effects.tryEmit(ExamEffect.ExamCompleted)
			} else {
				val nextIndex = currentState.currentIndex + 1
				_uiState.value = currentState.copy(
					currentIndex = nextIndex,
					currentScore = newScore
				)

				// Update session progress
				val session = sessionDao.getSession()
				if (session != null) {
					sessionDao.upsert(session.copy(
						lastQuestionIndex = nextIndex,
						lastCategory = currentState.sessionCategory
					))
				}

				_effects.tryEmit(ExamEffect.NavigateToPage(nextIndex))
			}
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
				_effects.tryEmit(ExamEffect.QuestionReported)
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
