package com.jigen.practicse.ui.screens.ranking

import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity

sealed class RankingUiState {
    object Loading : RankingUiState()
    data class Success(
        val top: List<LeaderboardEntryEntity> = emptyList(),
        val userRank: Int? = null,
        val userEntry: LeaderboardEntryEntity? = null,
        val isPlaceholder: Boolean = false
    ) : RankingUiState()

    data class Error(val message: String) : RankingUiState()
}
