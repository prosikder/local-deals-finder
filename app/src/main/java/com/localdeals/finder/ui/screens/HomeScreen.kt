package com.localdeals.finder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.ui.components.DealCard
import com.localdeals.finder.ui.components.StoreCard
import com.localdeals.finder.ui.components.ErrorState
import com.localdeals.finder.ui.components.EmptyState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateGallery: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDeal: (String) -> Unit,
    onNavigateToStore: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            if (uiState is UiState.Success) {
                val data = (uiState as UiState.Success<HomeData>).data
                HomeTopBar(
                    locationName = data.location.name,
                    onChangeLocation = onNavigateToLocation,
                    onSearchClick = onNavigateToSearch,
                    onNavigateGallery = onNavigateGallery,
                    onNavigateNotifications = onNavigateToNotifications
                )
            } else {
                HomeTopBar(
                    locationName = "Loading...",
                    onChangeLocation = onNavigateToLocation,
                    onSearchClick = onNavigateToSearch,
                    onNavigateGallery = onNavigateGallery,
                    onNavigateNotifications = onNavigateToNotifications
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    HomeSkeleton()
                }
                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadHomeData() }
                    )
                }
                is UiState.Empty -> {
                    EmptyState(
                        message = "No deals found near you. Try expanding your search area or changing location.",
                        onAction = onNavigateToLocation,
                        actionText = "Change Location"
                    )
                }
                is UiState.Success -> {
                    HomeContent(
                        data = state.data,
                        onCategoryClick = onNavigateToCategory,
                        onDealClick = onNavigateToDeal,
                        onStoreClick = onNavigateToStore,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    locationName: String,
    onChangeLocation: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateGallery: () -> Unit,
    onNavigateNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChangeLocation() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebug) {
                TextButton(onClick = onNavigateGallery) {
                    Text("UI Gallery")
                }
            }
            IconButton(onClick = onNavigateNotifications) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
        }
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() },
            placeholder = { Text("Search for deals, food, electronics...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            enabled = false, // disabled to act as a button
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledBorderColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.large
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    data: HomeData,
    onCategoryClick: (String) -> Unit,
    onDealClick: (String) -> Unit,
    onStoreClick: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    // PullToRefresh container requires state
    // For simplicity, we just use a basic LazyColumn. A proper PullToRefresh needs more boilerplate in M3.
    // Assuming PullToRefreshBox if available, else a workaround:
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 50 })
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                if (isRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            item {
                SectionTitle("Categories")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(data.categories, key = { it.id }) { category ->
                        CategoryChip(category = category, onClick = { onCategoryClick(category.id) })
                    }
                }
            }

            if (data.hotDeals.isNotEmpty()) {
                item {
                    SectionTitle("Hot Deals Near You")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(data.hotDeals, key = { it.id }) { deal ->
                            DealCard(
                                deal = deal,
                                onClick = { onDealClick(deal.id) },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }
            }

            if (data.endingSoon.isNotEmpty()) {
                item {
                    SectionTitle("Ending Soon")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(data.endingSoon, key = { "es_${it.id}" }) { deal ->
                            DealCard(
                                deal = deal,
                                onClick = { onDealClick(deal.id) },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }
            }

            if (data.nearbyStores.isNotEmpty()) {
                item {
                    SectionTitle("Nearby Stores")
                }
                items(data.nearbyStores, key = { it.id }) { seller ->
                    StoreCard(
                        seller = seller,
                        onClick = { onStoreClick(seller.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun CategoryChip(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(category.name.take(1), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.name, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun HomeSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Skeleton Categories
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // Skeleton Title
        Box(
            modifier = Modifier
                .height(24.dp)
                .width(150.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Skeleton Cards
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .width(280.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                )
            }
        }
    }
}
