package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index("userId"),
        Index("timestampMillis")
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestampMillis: Long,
    val isRead: Boolean = false,
    val deepLinkUrl: String = ""
)
