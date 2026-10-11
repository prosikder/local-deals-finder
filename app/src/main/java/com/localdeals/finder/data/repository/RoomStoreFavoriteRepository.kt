package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.data.local.dao.StoreFavoriteDao
import com.localdeals.finder.data.local.entity.StoreFavoriteEntity
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.StoreFavoriteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RoomStoreFavoriteRepository @Inject constructor(
    private val storeFavoriteDao: StoreFavoriteDao,
    private val dataStoreManager: DataStoreManager
) : StoreFavoriteRepository {

    private suspend fun getActiveUserId(): String {
        return dataStoreManager.loggedInUserId.firstOrNull() ?: "user_1"
    }

    override fun getFavoriteStores(): Flow<List<Seller>> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            val activeId = userId ?: "user_1"
            storeFavoriteDao.getFavoriteStores(activeId).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun addFavorite(sellerId: String) {
        val userId = getActiveUserId()
        storeFavoriteDao.insertFavorite(
            StoreFavoriteEntity(
                userId = userId,
                sellerId = sellerId
            )
        )
    }

    override suspend fun removeFavorite(sellerId: String) {
        val userId = getActiveUserId()
        storeFavoriteDao.deleteFavorite(userId = userId, sellerId = sellerId)
    }

    override fun isFavorite(sellerId: String): Flow<Boolean> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            storeFavoriteDao.isFavorite(userId ?: "user_1", sellerId)
        }
    }
}
