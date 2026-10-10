package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.model.Role
import javax.inject.Inject

class DeleteDealUseCase @Inject constructor(
    private val dealRepository: DealRepository,
    private val authRepository: AuthRepository
) {
    suspend fun execute(dealId: String): Result<Unit> {
        val user = authRepository.getCurrentUser()
        if (user == null || user.role != Role.SELLER) {
            return Result.failure(Exception("Unauthorized"))
        }
        val deal = dealRepository.getDealById(dealId) ?: return Result.failure(Exception("Deal not found"))
        if (deal.sellerId != user.id) {
            return Result.failure(Exception("Unauthorized to delete this deal"))
        }
        
        dealRepository.deleteDeal(dealId)
        return Result.success(Unit)
    }
}
