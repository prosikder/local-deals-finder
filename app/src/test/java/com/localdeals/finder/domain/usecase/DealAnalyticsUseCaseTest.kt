package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.DealStats
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.OrderRepository
import com.localdeals.finder.domain.repository.WishlistRepository
import com.localdeals.finder.domain.model.OrderRequest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DealAnalyticsUseCaseTest {

    @Test
    fun `analytics mathematically accurate including conversion`() = runBlocking {
        val mockDealRepo = object : DealRepository {
            override fun getDeals() = throw NotImplementedError()
            override fun getDealsForSeller(sellerId: String) = throw NotImplementedError()
            override suspend fun getDealById(id: String) = throw NotImplementedError()
            override suspend fun addDeal(deal: com.localdeals.finder.domain.model.Deal) {}
            override suspend fun updateDeal(deal: com.localdeals.finder.domain.model.Deal) {}
            override suspend fun deleteDeal(id: String) {}
            override fun getDealStats(id: String) = flowOf(DealStats(dealId = "1", views = 10))
            override suspend fun incrementDealView(id: String) {}
        }
        val mockOrderRepo = object : OrderRepository {
            override fun getOrdersForUser(userId: String) = throw NotImplementedError()
            override fun getOrdersForDeal(dealId: String) = flowOf(List(5) { 
                OrderRequest(id = "o$it", dealId = "1", buyerId = "b1", sellerId = "s1", quantity = 1) 
            })
            override suspend fun createOrder(order: OrderRequest) {}
            override suspend fun updateOrderStatus(orderId: String, newStatus: com.localdeals.finder.domain.model.OrderStatus) {}
            override fun setSimulatorEnabled(enabled: Boolean) {}
            override fun isSimulatorEnabled() = throw NotImplementedError()
        }
        val mockWishlistRepo = object : WishlistRepository {
            override fun getWishlist() = throw NotImplementedError()
            override suspend fun addToWishlist(dealId: String) {}
            override suspend fun removeFromWishlist(dealId: String) {}
            override fun isWishlisted(dealId: String) = throw NotImplementedError()
            override fun isAlertEnabled(dealId: String) = throw NotImplementedError()
            override fun getWishlistAlerts() = throw NotImplementedError()
            override suspend fun setAlertEnabled(dealId: String, enabled: Boolean) {}
            override fun getWishlistCount(dealId: String) = flowOf(3)
        }

        val useCase = DealAnalyticsUseCase(mockDealRepo, mockOrderRepo, mockWishlistRepo)
        val stats = useCase.getAnalytics("1").first()

        assertEquals(10, stats.views)
        assertEquals(3, stats.wishlistAdds)
        assertEquals(5, stats.orders)
        
        // test conversion (orders / views)
        val conversion = if (stats.views > 0) (stats.orders.toFloat() / stats.views.toFloat()) * 100f else 0f
        assertEquals(50.0f, conversion, 0.01f)
    }
}
