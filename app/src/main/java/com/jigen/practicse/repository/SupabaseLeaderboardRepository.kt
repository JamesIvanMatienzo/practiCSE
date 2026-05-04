package com.jigen.practicse.repository

import android.content.Context
import com.jigen.practicse.network.SupabaseService
import com.jigen.practicse.network.LeaderboardUpsertRequest
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseLeaderboardRepository(private val context: Context) {
    private val supabaseUrl = com.jigen.practicse.BuildConfig.SUPABASE_URL
    private val supabaseKey = com.jigen.practicse.BuildConfig.SUPABASE_KEY
    private val api = SupabaseService.create(supabaseUrl, supabaseKey)
    private val db = PractiCSEDatabase.getInstance(context)

    suspend fun upsertScore(displayName: String, totalScore: Int): Boolean = withContext(Dispatchers.IO) {
        // Only send display name and score to the backend
        val body = LeaderboardUpsertRequest(user_name = displayName, total_score = totalScore)
        val resp = api.upsertLeaderboard(body)
        if (resp.isSuccessful) {
            // cache locally
            db.leaderboardDao().upsert(LeaderboardEntryEntity(userName = displayName, totalScore = totalScore, lastUpdatedMillis = System.currentTimeMillis()))
            true
        } else {
            false
        }
    }

    suspend fun fetchTop(limit: Int = 100): List<LeaderboardEntryEntity> = withContext(Dispatchers.IO) {
        val resp = api.getTop(limit = limit)
        if (resp.isSuccessful) {
            val body = resp.body() ?: emptyList()
            val parsed = body.mapNotNull { map ->
                val name = map["user_name"] as? String ?: return@mapNotNull null
                val score = (map["total_score"] as? Number)?.toInt() ?: return@mapNotNull null
                LeaderboardEntryEntity(userName = name, totalScore = score, lastUpdatedMillis = System.currentTimeMillis())
            }
            parsed.forEach { db.leaderboardDao().upsert(it) }
            parsed
        } else {
            db.leaderboardDao().getTop(limit)
        }
    }
}
