package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.ReviewDao
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.Review
import com.localdeals.finder.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao
) : ReviewRepository {

    override fun getReviewsForDeal(dealId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForDeal(dealId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getReviewsForSeller(sellerId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForSeller(sellerId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addReview(review: Review) {
        reviewDao.submitReviewTransaction(review.toEntity())
    }

    override fun getReportedReviews(): Flow<List<Review>> {
        return reviewDao.getReportedReviews().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun deleteReview(reviewId: String) {
        val review = reviewDao.getReviewById(reviewId)
        if (review != null) {
            reviewDao.deleteReviewTransaction(reviewId, review.sellerId)
        } else {
            reviewDao.deleteReview(reviewId)
        }
    }

    override suspend fun dismissReport(reviewId: String) {
        reviewDao.updateReportStatus(reviewId, isReported = false)
    }

    override suspend fun reportReview(reviewId: String) {
        reviewDao.updateReportStatus(reviewId, isReported = true)
    }
}
