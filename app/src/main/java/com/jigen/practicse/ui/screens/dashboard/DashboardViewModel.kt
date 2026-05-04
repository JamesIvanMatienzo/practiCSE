package com.jigen.practicse.ui.screens.dashboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.SessionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
	private val progressDao: ProgressDao,
	private val sessionDao: SessionDao,
	private val context: Context
) : ViewModel() {

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
					.groupBy { it.category }
					.map { (category, entries) ->
						val correctCount = entries.count { it.isCorrect }
						CategoryScore(
							category = category,
							correctCount = correctCount,
							totalCount = entries.size
						)
					}
					.sortedBy { it.category }

				// Check if there's a session to resume
				val session = sessionDao.getSession()
				val hasSessionToResume = session != null

				// Check connectivity
				val isOffline = !isNetworkConnected()

				_uiState.value = DashboardUiState.Success(
					categoryScores = categoryScores,
					hasSessionToResume = hasSessionToResume,
					lastQuestionIndex = session?.lastQuestionIndex ?: 0,
					isOffline = isOffline,
					totalAttempts = totalAttempts
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

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				val database = PractiCSEDatabase.getInstance(context)
				return DashboardViewModel(
					database.progressDao(),
					database.sessionDao(),
					context
				) as T
			}
		}
	}
}
