package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.DealStats
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.OrderRepository
import com.localdeals.finder.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DealAnalyticsUseCase @Inject constructor(
    private val dealRepository: DealRepository,
    private val orderRepository: OrderRepository,
    private val wishlistRepository: WishlistRepository
) {
    fun getAnalytics(dealId: String): Flow<DealStats> {
        return combine(
            dealRepository.getDealStats(dealId),
            orderRepository.getOrdersForDeal(dealId),
            wishlistRepository.getWishlistCount(dealId)
        ) { stats, orders, wishlistCount ->
            stats.copy(
                orders = orders.size,
                wishlistAdds = wishlistCount
            )
        }
    }

    suspend fun recordView(dealId: String) {
        dealRepository.incrementDealView(dealId)
    }
}
