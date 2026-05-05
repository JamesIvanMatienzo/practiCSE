package com.jigen.practicse.ui.screens.study_library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.PractiCSEDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudyLibraryViewModel(
	private val context: Context
) : ViewModel() {

	private val _uiState = MutableStateFlow<StudyLibraryUiState>(StudyLibraryUiState.Loading)
	val uiState: StateFlow<StudyLibraryUiState> = _uiState

	init {
		loadQuestions()
	}

	private fun loadQuestions() {
		viewModelScope.launch {
			try {
				val database = PractiCSEDatabase.getInstance(context)
				val questions = database.questionDao().getAllQuestions()
				val countsByCategory = questions
					.groupingBy { normalizeCategoryKey(it.category) }
					.eachCount()

				val categories = listOf(
					StudyCategoryItem(
						title = "Numerical Ability",
						categoryKey = "numerical_ability",
						questionCount = countsByCategory["numerical_ability"] ?: 0
					),
					StudyCategoryItem(
						title = "Verbal Ability",
						categoryKey = "verbal_ability",
						questionCount = countsByCategory["verbal_ability"] ?: 0
					),
					StudyCategoryItem(
						title = "General Information",
						categoryKey = "general_information",
						questionCount = countsByCategory["general_information"] ?: 0
					)
				)

				_uiState.value = StudyLibraryUiState.Success(categories)
			} catch (e: Exception) {
				_uiState.value = StudyLibraryUiState.Error("Failed to load study categories: ${e.message}")
			}
		}
	}

	private fun normalizeCategoryKey(category: String): String {
		val value = category.lowercase()
		return when {
			"numerical" in value -> "numerical_ability"
			"verbal" in value -> "verbal_ability"
			else -> "general_information"
		}
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return StudyLibraryViewModel(context) as T
			}
		}
	}
}
