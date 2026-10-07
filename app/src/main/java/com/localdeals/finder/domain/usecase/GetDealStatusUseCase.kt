package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import javax.inject.Inject

enum class DealStatus {
    ACTIVE,
    ENDING_SOON,
    OUT_OF_STOCK,
    EXPIRED
}

class GetDealStatusUseCase @Inject constructor() {
    operator fun invoke(deal: Deal, currentTimeMillis: Long = System.currentTimeMillis()): DealStatus {
        val remaining = deal.expirationTimeMillis - currentTimeMillis

        return when {
            deal.stock <= 0 -> DealStatus.OUT_OF_STOCK
            remaining <= 0 -> DealStatus.EXPIRED
            remaining <= 24 * 60 * 60 * 1000L -> DealStatus.ENDING_SOON // within 24 hours
            else -> DealStatus.ACTIVE
        }
    }
}
