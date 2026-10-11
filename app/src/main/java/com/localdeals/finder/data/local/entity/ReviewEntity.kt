package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = DealEntity::class,
            parentColumns = ["id"],
            childColumns = ["dealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SellerEntity::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("dealId"),
        Index("sellerId"),
        Index("userId"),
        Index("isReported")
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val dealId: String,
    val sellerId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestampMillis: Long,
    val isReported: Boolean = false
)
