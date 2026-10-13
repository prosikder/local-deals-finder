package com.localdeals.finder.data.cloud

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.localdeals.finder.data.local.AppDatabase
import com.localdeals.finder.data.local.entity.ChatMessageEntity
import com.localdeals.finder.data.local.entity.DealEntity
import com.localdeals.finder.data.local.entity.OrderRequestEntity
import com.localdeals.finder.domain.model.OrderStatus
import com.localdeals.finder.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val database: AppDatabase
) {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var ordersListenerRegistration: ListenerRegistration? = null
    private var chatListenerRegistration: ListenerRegistration? = null

    /**
     * Sync deals between Firestore and Room:
     * 1. Pulls newly published deals from Firestore to local Room.
     * 2. If remote collection is empty, pushes local deals to Firestore so all devices share the deals!
     */
    suspend fun syncDeals() = withContext(Dispatchers.IO) {
        try {
            AppLogger.d("FirebaseSync", "Starting deals sync with Firestore...")
            val snapshot = firestore.collection("deals").get().await()

            if (snapshot.isEmpty) {
                AppLogger.d("FirebaseSync", "Firestore deals collection empty. Seeding local deals to Firestore...")
                val localDeals = database.dealDao().getAllDealsSync()
                for (deal in localDeals) {
                    val map = mapOf(
                        "id" to deal.id,
                        "sellerId" to deal.sellerId,
                        "title" to deal.title,
                        "description" to deal.description,
                        "originalPrice" to deal.originalPrice,
                        "discountedPrice" to deal.discountedPrice,
                        "discountPercentage" to deal.discountPercentage,
                        "stock" to deal.stock,
                        "categoryId" to deal.categoryId,
                        "expirationTimeMillis" to deal.expirationTimeMillis,
                        "imageUrl" to deal.imageUrl
                    )
                    firestore.collection("deals").document(deal.id).set(map)
                }
                AppLogger.d("FirebaseSync", "Successfully seeded ${localDeals.size} deals to Firestore.")
            } else {
                val remoteDeals = snapshot.documents.mapNotNull { doc ->
                    try {
                        DealEntity(
                            id = doc.getString("id") ?: doc.id,
                            sellerId = doc.getString("sellerId") ?: "",
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            originalPrice = doc.getDouble("originalPrice") ?: 0.0,
                            discountedPrice = doc.getDouble("discountedPrice") ?: 0.0,
                            discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0,
                            stock = doc.getLong("stock")?.toInt() ?: 0,
                            categoryId = doc.getString("categoryId") ?: "misc",
                            expirationTimeMillis = doc.getLong("expirationTimeMillis") ?: 0L,
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                if (remoteDeals.isNotEmpty()) {
                    remoteDeals.forEach { deal ->
                        if (database.sellerDao().getSellerByIdSync(deal.sellerId) == null) {
                            database.sellerDao().insertSeller(
                                com.localdeals.finder.data.local.entity.SellerEntity(
                                    id = deal.sellerId,
                                    userId = deal.sellerId,
                                    businessName = "Partner Merchant",
                                    address = "Dhaka, Bangladesh",
                                    latitude = 23.8103,
                                    longitude = 90.4125,
                                    rating = 5.0,
                                    status = "ACTIVE"
                                )
                            )
                        }
                    }
                    database.dealDao().insertDeals(remoteDeals)
                    AppLogger.d("FirebaseSync", "Successfully synced ${remoteDeals.size} deals from Firestore to Room.")
                }
            }
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Deals sync skipped or offline: ${e.message}")
        }
    }

    /**
     * Publishes a deal to Firestore when created or updated by a merchant
     */
    suspend fun publishDeal(deal: DealEntity) = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "id" to deal.id,
                "sellerId" to deal.sellerId,
                "title" to deal.title,
                "description" to deal.description,
                "originalPrice" to deal.originalPrice,
                "discountedPrice" to deal.discountedPrice,
                "discountPercentage" to deal.discountPercentage,
                "stock" to deal.stock,
                "categoryId" to deal.categoryId,
                "expirationTimeMillis" to deal.expirationTimeMillis,
                "imageUrl" to deal.imageUrl
            )
            firestore.collection("deals").document(deal.id).set(map).await()
            AppLogger.d("FirebaseSync", "Deal ${deal.id} published to Firestore.")
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to publish deal to Firestore: ${e.message}")
        }
    }

    /**
     * Deletes a deal from Firestore when deleted by a merchant or admin
     */
    suspend fun deleteDeal(dealId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("deals").document(dealId).delete().await()
            AppLogger.d("FirebaseSync", "Deal $dealId deleted from Firestore.")
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to delete deal from Firestore: ${e.message}")
        }
    }

    /**
     * Uploads an order request to Firestore
     */
    suspend fun uploadOrder(order: OrderRequestEntity) = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "id" to order.id,
                "dealId" to order.dealId,
                "buyerId" to order.buyerId,
                "sellerId" to order.sellerId,
                "quantity" to order.quantity,
                "note" to order.note,
                "phoneConfirmation" to order.phoneConfirmation,
                "status" to order.status.name,
                "timestampMillis" to order.timestampMillis
            )
            firestore.collection("orders").document(order.id).set(map).await()
            AppLogger.d("FirebaseSync", "Order ${order.id} uploaded to Firestore.")
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to upload order to Firestore: ${e.message}")
        }
    }

    /**
     * Listens to real-time orders for a seller and syncs them to Room
     */
    fun startListeningToSellerOrders(sellerId: String) {
        try {
            ordersListenerRegistration?.remove()
            ordersListenerRegistration = firestore.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        AppLogger.w("FirebaseSync", "Orders listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val orders = snapshot.documents.mapNotNull { doc ->
                            try {
                                OrderRequestEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    dealId = doc.getString("dealId") ?: "",
                                    buyerId = doc.getString("buyerId") ?: "",
                                    sellerId = doc.getString("sellerId") ?: "",
                                    quantity = doc.getLong("quantity")?.toInt() ?: 1,
                                    note = doc.getString("note") ?: "",
                                    phoneConfirmation = doc.getString("phoneConfirmation") ?: "",
                                    status = OrderStatus.valueOf(doc.getString("status") ?: OrderStatus.PENDING.name),
                                    timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            database.orderDao().insertOrders(orders)
                        }
                    }
                }
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to start orders listener: ${e.message}")
        }
    }

    /**
     * Sends a chat message to Firestore
     */
    suspend fun sendChatMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "id" to message.id,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "timestampMillis" to message.timestampMillis,
                "isRead" to message.isRead
            )
            firestore.collection("chat_messages").document(message.id).set(map).await()
            AppLogger.d("FirebaseSync", "Chat message ${message.id} sent to Firestore.")
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to send chat message to Firestore: ${e.message}")
        }
    }

    /**
     * Listens to real-time chat messages between two users and syncs them to Room
     */
    fun startListeningToChat(currentUserId: String, otherUserId: String) {
        try {
            chatListenerRegistration?.remove()
            chatListenerRegistration = firestore.collection("chat_messages")
                .whereIn("senderId", listOf(currentUserId, otherUserId))
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        AppLogger.w("FirebaseSync", "Chat listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val senderId = doc.getString("senderId") ?: ""
                                val receiverId = doc.getString("receiverId") ?: ""
                                if ((senderId == currentUserId && receiverId == otherUserId) ||
                                    (senderId == otherUserId && receiverId == currentUserId)
                                ) {
                                    ChatMessageEntity(
                                        id = doc.getString("id") ?: doc.id,
                                        senderId = senderId,
                                        receiverId = receiverId,
                                        content = doc.getString("content") ?: "",
                                        timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis(),
                                        isRead = doc.getBoolean("isRead") ?: false
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (messages.isNotEmpty()) {
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                database.chatDao().insertMessages(messages)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            AppLogger.w("FirebaseSync", "Failed to start chat listener: ${e.message}")
        }
    }

    fun stopListeners() {
        ordersListenerRegistration?.remove()
        ordersListenerRegistration = null
        chatListenerRegistration?.remove()
        chatListenerRegistration = null
    }
}
