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
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.repository.SupabaseLeaderboardRepository
import com.jigen.practicse.util.CrashReporter
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
            // Skip sync entirely for guest users
            if (AppPreferencesStore(applicationContext).isGuest()) {
                return@withContext Result.success()
            }

            val displayName = AppPreferencesStore(applicationContext).getDisplayName().ifBlank { "anonymous" }

            // Compute total score: sum of correct answers
            val progress = database.progressDao().getAllProgress()
            val totalScore = progress.count { it.isCorrect }

            // Use SupabaseRepository to upsert only display name and score (no PII)
            val repo = SupabaseLeaderboardRepository(applicationContext)
            return@withContext try {
                val ok = repo.upsertScore(displayName, totalScore)
                if (ok) {
                    Result.success()
                } else {
                    // non-fatal report so we can monitor reliability
                    CrashReporter.recordException(RuntimeException("SyncScoresWorker: upsert returned false"), "SyncScoresWorker")
                    Result.retry()
                }
            } catch (e: Exception) {
                CrashReporter.recordException(e, "SyncScoresWorker")
                Result.retry()
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
