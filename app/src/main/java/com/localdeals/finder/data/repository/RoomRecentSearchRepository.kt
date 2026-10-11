package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.RecentSearchDao
import com.localdeals.finder.data.local.entity.RecentSearchEntity
import com.localdeals.finder.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRecentSearchRepository @Inject constructor(
    private val recentSearchDao: RecentSearchDao
) : RecentSearchRepository {

    override fun getRecentSearches(): Flow<List<String>> {
        return recentSearchDao.getRecentSearches().map { list ->
            list.map { it.query }
        }
    }

    override suspend fun addRecentSearch(query: String) {
        if (query.isNotBlank()) {
            recentSearchDao.insertRecentSearch(
                RecentSearchEntity(
                    query = query.trim(),
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearRecentSearches()
    }
}
