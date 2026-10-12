package com.localdeals.finder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class LocalDealsApplication : Application() {

    @javax.inject.Inject
    lateinit var firebaseSyncManager: com.localdeals.finder.data.cloud.FirebaseSyncManager

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
        
        scheduleNotificationWorker()

        // Background Cloud Sync with Firebase Firestore
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                firebaseSyncManager.syncDeals()
            } catch (e: Exception) {
                // Gracefully ignore sync errors if offline
            }
        }
    }
    
    private fun scheduleNotificationWorker() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
                
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.localdeals.finder.worker.DealNotificationWorker>(
                24, java.util.concurrent.TimeUnit.HOURS,
                1, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
                
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DealNotificationWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            // Gracefully ignore WorkManager scheduling if uninitialized (e.g., during tests)
        }
    }
}
