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

class ExamViewModel(
	private val questionDao: QuestionDao,
	private val sessionDao: SessionDao,
	private val progressDao: ProgressDao,
	private val errorReportDao: ErrorReportDao
) : ViewModel() {

	private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
	val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

	private val _effects = MutableSharedFlow<ExamEffect>(extraBufferCapacity = 1)
	val effects: SharedFlow<ExamEffect> = _effects.asSharedFlow()

	private var timerJob: Job? = null

	companion object {
		private const val DEFAULT_EXAM_DURATION_MILLIS = 165L * 60_000L

		fun factory(context: Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
						val database = PractiCSEDatabase.getInstance(context)
						@Suppress("UNCHECKED_CAST")
						return ExamViewModel(
							database.questionDao(),
							database.sessionDao(),
							database.progressDao(),
							database.errorReportDao()
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
			val questions = questionDao.getAllQuestions()
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

			_uiState.value = ExamUiState.Success(
				questions = uiQuestions,
				currentIndex = 0,
				currentScore = 0,
				remainingTimeMillis = DEFAULT_EXAM_DURATION_MILLIS
			)

			startTimer()
		} catch (e: Exception) {
			_uiState.value = ExamUiState.Error(e.message ?: "Unknown error")
		}
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
				_effects.tryEmit(ExamEffect.ExamCompleted)
			} else {
				_uiState.value = currentState.copy(
					currentIndex = currentState.currentIndex + 1,
					currentScore = newScore
				)
				_effects.tryEmit(ExamEffect.NavigateToPage(currentState.currentIndex + 1))
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

	sealed class ExamEffect {
		data class NavigateToPage(val index: Int) : ExamEffect()
		object ExamCompleted : ExamEffect()
		object TimeExpired : ExamEffect()
		object QuestionReported : ExamEffect()
	}
}
