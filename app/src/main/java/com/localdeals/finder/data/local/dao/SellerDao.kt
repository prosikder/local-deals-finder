package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.localdeals.finder.data.local.entity.SellerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SellerDao {
    @Query("SELECT * FROM sellers")
    fun getAllSellers(): Flow<List<SellerEntity>>

    @Query("SELECT * FROM sellers WHERE id = :id")
    fun getSellerById(id: String): Flow<SellerEntity?>

    @Query("SELECT * FROM sellers WHERE id = :id")
    suspend fun getSellerByIdSync(id: String): SellerEntity?

    @Query("SELECT * FROM sellers WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    fun getSellersInBoundingBox(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<SellerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellers(sellers: List<SellerEntity>)

    @Update
    suspend fun updateSeller(seller: SellerEntity)

    @Query("UPDATE sellers SET status = :status WHERE id = :sellerId")
    suspend fun updateSellerStatus(sellerId: String, status: String)

    @Query("UPDATE sellers SET rating = :rating WHERE id = :sellerId")
    suspend fun updateSellerRating(sellerId: String, rating: Double)

    @Query("DELETE FROM sellers")
    suspend fun clearSellers()
}
