package com.localdeals.finder.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.ui.components.DealCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerStoreScreen(
    sellerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDeal: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: SellerStoreViewModel = hiltViewModel()
) {
    LaunchedEffect(sellerId) {
        viewModel.loadSeller(sellerId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.seller?.businessName ?: "Store") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.seller != null && uiState.error != "This store is currently suspended.") {
                        IconButton(onClick = { viewModel.toggleFavorite(sellerId) }) {
                            Icon(
                                if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.seller != null && uiState.error != "This store is currently suspended.") {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToChat(sellerId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Chat")
                        }
                        // Order placeholder for Prompt 10
                        Button(
                            onClick = { Toast.makeText(context, "Order coming soon", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Order")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.seller == null) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (uiState.seller != null) {
            val seller = uiState.seller!!
            
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // Header
                StoreHeader(seller, uiState)

                if (uiState.error == "This store is currently suspended.") {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("This store is currently suspended.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    val tabs = listOf("Deals", "Reviews", "About")
                    
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                    
                    when (selectedTab) {
                        0 -> DealsTab(uiState, onNavigateToDeal)
                        1 -> ReviewsTab(uiState, { viewModel.reportReview(it) })
                        2 -> AboutTab(seller, context)
                    }
                }
            }
        }
    }
}

@Composable
fun StoreHeader(seller: Seller, uiState: SellerStoreUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                    )
                )
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(seller.businessName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(seller.description.ifEmpty { "No description available." }, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(seller.address, style = MaterialTheme.typography.bodySmall)
                
                if (uiState.distanceMeters != null) {
                    Spacer(Modifier.width(8.dp))
                    val distStr = com.localdeals.finder.util.FormatUtils.formatDistance(uiState.distanceMeters)
                    if (distStr.isNotEmpty()) {
                        Text("• $distStr away", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(String.format(Locale.getDefault(), "%.1f", uiState.averageRating), fontWeight = FontWeight.Bold)
                    Text(" (${uiState.reviews.size} reviews)", style = MaterialTheme.typography.bodySmall)
                }
                
                if (uiState.error != "This store is currently suspended.") {
                    Surface(
                        color = if (uiState.isStoreOpen) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (uiState.isStoreOpen) "OPEN" else "CLOSED",
                            color = if (uiState.isStoreOpen) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DealsTab(uiState: SellerStoreUiState, onNavigateToDeal: (String) -> Unit) {
    if (uiState.deals.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No active deals at the moment.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.deals) { deal ->
                DealCard(deal = deal, onClick = { onNavigateToDeal(deal.id) })
            }
        }
    }
}

@Composable
fun ReviewsTab(uiState: SellerStoreUiState, onReportReview: (String) -> Unit) {
    if (uiState.reviews.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No reviews yet.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Rating Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                for (i in 5 downTo 1) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$i ★", modifier = Modifier.width(32.dp))
                        val frac = uiState.ratingDistribution[i] ?: 0f
                        LinearProgressIndicator(
                            progress = { frac },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Text("${(frac * 100).toInt()}%", modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }
            items(uiState.reviews) { review ->
                ReviewItem(review, { onReportReview(review.id) })
            }
        }
    }
}



@Composable
fun AboutTab(seller: Seller, context: android.content.Context) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Text("Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            if (seller.phoneNumber.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${seller.phoneNumber}"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Text(seller.phoneNumber, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val uri = Uri.parse("geo:${seller.latitude},${seller.longitude}?q=${seller.latitude},${seller.longitude}(${Uri.encode(seller.businessName)})")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No maps app installed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(seller.address, style = MaterialTheme.typography.bodyLarge)
                    Text("Get Directions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            Text("Opening Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            if (seller.openingHours.isEmpty()) {
                Text("No opening hours provided.", style = MaterialTheme.typography.bodyMedium)
            } else {
                days.forEach { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day, style = MaterialTheme.typography.bodyMedium)
                        Text(seller.openingHours[day] ?: "Closed", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
