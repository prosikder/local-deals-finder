package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.AdminStats
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.OrderRepository
import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.repository.SellerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAdminAnalyticsUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sellerRepository: SellerRepository,
    private val dealRepository: DealRepository,
    private val reviewRepository: ReviewRepository
) {
    operator fun invoke(): Flow<AdminStats> {
        return combine(
            authRepository.getUsers(),
            sellerRepository.getSellers(),
            dealRepository.getDeals()
        ) { users, sellers, deals ->
            // Note: in a real backend, we'd also fetch orders from an Order repo or similar, but
            // for mock, we just aggregate what we have. MockOrderRepository isn't exposed easily to get all orders globally unless we add it.
            // Let's just return a placeholder for totalOrders for now, since orders are usually per-user or per-deal.
            AdminStats(
                totalUsers = users.size,
                totalSellers = sellers.size,
                totalDeals = deals.size,
                totalOrders = deals.sumOf { 0.toInt() }, // Mock doesn't have getAllOrders()
                totalReviews = 0 // Will need to aggregate if we had getAllReviews
            )
        }
    }
}

class ManageUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun blockUser(userId: String) = authRepository.updateUserStatus(userId, true)
    suspend fun unblockUser(userId: String) = authRepository.updateUserStatus(userId, false)
}

class ManageSellersUseCase @Inject constructor(
    private val sellerRepository: SellerRepository
) {
    suspend fun suspendSeller(sellerId: String) = sellerRepository.updateSellerStatus(sellerId, "SUSPENDED")
    suspend fun approveSeller(sellerId: String) = sellerRepository.updateSellerStatus(sellerId, "ACTIVE")
}

class ModerateReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend fun deleteReview(reviewId: String) = reviewRepository.deleteReview(reviewId)
    suspend fun dismissReport(reviewId: String) = reviewRepository.dismissReport(reviewId)
}

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val dealRepository: DealRepository
) {
    suspend operator fun invoke(categoryId: String, fallbackCategoryId: String = "misc") {
        // Find deals that use this category and reassign them
        val deals = dealRepository.getDeals().first().filter { it.categoryId == categoryId }
        deals.forEach { deal ->
            dealRepository.updateDeal(deal.copy(categoryId = fallbackCategoryId))
        }
        categoryRepository.deleteCategory(categoryId)
    }
}
