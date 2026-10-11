package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deals",
    foreignKeys = [
        ForeignKey(
            entity = SellerEntity::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sellerId"),
        Index("categoryId"),
        Index("expirationTimeMillis"),
        Index("stock"),
        Index("discountPercentage"),
        Index("discountedPrice")
    ]
)
data class DealEntity(
    @PrimaryKey
    val id: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercentage: Int,
    val stock: Int,
    val categoryId: String,
    val expirationTimeMillis: Long,
    val imageUrl: String = ""
)
