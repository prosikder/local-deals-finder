package com.localdeals.finder.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.usecase.SearchDealsUseCase
import com.localdeals.finder.domain.usecase.SearchFilters
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryScreenData(
    val category: Category,
    val results: List<Deal>,
    val filters: SearchFilters
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val searchDealsUseCase: SearchDealsUseCase,
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    private val _filters = MutableStateFlow(SearchFilters(categoryId = categoryId))

    private val _uiState = MutableStateFlow<UiState<CategoryScreenData>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _filters.collectLatest { filters ->
                loadCategoryData(filters)
            }
        }
    }

    fun updateFilters(newFilters: SearchFilters) {
        // Ensure categoryId is always maintained
        _filters.value = newFilters.copy(categoryId = categoryId)
    }

    fun resetFilters() {
        _filters.value = SearchFilters(categoryId = categoryId)
    }

    private suspend fun loadCategoryData(filters: SearchFilters) {
        _uiState.value = UiState.Loading
        try {
            val location = locationRepository.locationFlow.first()
            val categories = categoryRepository.getCategories().first()
            val category = categories.find { it.id == categoryId }
                ?: throw IllegalStateException("Category not found")

            val lat = location?.latitude ?: 23.8103
            val lon = location?.longitude ?: 90.4125

            val results = searchDealsUseCase(lat, lon, filters).first()

            if (results.isEmpty()) {
                // Show empty/error state but keep category context
                _uiState.value = UiState.Error("No deals found in this category. Try widening your filters.")
            } else {
                _uiState.value = UiState.Success(
                    CategoryScreenData(
                        category = category,
                        results = results,
                        filters = filters
                    )
                )
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Failed to load category deals")
        }
    }
}
