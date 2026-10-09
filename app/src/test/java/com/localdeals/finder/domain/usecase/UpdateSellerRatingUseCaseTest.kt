package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.ReviewRepository
import com.localdeals.finder.domain.repository.SellerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSellerRatingUseCaseTest {

    private var updatedSeller: Seller? = null

    private val fakeSellerRepo = object : SellerRepository {
        override fun getSellers(): Flow<List<Seller>> = flowOf(emptyList())
        override suspend fun getSellerById(id: String): Seller? {
            if (id == "s1") return Seller("s1", "u1", "Store 1", "Addr", 0.0, 0.0, rating = 0.0)
            return null
        }
        override suspend fun updateSeller(seller: Seller) {
            updatedSeller = seller
        }
    }

    private val fakeReviewRepo = object : ReviewRepository {
        override fun getReviewsForDeal(dealId: String): Flow<List<Review>> = flowOf(emptyList())
        override fun getReviewsForSeller(sellerId: String): Flow<List<Review>> {
            if (sellerId == "s1") {
                return flowOf(
                    listOf(
                        Review("r1", "d1", "s1", "u1", "User 1", 5, "", 0L),
                        Review("r2", "d2", "s1", "u2", "User 2", 3, "", 0L),
                        Review("r3", "d3", "s1", "u3", "User 3", 4, "", 0L)
                    )
                )
            }
            return flowOf(emptyList())
        }
        override suspend fun addReview(review: Review) {}
    }

    @Test
    fun `invoke updates seller rating with average of reviews`() = runBlocking {
        val useCase = UpdateSellerRatingUseCase(fakeReviewRepo, fakeSellerRepo)
        useCase("s1")
        
        // (5 + 3 + 4) / 3 = 4.0
        assertEquals(4.0, updatedSeller?.rating)
    }
}
