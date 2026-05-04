package com.jigen.practicse.util

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.ktx.Firebase
import com.jigen.practicse.R

/**
 * RemoteConfigManager provides a simple abstraction for a remote-updatable
 * passing threshold and a feature toggle for the AI step-by-step.
 *
 * Implementation uses Firebase Remote Config directly.
 */
class RemoteConfigManager(private val context: Context) {
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    companion object {
        private const val KEY_PASSING_THRESHOLD = "passing_threshold"
        private const val KEY_AI_ENABLED = "ai_step_enabled"
        private const val DEFAULT_PASSING = 80
        private const val DEFAULT_AI_ENABLED = true
    }

    fun initializeDefaults() {
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (DEFAULT_AI_ENABLED) 3600 else 0
        }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun getPassingThresholdPercent(): Int {
        return remoteConfig.getLong(KEY_PASSING_THRESHOLD).toInt().takeIf { it > 0 } ?: DEFAULT_PASSING
    }

    fun isAiStepEnabled(): Boolean {
        return remoteConfig.getBoolean(KEY_AI_ENABLED)
    }

    fun fetchAndActivateRemote() {
        remoteConfig.fetchAndActivate()
    }
}
