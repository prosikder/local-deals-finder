package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.DealRepository
import com.localdeals.finder.domain.repository.OrderRepository
import com.localdeals.finder.domain.usecase.OrderLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OrderRequestUiState(
    val isLoading: Boolean = false,
    val deal: Deal? = null,
    val quantity: Int = 1,
    val note: String = "",
    val phoneConfirmation: String = "",
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class OrderRequestViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dealRepository: DealRepository,
    private val orderRepository: OrderRepository,
    private val analyticsTracker: com.localdeals.finder.domain.analytics.AnalyticsTracker,
    private val firebaseSyncManager: com.localdeals.finder.data.cloud.FirebaseSyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderRequestUiState(isLoading = true))
    val uiState: StateFlow<OrderRequestUiState> = _uiState.asStateFlow()

    fun loadDeal(dealId: String) {
        viewModelScope.launch {
            val deal = dealRepository.getDealById(dealId)
            val user = authRepository.getCurrentUser()
            if (deal != null && user != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    deal = deal,
                    phoneConfirmation = user.phone
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Deal or user not found")
            }
        }
    }

    fun updateQuantity(qty: Int) {
        val deal = _uiState.value.deal ?: return
        if (qty in 1..deal.stock) {
            _uiState.value = _uiState.value.copy(quantity = qty)
        }
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phoneConfirmation = phone)
    }

    fun submitOrder() {
        val state = _uiState.value
        val deal = state.deal ?: return
        
        if (!OrderLifecycle.canAcceptOrder(state.quantity, deal.stock)) {
            _uiState.value = state.copy(error = "Insufficient stock")
            return
        }
        val cleanPhone = com.localdeals.finder.util.SanitizationUtil.sanitizePhone(state.phoneConfirmation)
        if (cleanPhone.isBlank()) {
            _uiState.value = state.copy(error = "Phone number is required")
            return
        }
        val cleanNote = com.localdeals.finder.util.SanitizationUtil.sanitizeText(state.note, 500)

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val user = authRepository.getCurrentUser() ?: return@launch
            val orderId = UUID.randomUUID().toString()
            val order = OrderRequest(
                id = orderId,
                dealId = deal.id,
                buyerId = user.id,
                sellerId = deal.sellerId,
                quantity = state.quantity,
                note = cleanNote,
                phoneConfirmation = cleanPhone,
                status = OrderStatus.PENDING
            )
            orderRepository.createOrder(order)
            firebaseSyncManager.uploadOrder(
                com.localdeals.finder.data.local.entity.OrderRequestEntity(
                    id = orderId,
                    dealId = deal.id,
                    buyerId = user.id,
                    sellerId = deal.sellerId,
                    quantity = state.quantity,
                    note = cleanNote,
                    phoneConfirmation = cleanPhone,
                    status = OrderStatus.PENDING,
                    timestampMillis = System.currentTimeMillis()
                )
            )
            analyticsTracker.logEvent(
                "order_placed",
                mapOf("orderId" to orderId, "dealId" to deal.id, "sellerId" to deal.sellerId)
            )
            _uiState.value = state.copy(isLoading = false, success = true)
        }
    }
}

data class MyOrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderWithDeal> = emptyList(),
    val isSimulatorEnabled: Boolean = false,
    val currentUserId: String = ""
)

data class OrderWithDeal(
    val order: OrderRequest,
    val deal: Deal?
)

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val dealRepository: DealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyOrdersUiState(isLoading = true))
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                combine(
                    orderRepository.getOrdersForUser(user.id),
                    dealRepository.getDeals(),
                    orderRepository.isSimulatorEnabled()
                ) { orders, deals, simEnabled ->
                    val ordersWithDeals = orders.map { order ->
                        OrderWithDeal(order, deals.find { it.id == order.dealId })
                    }
                    MyOrdersUiState(isLoading = false, orders = ordersWithDeals, isSimulatorEnabled = simEnabled, currentUserId = user.id)
                }.collect {
                    _uiState.value = it
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val currentOrder = _uiState.value.orders.find { it.order.id == orderId }?.order
            if (currentOrder != null && OrderLifecycle.isValidTransition(currentOrder.status, OrderStatus.CANCELLED)) {
                orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val currentOrder = _uiState.value.orders.find { it.order.id == orderId }?.order
            if (currentOrder != null && OrderLifecycle.isValidTransition(currentOrder.status, status)) {
                orderRepository.updateOrderStatus(orderId, status)
            }
        }
    }

    fun toggleSimulator(enabled: Boolean) {
        orderRepository.setSimulatorEnabled(enabled)
    }
}
