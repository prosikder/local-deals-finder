package com.localdeals.finder.util

import android.util.Log
import com.localdeals.finder.BuildConfig

object AppLogger {
    private val EMAIL_REGEX = Regex("([a-zA-Z0-9._%+-]{1,2})[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")
    private val PHONE_REGEX = Regex("(\\+?\\d{3,4})\\d{4,}(\\d{2})")
    private val TOKEN_REGEX = Regex("(?i)(password|token|secret|key)=([^&\\s]+)")

    fun maskPii(message: String): String {
        var masked = message.replace(EMAIL_REGEX, "$1***@***")
        masked = masked.replace(PHONE_REGEX, "$1****$2")
        masked = masked.replace(TOKEN_REGEX, "$1=[REDACTED]")
        return masked
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, maskPii(message))
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, maskPii(message))
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val safeMsg = maskPii(message)
        if (BuildConfig.DEBUG) {
            Log.w(tag, safeMsg, throwable)
        } else {
            // In release, log message without PII and without full sensitive stack trace
            Log.w(tag, safeMsg)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeMsg = maskPii(message)
        if (BuildConfig.DEBUG) {
            Log.e(tag, safeMsg, throwable)
        } else {
            Log.e(tag, safeMsg)
        }
    }
}
