package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.ui.components.DealCard
import com.localdeals.finder.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onNavigateToDeal: (String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    val categoryName = when (val state = uiState) {
        is UiState.Success -> state.data.category.name
        else -> "Category"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Filters")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    HomeSkeleton()
                }
                is UiState.Error -> {
                    EmptyState(
                        message = state.message,
                        onAction = { showFilters = true },
                        actionText = "Change Filters"
                    )
                }
                is UiState.Success -> {
                    val data = state.data
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("${data.results.size} results found", style = MaterialTheme.typography.labelMedium)
                            }
                            items(data.results, key = { it.id }) { deal ->
                                DealCard(
                                    deal = deal,
                                    onClick = { onNavigateToDeal(deal.id) }
                                )
                            }
                        }
                    }

                    if (showFilters) {
                        // CategoryScreen doesn't need to show category chips in its filter sheet normally, 
                        // but we can reuse FiltersBottomSheet and pass the categories list if needed, 
                        // or make a simpler one. To save time, we reuse the one from SearchScreen.
                        // We just need a list of categories (or just pass the single category to keep it disabled/read-only).
                        FiltersBottomSheet(
                            filters = data.filters,
                            categories = listOf(data.category), // Only show this category
                            onDismiss = { showFilters = false },
                            onApply = { newFilters ->
                                viewModel.updateFilters(newFilters)
                                showFilters = false
                            },
                            onReset = {
                                viewModel.resetFilters()
                                showFilters = false
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
