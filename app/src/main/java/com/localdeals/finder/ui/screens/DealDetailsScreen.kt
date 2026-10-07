package com.localdeals.finder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.ui.components.DiscountBadge
import com.localdeals.finder.ui.components.EmptyState
import com.localdeals.finder.ui.components.StoreCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailsScreen(
    onBack: () -> Unit,
    onNavigateToSeller: (String) -> Unit,
    viewModel: DealDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deal Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    EmptyState(
                        message = state.message,
                        onAction = { viewModel.loadDealDetails() },
                        actionText = "Retry"
                    )
                }
                is UiState.Empty -> {
                    // Not used for this screen, but needed to be exhaustive
                }
                is UiState.Success -> {
                    val data = state.data
                    val deal = data.deal
                    val seller = data.seller
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Image Area (Gradient placeholder as per requirements)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = deal.title.take(1),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White
                                )
                            }
                        }

                        // Title, Price, Discount
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = deal.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DiscountBadge(percentage = deal.discountPercentage)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "৳${deal.discountedPrice.toInt()}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "৳${deal.originalPrice.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Distance & Stock & Countdown
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val dist = data.distanceMeters?.let { "${it}m away" } ?: "Distance unknown"
                                        Text(dist, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    
                                    Text(
                                        text = "${deal.stock} left in stock",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LiveCountdownText(expirationTimeMillis = deal.expirationTimeMillis)
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        // Description
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = deal.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        // Seller Mini-card
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Sold by", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                StoreCard(
                                    seller = seller,
                                    onClick = { onNavigateToSeller(seller.id) }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        // Reviews
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Reviews (${data.reviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        if (data.reviews.isEmpty()) {
                            item {
                                Text(
                                    "No reviews yet.",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(data.reviews) { review ->
                                ReviewItem(review = review)
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for floating button
                        }
                    }

                    // Floating Action Button at bottom (Get Deal)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Button(
                            onClick = { /* Will be implemented in future prompt */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Request Order", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveCountdownText(expirationTimeMillis: Long) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val remaining = expirationTimeMillis - currentTime
    if (remaining > 0) {
        val days = remaining / (24 * 60 * 60 * 1000)
        val hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val mins = (remaining % (60 * 60 * 1000)) / (60 * 1000)
        val secs = (remaining % (60 * 1000)) / 1000
        
        val timeStr = if (days > 0) {
            "${days}d ${hours}h ${mins}m remaining"
        } else {
            String.format("%02d:%02d:%02d remaining", hours, mins, secs)
        }
        
        Text(
            text = "Ends in: $timeStr",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
    } else {
        Text(
            text = "Expired",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(review.userName.take(1), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(review.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(sdf.format(Date(review.timestampMillis)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(review.rating.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(start = 48.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    }
}
