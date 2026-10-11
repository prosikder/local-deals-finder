package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.localdeals.finder.data.local.entity.DealStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DealStatsDao {
    @Query("SELECT * FROM deal_stats WHERE dealId = :dealId")
    fun getStatsForDeal(dealId: String): Flow<DealStatsEntity?>

    @Query("SELECT * FROM deal_stats WHERE dealId = :dealId")
    suspend fun getStatsForDealSync(dealId: String): DealStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: DealStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(statsList: List<DealStatsEntity>)

    @Transaction
    suspend fun incrementView(dealId: String) {
        val current = getStatsForDealSync(dealId)
        if (current == null) {
            insertOrUpdate(DealStatsEntity(dealId = dealId, views = 1))
        } else {
            insertOrUpdate(current.copy(views = current.views + 1))
        }
    }

    @Transaction
    suspend fun incrementWishlistAdd(dealId: String) {
        val current = getStatsForDealSync(dealId)
        if (current == null) {
            insertOrUpdate(DealStatsEntity(dealId = dealId, wishlistAdds = 1))
        } else {
            insertOrUpdate(current.copy(wishlistAdds = current.wishlistAdds + 1))
        }
    }

    @Transaction
    suspend fun decrementWishlistAdd(dealId: String) {
        val current = getStatsForDealSync(dealId)
        if (current != null && current.wishlistAdds > 0) {
            insertOrUpdate(current.copy(wishlistAdds = current.wishlistAdds - 1))
        }
    }

    @Transaction
    suspend fun incrementOrder(dealId: String) {
        val current = getStatsForDealSync(dealId)
        if (current == null) {
            insertOrUpdate(DealStatsEntity(dealId = dealId, orders = 1))
        } else {
            insertOrUpdate(current.copy(orders = current.orders + 1))
        }
    }

    @Query("DELETE FROM deal_stats")
    suspend fun clearStats()
}
