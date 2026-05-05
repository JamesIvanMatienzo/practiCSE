package com.jigen.practicse

import android.app.Application
import com.google.firebase.FirebaseApp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jigen.practicse.data.local.worker.JsonPreloadWorker
import com.jigen.practicse.util.RemoteConfigManager

class PractiCSEApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        WorkManager.getInstance(this).enqueueUniqueWork(
            "seed_questions",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<JsonPreloadWorker>().build()
        )

        val remoteConfigManager = RemoteConfigManager(this)
        remoteConfigManager.initializeDefaults()
        remoteConfigManager.fetchAndActivateRemote()
    }
}
