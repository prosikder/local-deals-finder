package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sellers",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["userId"])
    ]
)
data class SellerEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val businessName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val openingHours: Map<String, String> = emptyMap(),
    val status: String = "ACTIVE"
)
