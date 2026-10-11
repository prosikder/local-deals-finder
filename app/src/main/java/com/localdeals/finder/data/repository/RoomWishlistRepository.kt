package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.data.local.dao.DealStatsDao
import com.localdeals.finder.data.local.dao.WishlistDao
import com.localdeals.finder.data.local.entity.WishlistEntity
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.WishlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RoomWishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val dealStatsDao: DealStatsDao,
    private val dataStoreManager: DataStoreManager
) : WishlistRepository {

    private suspend fun getActiveUserId(): String {
        return dataStoreManager.loggedInUserId.firstOrNull() ?: "user_1"
    }

    override fun getWishlist(): Flow<List<Deal>> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            val activeId = userId ?: "user_1"
            wishlistDao.getWishlistedDealsForUser(activeId).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun addToWishlist(dealId: String) {
        val userId = getActiveUserId()
        wishlistDao.insertWishlist(
            WishlistEntity(
                userId = userId,
                dealId = dealId,
                isAlertEnabled = true
            )
        )
        dealStatsDao.incrementWishlistAdd(dealId)
    }

    override suspend fun removeFromWishlist(dealId: String) {
        val userId = getActiveUserId()
        wishlistDao.deleteWishlist(userId = userId, dealId = dealId)
        dealStatsDao.decrementWishlistAdd(dealId)
    }

    override fun isWishlisted(dealId: String): Flow<Boolean> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            wishlistDao.isWishlisted(userId ?: "user_1", dealId)
        }
    }

    override fun isAlertEnabled(dealId: String): Flow<Boolean> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            wishlistDao.isAlertEnabled(userId ?: "user_1", dealId)
        }
    }

    override fun getWishlistAlerts(): Flow<Map<String, Boolean>> {
        return dataStoreManager.loggedInUserId.flatMapLatest { userId ->
            wishlistDao.getWishlistAlerts(userId ?: "user_1").map { list ->
                list.associate { it.dealId to it.isAlertEnabled }
            }
        }
    }

    override suspend fun setAlertEnabled(dealId: String, enabled: Boolean) {
        val userId = getActiveUserId()
        wishlistDao.setAlertEnabled(userId = userId, dealId = dealId, enabled = enabled)
    }

    override fun getWishlistCount(dealId: String): Flow<Int> {
        return wishlistDao.getWishlistCountForDeal(dealId)
    }
}
