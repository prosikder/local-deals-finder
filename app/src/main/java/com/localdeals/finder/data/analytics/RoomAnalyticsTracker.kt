package com.localdeals.finder.data.analytics

import com.localdeals.finder.data.local.dao.AnalyticsDao
import com.localdeals.finder.data.local.entity.AnalyticsEventEntity
import com.localdeals.finder.domain.analytics.AnalyticsTracker
import com.localdeals.finder.domain.analytics.LocalAnalyticsEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomAnalyticsTracker @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : AnalyticsTracker {

    override suspend fun logEvent(name: String, params: Map<String, String>) {
        val serializedParams = params.entries.joinToString(";") { "${it.key}=${it.value}" }
        val entity = AnalyticsEventEntity(
            name = name,
            params = serializedParams,
            timestamp = System.currentTimeMillis()
        )
        analyticsDao.insertEvent(entity)
    }

    override fun getEvents(): Flow<List<LocalAnalyticsEvent>> {
        return analyticsDao.getAllEvents().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEventsByName(name: String): Flow<List<LocalAnalyticsEvent>> {
        return analyticsDao.getEventsByName(name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEventCount(name: String): Flow<Int> {
        return analyticsDao.getEventCount(name)
    }

    override fun getEventCountForEntity(name: String, entityId: String): Flow<Int> {
        return analyticsDao.getEventCountWithParam(name, entityId)
    }

    override suspend fun clearEvents() {
        analyticsDao.clearAll()
    }

    private fun AnalyticsEventEntity.toDomain(): LocalAnalyticsEvent {
        val parsedParams = if (params.isBlank()) {
            emptyMap()
        } else {
            params.split(";").mapNotNull { entry ->
                val parts = entry.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
        }
        return LocalAnalyticsEvent(
            id = id,
            name = name,
            params = parsedParams,
            timestamp = timestamp
        )
    }
}
