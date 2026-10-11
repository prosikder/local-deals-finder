package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localdeals.finder.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("""
        SELECT * FROM chat_messages
        WHERE (senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1)
        ORDER BY timestampMillis ASC
    """)
    fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_messages
        WHERE senderId = :userId OR receiverId = :userId
        ORDER BY timestampMillis DESC
    """)
    fun getChatsForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    suspend fun markAsRead(receiverId: String, senderId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}
