package com.jigen.practicse.ui.screens.exam

import com.jigen.practicse.data.local.entity.QuestionEntity

sealed class ExamUiState {
	data object Loading : ExamUiState()

	data class Success(
		val questions: List<QuestionEntity> = emptyList(),
		val currentQuestionIndex: Int = 0,
		val selectedAnswerIndex: Int? = null,
		val isSelectionLocked: Boolean = false,
		val remainingTimeMillis: Long = 0L,
		val sessionTrack: String? = null,
		val sessionCategory: String? = null,
		val totalQuestionCount: Int = 165
	) : ExamUiState()

	data class Error(val message: String) : ExamUiState()
}