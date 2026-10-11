package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_searches",
    indices = [
        Index("timestampMillis")
    ]
)
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
