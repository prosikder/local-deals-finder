package com.localdeals.finder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.AppNotification
import com.localdeals.finder.ui.components.EmptyState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.Check, contentDescription = "Mark all as read")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.notifications.isEmpty()) {
                EmptyState(
                    message = "No notifications. You're all caught up!"
                )
            } else {
                val context = androidx.compose.ui.platform.LocalContext.current
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        SwipeToDeleteNotificationItem(
                            notification = notification,
                            onDelete = { viewModel.deleteNotification(notification.id) },
                            onClick = { 
                                viewModel.markAsRead(notification.id)
                                if (notification.deepLinkUrl.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(notification.deepLinkUrl))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNotificationItem(
    notification: AppNotification,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                isDeleted = true
                true
            } else false
        }
    )

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            delay(300)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = MaterialTheme.colorScheme.errorContainer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                        )
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
