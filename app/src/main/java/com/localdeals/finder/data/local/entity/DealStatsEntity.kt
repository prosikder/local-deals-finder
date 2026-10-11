package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "deal_stats",
    foreignKeys = [
        ForeignKey(
            entity = DealEntity::class,
            parentColumns = ["id"],
            childColumns = ["dealId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DealStatsEntity(
    @PrimaryKey
    val dealId: String,
    val views: Int = 0,
    val wishlistAdds: Int = 0,
    val orders: Int = 0
)
