package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.CurrentLocation
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.util.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapData(
    val deals: List<Deal> = emptyList(),
    val sellers: Map<String, Seller> = emptyMap(),
    val categories: List<Category> = emptyList(),
    val userLocation: CurrentLocation? = null,
    val selectedCategoryId: String? = null,
    val searchRadiusMeters: Int = 5000,
    val isListView: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val dealRepository: DealRepository,
    private val sellerRepository: SellerRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapData())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val location = locationRepository.locationFlow.firstOrNull()
            val categories = categoryRepository.getCategories().firstOrNull() ?: emptyList()
            val sellers = sellerRepository.getSellers().firstOrNull()?.associateBy { it.id } ?: emptyMap()
            val allDeals = dealRepository.getDeals().firstOrNull() ?: emptyList()

            // Filter deals within default radius if user location is available
            val filteredDeals = if (location != null) {
                allDeals.filter { deal ->
                    val seller = sellers[deal.sellerId]
                    if (seller != null) {
                        val dist = LocationUtils.haversineDistance(
                            location.latitude, location.longitude,
                            seller.latitude, seller.longitude
                        ) * 1000
                        dist <= 5000 // default 5km
                    } else false
                }
            } else {
                allDeals
            }

            _uiState.value = _uiState.value.copy(
                deals = filteredDeals,
                sellers = sellers,
                categories = categories,
                userLocation = location
            )
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
            
            // Re-apply filters
            val location = _uiState.value.userLocation
            val sellers = _uiState.value.sellers
            val allDeals = dealRepository.getDeals().firstOrNull() ?: emptyList()
            
            val filteredDeals = allDeals.filter { deal ->
                val seller = sellers[deal.sellerId]
                val matchesCategory = categoryId == null || deal.categoryId == categoryId
                val matchesDistance = if (location != null && seller != null) {
                    val dist = LocationUtils.haversineDistance(
                        location.latitude, location.longitude,
                        seller.latitude, seller.longitude
                    ) * 1000
                    dist <= _uiState.value.searchRadiusMeters
                } else true
                
                matchesCategory && matchesDistance
            }
            
            _uiState.value = _uiState.value.copy(deals = filteredDeals)
        }
    }

    fun setRadius(radiusMeters: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchRadiusMeters = radiusMeters)
            setCategoryFilter(_uiState.value.selectedCategoryId) // triggers refilter
        }
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isListView = !_uiState.value.isListView)
    }
}
