package com.jigen.practicse.ui.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

	init {
		viewModelScope.launch {
			resumeSession()
		}
	}

	suspend fun resumeSession() {
		val session = sessionDao.getSession()
		val allQuestions = questionDao.getAllQuestions()
		val filteredQuestions = filterQuestions(
			questions = allQuestions,
			track = session?.lastTrack,
			category = session?.lastCategory
		)
		val safeIndex = (session?.lastQuestionIndex ?: 0).coerceIn(0, filteredQuestions.lastIndex.coerceAtLeast(0))

		_uiState.value = ExamUiState.Success(
			questions = filteredQuestions,
			currentQuestionIndex = safeIndex,
			selectedAnswerIndex = null,
			isSelectionLocked = false,
			remainingTimeMillis = remainingTime(session?.examEndTimeMillis),
			sessionTrack = session?.lastTrack,
			sessionCategory = session?.lastCategory
		)

		startTimer(session?.examEndTimeMillis)
	}

	fun startSession(
		track: String? = null,
		category: String? = null,
		totalDurationMillis: Long = DEFAULT_MOCK_EXAM_DURATION_MILLIS
	) {
		viewModelScope.launch {
			val questions = filterQuestions(questionDao.getAllQuestions(), track, category)
			val endTime = System.currentTimeMillis() + totalDurationMillis
			sessionDao.upsert(
				SessionEntity(
					lastTrack = track,
					lastCategory = category,
					lastQuestionIndex = 0,
					examEndTimeMillis = endTime
				)
			)

			_uiState.value = ExamUiState.Success(
				questions = questions,
				currentQuestionIndex = 0,
				remainingTimeMillis = totalDurationMillis,
				sessionTrack = track,
				sessionCategory = category
			)

			startTimer(endTime)
		}
	}

	fun selectAnswer(choiceIndex: Int) {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		val question = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return
		if (currentState.isSelectionLocked) return

		viewModelScope.launch {
			_uiState.value = currentState.copy(
				selectedAnswerIndex = choiceIndex,
				isSelectionLocked = true
			)

			progressDao.upsert(
				UserProgressEntity(
					questionId = question.id,
					selectedIndex = choiceIndex,
					isCorrect = choiceIndex == question.correctIndex,
					answeredAtMillis = System.currentTimeMillis(),
					track = currentState.sessionTrack ?: question.track,
					category = currentState.sessionCategory ?: question.category
				)
			)

			delay(300)

			val nextIndex = currentState.currentQuestionIndex + 1
			sessionDao.upsert(
				SessionEntity(
					lastTrack = currentState.sessionTrack ?: question.track,
					lastCategory = currentState.sessionCategory ?: question.category,
					lastQuestionIndex = nextIndex.coerceAtMost(currentState.questions.lastIndex.coerceAtLeast(0)),
					examEndTimeMillis = (sessionDao.getSession()?.examEndTimeMillis)
				)
			)

			if (nextIndex <= currentState.questions.lastIndex) {
				_uiState.value = currentState.copy(
					currentQuestionIndex = nextIndex,
					selectedAnswerIndex = null,
					isSelectionLocked = false
				)
				_effects.tryEmit(ExamEffect.NavigateToPage(nextIndex))
			} else {
				_effects.tryEmit(ExamEffect.ExamCompleted)
			}
		}
	}

	fun fetchLastQuestionIndex() {
		viewModelScope.launch {
			val session = sessionDao.getSession() ?: return@launch
			val currentState = _uiState.value as? ExamUiState.Success ?: return@launch
			_uiState.value = currentState.copy(currentQuestionIndex = session.lastQuestionIndex)
		}
	}

	fun updateRemainingTimeFromSystem() {
		viewModelScope.launch {
			val session = sessionDao.getSession()
			val currentState = _uiState.value as? ExamUiState.Success ?: return@launch
			_uiState.value = currentState.copy(remainingTimeMillis = remainingTime(session?.examEndTimeMillis))
		}
	}

	fun reportCurrentQuestion() {
		val currentState = _uiState.value as? ExamUiState.Success ?: return
		val question = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return

		viewModelScope.launch {
			errorReportDao.insert(
				ErrorReportEntity(
					questionId = question.id,
					reportedAtMillis = System.currentTimeMillis()
				)
			)
		}
	}

	override fun onCleared() {
		super.onCleared()
		timerJob?.cancel()
	}

	private fun startTimer(examEndTimeMillis: Long?) {
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			while (true) {
				val currentState = _uiState.value as? ExamUiState.Success ?: break
				val remaining = remainingTime(examEndTimeMillis)
				_uiState.value = currentState.copy(remainingTimeMillis = remaining)

				if (remaining <= 0L) {
					_effects.tryEmit(ExamEffect.TimeExpired)
					break
				}

				delay(1_000)
			}
		}
	}

	private fun remainingTime(examEndTimeMillis: Long?): Long {
		if (examEndTimeMillis == null) return DEFAULT_MOCK_EXAM_DURATION_MILLIS
		return (examEndTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
	}

	private fun filterQuestions(
		questions: List<com.jigen.practicse.data.local.entity.QuestionEntity>,
		track: String?,
		category: String?
	): List<com.jigen.practicse.data.local.entity.QuestionEntity> {
		return questions.filter { question ->
			(track.isNullOrBlank() || question.track.equals(track, ignoreCase = true)) &&
				(category.isNullOrBlank() || question.category.equals(category, ignoreCase = true))
		}
	}

	sealed class ExamEffect {
		data class NavigateToPage(val pageIndex: Int) : ExamEffect()
		data object ExamCompleted : ExamEffect()
		data object TimeExpired : ExamEffect()
	}

	companion object {
		private const val DEFAULT_MOCK_EXAM_DURATION_MILLIS = 165L * 60_000L

		fun factory(
			questionDao: QuestionDao,
			sessionDao: SessionDao,
			progressDao: ProgressDao,
			errorReportDao: ErrorReportDao
		): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
						@Suppress("UNCHECKED_CAST")
						return ExamViewModel(questionDao, sessionDao, progressDao, errorReportDao) as T
					}
					throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
				}
			}
		}
	}
}