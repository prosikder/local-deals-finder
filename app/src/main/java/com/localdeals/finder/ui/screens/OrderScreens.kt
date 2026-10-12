package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRequestScreen(
    dealId: String?,
    onBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: OrderRequestViewModel = hiltViewModel()
) {
    LaunchedEffect(dealId) {
        if (dealId != null) {
            viewModel.loadDeal(dealId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onOrderSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.deal == null) {
                Text(uiState.error ?: "Deal not found", modifier = Modifier.align(Alignment.Center))
            } else {
                val deal = uiState.deal!!
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text(deal.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("৳${deal.discountedPrice.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Quantity selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quantity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.updateQuantity(uiState.quantity - 1) }) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(uiState.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { viewModel.updateQuantity(uiState.quantity + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    Text("${deal.stock} left in stock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { viewModel.updateNote(it) },
                        label = { Text("Pickup/Delivery Note") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.phoneConfirmation,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Phone Confirmation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))

                    // Summary
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount:")
                        Text("৳${(deal.discountedPrice * uiState.quantity).toInt()}", fontWeight = FontWeight.Bold)
                    }

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.submitOrder() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Submit Order Request")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onBack: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    viewModel: MyOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Simulate", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = uiState.isSimulatorEnabled,
                            onCheckedChange = { viewModel.toggleSimulator(it) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.orders.isEmpty()) {
                Text("No orders found.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(uiState.orders, key = { it.order.id }) { orderData ->
                        OrderCard(
                            orderData = orderData,
                            currentUserId = uiState.currentUserId,
                            onCancel = { viewModel.cancelOrder(orderData.order.id) },
                            onReview = { onNavigateToReview(orderData.deal?.id ?: "") },
                            onUpdateStatus = { status -> viewModel.updateOrderStatus(orderData.order.id, status) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    orderData: OrderWithDeal, 
    currentUserId: String,
    onCancel: () -> Unit, 
    onReview: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit = {}
) {
    val order = orderData.order
    val deal = orderData.deal

    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)
        OrderStatus.ACCEPTED -> Color(0xFF1976D2)
        OrderStatus.READY -> Color(0xFF388E3C)
        OrderStatus.COMPLETED -> Color(0xFF388E3C)
        OrderStatus.REJECTED -> Color.Red
        OrderStatus.CANCELLED -> Color.Gray
    }

    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateStr = sdf.format(Date(order.timestampMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(deal?.title ?: "Unknown Deal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status.name,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val sellerAcc = deal?.sellerId?.removePrefix("s_")?.let { "Seller: user_s$it@local.app" } ?: ""
            if (sellerAcc.isNotBlank()) {
                Text(sellerAcc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Quantity: ${order.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (order.note.isNotBlank()) {
                Text("Note: ${order.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            val isBuyer = order.buyerId == currentUserId
            val sellerSuffix = currentUserId.removePrefix("user_s")
            val isSeller = order.sellerId == currentUserId ||
                (sellerSuffix.isNotEmpty() && (order.sellerId == "s_$sellerSuffix" || order.sellerId == "user_s$sellerSuffix")) ||
                (deal != null && (deal.sellerId == currentUserId || (sellerSuffix.isNotEmpty() && deal.sellerId == "s_$sellerSuffix")))

            if (isBuyer) {
                if (order.status == OrderStatus.PENDING) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel Order")
                    }
                } else if (order.status == OrderStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onReview,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave a Review")
                    }
                }
            }

            if (isSeller) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    if (order.status == OrderStatus.PENDING) {
                        Button(onClick = { onUpdateStatus(OrderStatus.ACCEPTED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("Accept") }
                        OutlinedButton(onClick = { onUpdateStatus(OrderStatus.REJECTED) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("Reject") }
                    } else if (order.status == OrderStatus.ACCEPTED) {
                        Button(onClick = { onUpdateStatus(OrderStatus.READY) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) { Text("Mark Ready") }
                    } else if (order.status == OrderStatus.READY) {
                        Button(onClick = { onUpdateStatus(OrderStatus.COMPLETED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Complete") }
                    }
                }
            }
        }
    }
}
