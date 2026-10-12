package com.localdeals.finder.domain.analytics

import kotlinx.coroutines.flow.Flow

data class LocalAnalyticsEvent(
    val id: Long = 0,
    val name: String,
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

interface AnalyticsTracker {
    suspend fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun getEvents(): Flow<List<LocalAnalyticsEvent>>
    fun getEventsByName(name: String): Flow<List<LocalAnalyticsEvent>>
    fun getEventCount(name: String): Flow<Int>
    fun getEventCountForEntity(name: String, entityId: String): Flow<Int>
    suspend fun clearEvents()
}
