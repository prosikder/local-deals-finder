package com.localdeals.finder.data.mock

import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.util.HashUtil
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class MockAuthRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AuthRepository {
    private val userDatabase = mutableListOf<User>()
    private val credentials = mutableMapOf<String, Pair<String, String>>() // email -> Pair(hash, salt)

    init {
        val adminSalt = HashUtil.generateSalt()
        val adminHash = HashUtil.hashPassword("Admin@123", adminSalt)
        credentials["admin@local.app"] = adminHash to adminSalt
        userDatabase.add(User("admin_id", "Admin", "admin@local.app", role = Role.ADMIN))
    }

    override suspend fun getCurrentUser(): User? {
        val userId = dataStoreManager.loggedInUserId.firstOrNull()
        return userDatabase.find { it.id == userId }
    }

    override suspend fun login(email: String, pass: String): Result<User> {
        val creds = credentials[email] ?: return Result.failure(Exception("Invalid email or password"))
        val hash = HashUtil.hashPassword(pass, creds.second)
        if (hash == creds.first) {
            val user = userDatabase.find { it.email == email }
            if (user != null) {
                dataStoreManager.saveSession(user.id)
                return Result.success(user)
            }
        }
        return Result.failure(Exception("Invalid email or password"))
    }

    override suspend fun register(name: String, email: String, pass: String, role: Role): Result<User> {
        if (credentials.containsKey(email)) {
            return Result.failure(Exception("Email already exists"))
        }
        val salt = HashUtil.generateSalt()
        val hash = HashUtil.hashPassword(pass, salt)
        credentials[email] = hash to salt
        val newUser = User(java.util.UUID.randomUUID().toString(), name, email, role = role)
        userDatabase.add(newUser)
        dataStoreManager.saveSession(newUser.id)
        return Result.success(newUser)
    }

    override suspend fun updateProfile(user: User): Result<User> {
        val index = userDatabase.indexOfFirst { it.id == user.id }
        if (index != -1) {
            userDatabase[index] = user
            return Result.success(user)
        }
        return Result.failure(Exception("User not found"))
    }

    override suspend fun logout() {
        dataStoreManager.clearSession()
    }
}

@Singleton
class MockDealRepository @Inject constructor() : DealRepository {
    private val deals = MutableStateFlow<List<Deal>>(MockData.deals)

    override fun getDeals(): Flow<List<Deal>> = deals.asStateFlow()

    override suspend fun getDealById(id: String): Deal? = deals.value.find { it.id == id }

    override suspend fun addDeal(deal: Deal) {
        deals.value = deals.value + deal
    }

    override suspend fun updateDeal(deal: Deal) {
        val currentDeals = deals.value.toMutableList()
        val index = currentDeals.indexOfFirst { it.id == deal.id }
        if (index != -1) {
            currentDeals[index] = deal
            deals.value = currentDeals
        }
    }
}

@Singleton
class MockSellerRepository @Inject constructor() : SellerRepository {
    private val sellers = MutableStateFlow<List<Seller>>(MockData.sellers)

    override fun getSellers(): Flow<List<Seller>> = sellers.asStateFlow()

    override suspend fun getSellerById(id: String): Seller? = sellers.value.find { it.id == id }
}

@Singleton
class MockCategoryRepository @Inject constructor() : CategoryRepository {
    private val categories = MutableStateFlow<List<Category>>(MockData.categories)

    override fun getCategories(): Flow<List<Category>> = categories.asStateFlow()
}

@Singleton
class MockReviewRepository @Inject constructor() : ReviewRepository {
    private val reviews = MutableStateFlow<List<Review>>(
        MockData.deals.take(15).flatMap { deal ->
            listOf(
                Review(
                    id = "rev_1_${deal.id}",
                    dealId = deal.id,
                    sellerId = deal.sellerId,
                    userId = "user_1",
                    userName = "Rahim Ahmed",
                    rating = 5,
                    comment = "Excellent deal! Highly recommend.",
                    timestampMillis = System.currentTimeMillis() - 86400000 * 2
                ),
                Review(
                    id = "rev_2_${deal.id}",
                    dealId = deal.id,
                    sellerId = deal.sellerId,
                    userId = "user_2",
                    userName = "Sadia Islam",
                    rating = 4,
                    comment = "Good value for money, but the store was a bit crowded.",
                    timestampMillis = System.currentTimeMillis() - 86400000 * 5
                )
            )
        }
    )

    override fun getReviewsForDeal(dealId: String): Flow<List<Review>> {
        return reviews.map { list -> list.filter { it.dealId == dealId } }
    }

    override fun getReviewsForSeller(sellerId: String): Flow<List<Review>> {
        return reviews.map { list -> list.filter { it.sellerId == sellerId } }
    }

    override suspend fun addReview(review: Review) {
        reviews.value = reviews.value + review
    }
}

@Singleton
class MockChatRepository @Inject constructor() : ChatRepository {
    private val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val typingStates = MutableStateFlow<Set<String>>(emptySet())

    override fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessage>> {
        return messages.map { list ->
            list.filter {
                (it.senderId == userId1 && it.receiverId == userId2) ||
                (it.senderId == userId2 && it.receiverId == userId1)
            }.sortedBy { it.timestampMillis }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return messages.map { list -> list.count { it.receiverId == userId && !it.isRead } }
    }

    override fun isTyping(userId1: String, userId2: String): Flow<Boolean> {
        return typingStates.map { it.contains("${userId2}_${userId1}") }
    }

    override suspend fun sendMessage(message: ChatMessage) {
        messages.value = messages.value + message
        
        // Auto-reply simulator (if message is from USER to SELLER)
        if (message.receiverId.startsWith("user_s") && !message.senderId.startsWith("user_s")) {
            kotlinx.coroutines.GlobalScope.launch {
                val typingKey = "${message.receiverId}_${message.senderId}"
                kotlinx.coroutines.delay(1000)
                typingStates.value = typingStates.value + typingKey
                kotlinx.coroutines.delay((2000L..4000L).random())
                typingStates.value = typingStates.value - typingKey
                
                val replyContent = if (message.content.contains("stock", ignoreCase = true)) {
                    "Yes, it's in stock!"
                } else if (message.content.contains("price", ignoreCase = true)) {
                    "The price is fixed as shown in the deal."
                } else {
                    "Thanks for reaching out! Let me know how I can help."
                }
                
                messages.value = messages.value + ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    senderId = message.receiverId,
                    receiverId = message.senderId,
                    content = replyContent,
                    timestampMillis = System.currentTimeMillis()
                )
            }
        }
    }

    override suspend fun markAsRead(userId1: String, userId2: String) {
        messages.value = messages.value.map {
            if (it.receiverId == userId1 && it.senderId == userId2) it.copy(isRead = true) else it
        }
    }
}

@Singleton
class MockOrderRepository @Inject constructor(
    private val dealRepository: DealRepository,
    private val notificationRepository: NotificationRepository
) : OrderRepository {
    private val orders = MutableStateFlow<List<OrderRequest>>(emptyList())
    private val simulatorEnabled = MutableStateFlow(false)

    override fun getOrdersForUser(userId: String): Flow<List<OrderRequest>> {
        return orders.map { list -> list.filter { it.buyerId == userId || it.sellerId == userId }.sortedByDescending { it.timestampMillis } }
    }

    override suspend fun createOrder(order: OrderRequest) {
        val deal = dealRepository.getDealById(order.dealId)
        if (deal != null && deal.stock >= order.quantity) {
            orders.value = orders.value + order.copy(status = OrderStatus.PENDING)
            
            if (simulatorEnabled.value) {
                simulateOrderLifecycle(order.id)
            }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val currentOrders = orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val order = currentOrders[index]
            currentOrders[index] = order.copy(status = newStatus)
            orders.value = currentOrders
            
            // If accepted, decrement stock
            if (newStatus == OrderStatus.ACCEPTED) {
                val deal = dealRepository.getDealById(order.dealId)
                if (deal != null) {
                    val updatedDeal = deal.copy(stock = deal.stock - order.quantity)
                    dealRepository.updateDeal(updatedDeal)
                }
            }
            
            // Create notification
            notificationRepository.addNotification(
                AppNotification(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = order.buyerId,
                    title = "Order Status Updated",
                    message = "Your order ${order.id} is now $newStatus",
                    timestampMillis = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
    }

    override fun setSimulatorEnabled(enabled: Boolean) {
        simulatorEnabled.value = enabled
    }

    override fun isSimulatorEnabled(): Flow<Boolean> = simulatorEnabled.asStateFlow()

    private fun simulateOrderLifecycle(orderId: String) {
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(5000)
            updateOrderStatus(orderId, OrderStatus.ACCEPTED)
            kotlinx.coroutines.delay(5000)
            updateOrderStatus(orderId, OrderStatus.READY)
            kotlinx.coroutines.delay(5000)
            updateOrderStatus(orderId, OrderStatus.COMPLETED)
        }
    }
}

@Singleton
class MockNotificationRepository @Inject constructor() : NotificationRepository {
    private val notifications = MutableStateFlow<List<AppNotification>>(emptyList())

    override fun getNotifications(userId: String): Flow<List<AppNotification>> {
        return notifications.map { list -> list.filter { it.userId == userId }.sortedByDescending { it.timestampMillis } }
    }

    override suspend fun addNotification(notification: AppNotification) {
        notifications.value = notifications.value + notification
    }

    override suspend fun markAsRead(notificationId: String) {
        notifications.value = notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }
}

@Singleton
class MockLocationRepository @Inject constructor() : LocationRepository {
    override val locationFlow: Flow<CurrentLocation?> = MutableStateFlow(CurrentLocation.Default)
    
    override suspend fun getLastKnownLocation(): CurrentLocation? = CurrentLocation.Default
    override suspend fun getFreshLocation(timeoutMs: Long): CurrentLocation? = CurrentLocation.Default
    override suspend fun setManualLocation(location: CurrentLocation) {}
    override fun isLocationEnabled(): Boolean = true
    override fun startLocationUpdates() {}
    override fun stopLocationUpdates() {}
}

@Singleton
class MockWishlistRepository @Inject constructor(
    private val dealRepository: DealRepository
) : WishlistRepository {
    private val wishlistedDealIds = MutableStateFlow<Set<String>>(emptySet())

    override fun getWishlist(): Flow<List<Deal>> {
        return kotlinx.coroutines.flow.combine(dealRepository.getDeals(), wishlistedDealIds) { deals, ids ->
            deals.filter { ids.contains(it.id) }
        }
    }

    override suspend fun addToWishlist(dealId: String) {
        wishlistedDealIds.value = wishlistedDealIds.value + dealId
    }

    override suspend fun removeFromWishlist(dealId: String) {
        wishlistedDealIds.value = wishlistedDealIds.value - dealId
    }

    override fun isWishlisted(dealId: String): Flow<Boolean> {
        return wishlistedDealIds.map { ids -> ids.contains(dealId) }
    }
}

@Singleton
class MockStoreFavoriteRepository @Inject constructor(
    private val sellerRepository: SellerRepository
) : StoreFavoriteRepository {
    private val favoriteSellerIds = MutableStateFlow<Set<String>>(emptySet())

    override fun getFavoriteStores(): Flow<List<Seller>> {
        return kotlinx.coroutines.flow.combine(sellerRepository.getSellers(), favoriteSellerIds) { sellers, ids ->
            sellers.filter { ids.contains(it.id) }
        }
    }

    override suspend fun addFavorite(sellerId: String) {
        favoriteSellerIds.value = favoriteSellerIds.value + sellerId
    }

    override suspend fun removeFavorite(sellerId: String) {
        favoriteSellerIds.value = favoriteSellerIds.value - sellerId
    }

    override fun isFavorite(sellerId: String): Flow<Boolean> {
        return favoriteSellerIds.map { ids -> ids.contains(sellerId) }
    }
}
