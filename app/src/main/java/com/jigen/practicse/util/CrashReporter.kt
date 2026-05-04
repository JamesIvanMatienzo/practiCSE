package com.jigen.practicse.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashReporter {
    private const val TAG = "CrashReporter"

    fun recordException(throwable: Throwable, context: String? = null) {
        runCatching {
            FirebaseCrashlytics.getInstance().recordException(
                if (context.isNullOrBlank()) throwable else RuntimeException("$context: ${throwable.message}", throwable)
            )
        }.onFailure {
            Log.e(TAG, "Reported exception${if (context != null) " [$context]" else ""}", throwable)
        }
    }
}
