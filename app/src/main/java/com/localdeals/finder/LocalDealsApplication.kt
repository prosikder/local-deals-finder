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
        if (com.localdeals.finder.BuildConfig.IS_TEST_BUILD) {
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

        // Background Cloud Sync with Firebase Firestore (guarded behind ENABLE_CLOUD_SYNC flag)
        if (com.localdeals.finder.BuildConfig.ENABLE_CLOUD_SYNC) {
            try {
                com.google.firebase.FirebaseApp.initializeApp(this)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        firebaseSyncManager.syncDeals()
                    } catch (e: Exception) {
                        // Gracefully ignore sync errors if offline
                    }
                }
            } catch (e: Exception) {
                // Gracefully ignore if Firebase config is missing
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
