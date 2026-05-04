package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity

@Dao
interface LeaderboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: LeaderboardEntryEntity)

    @Query("SELECT * FROM leaderboard_entries ORDER BY totalScore DESC LIMIT :limit")
    suspend fun getTop(limit: Int): List<LeaderboardEntryEntity>

    @Query("SELECT * FROM leaderboard_entries WHERE userName = :userName LIMIT 1")
    suspend fun getForUser(userName: String): LeaderboardEntryEntity?

    @Query("DELETE FROM leaderboard_entries")
    suspend fun clearAll()
}
