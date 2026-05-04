package com.jigen.practicse.ui.screens.ranking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.PractiCSEDatabase
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
            try {
                val list = repository.fetchGlobalTop(100)
                // determine user's rank
                val prefs = context.getSharedPreferences("practicse_prefs", Context.MODE_PRIVATE)
                val userName = prefs.getString("user_name", "anonymous") ?: "anonymous"

                val sorted = list.sortedByDescending { it.totalScore }
                val rank = sorted.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }
                val userEntry = sorted.find { it.userName == userName }

                _uiState.value = RankingUiState.Success(
                    top = sorted,
                    userRank = rank,
                    userEntry = userEntry
                )
            } catch (e: Exception) {
                // fallback cached
                val cached = repository.getCachedTop(100)
                val prefs = context.getSharedPreferences("practicse_prefs", Context.MODE_PRIVATE)
                val userName = prefs.getString("user_name", "anonymous") ?: "anonymous"
                val sorted = cached.sortedByDescending { it.totalScore }
                val rank = sorted.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }
                val userEntry = sorted.find { it.userName == userName }

                _uiState.value = RankingUiState.Success(
                    top = sorted,
                    userRank = rank,
                    userEntry = userEntry
                )
            }
        }
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
