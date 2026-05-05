package com.jigen.practicse.ui.screens.dashboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.QuestionDao
import com.jigen.practicse.data.local.dao.SessionDao
import com.jigen.practicse.data.local.trackKeyToLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.comparisons.compareByDescending

class DashboardViewModel(
	private val progressDao: ProgressDao,
	private val sessionDao: SessionDao,
	private val questionDao: QuestionDao,
	private val context: Context
) : ViewModel() {

	private val appPreferencesStore = AppPreferencesStore(context)

	private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
	val uiState: StateFlow<DashboardUiState> = _uiState

	init {
		loadDashboardData()
	}

	private fun loadDashboardData() {
		viewModelScope.launch {
			try {
				// Fetch all progress entries
				val allProgress = progressDao.getAllProgress()
				val totalAttempts = allProgress.size

				// Group by category and calculate percentages
				val categoryScores = allProgress
					.groupBy { normalizeCategoryKey(it.category) }
					.map { (categoryKey, entries) ->
						val correctCount = entries.count { it.isCorrect }
						CategoryScore(
							categoryKey = categoryKey,
							categoryLabel = categoryLabel(categoryKey),
							correctCount = correctCount,
							totalCount = entries.size
						)
					}
					.sortedWith(compareByDescending<CategoryScore> { it.percentage }.thenByDescending { it.correctCount })

				// Check if there's a session to resume
				val session = sessionDao.getSession()
				val hasSessionToResume = session != null && session.examEndTimeMillis == null
				val activeTrackLabel = session?.lastTrack?.let(::trackKeyToLabel)
					?: appPreferencesStore.getActiveTrackLabel()

				// Check connectivity
				val isOffline = !isNetworkConnected()
				val availableQuestionCount = questionDao.countQuestions()

				_uiState.value = DashboardUiState.Success(
					categoryScores = categoryScores,
					activeTrackLabel = activeTrackLabel,
					hasSessionToResume = hasSessionToResume,
					lastQuestionIndex = session?.lastQuestionIndex ?: 0,
					isOffline = isOffline,
					totalAttempts = totalAttempts,
					availableQuestionCount = availableQuestionCount
				)
			} catch (e: Exception) {
				_uiState.value = DashboardUiState.Error("Failed to load dashboard: ${e.message}")
			}
		}
	}

	private fun isNetworkConnected(): Boolean {
		val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val network = connectivityManager.activeNetwork ?: return false
		val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
		return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
	}

	private fun normalizeCategoryKey(category: String): String {
		val value = category.lowercase()
		return when {
			"numerical" in value -> "numerical_ability"
			"verbal" in value -> "verbal_ability"
			else -> "general_information"
		}
	}

	private fun categoryLabel(categoryKey: String): String {
		return when (categoryKey) {
			"numerical_ability" -> "Numerical Ability"
			"verbal_ability" -> "Verbal Ability"
			else -> "General Information"
		}
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				val database = PractiCSEDatabase.getInstance(context)
				return DashboardViewModel(
					database.progressDao(),
					database.sessionDao(),
					database.questionDao(),
					context
				) as T
			}
		}
	}
}
