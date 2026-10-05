package com.localdeals.finder.data.mock

import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    private var currentUser: User? = null

    override suspend fun getCurrentUser(): User? = currentUser

    override suspend fun login(email: String, pass: String): Result<User> {
        currentUser = User("1", "Mock User", email, Role.USER)
        return Result.success(currentUser!!)
    }

    override suspend fun logout() {
        currentUser = null
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
    override suspend fun getCurrentLocation(): Pair<Double, Double>? = Pair(0.0, 0.0)
}
