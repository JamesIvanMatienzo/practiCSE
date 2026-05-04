package com.jigen.practicse.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.entity.LeaderboardEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class SyncScoresWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val prefs: SharedPreferences = context.getSharedPreferences("practicse_prefs", Context.MODE_PRIVATE)
    private val database = PractiCSEDatabase.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userName = prefs.getString("user_name", "anonymous") ?: "anonymous"

            // Compute total score: sum of correct answers
            val progress = database.progressDao().getAllProgress()
            val totalScore = progress.count { it.isCorrect }

            // Only send minimal fields
            val payload = "{\"userName\":\"${userName}\",\"totalScore\":$totalScore}"

            val endpoint = options.getString("endpoint") ?: System.getenv("SCORES_ENDPOINT")
            val apiKey = options.getString("apiKey") ?: System.getenv("SCORES_API_KEY")

            if (endpoint.isNullOrBlank()) {
                return@withContext Result.failure()
            }

            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                if (!apiKey.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $apiKey")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            conn.outputStream.use { os ->
                OutputStreamWriter(os).use { it.write(payload) }
            }

            val code = conn.responseCode
            if (code in 200..299) {
                // update local cache
                val entry = LeaderboardEntryEntity(userName = userName, totalScore = totalScore, lastUpdatedMillis = System.currentTimeMillis())
                database.leaderboardDao().upsert(entry)
                return@withContext Result.success()
            } else {
                return@withContext Result.retry()
            }

        } catch (e: Exception) {
            return@withContext Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sync_scores_periodic"

        fun enqueuePeriodic(context: Context, endpoint: String? = null, apiKey: String? = null) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncScoresWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            // Optionally set input data via WorkManager if needed
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
