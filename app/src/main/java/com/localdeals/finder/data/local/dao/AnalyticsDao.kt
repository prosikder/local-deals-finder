package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localdeals.finder.data.local.entity.AnalyticsEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT * FROM analytics_events WHERE name = :name ORDER BY timestamp DESC")
    fun getEventsByName(name: String): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT COUNT(*) FROM analytics_events WHERE name = :name")
    fun getEventCount(name: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM analytics_events WHERE name = :name AND params LIKE '%' || :paramSubstring || '%'")
    fun getEventCountWithParam(name: String, paramSubstring: String): Flow<Int>

    @Query("DELETE FROM analytics_events")
    suspend fun clearAll()
}
