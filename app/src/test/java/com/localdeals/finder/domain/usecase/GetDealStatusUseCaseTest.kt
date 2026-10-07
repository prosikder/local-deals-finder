package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDealStatusUseCaseTest {

    private val useCase = GetDealStatusUseCase()

    private fun createDeal(stock: Int, expiration: Long): Deal {
        return Deal(
            id = "d1",
            sellerId = "s1",
            title = "Test",
            description = "Desc",
            originalPrice = 100.0,
            discountedPrice = 50.0,
            discountPercentage = 50,
            stock = stock,
            categoryId = "c1",
            expirationTimeMillis = expiration
        )
    }

    @Test
    fun `deal with 0 stock returns OUT_OF_STOCK even if active`() {
        val deal = createDeal(0, System.currentTimeMillis() + 86400000 * 2)
        assertEquals(DealStatus.OUT_OF_STOCK, useCase(deal))
    }

    @Test
    fun `deal with past expiration returns EXPIRED`() {
        val deal = createDeal(10, System.currentTimeMillis() - 1000)
        assertEquals(DealStatus.EXPIRED, useCase(deal))
    }

    @Test
    fun `deal expiring within 24 hours returns ENDING_SOON`() {
        val deal = createDeal(10, System.currentTimeMillis() + 3600000) // 1 hour
        assertEquals(DealStatus.ENDING_SOON, useCase(deal))
    }

    @Test
    fun `deal with stock and time returns ACTIVE`() {
        val deal = createDeal(10, System.currentTimeMillis() + 86400000 * 2) // 2 days
        assertEquals(DealStatus.ACTIVE, useCase(deal))
    }
}
