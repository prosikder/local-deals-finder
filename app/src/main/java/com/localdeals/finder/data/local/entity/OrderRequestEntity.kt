package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.localdeals.finder.domain.model.OrderStatus

@Entity(
    tableName = "order_requests",
    indices = [
        Index("dealId"),
        Index("buyerId"),
        Index("sellerId"),
        Index("status"),
        Index("timestampMillis")
    ]
)
data class OrderRequestEntity(
    @PrimaryKey
    val id: String,
    val dealId: String,
    val buyerId: String,
    val sellerId: String,
    val quantity: Int,
    val note: String = "",
    val phoneConfirmation: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val timestampMillis: Long = System.currentTimeMillis()
)
