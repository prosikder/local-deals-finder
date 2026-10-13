package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class SellerDashboardState(
    val isLoading: Boolean = true,
    val activeDealsCount: Int = 0,
    val pendingOrdersCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val totalViews: Int = 0,
    val chartData: List<Int> = emptyList(), // 7 day views
    val activeDeals: List<Deal> = emptyList(),
    val dealStats: Map<String, DealStats> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class SellerDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dealRepository: DealRepository,
    private val orderRepository: OrderRepository,
    private val chatRepository: ChatRepository,
    private val dealAnalyticsUseCase: com.localdeals.finder.domain.usecase.DealAnalyticsUseCase,
    private val deleteDealUseCase: com.localdeals.finder.domain.usecase.DeleteDealUseCase,
    private val firebaseSyncManager: com.localdeals.finder.data.cloud.FirebaseSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(SellerDashboardState())
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val user = authRepository.getCurrentUser()
                if (user == null || user.role != Role.SELLER) {
                    _state.value = _state.value.copy(isLoading = false, error = "Unauthorized")
                    return@launch
                }
                
                val sellerId = user.id
                firebaseSyncManager.startListeningToSellerOrders(sellerId)
                
                val deals = dealRepository.getDealsForSeller(sellerId).firstOrNull() ?: emptyList()
                val activeDealsCount = deals.count { it.stock > 0 }
                
                // Pending orders count
                val orders = orderRepository.getOrdersForUser(sellerId).firstOrNull() ?: emptyList()
                val pendingOrdersCount = orders.count { it.status == OrderStatus.PENDING }
                
                // Unread messages
                val chats = chatRepository.getChatsForUser(sellerId).firstOrNull() ?: emptyList()
                val unreadMessagesCount = chats.count { it.receiverId == sellerId && !it.isRead }
                
                // Mock chart data for 7 days
                val chartData = listOf(12, 45, 23, 78, 56, 89, 41)
                
                val statsMap = mutableMapOf<String, DealStats>()
                var totalViews = 0
                for (deal in deals) {
                    val stats = dealAnalyticsUseCase.getAnalytics(deal.id).firstOrNull()
                    if (stats != null) {
                        statsMap[deal.id] = stats
                        totalViews += stats.views
                    }
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    activeDealsCount = activeDealsCount,
                    pendingOrdersCount = pendingOrdersCount,
                    unreadMessagesCount = unreadMessagesCount,
                    totalViews = totalViews,
                    chartData = chartData,
                    activeDeals = deals,
                    dealStats = statsMap
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateStock(deal: Deal, increment: Int) {
        viewModelScope.launch {
            val newStock = (deal.stock + increment).coerceAtLeast(0)
            dealRepository.updateDeal(deal.copy(stock = newStock))
            loadDashboardData()
        }
    }

    fun deleteDeal(dealId: String) {
        viewModelScope.launch {
            val result = deleteDealUseCase.execute(dealId)
            if (result.isSuccess) {
                firebaseSyncManager.deleteDeal(dealId)
                loadDashboardData()
            } else {
                _state.value = _state.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }
}
