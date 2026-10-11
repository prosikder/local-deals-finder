package com.localdeals.finder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.localdeals.finder.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE dealId = :dealId ORDER BY timestampMillis DESC")
    fun getReviewsForDeal(dealId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE sellerId = :sellerId ORDER BY timestampMillis DESC")
    fun getReviewsForSeller(sellerId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE isReported = 1 ORDER BY timestampMillis DESC")
    fun getReportedReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: String)

    @Query("UPDATE reviews SET isReported = :isReported WHERE id = :reviewId")
    suspend fun updateReportStatus(reviewId: String, isReported: Boolean)

    @Query("UPDATE sellers SET rating = (SELECT COALESCE(AVG(rating), 0.0) FROM reviews WHERE sellerId = :sellerId) WHERE id = :sellerId")
    suspend fun updateSellerAverageRating(sellerId: String)

    @Transaction
    suspend fun submitReviewTransaction(review: ReviewEntity) {
        insertReview(review)
        updateSellerAverageRating(review.sellerId)
    }

    @Transaction
    suspend fun deleteReviewTransaction(reviewId: String, sellerId: String) {
        deleteReview(reviewId)
        updateSellerAverageRating(sellerId)
    }

    @Query("DELETE FROM reviews")
    suspend fun clearReviews()
}
