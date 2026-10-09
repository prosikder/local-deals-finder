package com.localdeals.finder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.localdeals.finder.domain.model.AppNotification
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.NotificationRepository
import com.localdeals.finder.domain.repository.WishlistRepository
import com.localdeals.finder.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.UUID

class DealNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun wishlistRepository(): WishlistRepository
        fun dealRepository(): DealRepository
        fun notificationRepository(): NotificationRepository
        fun authRepository(): AuthRepository
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WorkerEntryPoint::class.java)
        val authRepository = entryPoint.authRepository()
        val wishlistRepository = entryPoint.wishlistRepository()
        val notificationRepository = entryPoint.notificationRepository()

        val user = authRepository.getCurrentUser() ?: return Result.success()
        val wishlist = wishlistRepository.getWishlist().first()
        val alertsMap = wishlistRepository.getWishlistAlerts().first()

        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        for (deal in wishlist) {
            val isAlertEnabled = alertsMap[deal.id] ?: false
            if (isAlertEnabled) {
                val timeRemaining = deal.expirationTimeMillis - currentTime
                if (timeRemaining in 0..oneDayMillis) {
                    sendNotification(
                        title = "Deal Expiring Soon!",
                        message = "${deal.title} expires in less than 24 hours.",
                        dealId = deal.id,
                        userId = user.id,
                        notificationRepository = notificationRepository
                    )
                }
            }
        }

        return Result.success()
    }

    private suspend fun sendNotification(
        title: String, 
        message: String, 
        dealId: String, 
        userId: String,
        notificationRepository: NotificationRepository
    ) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wishlist_alerts_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wishlist Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("localdeals://app/deal/$dealId"),
            appContext,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            dealId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(dealId.hashCode(), notification)

        val appNotification = AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            message = message,
            timestampMillis = System.currentTimeMillis(),
            isRead = false,
            deepLinkUrl = "localdeals://app/deal/$dealId"
        )
        notificationRepository.addNotification(appNotification)
    }
}
