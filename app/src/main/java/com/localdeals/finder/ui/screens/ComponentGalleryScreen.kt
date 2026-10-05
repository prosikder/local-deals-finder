package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.ui.components.*

@Composable
fun ComponentGalleryScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TopBar(title = "Component Gallery", onBackClick = onBack)
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Buttons")
            PrimaryButton(text = "Primary Button", onClick = {})
            Spacer(modifier = Modifier.height(16.dp))

            Text("Bars")
            SearchBar(query = "", onQueryChange = {})
            Spacer(modifier = Modifier.height(16.dp))

            Text("Badges & Chips")
            Row {
                CategoryChip(text = "Food", isSelected = true, onClick = {})
                Spacer(modifier = Modifier.width(8.dp))
                CategoryChip(text = "Electronics", isSelected = false, onClick = {})
            }
            Spacer(modifier = Modifier.height(8.dp))
            DiscountBadge(percentage = 50)
            Spacer(modifier = Modifier.height(8.dp))
            RatingBar(rating = 4.5, reviewCount = 120)
            Spacer(modifier = Modifier.height(8.dp))
            CountdownText(timeRemaining = "01:23:45")
            Spacer(modifier = Modifier.height(16.dp))

            Text("Cards")
            DealCard(
                deal = Deal("1", "s1", "Awesome Pizza", "Desc", 20.0, 10.0, 50, 5, "c1", 0L, 500),
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            StoreCard(
                seller = Seller("s1", "u1", "Pizza Hut", "123 Main St", 0.0, 0.0, 4.2),
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("States")
            EmptyState(message = "No items found")
            Spacer(modifier = Modifier.height(8.dp))
            ErrorState(message = "An error occurred", onRetry = {})
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.height(100.dp)) {
                LoadingSkeleton()
            }
        }
    }
}
