package com.localdeals.finder.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DealWithSeller(
    @Embedded
    val deal: DealEntity,
    @Relation(
        parentColumn = "sellerId",
        entityColumn = "id"
    )
    val seller: SellerEntity?
)
