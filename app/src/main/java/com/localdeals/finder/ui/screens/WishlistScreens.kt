package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.ui.screens.SortType
import com.localdeals.finder.ui.components.DealCard
import com.localdeals.finder.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDeal: (String) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.List, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nearest") },
                            onClick = { viewModel.setSortType(SortType.NEAREST); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Biggest Discount") },
                            onClick = { viewModel.setSortType(SortType.BIGGEST_DISCOUNT); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Cheapest") },
                            onClick = { viewModel.setSortType(SortType.CHEAPEST); showSortMenu = false }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.wishlist.isEmpty()) {
                EmptyState(
                    message = "Your Wishlist is Empty.\nExplore deals and add them to your wishlist to get price and expiry alerts."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = uiState.wishlist, key = { deal: Deal -> deal.id }) { deal ->
                        val isAlertEnabled = uiState.alertsEnabled[deal.id] ?: false
                        WishlistItemCard(
                            deal = deal,
                            isAlertEnabled = isAlertEnabled,
                            onDealClick = { onNavigateToDeal(deal.id) },
                            onToggleAlert = { viewModel.toggleAlert(deal.id, it) },
                            onRemove = { viewModel.removeDeal(deal.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    deal: Deal,
    isAlertEnabled: Boolean,
    onDealClick: () -> Unit,
    onToggleAlert: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onDealClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = deal.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Price: ৳${deal.discountedPrice.toInt()}", style = MaterialTheme.typography.bodyLarge)
                Text("-${deal.discountPercentage.toInt()}%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAlertEnabled) Icons.Default.Notifications else Icons.Default.FavoriteBorder,
                        contentDescription = "Alert Toggle",
                        tint = if (isAlertEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Expiry & Price Alerts", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = isAlertEnabled, onCheckedChange = onToggleAlert)
            }
        }
    }
}
