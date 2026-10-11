package com.localdeals.finder.data.local.mapper

import com.localdeals.finder.data.local.entity.*
import com.localdeals.finder.domain.model.*

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    role = role,
    searchRadiusKm = searchRadiusKm,
    language = language,
    isBlocked = isBlocked
)

fun User.toEntity(passwordHash: String = "", passwordSalt: String = ""): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    passwordHash = passwordHash,
    passwordSalt = passwordSalt,
    phone = phone,
    role = role,
    searchRadiusKm = searchRadiusKm,
    language = language,
    isBlocked = isBlocked
)

fun SellerEntity.toDomain(): Seller = Seller(
    id = id,
    userId = userId,
    businessName = businessName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    rating = rating,
    imageUrl = imageUrl,
    description = description,
    phoneNumber = phoneNumber,
    openingHours = openingHours,
    status = status
)

fun Seller.toEntity(): SellerEntity = SellerEntity(
    id = id,
    userId = userId,
    businessName = businessName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    rating = rating,
    imageUrl = imageUrl,
    description = description,
    phoneNumber = phoneNumber,
    openingHours = openingHours,
    status = status
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconResName = iconResName
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconResName = iconResName
)

fun DealEntity.toDomain(distanceMeters: Int? = null): Deal = Deal(
    id = id,
    sellerId = sellerId,
    title = title,
    description = description,
    originalPrice = originalPrice,
    discountedPrice = discountedPrice,
    discountPercentage = discountPercentage,
    stock = stock,
    categoryId = categoryId,
    expirationTimeMillis = expirationTimeMillis,
    distanceMeters = distanceMeters,
    imageUrl = imageUrl
)

fun Deal.toEntity(): DealEntity = DealEntity(
    id = id,
    sellerId = sellerId,
    title = title,
    description = description,
    originalPrice = originalPrice,
    discountedPrice = discountedPrice,
    discountPercentage = discountPercentage,
    stock = stock,
    categoryId = categoryId,
    expirationTimeMillis = expirationTimeMillis,
    imageUrl = imageUrl
)

fun DealStatsEntity.toDomain(): DealStats = DealStats(
    dealId = dealId,
    views = views,
    wishlistAdds = wishlistAdds,
    orders = orders
)

fun DealStats.toEntity(): DealStatsEntity = DealStatsEntity(
    dealId = dealId,
    views = views,
    wishlistAdds = wishlistAdds,
    orders = orders
)

fun ReviewEntity.toDomain(): Review = Review(
    id = id,
    dealId = dealId,
    sellerId = sellerId,
    userId = userId,
    userName = userName,
    rating = rating,
    comment = comment,
    timestampMillis = timestampMillis,
    isReported = isReported
)

fun Review.toEntity(): ReviewEntity = ReviewEntity(
    id = id,
    dealId = dealId,
    sellerId = sellerId,
    userId = userId,
    userName = userName,
    rating = rating,
    comment = comment,
    timestampMillis = timestampMillis,
    isReported = isReported
)

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    content = content,
    timestampMillis = timestampMillis,
    isRead = isRead
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    content = content,
    timestampMillis = timestampMillis,
    isRead = isRead
)

fun OrderRequestEntity.toDomain(): OrderRequest = OrderRequest(
    id = id,
    dealId = dealId,
    buyerId = buyerId,
    sellerId = sellerId,
    quantity = quantity,
    note = note,
    phoneConfirmation = phoneConfirmation,
    status = status,
    timestampMillis = timestampMillis
)

fun OrderRequest.toEntity(): OrderRequestEntity = OrderRequestEntity(
    id = id,
    dealId = dealId,
    buyerId = buyerId,
    sellerId = sellerId,
    quantity = quantity,
    note = note,
    phoneConfirmation = phoneConfirmation,
    status = status,
    timestampMillis = timestampMillis
)

fun NotificationEntity.toDomain(): AppNotification = AppNotification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    timestampMillis = timestampMillis,
    isRead = isRead,
    deepLinkUrl = deepLinkUrl
)

fun AppNotification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    userId = userId,
    title = title,
    message = message,
    timestampMillis = timestampMillis,
    isRead = isRead,
    deepLinkUrl = deepLinkUrl
)
