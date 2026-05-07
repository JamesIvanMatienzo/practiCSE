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

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = RankingUiState.Loading
            val prefs = AppPreferencesStore(context)
            val userName = prefs.getDisplayName().ifBlank { "You" }
            try {
                val list = repository.fetchGlobalTop(100)
                val sorted = list.sortedByDescending { it.totalScore }
                val offlineMode = prefs.isOfflineRankingEnabled()
                val withFallback = when {
                    offlineMode -> placeholderEntries(userName)
                    sorted.isEmpty() -> placeholderEntries(userName)
                    else -> sorted
                }
                val isPlaceholder = offlineMode || sorted.isEmpty()
                val userEntry = withFallback.find { it.userName == userName }
                val userRank = withFallback.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }

                _uiState.value = RankingUiState.Success(
                    top = withFallback,
                    userRank = userRank,
                    userEntry = userEntry,
                    isPlaceholder = isPlaceholder
                )
            } catch (e: Exception) {
                // fallback cached
                val cached = repository.getCachedTop(100)
                val sorted = cached.sortedByDescending { it.totalScore }
                val offlineMode = AppPreferencesStore(context).isOfflineRankingEnabled()
                val withFallback = if (offlineMode) placeholderEntries(userName) else if (sorted.isEmpty()) placeholderEntries(userName) else sorted
                val isPlaceholder = offlineMode || sorted.isEmpty()
                val rank = withFallback.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }
                val userEntry = withFallback.find { it.userName == userName }

                _uiState.value = RankingUiState.Success(
                    top = withFallback,
                    userRank = rank,
                    userEntry = userEntry,
                    isPlaceholder = isPlaceholder
                )
            }
        }
    }

    private fun placeholderEntries(userName: String): List<LeaderboardEntryEntity> {
        val now = System.currentTimeMillis()
        // Offline sample entries requested by the user (capitalized)
        return listOf(
            LeaderboardEntryEntity(userName = "Emman", totalScore = 8700, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Ivan", totalScore = 1250, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Gem", totalScore = 150, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "James", totalScore = 69, lastUpdatedMillis = now),
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
