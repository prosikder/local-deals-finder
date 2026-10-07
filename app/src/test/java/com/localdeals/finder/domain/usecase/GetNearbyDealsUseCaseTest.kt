package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.SellerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetNearbyDealsUseCaseTest {

    private val fakeDeals = MutableStateFlow<List<Deal>>(emptyList())
    private val fakeSellers = MutableStateFlow<List<Seller>>(emptyList())

    private val dealRepository = object : DealRepository {
        override fun getDeals(): Flow<List<Deal>> = fakeDeals
        override suspend fun getDealById(id: String): Deal? = null
        override suspend fun addDeal(deal: Deal) {}
    }

    private val sellerRepository = object : SellerRepository {
        override fun getSellers(): Flow<List<Seller>> = fakeSellers
        override suspend fun getSellerById(id: String): Seller? = null
    }

    private val useCase = GetNearbyDealsUseCase(dealRepository, sellerRepository)

    @Test
    fun `invoke returns deals sorted by score of discount and distance`() = runBlocking {
        // User is at 0, 0
        val userLat = 0.0
        val userLon = 0.0

        // Seller 1 is far (approx 111km away at 1.0, 0.0)
        val seller1 = Seller("s1", "u1", "Seller 1", "Addr", 1.0, 0.0)
        // Seller 2 is very close (approx 11km away at 0.1, 0.0)
        val seller2 = Seller("s2", "u2", "Seller 2", "Addr", 0.1, 0.0)

        fakeSellers.value = listOf(seller1, seller2)

        // Deal 1: 50% discount, far away (score approx 50 - 111000/100 = -1060)
        val deal1 = Deal("d1", "s1", "Deal 1", "", 100.0, 50.0, 50, 10, "c1", 0L)
        // Deal 2: 10% discount, very close (score approx 10 - 11000/100 = -100)
        val deal2 = Deal("d2", "s2", "Deal 2", "", 100.0, 90.0, 10, 10, "c1", 0L)
        // Deal 3: 60% discount, very close (score approx 60 - 11000/100 = -50)
        val deal3 = Deal("d3", "s2", "Deal 3", "", 100.0, 40.0, 60, 10, "c1", 0L)

        fakeDeals.value = listOf(deal1, deal2, deal3)

        val result = useCase(userLat, userLon).first()

        assertEquals(3, result.size)
        // Expected order: Deal 3 (highest score), Deal 2, Deal 1 (lowest score)
        assertEquals("d3", result[0].id)
        assertEquals("d2", result[1].id)
        assertEquals("d1", result[2].id)
    }
}
