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
            val userName = AppPreferencesStore(context).getDisplayName().ifBlank { "You" }
            try {
                val list = repository.fetchGlobalTop(100)
                val sorted = list.sortedByDescending { it.totalScore }
                val withFallback = if (sorted.isEmpty()) placeholderEntries(userName) else sorted
                val isPlaceholder = sorted.isEmpty()
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
                val withFallback = if (sorted.isEmpty()) placeholderEntries(userName) else sorted
                val isPlaceholder = sorted.isEmpty()
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
        return listOf(
            LeaderboardEntryEntity(userName = "Juan Santos", totalScore = 3120, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Maria Cruz", totalScore = 2845, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Ana Reyes", totalScore = 2710, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Pedro Garcia", totalScore = 2580, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Sofia Tan", totalScore = 2465, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Carlos Lim", totalScore = 2340, lastUpdatedMillis = now),
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
