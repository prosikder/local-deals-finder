package com.localdeals.finder.domain.usecase

import java.util.Calendar

class DealValidationUseCase {
    fun execute(
        title: String,
        price: Double,
        discount: Int,
        stock: Int,
        expiryTimeMillis: Long
    ): Result<Unit> {
        if (title.isBlank()) {
            return Result.failure(Exception("Title cannot be blank"))
        }
        if (price <= 0.0) {
            return Result.failure(Exception("Price must be greater than 0"))
        }
        if (discount < 0 || discount > 100) {
            return Result.failure(Exception("Discount must be between 0 and 100"))
        }
        if (stock < 0) {
            return Result.failure(Exception("Stock cannot be negative"))
        }
        if (expiryTimeMillis <= System.currentTimeMillis()) {
            return Result.failure(Exception("Expiry time must be in the future"))
        }
        return Result.success(Unit)
    }
}
