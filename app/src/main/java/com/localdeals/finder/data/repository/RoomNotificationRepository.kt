package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.NotificationDao
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.AppNotification
import com.localdeals.finder.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomNotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addNotification(notification: AppNotification) {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    override suspend fun deleteNotification(notificationId: String) {
        notificationDao.deleteNotification(notificationId)
    }
}
