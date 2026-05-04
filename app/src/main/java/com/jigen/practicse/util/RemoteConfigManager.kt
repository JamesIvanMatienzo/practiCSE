package com.jigen.practicse.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * RemoteConfigManager provides a simple abstraction for a remote-updatable
 * passing threshold and a feature toggle for the AI step-by-step.
 *
 * Implementation uses Firebase Remote Config reflectively if available,
 * otherwise falls back to SharedPreferences defaults.
 */
class RemoteConfigManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("remote_config", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "RemoteConfigManager"
        private const val KEY_PASSING_THRESHOLD = "passing_threshold"
        private const val KEY_AI_ENABLED = "ai_step_enabled"
        private const val DEFAULT_PASSING = 80
        private const val DEFAULT_AI_ENABLED = true
    }

    fun getPassingThresholdPercent(): Int {
        // Try Firebase Remote Config first (reflective)
        try {
            val cls = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
            val instMethod = cls.getMethod("getInstance")
            val instance = instMethod.invoke(null)
            val getLong = cls.getMethod("getLong", String::class.java)
            val value = (getLong.invoke(instance, KEY_PASSING_THRESHOLD) as Number).toLong()
            if (value > 0) return value.toInt()
        } catch (e: Exception) {
            // ignore, fallback to prefs
        }

        return prefs.getInt(KEY_PASSING_THRESHOLD, DEFAULT_PASSING)
    }

    fun isAiStepEnabled(): Boolean {
        try {
            val cls = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
            val instMethod = cls.getMethod("getInstance")
            val instance = instMethod.invoke(null)
            val getBoolean = cls.getMethod("getBoolean", String::class.java)
            val value = getBoolean.invoke(instance, KEY_AI_ENABLED) as Boolean
            return value
        } catch (e: Exception) {
            // fallback
        }

        return prefs.getBoolean(KEY_AI_ENABLED, DEFAULT_AI_ENABLED)
    }

    fun setLocalDefaults(passingThreshold: Int = DEFAULT_PASSING, aiEnabled: Boolean = DEFAULT_AI_ENABLED) {
        prefs.edit().apply {
            putInt(KEY_PASSING_THRESHOLD, passingThreshold)
            putBoolean(KEY_AI_ENABLED, aiEnabled)
            apply()
        }
    }

    fun fetchAndActivateRemote() {
        try {
            val cls = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
            val instMethod = cls.getMethod("getInstance")
            val instance = instMethod.invoke(null)
            val fetch = cls.getMethod("fetch")
            val task = fetch.invoke(instance) // returns Task
            // We won't block here; best-effort fetch via SDK if available
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Remote Config not available, skipping fetch")
        }
    }
}
