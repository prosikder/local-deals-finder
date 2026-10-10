package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.CurrentLocation
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.domain.repository.StoreFavoriteRepository
import com.localdeals.finder.domain.usecase.IsStoreOpenUseCase
import com.localdeals.finder.util.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellerStoreUiState(
    val isLoading: Boolean = true,
    val seller: Seller? = null,
    val deals: List<Deal> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val isStoreOpen: Boolean = false,
    val distanceMeters: Int? = null,
    val averageRating: Double = 0.0,
    val ratingDistribution: Map<Int, Float> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class SellerStoreViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
    private val dealRepository: DealRepository,
    private val reviewRepository: ReviewRepository,
    private val storeFavoriteRepository: StoreFavoriteRepository,
    private val locationRepository: LocationRepository,
    private val isStoreOpenUseCase: IsStoreOpenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerStoreUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSeller(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val seller = sellerRepository.getSellerById(sellerId)
                if (seller == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Seller not found") }
                    return@launch
                }

                // Check status
                if (seller.status == "SUSPENDED") {
                    _uiState.update { it.copy(
                        isLoading = false,
                        seller = seller,
                        error = "This store is currently suspended."
                    ) }
                    return@launch
                }

                // Load flows in parallel and combine
                combine(
                    dealRepository.getDeals(),
                    reviewRepository.getReviewsForSeller(sellerId),
                    storeFavoriteRepository.isFavorite(sellerId),
                    locationRepository.locationFlow
                ) { allDeals, reviews, isFav, loc ->
                    val sellerDeals = allDeals.filter { it.sellerId == sellerId && it.expirationTimeMillis > System.currentTimeMillis() }
                    
                    val isOpen = isStoreOpenUseCase(seller.openingHours)
                    
                    var distMeters: Int? = null
                    if (loc != null) {
                        distMeters = (LocationUtils.haversineDistance(
                            loc.latitude, loc.longitude,
                            seller.latitude, seller.longitude
                        ) * 1000).toInt()
                    }

                    val avgRating = if (reviews.isEmpty()) seller.rating else reviews.map { it.rating }.average()
                    
                    // Rating distribution
                    val dist = mutableMapOf<Int, Float>(1 to 0f, 2 to 0f, 3 to 0f, 4 to 0f, 5 to 0f)
                    if (reviews.isNotEmpty()) {
                        val total = reviews.size.toFloat()
                        reviews.groupBy { it.rating }.forEach { (rating, list) ->
                            dist[rating] = list.size / total
                        }
                    }

                    SellerStoreUiState(
                        isLoading = false,
                        seller = seller,
                        deals = sellerDeals,
                        reviews = reviews,
                        isFavorite = isFav,
                        isStoreOpen = isOpen,
                        distanceMeters = distMeters,
                        averageRating = avgRating,
                        ratingDistribution = dist,
                        error = null
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleFavorite(sellerId: String) {
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                storeFavoriteRepository.removeFavorite(sellerId)
            } else {
                storeFavoriteRepository.addFavorite(sellerId)
            }
        }
    }

    fun reportReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.reportReview(reviewId)
        }
    }
}
