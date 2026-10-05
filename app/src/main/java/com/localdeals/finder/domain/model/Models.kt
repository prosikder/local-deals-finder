package com.localdeals.finder.domain.model

enum class Role {
    USER, SELLER, ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role = Role.USER
)

data class Seller(
    val id: String,
    val userId: String,
    val businessName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 0.0
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
    val distanceMeters: Int? = null
)

data class Review(
    val id: String,
    val dealId: String,
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
    val timestampMillis: Long
)

data class OrderRequest(
    val id: String,
    val dealId: String,
    val buyerId: String,
    val sellerId: String,
    val quantity: Int,
    val status: String // PENDING, ACCEPTED, REJECTED, COMPLETED
)

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestampMillis: Long,
    val isRead: Boolean
)
