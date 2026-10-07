package com.localdeals.finder.domain.repository

import com.localdeals.finder.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun getCurrentUser(): User?
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(name: String, email: String, pass: String, role: Role): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun logout()
}

interface DealRepository {
    fun getDeals(): Flow<List<Deal>>
    suspend fun getDealById(id: String): Deal?
    suspend fun addDeal(deal: Deal)
}

interface SellerRepository {
    fun getSellers(): Flow<List<Seller>>
    suspend fun getSellerById(id: String): Seller?
}

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
}

interface ReviewRepository {
    fun getReviewsForDeal(dealId: String): Flow<List<Review>>
    suspend fun addReview(review: Review)
}

interface ChatRepository {
    fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage)
}

interface OrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<OrderRequest>>
    suspend fun createOrder(order: OrderRequest)
}

interface LocationRepository {
    val locationFlow: Flow<CurrentLocation?>
    
    suspend fun getLastKnownLocation(): CurrentLocation?
    suspend fun getFreshLocation(timeoutMs: Long = 10000): CurrentLocation?
    suspend fun setManualLocation(location: CurrentLocation)
    fun isLocationEnabled(): Boolean
    fun startLocationUpdates()
    fun stopLocationUpdates()
}
