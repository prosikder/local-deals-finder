package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localdeals.finder.data.local.entity.SellerEntity
import com.localdeals.finder.data.local.entity.StoreFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreFavoriteDao {
    @Query("""
        SELECT sellers.* FROM sellers
        INNER JOIN store_favorites ON sellers.id = store_favorites.sellerId
        WHERE store_favorites.userId = :userId
    """)
    fun getFavoriteStores(userId: String): Flow<List<SellerEntity>>

    @Query("SELECT COUNT(*) > 0 FROM store_favorites WHERE userId = :userId AND sellerId = :sellerId")
    fun isFavorite(userId: String, sellerId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: StoreFavoriteEntity)

    @Query("DELETE FROM store_favorites WHERE userId = :userId AND sellerId = :sellerId")
    suspend fun deleteFavorite(userId: String, sellerId: String)

    @Query("DELETE FROM store_favorites")
    suspend fun clearFavorites()
}
