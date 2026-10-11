package com.localdeals.finder.data.local.converter

import androidx.room.TypeConverter
import com.localdeals.finder.domain.model.OrderStatus
import com.localdeals.finder.domain.model.Role

class Converters {
    @TypeConverter
    fun fromRole(role: Role): String = role.name

    @TypeConverter
    fun toRole(value: String): Role {
        return try {
            Role.valueOf(value)
        } catch (e: Exception) {
            Role.USER
        }
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus {
        return try {
            OrderStatus.valueOf(value)
        } catch (e: Exception) {
            OrderStatus.PENDING
        }
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String {
        if (map.isNullOrEmpty()) return ""
        return map.entries.joinToString(separator = ";;") { "${it.key}::${it.value}" }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        return value.split(";;").mapNotNull { entry ->
            val parts = entry.split("::", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}
