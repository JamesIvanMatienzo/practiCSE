package com.jigen.practicse.ui.screens.exam

/**
 * Represents the UI state of an exam session.
 * 
 * - Loading: Initial state while questions are being fetched
 * - Success: Active exam with questions, current progress, and timer
 * - Completed: Exam finished with final score
 * - Error: Exam failed to load or encountered an error
 */
sealed class ExamUiState {
	data object Loading : ExamUiState()

	/**
	 * Active exam state with pre-shuffled questions.
	 * @param questions List of QuestionUiState with shuffled options (shuffled once per session)
	 * @param currentIndex Index of the currently displayed question
	 * @param currentScore Number of correctly answered questions
	 * @param remainingTimeMillis Time remaining in milliseconds
	 * @param sessionTrack Professional or Sub-Professional track
	 * @param sessionCategory Exam category (General Information, Numerical Ability, Verbal Ability)
	 */
	data class Success(
		val questions: List<QuestionUiState> = emptyList(),
		val currentIndex: Int = 0,
		val currentScore: Int = 0,
		val remainingTimeMillis: Long = 0L,
		val sessionTrack: String? = null,
		val sessionCategory: String? = null,
		val selectedAnswers: Map<Int, String> = emptyMap(),
		val evaluatedQuestions: Set<Int> = emptySet(),
		val flaggedQuestionIds: Set<Int> = emptySet()
	) : ExamUiState() {
		val currentQuestion: QuestionUiState?
			get() = if (currentIndex < questions.size) questions[currentIndex] else null
		
		val totalQuestions: Int
			get() = questions.size
		
		val isLastQuestion: Boolean
			get() = currentIndex >= totalQuestions - 1

		val answeredCount: Int
			get() = evaluatedQuestions.size
		
		val correctCount: Int
			get() {
				var count = 0
				evaluatedQuestions.forEach { questionId ->
					val question = questions.find { it.id == questionId }
					val selectedAnswer = selectedAnswers[questionId]
					if (question != null && selectedAnswer != null &&
						selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
						count++
					}
				}
				return count
			}
		
		val wrongCount: Int
			get() {
				var count = 0
				evaluatedQuestions.forEach { questionId ->
					val question = questions.find { it.id == questionId }
					val selectedAnswer = selectedAnswers[questionId]
					if (question != null && selectedAnswer != null &&
						!selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
						count++
					}
				}
				return count
			}
		
		val skippedCount: Int
			get() = totalQuestions - evaluatedQuestions.size
	}

	/**
	 * Exam completed state with final score.
	 * @param totalScore Number of correctly answered questions
	 * @param totalQuestions Total number of questions in the exam
	 */
	data class Completed(
		val totalScore: Int = 0,
		val totalQuestions: Int = 0
	) : ExamUiState() {
		val percentage: Float
			get() = if (totalQuestions > 0) (totalScore * 100f) / totalQuestions else 0f
		
		val isPassed: Boolean
			get() = percentage >= 50f // 50% passing threshold
	}

	data class Error(val message: String) : ExamUiState()
}