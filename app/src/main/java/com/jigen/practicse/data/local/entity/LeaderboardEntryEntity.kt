package com.jigen.practicse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val userName: String,
    val totalScore: Int,
    val lastUpdatedMillis: Long
)
