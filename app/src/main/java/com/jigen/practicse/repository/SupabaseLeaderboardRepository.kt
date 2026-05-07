package com.jigen.practicse.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.jigen.practicse.network.SupabaseService
import com.jigen.practicse.network.LeaderboardUpsertRequest
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SupabaseLeaderboardRepository(private val context: Context) {
    private val supabaseUrl = com.jigen.practicse.BuildConfig.SUPABASE_URL
    private val supabaseKey = com.jigen.practicse.BuildConfig.SUPABASE_KEY
    private val api = SupabaseService.create(supabaseUrl, supabaseKey)
    private val db = PractiCSEDatabase.getInstance(context)
    private val prefs = AppPreferencesStore(context)

    suspend fun upsertScore(displayName: String, totalScore: Int): Boolean = withContext(Dispatchers.IO) {
        // Get user's photo if available and convert to base64
        val profile = prefs.loadProfile()
        val photoBase64 = profile.photoUri?.let { photoUri ->
            try {
                val file = File(photoUri)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    Base64.encodeToString(bytes, Base64.DEFAULT)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        // Only send display name and score to the backend
        val body = LeaderboardUpsertRequest(user_name = displayName, total_score = totalScore)
        val resp = api.upsertLeaderboard(body)
        if (resp.isSuccessful) {
            // cache locally with photo
            db.leaderboardDao().upsert(
                LeaderboardEntryEntity(
                    userName = displayName,
                    totalScore = totalScore,
                    lastUpdatedMillis = System.currentTimeMillis(),
                    photoBase64 = photoBase64
                )
            )
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
