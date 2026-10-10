package com.localdeals.finder.domain.usecase

import org.junit.Assert.*
import org.junit.Test

class DealValidationUseCaseTest {

    private val useCase = DealValidationUseCase()

    @Test
    fun `valid deal returns success`() {
        val result = useCase.execute(
            title = "Valid Title",
            price = 10.0,
            discount = 20,
            stock = 5,
            expiryTimeMillis = System.currentTimeMillis() + 86400000 // 1 day future
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `blank title returns failure`() {
        val result = useCase.execute(
            title = "   ",
            price = 10.0,
            discount = 20,
            stock = 5,
            expiryTimeMillis = System.currentTimeMillis() + 86400000
        )
        assertTrue(result.isFailure)
        assertEquals("Title cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `zero or negative price returns failure`() {
        val result = useCase.execute(
            title = "Title",
            price = 0.0,
            discount = 20,
            stock = 5,
            expiryTimeMillis = System.currentTimeMillis() + 86400000
        )
        assertTrue(result.isFailure)
        assertEquals("Price must be greater than 0", result.exceptionOrNull()?.message)
    }

    @Test
    fun `discount out of bounds returns failure`() {
        val result = useCase.execute(
            title = "Title",
            price = 10.0,
            discount = 101,
            stock = 5,
            expiryTimeMillis = System.currentTimeMillis() + 86400000
        )
        assertTrue(result.isFailure)
        assertEquals("Discount must be between 0 and 100", result.exceptionOrNull()?.message)
    }

    @Test
    fun `negative stock returns failure`() {
        val result = useCase.execute(
            title = "Title",
            price = 10.0,
            discount = 20,
            stock = -1,
            expiryTimeMillis = System.currentTimeMillis() + 86400000
        )
        assertTrue(result.isFailure)
        assertEquals("Stock cannot be negative", result.exceptionOrNull()?.message)
    }

    @Test
    fun `past expiry returns failure`() {
        val result = useCase.execute(
            title = "Title",
            price = 10.0,
            discount = 20,
            stock = 5,
            expiryTimeMillis = System.currentTimeMillis() - 1000 // 1 sec in past
        )
        assertTrue(result.isFailure)
        assertEquals("Expiry time must be in the future", result.exceptionOrNull()?.message)
    }
}
