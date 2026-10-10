package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.CategoryRepository
import com.localdeals.finder.domain.repository.DealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteCategoryUseCaseTest {
    @Test
    fun deleteCategory_updatesDealsWithFallbackCategory() = kotlinx.coroutines.runBlocking {
        val categoryIdToDelete = "cat_1"
        val fallbackId = "misc"
        val deal1 = Deal(id = "1", sellerId = "s1", title = "A", description = "", originalPrice = 10.0, discountedPrice = 5.0, discountPercentage = 50, stock = 10, categoryId = categoryIdToDelete, expirationTimeMillis = 0)
        val deal2 = Deal(id = "2", sellerId = "s1", title = "B", description = "", originalPrice = 10.0, discountedPrice = 5.0, discountPercentage = 50, stock = 10, categoryId = "cat_2", expirationTimeMillis = 0)
        
        val fakeDealRepo = object : DealRepository {
            val deals = MutableStateFlow(listOf(deal1, deal2))
            override fun getDeals() = deals.asStateFlow()
            override fun getDealsForSeller(sellerId: String) = throw NotImplementedError()
            override suspend fun getDealById(id: String) = throw NotImplementedError()
            override suspend fun addDeal(deal: Deal) = throw NotImplementedError()
            override suspend fun updateDeal(deal: Deal) {
                deals.value = deals.value.map { if (it.id == deal.id) deal else it }
            }
            override suspend fun deleteDeal(id: String) = throw NotImplementedError()
            override fun getDealStats(id: String) = throw NotImplementedError()
            override suspend fun incrementDealView(id: String) = throw NotImplementedError()
        }
        
        val fakeCategoryRepo = object : CategoryRepository {
            val categories = MutableStateFlow(listOf(Category("cat_1", "One", "ic"), Category("misc", "Misc", "ic")))
            override fun getCategories() = categories.asStateFlow()
            override suspend fun addCategory(category: Category) = throw NotImplementedError()
            override suspend fun updateCategory(category: Category) = throw NotImplementedError()
            override suspend fun deleteCategory(categoryId: String) {
                categories.value = categories.value.filter { it.id != categoryId }
            }
        }
        
        val useCase = DeleteCategoryUseCase(fakeCategoryRepo, fakeDealRepo)
        useCase(categoryIdToDelete, fallbackId)
        
        val updatedDeals = fakeDealRepo.getDeals().first()
        val remainingCategories = fakeCategoryRepo.getCategories().first()
        
        assertEquals(fallbackId, updatedDeals.find { it.id == "1" }?.categoryId)
        assertEquals("cat_2", updatedDeals.find { it.id == "2" }?.categoryId)
        assertEquals(1, remainingCategories.size)
        assertEquals(fallbackId, remainingCategories[0].id)
    }
}
