package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.util.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNearbyDealsUseCase @Inject constructor(
    private val dealRepository: DealRepository,
    private val sellerRepository: SellerRepository
) {
    operator fun invoke(userLat: Double, userLon: Double): Flow<List<Deal>> {
        return combine(
            dealRepository.getDeals(),
            sellerRepository.getSellers()
        ) { deals, sellers ->
            val sellerMap = sellers.associateBy { it.id }
            
            deals.mapNotNull { deal ->
                val seller = sellerMap[deal.sellerId] ?: return@mapNotNull null
                val distance = LocationUtils.haversineDistance(
                    userLat, userLon,
                    seller.latitude, seller.longitude
                )
                val distanceMeters = (distance * 1000).toInt()
                deal.copy(distanceMeters = distanceMeters)
            }.sortedByDescending { deal ->
                // Score formula: Higher discount is better, lower distance is better.
                // Score = discountPercentage - (distanceMeters / 100)
                // For example, 50% discount and 1000m -> 50 - 10 = 40
                // 10% discount and 100m -> 10 - 1 = 9
                deal.discountPercentage - (deal.distanceMeters!! / 100.0)
            }
        }
    }
}
