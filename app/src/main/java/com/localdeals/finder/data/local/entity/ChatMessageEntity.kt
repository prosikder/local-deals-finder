package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [
        Index("senderId"),
        Index("receiverId"),
        Index("timestampMillis")
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestampMillis: Long,
    val isRead: Boolean = false
)
