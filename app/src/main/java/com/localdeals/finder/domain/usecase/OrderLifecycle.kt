package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.OrderStatus

object OrderLifecycle {
    fun isValidTransition(current: OrderStatus, next: OrderStatus): Boolean {
        return when (current) {
            OrderStatus.PENDING -> next == OrderStatus.ACCEPTED || next == OrderStatus.REJECTED || next == OrderStatus.CANCELLED
            OrderStatus.ACCEPTED -> next == OrderStatus.READY || next == OrderStatus.CANCELLED
            OrderStatus.READY -> next == OrderStatus.COMPLETED
            OrderStatus.COMPLETED, OrderStatus.REJECTED, OrderStatus.CANCELLED -> false
        }
    }

    fun canAcceptOrder(quantity: Int, currentStock: Int): Boolean {
        return quantity > 0 && quantity <= currentStock
    }
}
