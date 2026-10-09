package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.ChatMessage
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.ChatRepository
import com.localdeals.finder.domain.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class ChatListUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null
)

data class Conversation(
    val userId: String,
    val userName: String,
    val lastMessage: String,
    val unreadCount: Int,
    val timestampMillis: Long
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState(isLoading = true))
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _uiState.value = ChatListUiState(error = "User not logged in")
                return@launch
            }

            // In a real app we'd have a specific getConversations method
            // Here we'll just mock it or assume empty if we don't have a way to query all messages
            _uiState.value = ChatListUiState(isLoading = false, conversations = emptyList())
        }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val currentUserId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadChat(otherUserId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Not logged in")
                return@launch
            }

            // Fetch other user name (might be a seller)
            // If it starts with user_s, it might be a seller's userId
            val seller = sellerRepository.getSellers().first().find { it.userId == otherUserId }
            val otherName = seller?.businessName ?: "User"

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentUserId = user.id,
                otherUserId = otherUserId,
                otherUserName = otherName
            )

            // Mark as read immediately
            chatRepository.markAsRead(user.id, otherUserId)

            combine(
                chatRepository.getMessages(user.id, otherUserId),
                chatRepository.isTyping(user.id, otherUserId)
            ) { messages, isTyping ->
                _uiState.value.copy(messages = messages, isTyping = isTyping)
            }.collect { state ->
                _uiState.value = state
                // Mark incoming as read
                if (state.messages.any { it.receiverId == user.id && !it.isRead }) {
                    chatRepository.markAsRead(user.id, otherUserId)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val state = _uiState.value
        viewModelScope.launch {
            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = state.currentUserId,
                receiverId = state.otherUserId,
                content = content,
                timestampMillis = System.currentTimeMillis()
            )
            chatRepository.sendMessage(msg)
        }
    }
}
