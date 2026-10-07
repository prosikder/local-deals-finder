package com.localdeals.finder.data.mock

import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val deals = MutableStateFlow<List<Deal>>(emptyList())

    override fun getDeals(): Flow<List<Deal>> = deals.asStateFlow()

    override suspend fun getDealById(id: String): Deal? = deals.value.find { it.id == id }

    override suspend fun addDeal(deal: Deal) {
        deals.value = deals.value + deal
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
