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

class SearchDealsUseCaseTest {
    private val fakeDeals = MutableStateFlow<List<Deal>>(emptyList())
    private val fakeSellers = MutableStateFlow<List<Seller>>(emptyList())

    private val dealRepository = object : DealRepository {
        override fun getDeals(): Flow<List<Deal>> = fakeDeals
        override suspend fun getDealById(id: String): Deal? = null
        override suspend fun addDeal(deal: Deal) {}
        override suspend fun updateDeal(deal: Deal) {}
    }

    private val sellerRepository = object : SellerRepository {
        override fun getSellers(): Flow<List<Seller>> = fakeSellers
        override suspend fun getSellerById(id: String): Seller? = null
        override suspend fun updateSeller(seller: Seller) {}
    }

    private val useCase = SearchDealsUseCase(dealRepository, sellerRepository)

    @Test
    fun `invoke with query filters correctly by title or seller`() = runBlocking {
        val s1 = Seller("s1", "u1", "Store A", "", 0.0, 0.0)
        val s2 = Seller("s2", "u2", "Store B", "", 0.0, 0.0)
        fakeSellers.value = listOf(s1, s2)

        val d1 = Deal("d1", "s1", "Apple", "", 10.0, 10.0, 0, 1, "c1", 0L)
        val d2 = Deal("d2", "s2", "Banana", "", 10.0, 10.0, 0, 1, "c1", 0L)
        fakeDeals.value = listOf(d1, d2)

        // Search by deal title
        var result = useCase(0.0, 0.0, SearchFilters(query = "app")).first()
        assertEquals(1, result.size)
        assertEquals("d1", result[0].id)

        // Search by store name
        result = useCase(0.0, 0.0, SearchFilters(query = "store b")).first()
        assertEquals(1, result.size)
        assertEquals("d2", result[0].id)
    }

    @Test
    fun `invoke with filters works correctly`() = runBlocking {
        val s1 = Seller("s1", "u1", "S1", "", 1.0, 0.0, rating = 4.0) // approx 111km away
        val s2 = Seller("s2", "u2", "S2", "", 0.1, 0.0, rating = 4.5) // approx 11km away
        fakeSellers.value = listOf(s1, s2)

        val d1 = Deal("d1", "s1", "D1", "", 100.0, 80.0, 20, 1, "c1", 0L)
        val d2 = Deal("d2", "s2", "D2", "", 100.0, 50.0, 50, 1, "c2", 0L)
        val d3 = Deal("d3", "s2", "D3", "", 50.0, 45.0, 10, 1, "c2", 0L)
        fakeDeals.value = listOf(d1, d2, d3)

        // Category filter
        val catRes = useCase(0.0, 0.0, SearchFilters(categoryId = "c1")).first()
        assertEquals(1, catRes.size)
        assertEquals("d1", catRes[0].id)

        // Price filter
        val priceRes = useCase(0.0, 0.0, SearchFilters(maxPrice = 60.0)).first()
        assertEquals(2, priceRes.size) // D2 and D3

        // Distance filter
        val distRes = useCase(0.0, 0.0, SearchFilters(maxDistanceMeters = 20000)).first()
        assertEquals(2, distRes.size) // D2 and D3 (11km)
    }

    @Test
    fun `invoke sorts correctly`() = runBlocking {
        val s1 = Seller("s1", "u1", "S1", "", 1.0, 0.0, rating = 4.0) // approx 111km
        val s2 = Seller("s2", "u2", "S2", "", 0.1, 0.0, rating = 4.8) // approx 11km
        fakeSellers.value = listOf(s1, s2)

        val d1 = Deal("d1", "s1", "D1", "", 100.0, 80.0, 20, 1, "c1", 0L)
        val d2 = Deal("d2", "s2", "D2", "", 100.0, 50.0, 50, 1, "c1", 0L)
        val d3 = Deal("d3", "s2", "D3", "", 50.0, 45.0, 10, 1, "c1", 0L)
        fakeDeals.value = listOf(d1, d2, d3)

        // Nearest (D2, D3 tie, then D1)
        val nearRes = useCase(0.0, 0.0, SearchFilters(sortType = SortType.NEAREST)).first()
        assertEquals("d1", nearRes.last().id) // D1 is furthest

        // Biggest discount
        val discRes = useCase(0.0, 0.0, SearchFilters(sortType = SortType.BIGGEST_DISCOUNT)).first()
        assertEquals("d2", discRes[0].id) // 50%
        assertEquals("d1", discRes[1].id) // 20%
        assertEquals("d3", discRes[2].id) // 10%

        // Cheapest
        val cheapRes = useCase(0.0, 0.0, SearchFilters(sortType = SortType.CHEAPEST)).first()
        assertEquals("d3", cheapRes[0].id) // 45.0

        // Top rated
        val rateRes = useCase(0.0, 0.0, SearchFilters(sortType = SortType.TOP_RATED)).first()
        assertEquals("d2", rateRes[0].id) // s2 has 4.8
        assertEquals("d1", rateRes[2].id) // s1 has 4.0
    }
}
