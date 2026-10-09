package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.OrderStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderLifecycleTest {

    @Test
    fun testValidTransitions() {
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED))
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.PENDING, OrderStatus.REJECTED))
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.READY))
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.CANCELLED))
        assertTrue(OrderLifecycle.isValidTransition(OrderStatus.READY, OrderStatus.COMPLETED))
    }

    @Test
    fun testInvalidTransitions() {
        // From PENDING
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.PENDING, OrderStatus.READY))
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.PENDING, OrderStatus.COMPLETED))

        // From ACCEPTED
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.COMPLETED))
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.REJECTED))

        // From READY
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.READY, OrderStatus.CANCELLED))

        // From Terminal States
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.REJECTED, OrderStatus.ACCEPTED))
        assertFalse(OrderLifecycle.isValidTransition(OrderStatus.CANCELLED, OrderStatus.READY))
    }

    @Test
    fun testStockRules() {
        assertTrue(OrderLifecycle.canAcceptOrder(1, 5))
        assertTrue(OrderLifecycle.canAcceptOrder(5, 5))
        assertFalse(OrderLifecycle.canAcceptOrder(6, 5))
        assertFalse(OrderLifecycle.canAcceptOrder(0, 5))
        assertFalse(OrderLifecycle.canAcceptOrder(-1, 5))
    }
}
