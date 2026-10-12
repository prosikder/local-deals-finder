package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.localdeals.finder.data.local.entity.NotificationEntity
import com.localdeals.finder.data.local.entity.OrderRequestEntity
import com.localdeals.finder.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("""
        SELECT DISTINCT orders.* FROM order_requests orders
        LEFT JOIN sellers ON orders.sellerId = sellers.id OR orders.sellerId = sellers.userId
        WHERE orders.buyerId = :userId 
           OR orders.sellerId = :userId 
           OR sellers.userId = :userId
           OR sellers.id = :userId
           OR orders.sellerId = 's_' || REPLACE(:userId, 'user_s', '')
           OR orders.sellerId = 'user_s' || REPLACE(:userId, 's_', '')
        ORDER BY orders.timestampMillis DESC
    """)
    fun getOrdersForUser(userId: String): Flow<List<OrderRequestEntity>>

    @Query("SELECT * FROM order_requests ORDER BY timestampMillis DESC")
    fun getAllOrders(): Flow<List<OrderRequestEntity>>

    @Query("SELECT * FROM order_requests WHERE dealId = :dealId ORDER BY timestampMillis DESC")
    fun getOrdersForDeal(dealId: String): Flow<List<OrderRequestEntity>>

    @Query("SELECT * FROM order_requests WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderRequestEntity>)

    @Query("UPDATE order_requests SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)

    @Query("UPDATE deals SET stock = MAX(0, stock - :quantity) WHERE id = :dealId")
    suspend fun decrementDealStock(dealId: String, quantity: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Transaction
    suspend fun acceptOrderTransaction(
        orderId: String,
        dealId: String,
        quantity: Int,
        notification: NotificationEntity
    ) {
        updateOrderStatus(orderId, OrderStatus.ACCEPTED)
        decrementDealStock(dealId, quantity)
        insertNotification(notification)
    }

    @Query("DELETE FROM order_requests")
    suspend fun clearOrders()
}
