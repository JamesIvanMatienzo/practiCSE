package com.jigen.practicse.ui.screens.ranking

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import com.jigen.practicse.repository.RankingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class RankingViewModel(private val repository: RankingRepository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<RankingUiState>(RankingUiState.Loading)
    val uiState: StateFlow<RankingUiState> = _uiState
    private var rankingJob: Job? = null

    init {
        refresh()
    }

    fun refresh(offlineModeOverride: Boolean? = null) {
        val prefs = AppPreferencesStore(context)
        val userName = prefs.getDisplayName().ifBlank { "You" }
        val offlineMode = offlineModeOverride ?: prefs.isOfflineRankingEnabled()

        rankingJob?.cancel()

        if (offlineMode) {
            rankingJob = viewModelScope.launch {
                _uiState.value = RankingUiState.Loading
                try {
                    repository.observeCachedTop(100).collectLatest { list ->
                        val sorted = list.sortedByDescending { it.totalScore }
                        val userEntry = sorted.find { it.userName == userName }
                        val userRank = sorted.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }

                        _uiState.value = RankingUiState.Success(
                            top = sorted,
                            userRank = userRank,
                            userEntry = userEntry,
                            isPlaceholder = false
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.value = RankingUiState.Error(e.message ?: "Failed to fetch rankings")
                }
            }
        } else {
            viewModelScope.launch {
                _uiState.value = RankingUiState.Loading
                try {
                    val profile = AppPreferencesStore(context).loadProfile()
                    val list = placeholderEntries(
                        userName = userName,
                        userPhotoBase64 = loadLocalPhotoBase64(profile.photoUri)
                    )
                    val sorted = list.sortedByDescending { it.totalScore }
                    val userEntry = sorted.find { it.userName == userName }
                    val userRank = sorted.indexOfFirst { it.userName == userName }.let { if (it == -1) null else it + 1 }

                    _uiState.value = RankingUiState.Success(
                        top = sorted,
                        userRank = userRank,
                        userEntry = userEntry,
                        isPlaceholder = true
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.value = RankingUiState.Error(e.message ?: "Failed to fetch rankings")
                }
            }
        }
    }

    private fun placeholderEntries(userName: String, userPhotoBase64: String?): List<LeaderboardEntryEntity> {
        val now = System.currentTimeMillis()
        // Offline sample entries requested by the user (capitalized)
        return listOf(
            LeaderboardEntryEntity(userName = "Emmanuel", totalScore = 8700, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Ivan", totalScore = 1250, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "Gem", totalScore = 150, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = "James", totalScore = 67, lastUpdatedMillis = now),
            LeaderboardEntryEntity(userName = userName, totalScore = 0, lastUpdatedMillis = now, photoBase64 = userPhotoBase64)
        ).sortedByDescending { it.totalScore }
    }

    private fun loadLocalPhotoBase64(photoUri: String?): String? {
        return try {
            val photoPath = photoUri?.trim().orEmpty()
            if (photoPath.isBlank()) return null

            val bytes = java.io.File(photoPath).takeIf { it.exists() }?.readBytes() ?: return null
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (_: Exception) {
            null
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
