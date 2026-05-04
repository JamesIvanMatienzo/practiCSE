package com.jigen.practicse

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jigen.practicse.util.RemoteConfigManager

class PractiCSEApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val remoteConfigManager = RemoteConfigManager(this)
        remoteConfigManager.initializeDefaults()
        remoteConfigManager.fetchAndActivateRemote()
    }
}
