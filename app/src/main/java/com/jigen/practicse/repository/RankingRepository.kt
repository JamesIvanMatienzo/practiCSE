package com.jigen.practicse.repository

import android.content.Context
import com.google.gson.Gson
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class RankingRepository(private val context: Context) {
    private val database = PractiCSEDatabase.getInstance(context)
    private val gson = Gson()

    suspend fun fetchGlobalTop(limit: Int = 100): List<LeaderboardEntryEntity> = withContext(Dispatchers.IO) {
        val endpoint = System.getenv("SCORES_LIST_ENDPOINT") ?: ""
        val apiKey = System.getenv("SCORES_API_KEY") ?: ""

        if (endpoint.isBlank()) {
            // fallback to local cache
            return@withContext database.leaderboardDao().getTop(limit)
        }

        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (apiKey.isNotBlank()) setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = gson.fromJson(text, Array<LeaderboardEntryEntity>::class.java).toList()
                // cache into Room
                arr.forEach { database.leaderboardDao().upsert(it) }
                return@withContext arr.take(limit)
            }
        } catch (_: Exception) {
            // ignore and fallback
        }

        // fallback cache
        return@withContext database.leaderboardDao().getTop(limit)
    }

    suspend fun getCachedTop(limit: Int = 100): List<LeaderboardEntryEntity> = withContext(Dispatchers.IO) {
        database.leaderboardDao().getTop(limit)
    }
}
