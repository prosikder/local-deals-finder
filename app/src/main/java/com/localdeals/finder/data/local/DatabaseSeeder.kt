package com.localdeals.finder.data.local

import androidx.room.withTransaction
import com.localdeals.finder.data.local.entity.*
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.data.mock.MockData
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun seedIfNotSeeded() = withContext(Dispatchers.IO) {
        val isSeeded = database.categoryDao().getCategoryById("cat_1") != null &&
                       database.userDao().getUserByEmail("rahim@local.app") != null
        if (!isSeeded) {
            seedDatabase()
        }
    }

    suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        seedDatabase()
    }

    private suspend fun seedDatabase() {
        database.withTransaction {
            // 1. Clear existing data
            database.wishlistDao().clearWishlist()
            database.storeFavoriteDao().clearFavorites()
            database.chatDao().clearMessages()
            database.notificationDao().clearNotifications()
            database.reviewDao().clearReviews()
            database.orderDao().clearOrders()
            database.dealStatsDao().clearStats()
            database.dealDao().clearDeals()
            database.sellerDao().clearSellers()
            database.categoryDao().clearCategories()
            database.userDao().clearUsers()
            database.recentSearchDao().clearRecentSearches()

            // 2. Seed Categories (always seeded so categories exist in all builds)
            val categories = MockData.categories.map { it.toEntity() }
            database.categoryDao().insertCategories(categories)

            // 3. Seed Demo Data (Users, Sellers, Deals, Reviews)
            val usersToSeed = mutableListOf<UserEntity>()
                val adminSalt = HashUtil.generateSalt()
                val adminHash = HashUtil.hashPassword("Admin@123", adminSalt)
                val adminUser = UserEntity(
                    id = "admin_id",
                    name = "Admin",
                    email = "admin@local.app",
                    passwordHash = adminHash,
                    passwordSalt = adminSalt,
                    role = Role.ADMIN
                )
                usersToSeed.add(adminUser)

                val user1Salt = HashUtil.generateSalt()
                val user1Hash = HashUtil.hashPassword("User@123", user1Salt)
                val user1 = UserEntity(
                    id = "user_1",
                    name = "Rahim Ahmed",
                    email = "rahim@local.app",
                    passwordHash = user1Hash,
                    passwordSalt = user1Salt,
                    phone = "01700000001",
                    role = Role.USER
                )
                usersToSeed.add(user1)

                val user2Salt = HashUtil.generateSalt()
                val user2Hash = HashUtil.hashPassword("User@123", user2Salt)
                val user2 = UserEntity(
                    id = "user_2",
                    name = "Sadia Islam",
                    email = "sadia@local.app",
                    passwordHash = user2Hash,
                    passwordSalt = user2Salt,
                    phone = "01700000002",
                    role = Role.USER
                )
                usersToSeed.add(user2)

                val aliceSalt = HashUtil.generateSalt()
                val aliceHash = HashUtil.hashPassword("pass123", aliceSalt)
                val alice = UserEntity(
                    id = "user_alice",
                    name = "Alice",
                    email = "alice@example.com",
                    passwordHash = aliceHash,
                    passwordSalt = aliceSalt,
                    phone = "01700000003",
                    role = Role.USER
                )
                usersToSeed.add(alice)

                // Seed seller users for each seller
                val sellerUsers = MockData.sellers.map { seller ->
                    val sellerSalt = HashUtil.generateSalt()
                    val sellerHash = HashUtil.hashPassword("Seller@123", sellerSalt)
                    UserEntity(
                        id = seller.userId,
                        name = seller.businessName,
                        email = "${seller.userId}@local.app",
                        passwordHash = sellerHash,
                        passwordSalt = sellerSalt,
                        phone = seller.phoneNumber,
                        role = Role.SELLER
                    )
                }
                usersToSeed.addAll(sellerUsers)

                database.userDao().insertUsers(usersToSeed)

                // Seed Sellers
                val sellers = MockData.sellers.map { it.toEntity() }
                database.sellerDao().insertSellers(sellers)

                // Seed Deals
                val deals = MockData.deals.map { it.toEntity() }
                database.dealDao().insertDeals(deals)

                // Seed Deal Stats
                val statsList = MockData.deals.map { deal ->
                    DealStatsEntity(
                        dealId = deal.id,
                        views = (12..150).random(),
                        wishlistAdds = (2..25).random(),
                        orders = (1..10).random()
                    )
                }
                database.dealStatsDao().insertStats(statsList)

                // Seed Initial Reviews
                val reviews = MockData.deals.take(15).flatMap { deal ->
                    listOf(
                        ReviewEntity(
                            id = "rev_1_${deal.id}",
                            dealId = deal.id,
                            sellerId = deal.sellerId,
                            userId = "user_1",
                            userName = "Rahim Ahmed",
                            rating = 5,
                            comment = "Excellent deal! Highly recommend.",
                            timestampMillis = System.currentTimeMillis() - 86400000L * 2
                        ),
                        ReviewEntity(
                            id = "rev_2_${deal.id}",
                            dealId = deal.id,
                            sellerId = deal.sellerId,
                            userId = "user_2",
                            userName = "Sadia Islam",
                            rating = 4,
                            comment = "Good value for money, but the store was a bit crowded.",
                            timestampMillis = System.currentTimeMillis() - 86400000L * 5
                        )
                    )
                }
                database.reviewDao().insertReviews(reviews)
        }
    }
}
