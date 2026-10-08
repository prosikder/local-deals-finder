package com.localdeals.finder.domain.usecase

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class IsStoreOpenUseCase @Inject constructor() {
    
    /**
     * Checks if a store is open right now based on its opening hours.
     * openingHours map format: {"Monday": "09:00-18:00", ...}
     */
    operator fun invoke(openingHours: Map<String, String>, currentDateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        if (openingHours.isEmpty()) return false
        
        val todayStr = currentDateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val yesterdayStr = currentDateTime.minusDays(1).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        
        val currentTime = currentDateTime.toLocalTime()
        
        // Check today's window
        if (openingHours.containsKey(todayStr)) {
            val window = openingHours[todayStr]!!
            if (isTimeInTodayWindow(currentTime, window)) return true
        }
        
        // Check yesterday's overnight window
        if (openingHours.containsKey(yesterdayStr)) {
            val window = openingHours[yesterdayStr]!!
            if (isTimeInYesterdayOvernightWindow(currentTime, window)) return true
        }
        
        return false
    }

    private fun isTimeInTodayWindow(currentTime: LocalTime, timeWindow: String): Boolean {
        return try {
            val (openTime, closeTime) = parseTimes(timeWindow) ?: return false
            if (openTime == LocalTime.of(0, 0) && closeTime == LocalTime.of(23, 59)) return true
            
            if (closeTime.isBefore(openTime)) {
                // Overnight window (e.g. 18:00-02:00). Today covers from openTime to midnight.
                !currentTime.isBefore(openTime)
            } else {
                // Normal window
                !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isTimeInYesterdayOvernightWindow(currentTime: LocalTime, timeWindow: String): Boolean {
        return try {
            val (openTime, closeTime) = parseTimes(timeWindow) ?: return false
            if (closeTime.isBefore(openTime)) {
                // Overnight window (e.g. 18:00-02:00). Yesterday covered up to midnight, today covers midnight to closeTime.
                !currentTime.isAfter(closeTime)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun parseTimes(timeWindow: String): Pair<LocalTime, LocalTime>? {
        val parts = timeWindow.split("-")
        if (parts.size != 2) return null
        val openTime = LocalTime.parse(parts[0].trim(), DateTimeFormatter.ofPattern("HH:mm"))
        val closeTime = LocalTime.parse(parts[1].trim(), DateTimeFormatter.ofPattern("HH:mm"))
        return openTime to closeTime
    }
}
