package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.CurrentLocation
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.domain.usecase.GetNearbyDealsUseCase
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeData(
    val location: CurrentLocation,
    val categories: List<Category>,
    val hotDeals: List<Deal>,
    val endingSoon: List<Deal>,
    val nearbyStores: List<Seller>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNearbyDealsUseCase: GetNearbyDealsUseCase,
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            fetchData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulate network delay
            delay(1000)
            fetchData()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchData() {
        try {
            combine(
                locationRepository.locationFlow.filterNotNull(),
                categoryRepository.getCategories(),
                sellerRepository.getSellers()
            ) { location, categories, sellers ->
                val dealsFlow = getNearbyDealsUseCase(location.latitude, location.longitude)
                val deals = dealsFlow.first()

                val now = System.currentTimeMillis()
                val endingSoon = deals.filter { it.expirationTimeMillis > now }
                    .sortedBy { it.expirationTimeMillis }
                    .take(10)

                val hotDeals = deals.take(10) // already sorted by distance + discount score

                val nearbyStores = sellers.map { seller ->
                    val distance = com.localdeals.finder.util.LocationUtils.haversineDistance(
                        location.latitude, location.longitude,
                        seller.latitude, seller.longitude
                    )
                    seller to distance
                }.sortedBy { it.second }.take(10).map { it.first }

                HomeData(
                    location = location,
                    categories = categories,
                    hotDeals = hotDeals,
                    endingSoon = endingSoon,
                    nearbyStores = nearbyStores
                )
            }.catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to load home data")
            }.collect { homeData ->
                if (homeData.hotDeals.isEmpty() && homeData.endingSoon.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(homeData)
                }
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
