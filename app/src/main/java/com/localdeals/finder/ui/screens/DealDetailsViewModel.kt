package com.localdeals.finder.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.util.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealDetailsData(
    val deal: Deal,
    val seller: Seller,
    val reviews: List<Review>,
    val distanceMeters: Int?
)

@HiltViewModel
class DealDetailsViewModel @Inject constructor(
    private val dealRepository: DealRepository,
    private val sellerRepository: SellerRepository,
    private val locationRepository: LocationRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dealId: String = checkNotNull(savedStateHandle["dealId"])

    private val _uiState = MutableStateFlow<UiState<DealDetailsData>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadDealDetails()
    }

    fun loadDealDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val deal = dealRepository.getDealById(dealId)
                if (deal == null) {
                    _uiState.value = UiState.Error("Deal not found")
                    return@launch
                }

                val seller = sellerRepository.getSellerById(deal.sellerId)
                if (seller == null) {
                    _uiState.value = UiState.Error("Seller not found")
                    return@launch
                }

                val reviews = reviewRepository.getReviewsForDeal(dealId).firstOrNull() ?: emptyList()
                val location = locationRepository.locationFlow.firstOrNull()
                
                val distanceMeters = if (location != null) {
                    (LocationUtils.haversineDistance(
                        location.latitude, location.longitude,
                        seller.latitude, seller.longitude
                    ) * 1000).toInt()
                } else null

                _uiState.value = UiState.Success(
                    DealDetailsData(
                        deal = deal,
                        seller = seller,
                        reviews = reviews,
                        distanceMeters = distanceMeters
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load details")
            }
        }
    }
}
