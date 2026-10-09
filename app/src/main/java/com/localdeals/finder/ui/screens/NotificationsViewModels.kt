package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.AppNotification
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val notifications: List<AppNotification> = emptyList()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.getNotifications(user.id).collectLatest { list ->
                _state.value = _state.value.copy(notifications = list)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.markAllAsRead(user.id)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }
}
