package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localdeals.finder.data.local.entity.DealEntity
import com.localdeals.finder.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlists WHERE userId = :userId")
    fun getWishlistForUser(userId: String): Flow<List<WishlistEntity>>

    @Query("""
        SELECT deals.* FROM deals
        INNER JOIN wishlists ON deals.id = wishlists.dealId
        WHERE wishlists.userId = :userId
    """)
    fun getWishlistedDealsForUser(userId: String): Flow<List<DealEntity>>

    @Query("SELECT COUNT(*) > 0 FROM wishlists WHERE userId = :userId AND dealId = :dealId")
    fun isWishlisted(userId: String, dealId: String): Flow<Boolean>

    @Query("SELECT COALESCE((SELECT isAlertEnabled FROM wishlists WHERE userId = :userId AND dealId = :dealId), 0)")
    fun isAlertEnabled(userId: String, dealId: String): Flow<Boolean>

    @Query("SELECT * FROM wishlists WHERE userId = :userId")
    fun getWishlistAlerts(userId: String): Flow<List<WishlistEntity>>

    @Query("SELECT COUNT(*) FROM wishlists WHERE dealId = :dealId")
    fun getWishlistCountForDeal(dealId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistEntity)

    @Query("DELETE FROM wishlists WHERE userId = :userId AND dealId = :dealId")
    suspend fun deleteWishlist(userId: String, dealId: String)

    @Query("UPDATE wishlists SET isAlertEnabled = :enabled WHERE userId = :userId AND dealId = :dealId")
    suspend fun setAlertEnabled(userId: String, dealId: String, enabled: Boolean)

    @Query("DELETE FROM wishlists")
    suspend fun clearWishlist()
}
