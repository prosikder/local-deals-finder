package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.DealDao
import com.localdeals.finder.data.local.dao.DealStatsDao
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.DealStats
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.util.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos

@Singleton
class RoomDealRepository @Inject constructor(
    private val dealDao: DealDao,
    private val dealStatsDao: DealStatsDao
) : DealRepository {

    override fun getDeals(): Flow<List<Deal>> {
        return dealDao.getAllDeals().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getDealsForSeller(sellerId: String): Flow<List<Deal>> {
        return dealDao.getDealsForSeller(sellerId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getDealById(id: String): Deal? {
        return dealDao.getDealByIdSync(id)?.toDomain()
    }

    override suspend fun addDeal(deal: Deal) {
        dealDao.insertDeal(deal.toEntity())
    }

    override suspend fun updateDeal(deal: Deal) {
        dealDao.updateDeal(deal.toEntity())
    }

    override suspend fun deleteDeal(id: String) {
        dealDao.deleteDeal(id)
    }

    override fun getDealStats(id: String): Flow<DealStats> {
        return dealStatsDao.getStatsForDeal(id).map { entity ->
            entity?.toDomain() ?: DealStats(dealId = id)
        }
    }

    override suspend fun incrementDealView(id: String) {
        dealStatsDao.incrementView(id)
    }

    /**
     * Bounding-box prefilter in SQLite query + exact Haversine distance sort in Kotlin.
     */
    fun getDealsSortedByDistance(
        userLat: Double,
        userLng: Double,
        radiusKm: Double = 50.0
    ): Flow<List<Deal>> {
        val deltaLat = radiusKm / 111.32
        val latRad = Math.toRadians(userLat)
        val deltaLng = radiusKm / (111.32 * cos(latRad).coerceAtLeast(0.01))

        val minLat = userLat - deltaLat
        val maxLat = userLat + deltaLat
        val minLng = userLng - deltaLng
        val maxLng = userLng + deltaLng

        return dealDao.getDealsWithSellerInBoundingBox(minLat, maxLat, minLng, maxLng).map { list ->
            list.mapNotNull { item ->
                val seller = item.seller ?: return@mapNotNull null
                val exactDistance = LocationUtils.haversineDistanceMeters(
                    userLat,
                    userLng,
                    seller.latitude,
                    seller.longitude
                ).toInt()

                // Filter items strictly within the circular radius
                if (exactDistance <= radiusKm * 1000) {
                    item.deal.toDomain(distanceMeters = exactDistance)
                } else {
                    null
                }
            }.sortedBy { it.distanceMeters ?: Int.MAX_VALUE }
        }
    }
}
