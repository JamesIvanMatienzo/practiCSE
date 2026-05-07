package com.jigen.practicse.ui.screens.ranking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import com.jigen.practicse.repository.RankingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel(private val repository: RankingRepository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<RankingUiState>(RankingUiState.Loading)
    val uiState: StateFlow<RankingUiState> = _uiState

    init {
        refresh()
    }

    fun refresh(offlineModeOverride: Boolean? = null) {
        viewModelScope.launch {
            _uiState.value = RankingUiState.Loading
            val prefs = AppPreferencesStore(context)
            val userName = prefs.getDisplayName().ifBlank { "You" }
            val offlineMode = offlineModeOverride ?: prefs.isOfflineRankingEnabled()

            try {
                // Reversed logic: Offline fetches local DB, Online pushes mock server data
                val list = if (offlineMode) {
                    repository.getCachedTop(100)
                } else {
                    placeholderEntries(userName)
                }

                val sorted = list.sortedByDescending { it.totalScore }
                val isPlaceholder = !offlineMode

                val userEntry = sorted.find { it.userName == userName }
                val userRank = sorted.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }

                _uiState.value = RankingUiState.Success(
                    top = sorted,
                    userRank = userRank,
                    userEntry = userEntry,
                    isPlaceholder = isPlaceholder
                )
            } catch (e: Exception) {
                _uiState.value = RankingUiState.Error(e.message ?: "Failed to fetch rankings")
            }
        }
    }

    private fun placeholderEntries(userName: String): List<LeaderboardEntryEntity> {
        val now = System.currentTimeMillis()
        // Offline sample entries requested by the user (capitalized)
        return listOf(
            LeaderboardEntryEntity(userName = "Emmanuel", totalScore = 8700, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Ivan", totalScore = 1250, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Gem", totalScore = 150, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "James", totalScore = 67, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = userName, totalScore = 0, lastUpdatedMillis = now)
        ).sortedByDescending { it.totalScore }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = RankingRepository(context)
                return RankingViewModel(repo, context) as T
            }
        }
    }
}
