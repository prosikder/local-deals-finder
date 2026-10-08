package com.localdeals.finder.data.mock

import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    override fun getMessages(userId1: String, userId2: String): Flow<List<ChatMessage>> = messages.asStateFlow()

    override suspend fun sendMessage(message: ChatMessage) {
        messages.value = messages.value + message
    }
}

@Singleton
class MockOrderRepository @Inject constructor() : OrderRepository {
    private val orders = MutableStateFlow<List<OrderRequest>>(emptyList())

    override fun getOrdersForUser(userId: String): Flow<List<OrderRequest>> = orders.asStateFlow()

    override suspend fun createOrder(order: OrderRequest) {
        orders.value = orders.value + order
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
