package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.localdeals.finder.data.local.entity.DealEntity
import com.localdeals.finder.data.local.entity.DealWithSeller
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {
    @Query("SELECT * FROM deals")
    fun getAllDeals(): Flow<List<DealEntity>>

    @Query("SELECT * FROM deals WHERE sellerId = :sellerId")
    fun getDealsForSeller(sellerId: String): Flow<List<DealEntity>>

    @Query("SELECT * FROM deals WHERE id = :id")
    fun getDealById(id: String): Flow<DealEntity?>

    @Query("SELECT * FROM deals WHERE id = :id")
    suspend fun getDealByIdSync(id: String): DealEntity?

    @Transaction
    @Query("SELECT * FROM deals")
    fun getAllDealsWithSeller(): Flow<List<DealWithSeller>>

    @Transaction
    @Query("""
        SELECT deals.* FROM deals
        INNER JOIN sellers ON deals.sellerId = sellers.id
        WHERE sellers.latitude BETWEEN :minLat AND :maxLat
          AND sellers.longitude BETWEEN :minLng AND :maxLng
    """)
    fun getDealsWithSellerInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): Flow<List<DealWithSeller>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: DealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeals(deals: List<DealEntity>)

    @Update
    suspend fun updateDeal(deal: DealEntity)

    @Query("DELETE FROM deals WHERE id = :id")
    suspend fun deleteDeal(id: String)

    @Query("UPDATE deals SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun updateCategoryForDeals(oldCategoryId: String, newCategoryId: String)

    @Query("UPDATE deals SET stock = MAX(0, stock - :quantity) WHERE id = :dealId")
    suspend fun decrementStock(dealId: String, quantity: Int)

    @Query("DELETE FROM deals")
    suspend fun clearDeals()
}
