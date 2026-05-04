package com.jigen.practicse.util

import android.util.Log

object CrashReporter {
    private const val TAG = "CrashReporter"

    fun recordException(throwable: Throwable, context: String? = null) {
        try {
            // Try to use FirebaseCrashlytics via reflection to avoid hard dependency
            val cls = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val instanceMethod = cls.getMethod("getInstance")
            val instance = instanceMethod.invoke(null)
            val record = cls.getMethod("recordException", Throwable::class.java)
            record.invoke(instance, throwable)
        } catch (e: Exception) {
            // Fallback to logcat
            Log.e(TAG, "Reported exception${if (context != null) " [$context]" else ""}", throwable)
        }
    }
}
