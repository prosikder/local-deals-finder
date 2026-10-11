package com.localdeals.finder.domain.usecase

import com.localdeals.finder.data.local.DatabaseSeeder
import javax.inject.Inject

class ResetDemoDataUseCase @Inject constructor(
    private val databaseSeeder: DatabaseSeeder
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            databaseSeeder.resetDemoData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
