package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "wishlists",
    primaryKeys = ["userId", "dealId"],
    foreignKeys = [
        ForeignKey(
            entity = DealEntity::class,
            parentColumns = ["id"],
            childColumns = ["dealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("dealId")
    ]
)
data class WishlistEntity(
    val userId: String,
    val dealId: String,
    val isAlertEnabled: Boolean = true,
    val addedAtMillis: Long = System.currentTimeMillis()
)
