package com.localdeals.finder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class LocalDealsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    val file = File(filesDir, "last_crash.txt")
                    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    file.writeText("Crash on $time\n\n")
                    file.appendText(android.util.Log.getStackTraceString(throwable))
                } catch (e: Exception) {
                    // Ignore errors in the crash handler itself
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
