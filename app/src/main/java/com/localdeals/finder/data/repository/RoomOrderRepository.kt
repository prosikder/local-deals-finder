package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.DealDao
import com.localdeals.finder.data.local.dao.NotificationDao
import com.localdeals.finder.data.local.dao.OrderDao
import com.localdeals.finder.data.local.entity.NotificationEntity
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.AppNotification
import com.localdeals.finder.domain.model.OrderStatus
import com.localdeals.finder.domain.model.OrderRequest
import com.localdeals.finder.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomOrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val dealDao: DealDao,
    private val notificationDao: NotificationDao
) : OrderRepository {

    private val simulatorEnabled = MutableStateFlow(false)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getOrdersForUser(userId: String): Flow<List<OrderRequest>> {
        return orderDao.getOrdersForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOrdersForDeal(dealId: String): Flow<List<OrderRequest>> {
        return orderDao.getOrdersForDeal(dealId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createOrder(order: OrderRequest) {
        val deal = dealDao.getDealByIdSync(order.dealId)
        if (deal != null && deal.stock >= order.quantity) {
            orderDao.insertOrder(order.copy(status = OrderStatus.PENDING).toEntity())

            if (simulatorEnabled.value) {
                simulateOrderLifecycle(order.id)
            }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val existingOrder = orderDao.getOrderById(orderId) ?: return

        if (newStatus == OrderStatus.ACCEPTED && existingOrder.status != OrderStatus.ACCEPTED) {
            // Critical flow: order accepted (status + stock + notification) executed in atomic Room transaction
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = existingOrder.buyerId,
                title = "Order Accepted",
                message = "Your order $orderId has been accepted by the seller.",
                timestampMillis = System.currentTimeMillis(),
                isRead = false,
                deepLinkUrl = "localdeals://orders/$orderId"
            )
            orderDao.acceptOrderTransaction(
                orderId = orderId,
                dealId = existingOrder.dealId,
                quantity = existingOrder.quantity,
                notification = notification
            )
        } else {
            orderDao.updateOrderStatus(orderId, newStatus)
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = existingOrder.buyerId,
                    title = "Order Status Updated",
                    message = "Your order $orderId is now $newStatus",
                    timestampMillis = System.currentTimeMillis(),
                    isRead = false,
                    deepLinkUrl = "localdeals://orders/$orderId"
                )
            )
        }
    }

    override fun setSimulatorEnabled(enabled: Boolean) {
        simulatorEnabled.value = enabled
        if (enabled) {
            repositoryScope.launch {
                val activeOrders = orderDao.getOrdersForUser("").firstOrNull() ?: emptyList()
                activeOrders.filter {
                    it.status == OrderStatus.PENDING || it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.READY
                }.forEach { simulateOrderLifecycle(it.id) }
            }
        }
    }

    override fun isSimulatorEnabled(): Flow<Boolean> = simulatorEnabled.asStateFlow()

    private fun simulateOrderLifecycle(orderId: String) {
        repositoryScope.launch {
            var order = orderDao.getOrderById(orderId) ?: return@launch

            if (order.status == OrderStatus.PENDING) {
                delay(5000)
                updateOrderStatus(orderId, OrderStatus.ACCEPTED)
                order = orderDao.getOrderById(orderId) ?: return@launch
            }
            if (order.status == OrderStatus.ACCEPTED) {
                delay(5000)
                updateOrderStatus(orderId, OrderStatus.READY)
                order = orderDao.getOrderById(orderId) ?: return@launch
            }
            if (order.status == OrderStatus.READY) {
                delay(5000)
                updateOrderStatus(orderId, OrderStatus.COMPLETED)
            }
        }
    }
}
