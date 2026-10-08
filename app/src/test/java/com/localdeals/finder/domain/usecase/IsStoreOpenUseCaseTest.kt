package com.localdeals.finder.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class IsStoreOpenUseCaseTest {

    private val useCase = IsStoreOpenUseCase()

    @Test
    fun `when store is open during normal hours, returns true`() {
        val hours = mapOf("Monday" to "09:00-18:00")
        val current = LocalDateTime.of(2026, 10, 12, 12, 0) // Oct 12, 2026 is a Monday
        assertTrue(useCase(hours, current))
    }

    @Test
    fun `when store is closed before normal hours, returns false`() {
        val hours = mapOf("Monday" to "09:00-18:00")
        val current = LocalDateTime.of(2026, 10, 12, 8, 59)
        assertFalse(useCase(hours, current))
    }

    @Test
    fun `when store is closed after normal hours, returns false`() {
        val hours = mapOf("Monday" to "09:00-18:00")
        val current = LocalDateTime.of(2026, 10, 12, 18, 1)
        assertFalse(useCase(hours, current))
    }

    @Test
    fun `when store is open 24-7, returns true`() {
        val hours = mapOf("Tuesday" to "00:00-23:59")
        val current = LocalDateTime.of(2026, 10, 13, 3, 0) // Tuesday
        assertTrue(useCase(hours, current))
    }

    @Test
    fun `when store is open overnight, and it is before midnight today, returns true`() {
        val hours = mapOf("Friday" to "18:00-02:00")
        val current = LocalDateTime.of(2026, 10, 16, 23, 0) // Oct 16 is Friday
        assertTrue(useCase(hours, current))
    }

    @Test
    fun `when store is open overnight, and it is after midnight next day, returns true`() {
        val hours = mapOf("Friday" to "18:00-02:00")
        val current = LocalDateTime.of(2026, 10, 17, 1, 0) // Oct 17 is Saturday
        assertTrue(useCase(hours, current))
    }

    @Test
    fun `when store is open overnight, and it is after close time next day, returns false`() {
        val hours = mapOf("Friday" to "18:00-02:00")
        val current = LocalDateTime.of(2026, 10, 17, 2, 1) // Oct 17 is Saturday
        assertFalse(useCase(hours, current))
    }

    @Test
    fun `when store has no hours for today or yesterday, returns false`() {
        val hours = mapOf("Monday" to "09:00-18:00")
        val current = LocalDateTime.of(2026, 10, 14, 12, 0) // Wednesday
        assertFalse(useCase(hours, current))
    }
}
