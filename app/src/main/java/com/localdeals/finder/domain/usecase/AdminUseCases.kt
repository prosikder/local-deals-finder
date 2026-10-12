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
    private val reviewRepository: ReviewRepository,
    private val analyticsTracker: com.localdeals.finder.domain.analytics.AnalyticsTracker
) {
    operator fun invoke(): Flow<AdminStats> {
        return combine(
            authRepository.getUsers(),
            sellerRepository.getSellers(),
            dealRepository.getDeals(),
            analyticsTracker.getEventCount("order_placed"),
            analyticsTracker.getEventCount("deal_view")
        ) { users, sellers, deals, ordersCount, viewsCount ->
            AdminStats(
                totalUsers = users.size,
                totalSellers = sellers.size,
                totalDeals = deals.size,
                totalOrders = ordersCount,
                totalReviews = 0,
                totalViews = viewsCount
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
