package com.localdeals.finder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.ui.components.CanvasChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddDeal: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: SellerDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Dashboard") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Unknown error")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DashboardCard(title = "Active Deals", value = state.activeDealsCount.toString(), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    DashboardCard(title = "Pending Orders", value = state.pendingOrdersCount.toString(), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DashboardCard(title = "Unread Msgs", value = state.unreadMessagesCount.toString(), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    DashboardCard(title = "Total Views", value = state.totalViews.toString(), modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("7-Day Views", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    CanvasChart(data = state.chartData)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = onNavigateToAddDeal) { Text("Add Deal") }
                    Button(onClick = onNavigateToOrders) { Text("Orders") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = onNavigateToInbox) { Text("Inbox") }
                    Button(onClick = onNavigateToProfile) { Text("Store Profile") }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("Your Deals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                state.activeDeals.forEach { deal ->
                    DealManageCard(
                        deal = deal,
                        onEdit = { onNavigateToAddDeal() }, // Normally would pass ID, but placeholder for now
                        onStockIncrease = { viewModel.updateStock(deal, 1) },
                        onStockDecrease = { viewModel.updateStock(deal, -1) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DealManageCard(
    deal: com.localdeals.finder.domain.model.Deal,
    onEdit: () -> Unit,
    onStockIncrease: () -> Unit,
    onStockDecrease: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(deal.title, style = MaterialTheme.typography.titleMedium)
                if (deal.stock == 0) {
                    Text("SOLD OUT", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("Stock: ${deal.stock}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onStockDecrease) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onStockIncrease) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

