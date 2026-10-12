package com.localdeals.finder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.localdeals.finder.MainActivity
import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLogger.d("FCM", "New Firebase device token generated: $token")
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        AppLogger.d("FCM", "Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Local Deals Finder"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You have a new update!"

        val deepLinkUrl = remoteMessage.data["deep_link"]
            ?: remoteMessage.data["deal_id"]?.let { "localdeals://app/deal/$it" }
            ?: "localdeals://app"

        showNotification(title, body, deepLinkUrl)
    }

    private fun showNotification(title: String, body: String, deepLinkUrl: String) {
        val channelId = "local_deals_fcm_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Local Deals Push Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time deal discounts, order updates, and chat messages"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(deepLinkUrl),
            applicationContext,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
