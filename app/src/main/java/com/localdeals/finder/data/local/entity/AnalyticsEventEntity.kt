package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analytics_events",
    indices = [
        Index(value = ["name"]),
        Index(value = ["timestamp"])
    ]
)
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val params: String = "", // Format: "key1=val1;key2=val2"
    val timestamp: Long = System.currentTimeMillis()
)
