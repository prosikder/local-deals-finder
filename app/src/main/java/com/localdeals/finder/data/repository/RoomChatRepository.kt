package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.ChatDao
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.ChatMessage
import com.localdeals.finder.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomChatRepository @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    private val typingStates = MutableStateFlow<Set<String>>(emptySet())
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessage>> {
        return chatDao.getMessages(userId1, userId2).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChatsForUser(userId: String): Flow<List<ChatMessage>> {
        return chatDao.getChatsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return chatDao.getUnreadCount(userId)
    }

    override fun isTyping(userId1: String, userId2: String): Flow<Boolean> {
        return typingStates.map { it.contains("${userId2}_${userId1}") }
    }

    override suspend fun sendMessage(message: ChatMessage) {
        chatDao.insertMessage(message.toEntity())

        // Simulated seller auto-reply if user messages seller
        if (message.receiverId.startsWith("user_s") && !message.senderId.startsWith("user_s")) {
            repositoryScope.launch {
                val typingKey = "${message.receiverId}_${message.senderId}"
                delay(1000)
                typingStates.value = typingStates.value + typingKey
                delay((2000L..3500L).random())
                typingStates.value = typingStates.value - typingKey

                val replyContent = when {
                    message.content.contains("stock", ignoreCase = true) -> "Yes, it's in stock!"
                    message.content.contains("price", ignoreCase = true) -> "The price is fixed as shown in the deal."
                    else -> "Thanks for reaching out! Let me know how I can help."
                }

                chatDao.insertMessage(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = message.receiverId,
                        receiverId = message.senderId,
                        content = replyContent,
                        timestampMillis = System.currentTimeMillis()
                    ).toEntity()
                )
            }
        }
    }

    override suspend fun markAsRead(userId1: String, userId2: String) {
        chatDao.markAsRead(receiverId = userId1, senderId = userId2)
    }
}
