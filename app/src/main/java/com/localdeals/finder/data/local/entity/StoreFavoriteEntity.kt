package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "store_favorites",
    primaryKeys = ["userId", "sellerId"],
    foreignKeys = [
        ForeignKey(
            entity = SellerEntity::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sellerId")
    ]
)
data class StoreFavoriteEntity(
    val userId: String,
    val sellerId: String,
    val addedAtMillis: Long = System.currentTimeMillis()
)
