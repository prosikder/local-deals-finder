package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.SellerRepository
import com.localdeals.finder.util.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class SortType {
    NEAREST, BIGGEST_DISCOUNT, CHEAPEST, TOP_RATED
}

data class SearchFilters(
    val query: String = "",
    val categoryId: String? = null,
    val maxDistanceMeters: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
    val minDiscountPercentage: Int? = null,
    val sortType: SortType = SortType.NEAREST
)

class SearchDealsUseCase @Inject constructor(
    private val dealRepository: DealRepository,
    private val sellerRepository: SellerRepository
) {
    operator fun invoke(userLat: Double, userLon: Double, filters: SearchFilters): Flow<List<Deal>> {
        return combine(
            dealRepository.getDeals(),
            sellerRepository.getSellers()
        ) { deals, sellers ->
            val sellerMap = sellers.associateBy { it.id }
            val q = filters.query.trim().lowercase()

            deals.mapNotNull { deal ->
                val seller = sellerMap[deal.sellerId] ?: return@mapNotNull null
                
                // Match search query against deal title or seller name
                if (q.isNotEmpty()) {
                    if (!deal.title.lowercase().contains(q) && !seller.businessName.lowercase().contains(q)) {
                        return@mapNotNull null
                    }
                }

                // Match category
                if (filters.categoryId != null && deal.categoryId != filters.categoryId) {
                    return@mapNotNull null
                }

                // Match price
                if (filters.minPrice != null && deal.discountedPrice < filters.minPrice) return@mapNotNull null
                if (filters.maxPrice != null && deal.discountedPrice > filters.maxPrice) return@mapNotNull null

                // Match discount
                if (filters.minDiscountPercentage != null && deal.discountPercentage < filters.minDiscountPercentage) return@mapNotNull null

                // Match rating
                if (filters.minRating != null && seller.rating < filters.minRating) return@mapNotNull null

                // Distance
                val distance = LocationUtils.haversineDistance(
                    userLat, userLon,
                    seller.latitude, seller.longitude
                )
                val distanceMeters = (distance * 1000).toInt()
                
                if (filters.maxDistanceMeters != null && distanceMeters > filters.maxDistanceMeters) return@mapNotNull null

                deal.copy(distanceMeters = distanceMeters)
            }.sortedWith { d1, d2 ->
                when (filters.sortType) {
                    SortType.NEAREST -> {
                        (d1.distanceMeters ?: Int.MAX_VALUE).compareTo(d2.distanceMeters ?: Int.MAX_VALUE)
                    }
                    SortType.BIGGEST_DISCOUNT -> {
                        d2.discountPercentage.compareTo(d1.discountPercentage)
                    }
                    SortType.CHEAPEST -> {
                        d1.discountedPrice.compareTo(d2.discountedPrice)
                    }
                    SortType.TOP_RATED -> {
                        val s1 = sellerMap[d1.sellerId]?.rating ?: 0.0
                        val s2 = sellerMap[d2.sellerId]?.rating ?: 0.0
                        s2.compareTo(s1)
                    }
                }
            }
        }
    }
}
