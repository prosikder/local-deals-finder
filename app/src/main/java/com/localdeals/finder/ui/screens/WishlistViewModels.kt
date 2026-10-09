package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortType {
    NEAREST, BIGGEST_DISCOUNT, CHEAPEST
}

data class WishlistUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val wishlist: List<Deal> = emptyList(),
    val alertsEnabled: Map<String, Boolean> = emptyMap(),
    val sortType: SortType = SortType.NEAREST
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WishlistUiState())
    val state: StateFlow<WishlistUiState> = _state.asStateFlow()

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            kotlinx.coroutines.flow.combine(
                wishlistRepository.getWishlist(),
                wishlistRepository.getWishlistAlerts()
            ) { deals, alertsMap ->
                val sortedDeals = sortDeals(deals, _state.value.sortType)
                _state.value = _state.value.copy(
                    wishlist = sortedDeals, 
                    alertsEnabled = alertsMap, 
                    isLoading = false
                )
            }.collectLatest {}
        }
    }

    fun removeDeal(dealId: String) {
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(dealId)
        }
    }

    fun toggleAlert(dealId: String, enabled: Boolean) {
        viewModelScope.launch {
            wishlistRepository.setAlertEnabled(dealId, enabled)
        }
    }

    fun setSortType(sortType: SortType) {
        _state.value = _state.value.copy(sortType = sortType)
        val sortedDeals = sortDeals(_state.value.wishlist, sortType)
        _state.value = _state.value.copy(wishlist = sortedDeals)
    }

    private fun sortDeals(deals: List<Deal>, sortType: SortType): List<Deal> {
        return when (sortType) {
            SortType.NEAREST -> deals 
            SortType.BIGGEST_DISCOUNT -> deals.sortedByDescending { it.discountPercentage }
            SortType.CHEAPEST -> deals.sortedBy { it.discountedPrice }
        }
    }
}
