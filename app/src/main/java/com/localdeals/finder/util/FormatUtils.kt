package com.localdeals.finder.util

import java.util.Locale

object FormatUtils {

    fun formatDistance(distanceMeters: Int?): String {
        if (distanceMeters == null) return ""
        return if (distanceMeters < 1000) {
            "${distanceMeters}m"
        } else {
            String.format(Locale.getDefault(), "%.1fkm", distanceMeters / 1000.0)
        }
    }

    fun formatCountdown(expirationTimeMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): String {
        val remaining = expirationTimeMillis - currentTimeMillis
        if (remaining <= 0) return "Expired"

        val days = remaining / (24 * 60 * 60 * 1000)
        val hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m left"
        }
    }

    fun isExpired(expirationTimeMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return expirationTimeMillis <= currentTimeMillis
    }
}
