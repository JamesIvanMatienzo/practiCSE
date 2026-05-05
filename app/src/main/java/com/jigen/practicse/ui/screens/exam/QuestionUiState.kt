package com.jigen.practicse.ui.screens.exam

/**
 * UI representation of a question with pre-shuffled answer options.
 * This is created during exam initialization and options are shuffled once per session.
 */
data class QuestionUiState(
    val id: Int,
    val text: String,
    val referenceText: String?,
    val shuffledOptions: List<String>,
    val correctAnswer: String
)
