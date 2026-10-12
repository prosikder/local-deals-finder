package com.localdeals.finder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.usecase.SearchFilters
import com.localdeals.finder.domain.usecase.SortType
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.ui.components.DealCard
import com.localdeals.finder.ui.components.EmptyState
import com.localdeals.finder.ui.components.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToDeal: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.onQueryChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        placeholder = { Text("Search deals or stores") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    viewModel.onQueryChange("")
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                },
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
                is UiState.Empty -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        if (recentSearches.isNotEmpty() && searchQuery.isBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                    Text("Clear All")
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(recentSearches.toList(), key = { it }) { search ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { 
                                            searchQuery = search
                                            viewModel.onQueryChange(search)
                                            viewModel.onSearch(search)
                                        },
                                        label = { Text(search) }
                                    )
                                }
                            }
                        } else {
                            EmptyState(
                                message = "Start typing to search for deals, or apply filters.",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                is UiState.Loading -> {
                    HomeSkeleton()
                }
                is UiState.Error -> {
                    EmptyState(
                        message = state.message, // "No deals found. Try widening your filters or distance."
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${data.results.size} results found", style = MaterialTheme.typography.labelMedium)
                                    val hasActiveFilters = data.filters.categoryId != null || data.filters.minRating != null || data.filters.maxDistanceMeters != null
                                    if (hasActiveFilters) {
                                        TextButton(onClick = { viewModel.resetFilters() }) {
                                            Text("Clear Filters")
                                        }
                                    }
                                }
                            }
                            
                            val activeFilters = buildList {
                                data.filters.categoryId?.let { catId -> data.categories.find { it.id == catId }?.let { add(it.name) } }
                                data.filters.minRating?.let { add("$it+ Stars") }
                                data.filters.maxDistanceMeters?.let { add("< ${it/1000}km") }
                            }
                            
                            if (activeFilters.isNotEmpty()) {
                                item {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(activeFilters) { filterLabel ->
                                            InputChip(
                                                selected = true,
                                                onClick = { },
                                                label = { Text(filterLabel) }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            items(data.results, key = { it.id }) { deal ->
                                DealCard(
                                    deal = deal,
                                    onClick = {
                                        viewModel.onSearch(searchQuery) // Save search when clicking a result
                                        onNavigateToDeal(deal.id)
                                    }
                                )
                            }
                        }
                    }

                    if (showFilters) {
                        FiltersBottomSheet(
                            filters = data.filters,
                            categories = data.categories,
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    filters: SearchFilters,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (SearchFilters) -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentFilters by remember { mutableStateOf(filters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters & Sort", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Sort By", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortType.entries.toTypedArray()) { sortType ->
                    FilterChip(
                        selected = currentFilters.sortType == sortType,
                        onClick = { currentFilters = currentFilters.copy(sortType = sortType) },
                        label = { Text(sortType.name.replace("_", " ")) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = currentFilters.categoryId == null,
                        onClick = { currentFilters = currentFilters.copy(categoryId = null) },
                        label = { Text("All") }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = currentFilters.categoryId == category.id,
                        onClick = { currentFilters = currentFilters.copy(categoryId = category.id) },
                        label = { Text(category.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Distance", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val distances = listOf(null to "Any", 5000 to "5 km", 10000 to "10 km", 20000 to "20 km")
                items(distances) { (dist, label) ->
                    FilterChip(
                        selected = currentFilters.maxDistanceMeters == dist,
                        onClick = { currentFilters = currentFilters.copy(maxDistanceMeters = dist) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Minimum Rating", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ratings = listOf(null to "Any", 3.0 to "3+", 4.0 to "4+", 4.5 to "4.5+")
                items(ratings) { (rating, label) ->
                    FilterChip(
                        selected = currentFilters.minRating == rating,
                        onClick = { currentFilters = currentFilters.copy(minRating = rating) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onApply(currentFilters) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
