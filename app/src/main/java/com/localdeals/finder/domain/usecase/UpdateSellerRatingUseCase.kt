package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.repository.SellerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateSellerRatingUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val sellerRepository: SellerRepository
) {
    suspend operator fun invoke(sellerId: String) {
        val seller = sellerRepository.getSellerById(sellerId) ?: return
        val reviews = reviewRepository.getReviewsForSeller(sellerId).first()
        
        if (reviews.isEmpty()) return
        
        val averageRating = reviews.map { it.rating }.average()
        val updatedSeller = seller.copy(rating = averageRating)
        
        sellerRepository.updateSeller(updatedSeller)
    }
}
