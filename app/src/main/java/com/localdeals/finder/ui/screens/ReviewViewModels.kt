package com.localdeals.finder.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.usecase.UpdateSellerRatingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dealId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitted: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    private val dealRepository: DealRepository,
    private val authRepository: AuthRepository,
    private val updateSellerRatingUseCase: UpdateSellerRatingUseCase
) : ViewModel() {

    private val dealId: String = savedStateHandle.get<String>("dealId") ?: ""

    private val _state = MutableStateFlow(ReviewUiState(dealId = dealId))
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    fun updateRating(rating: Int) {
        _state.value = _state.value.copy(rating = rating)
    }

    fun updateComment(comment: String) {
        _state.value = _state.value.copy(comment = comment)
    }

    fun submitReview() {
        val currentState = state.value
        if (currentState.rating == 0) {
            _state.value = _state.value.copy(error = "Please select a rating")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _state.value = _state.value.copy(error = "User not logged in", isLoading = false)
                return@launch
            }
            
            val deal = dealRepository.getDealById(dealId)
            if (deal == null) {
                _state.value = _state.value.copy(error = "Deal not found", isLoading = false)
                return@launch
            }

            val review = Review(
                id = UUID.randomUUID().toString(),
                dealId = dealId,
                sellerId = deal.sellerId,
                userId = user.id,
                userName = user.name,
                rating = currentState.rating,
                comment = currentState.comment,
                timestampMillis = System.currentTimeMillis()
            )

            reviewRepository.addReview(review)
            updateSellerRatingUseCase(deal.sellerId)

            _state.value = _state.value.copy(isLoading = false, isSubmitted = true)
        }
    }
}
