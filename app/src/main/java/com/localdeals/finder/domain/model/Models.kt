package com.localdeals.finder.domain.model

enum class Role {
    USER, SELLER, ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: Role = Role.USER,
    val searchRadiusKm: Int = 5,
    val language: String = "English"
)

data class Seller(
    val id: String,
    val userId: String,
    val businessName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val openingHours: Map<String, String> = emptyMap(),
    val status: String = "ACTIVE" // ACTIVE, SUSPENDED
)

data class Category(
    val id: String,
    val name: String,
    val iconResName: String
)

data class Deal(
    val id: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercentage: Int,
    val stock: Int,
    val categoryId: String,
    val expirationTimeMillis: Long,
    val distanceMeters: Int? = null,
    val imageUrl: String = ""
)

data class Review(
    val id: String,
    val dealId: String,
    val sellerId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestampMillis: Long
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestampMillis: Long,
    val isRead: Boolean = false
)

enum class OrderStatus {
    PENDING, ACCEPTED, READY, COMPLETED, REJECTED, CANCELLED
}

data class OrderRequest(
    val id: String,
    val dealId: String,
    val buyerId: String,
    val sellerId: String,
    val quantity: Int,
    val note: String = "",
    val phoneConfirmation: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestampMillis: Long,
    val isRead: Boolean,
    val deepLinkUrl: String = ""
)
