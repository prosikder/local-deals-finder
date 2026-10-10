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
    fun getDealsForSeller(sellerId: String): Flow<List<Deal>>
    suspend fun getDealById(id: String): Deal?
    suspend fun addDeal(deal: Deal)
    suspend fun updateDeal(deal: Deal)
}

interface SellerRepository {
    fun getSellers(): Flow<List<Seller>>
    suspend fun getSellerById(id: String): Seller?
    suspend fun updateSeller(seller: Seller)
}

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
}

interface ReviewRepository {
    fun getReviewsForDeal(dealId: String): Flow<List<Review>>
    fun getReviewsForSeller(sellerId: String): Flow<List<Review>>
    suspend fun addReview(review: Review)
}

interface ChatRepository {
    fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessage>>
    fun getChatsForUser(userId: String): Flow<List<ChatMessage>>
    fun getUnreadCount(userId: String): Flow<Int>
    fun isTyping(userId1: String, userId2: String): Flow<Boolean>
    suspend fun sendMessage(message: ChatMessage)
    suspend fun markAsRead(userId1: String, userId2: String)
}

interface OrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<OrderRequest>>
    suspend fun createOrder(order: OrderRequest)
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus)
    fun setSimulatorEnabled(enabled: Boolean)
    fun isSimulatorEnabled(): Flow<Boolean>
}

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun addNotification(notification: AppNotification)
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun deleteNotification(notificationId: String)
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

interface WishlistRepository {
    fun getWishlist(): Flow<List<Deal>>
    suspend fun addToWishlist(dealId: String)
    suspend fun removeFromWishlist(dealId: String)
    fun isWishlisted(dealId: String): Flow<Boolean>
    fun isAlertEnabled(dealId: String): Flow<Boolean>
    fun getWishlistAlerts(): Flow<Map<String, Boolean>>
    suspend fun setAlertEnabled(dealId: String, enabled: Boolean)
}

interface StoreFavoriteRepository {
    fun getFavoriteStores(): Flow<List<Seller>>
    suspend fun addFavorite(sellerId: String)
    suspend fun removeFavorite(sellerId: String)
    fun isFavorite(sellerId: String): Flow<Boolean>
}
