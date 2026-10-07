package com.localdeals.finder.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.usecase.SearchDealsUseCase
import com.localdeals.finder.domain.usecase.SearchFilters
import com.localdeals.finder.domain.usecase.SortType
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenData(
    val results: List<Deal>,
    val filters: SearchFilters,
    val categories: List<Category>
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchDealsUseCase: SearchDealsUseCase,
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository,
    private val dataStoreManager: DataStoreManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Survives rotation natively in ViewModel. For process death, we store query in SavedStateHandle.
    private val _query = savedStateHandle.getStateFlow("query", "")
    private val _filters = MutableStateFlow(SearchFilters())

    private val _uiState = MutableStateFlow<UiState<SearchScreenData>>(UiState.Empty)
    val uiState = _uiState.asStateFlow()

    val recentSearches = dataStoreManager.recentSearches.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    init {
        // Observe query and filters, debounce, and trigger search
        viewModelScope.launch {
            combine(_query, _filters) { query, filters ->
                filters.copy(query = query)
            }
            .debounce(500L) // Debounce search
            .collectLatest { activeFilters ->
                performSearch(activeFilters)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        savedStateHandle["query"] = newQuery
    }

    fun onSearch(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                dataStoreManager.addRecentSearch(query)
            }
        }
        savedStateHandle["query"] = query
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            dataStoreManager.clearRecentSearches()
        }
    }

    fun updateFilters(newFilters: SearchFilters) {
        _filters.value = newFilters
    }

    fun resetFilters() {
        _filters.value = SearchFilters(query = _query.value)
    }

    private suspend fun performSearch(filters: SearchFilters) {
        if (filters.query.isBlank() && filters == SearchFilters()) {
            // Show empty state / suggestions instead of searching everything initially if no query/filters
            _uiState.value = UiState.Empty
            return
        }

        _uiState.value = UiState.Loading
        try {
            val location = locationRepository.locationFlow.first()
            val categories = categoryRepository.getCategories().first()

            val lat = location?.latitude ?: 23.8103
            val lon = location?.longitude ?: 90.4125

            val results = searchDealsUseCase(lat, lon, filters).first()

            if (results.isEmpty()) {
                _uiState.value = UiState.Error("No deals found. Try widening your filters or distance.")
            } else {
                _uiState.value = UiState.Success(
                    SearchScreenData(
                        results = results,
                        filters = filters,
                        categories = categories
                    )
                )
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Search failed")
        }
    }
}
