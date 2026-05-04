package com.jigen.practicse.ui.screens.dashboard

data class CategoryScore(
	val category: String,
	val correctCount: Int,
	val totalCount: Int,
	val percentage: Float = if (totalCount > 0) (correctCount * 100f) / totalCount else 0f
)

sealed class DashboardUiState {
	data object Loading : DashboardUiState()

	data class Success(
		val categoryScores: List<CategoryScore> = emptyList(),
		val hasSessionToResume: Boolean = false,
		val lastQuestionIndex: Int = 0,
		val isOffline: Boolean = false,
		val totalAttempts: Int = 0
	) : DashboardUiState()

	data class Error(val message: String) : DashboardUiState()
}
