package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Users", "Sellers", "Deals", "Reviews", "Categories")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> AdminOverviewTab(state.stats)
                    1 -> AdminUsersTab(state.users, viewModel::toggleUserBlock)
                    2 -> AdminSellersTab(state.sellers, viewModel::toggleSellerSuspend)
                    3 -> AdminDealsTab(state.deals, viewModel::removeDeal)
                    4 -> AdminReviewsTab(state.reportedReviews, viewModel::dismissReviewReport, viewModel::deleteReview)
                    5 -> AdminCategoriesTab(state.categories, viewModel::addCategory, viewModel::updateCategory, viewModel::deleteCategory)
                }
            }
        }
    }
}

@Composable
fun AdminOverviewTab(stats: AdminStats?) {
    if (stats == null) return
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminStatCard("Total Users", stats.totalUsers)
        AdminStatCard("Total Sellers", stats.totalSellers)
        AdminStatCard("Total Deals", stats.totalDeals)
        AdminStatCard("Total Orders", stats.totalOrders)
        AdminStatCard("Total Views (Tracked)", stats.totalViews)
        AdminStatCard("Total Reviews", stats.totalReviews)
    }
}

@Composable
fun AdminStatCard(title: String, value: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun AdminUsersTab(users: List<User>, onToggleBlock: (String, Boolean) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users, key = { it.id }) { user ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium)
                        Text(if (user.isBlocked) "Blocked" else "Active", color = if (user.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                    Switch(
                        checked = user.isBlocked,
                        onCheckedChange = { onToggleBlock(user.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSellersTab(sellers: List<Seller>, onToggleSuspend: (String, Boolean) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sellers, key = { it.id }) { seller ->
            val isSuspended = seller.status == "SUSPENDED"
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(seller.businessName, style = MaterialTheme.typography.titleMedium)
                        Text(seller.status, color = if (isSuspended) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                    Switch(
                        checked = isSuspended,
                        onCheckedChange = { onToggleSuspend(seller.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDealsTab(deals: List<Deal>, onRemove: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(deals, key = { it.id }) { deal ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(deal.title, style = MaterialTheme.typography.titleMedium)
                        Text("Price: \$${deal.discountedPrice}", style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { onRemove(deal.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Deal", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReviewsTab(reviews: List<Review>, onDismiss: (String) -> Unit, onDelete: (String) -> Unit) {
    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reported reviews")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reviews, key = { it.id }) { review ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(review.userName, style = MaterialTheme.typography.titleMedium)
                        Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onDismiss(review.id) }) { Text("Dismiss") }
                            Button(onClick = { onDelete(review.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesTab(
    categories: List<Category>,
    onAdd: (Category) -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("ic_category") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Category") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAdd(Category(java.util.UUID.randomUUID().toString(), name, icon))
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { category ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { onDelete(category.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
