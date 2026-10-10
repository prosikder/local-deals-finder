package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.model.DealStats
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.DealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteDealUseCaseTest {

    @Test
    fun `delete deal works if user is seller and owns deal`() = runBlocking {
        var deletedId: String? = null
        val mockDealRepo = object : DealRepository {
            override fun getDeals() = throw NotImplementedError()
            override fun getDealsForSeller(sellerId: String) = throw NotImplementedError()
            override suspend fun getDealById(id: String) = Deal(
                id = "deal1", sellerId = "seller1", title = "T", description = "D",
                originalPrice = 1.0, discountedPrice = 1.0, discountPercentage = 0, stock = 1,
                categoryId = "c", expirationTimeMillis = 0
            )
            override suspend fun addDeal(deal: Deal) {}
            override suspend fun updateDeal(deal: Deal) {}
            override suspend fun deleteDeal(id: String) { deletedId = id }
            override fun getDealStats(id: String) = throw NotImplementedError()
            override suspend fun incrementDealView(id: String) {}
        }
        val mockAuthRepo = object : AuthRepository {
            override suspend fun getCurrentUser() = User("seller1", "Seller", "a@a", role = Role.SELLER)
            override suspend fun login(e: String, p: String) = throw NotImplementedError()
            override suspend fun register(n: String, e: String, p: String, r: Role) = throw NotImplementedError()
            override suspend fun logout() {}
            override suspend fun updateProfile(user: User): Result<User> = Result.success(user)
        }

        val useCase = DeleteDealUseCase(mockDealRepo, mockAuthRepo)
        val result = useCase.execute("deal1")

        assertTrue(result.isSuccess)
        assertTrue(deletedId == "deal1")
    }

    @Test
    fun `delete deal fails if user doesn't own deal`() = runBlocking {
        var deletedId: String? = null
        val mockDealRepo = object : DealRepository {
            override fun getDeals() = throw NotImplementedError()
            override fun getDealsForSeller(sellerId: String) = throw NotImplementedError()
            override suspend fun getDealById(id: String) = Deal(
                id = "deal1", sellerId = "seller2", title = "T", description = "D",
                originalPrice = 1.0, discountedPrice = 1.0, discountPercentage = 0, stock = 1,
                categoryId = "c", expirationTimeMillis = 0
            )
            override suspend fun addDeal(deal: Deal) {}
            override suspend fun updateDeal(deal: Deal) {}
            override suspend fun deleteDeal(id: String) { deletedId = id }
            override fun getDealStats(id: String) = throw NotImplementedError()
            override suspend fun incrementDealView(id: String) {}
        }
        val mockAuthRepo = object : AuthRepository {
            override suspend fun getCurrentUser() = User("seller1", "Seller", "a@a", role = Role.SELLER)
            override suspend fun login(e: String, p: String) = throw NotImplementedError()
            override suspend fun register(n: String, e: String, p: String, r: Role) = throw NotImplementedError()
            override suspend fun logout() {}
            override suspend fun updateProfile(user: User): Result<User> = Result.success(user)
        }

        val useCase = DeleteDealUseCase(mockDealRepo, mockAuthRepo)
        val result = useCase.execute("deal1")

        assertTrue(result.isFailure)
        assertTrue(deletedId == null)
    }
}
