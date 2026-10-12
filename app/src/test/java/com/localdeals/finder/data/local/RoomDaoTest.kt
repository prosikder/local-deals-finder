package com.localdeals.finder.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.localdeals.finder.data.local.dao.*
import com.localdeals.finder.data.local.entity.*
import com.localdeals.finder.domain.model.OrderStatus
import com.localdeals.finder.domain.model.Role
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class RoomDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var sellerDao: SellerDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var dealDao: DealDao
    private lateinit var dealStatsDao: DealStatsDao
    private lateinit var reviewDao: ReviewDao
    private lateinit var orderDao: OrderDao
    private lateinit var wishlistDao: WishlistDao
    private lateinit var chatDao: ChatDao
    private lateinit var recentSearchDao: RecentSearchDao
    private lateinit var analyticsDao: AnalyticsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        sellerDao = db.sellerDao()
        categoryDao = db.categoryDao()
        dealDao = db.dealDao()
        dealStatsDao = db.dealStatsDao()
        reviewDao = db.reviewDao()
        orderDao = db.orderDao()
        wishlistDao = db.wishlistDao()
        chatDao = db.chatDao()
        recentSearchDao = db.recentSearchDao()
        analyticsDao = db.analyticsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserDaoInsertAndQuery() = runBlocking {
        val user = UserEntity(
            id = "u1",
            name = "Test User",
            email = "test@example.com",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = Role.USER
        )
        userDao.insertUser(user)

        val retrieved = userDao.getUserByEmail("test@example.com")
        assertNotNull(retrieved)
        assertEquals("Test User", retrieved?.name)
        assertFalse(retrieved?.isBlocked ?: true)

        userDao.updateUserStatus("u1", true)
        val blockedUser = userDao.getUserByIdSync("u1")
        assertTrue(blockedUser?.isBlocked ?: false)
    }

    @Test
    fun testSellerBoundingBoxAndDealsWithSeller() = runBlocking {
        val seller1 = SellerEntity(
            id = "s1",
            userId = "u_s1",
            businessName = "Gulshan Electronics",
            address = "Gulshan 1, Dhaka",
            latitude = 23.7808,
            longitude = 90.4192,
            rating = 4.5
        )
        val seller2 = SellerEntity(
            id = "s2",
            userId = "u_s2",
            businessName = "Dhanmondi Sweets",
            address = "Dhanmondi, Dhaka",
            latitude = 23.7461,
            longitude = 90.3742,
            rating = 4.0
        )
        sellerDao.insertSellers(listOf(seller1, seller2))

        val deal1 = DealEntity(
            id = "d1",
            sellerId = "s1",
            title = "Earphones Sale",
            description = "50% off",
            originalPrice = 1000.0,
            discountedPrice = 500.0,
            discountPercentage = 50,
            stock = 10,
            categoryId = "cat_tech",
            expirationTimeMillis = System.currentTimeMillis() + 100000
        )
        dealDao.insertDeal(deal1)

        // Query bounding box covering Gulshan only:
        val results = dealDao.getDealsWithSellerInBoundingBox(
            minLat = 23.77,
            maxLat = 23.79,
            minLng = 90.40,
            maxLng = 90.43
        ).first()

        assertEquals(1, results.size)
        assertEquals("d1", results[0].deal.id)
        assertEquals("Gulshan Electronics", results[0].seller?.businessName)
    }

    @Test
    fun testCriticalTransaction_OrderAcceptedDecrementsStockAndNotifies() = runBlocking {
        val seller = SellerEntity(
            id = "s1",
            userId = "u_s1",
            businessName = "Store",
            address = "Dhaka",
            latitude = 23.8,
            longitude = 90.4
        )
        sellerDao.insertSeller(seller)

        val deal = DealEntity(
            id = "d1",
            sellerId = "s1",
            title = "Deal",
            description = "Desc",
            originalPrice = 200.0,
            discountedPrice = 100.0,
            discountPercentage = 50,
            stock = 15,
            categoryId = "cat1",
            expirationTimeMillis = System.currentTimeMillis() + 100000
        )
        dealDao.insertDeal(deal)

        val order = OrderRequestEntity(
            id = "ord_100",
            dealId = "d1",
            buyerId = "buyer_1",
            sellerId = "s1",
            quantity = 3,
            status = OrderStatus.PENDING
        )
        orderDao.insertOrder(order)

        val notification = NotificationEntity(
            id = "notif_1",
            userId = "buyer_1",
            title = "Order Accepted",
            message = "Your order was accepted",
            timestampMillis = System.currentTimeMillis()
        )

        // Execute critical atomic transaction
        orderDao.acceptOrderTransaction(
            orderId = "ord_100",
            dealId = "d1",
            quantity = 3,
            notification = notification
        )

        // Verify status updated
        val updatedOrder = orderDao.getOrderById("ord_100")
        assertEquals(OrderStatus.ACCEPTED, updatedOrder?.status)

        // Verify stock decremented (15 - 3 = 12)
        val updatedDeal = dealDao.getDealByIdSync("d1")
        assertEquals(12, updatedDeal?.stock)

        // Verify notification inserted
        val notifications = db.notificationDao().getNotificationsForUser("buyer_1").first()
        assertEquals(1, notifications.size)
        assertEquals("Order Accepted", notifications[0].title)
    }

    @Test
    fun testSellerCanViewOrdersByUserIdOrStoreId() = runBlocking {
        val seller = SellerEntity(
            id = "s_4",
            userId = "user_s4",
            businessName = "Fresh Bites",
            address = "Banani",
            latitude = 23.79,
            longitude = 90.40
        )
        sellerDao.insertSeller(seller)

        val order = OrderRequestEntity(
            id = "ord_cushion",
            dealId = "d_39",
            buyerId = "user_1",
            sellerId = "s_4",
            quantity = 2,
            status = OrderStatus.PENDING
        )
        orderDao.insertOrder(order)

        // Query with user_s4 (the logged-in seller user ID)
        val sellerOrders = orderDao.getOrdersForUser("user_s4").first()
        assertEquals(1, sellerOrders.size)
        assertEquals("ord_cushion", sellerOrders[0].id)

        // Query with user_1 (the buyer user ID)
        val buyerOrders = orderDao.getOrdersForUser("user_1").first()
        assertEquals(1, buyerOrders.size)
        assertEquals("ord_cushion", buyerOrders[0].id)
    }

    @Test
    fun testCriticalTransaction_ReviewSubmittedUpdatesSellerRating() = runBlocking {
        val seller = SellerEntity(
            id = "s_rate",
            userId = "u_s_rate",
            businessName = "Rating Store",
            address = "Dhaka",
            latitude = 23.8,
            longitude = 90.4,
            rating = 0.0
        )
        sellerDao.insertSeller(seller)

        val deal = DealEntity(
            id = "d_rate",
            sellerId = "s_rate",
            title = "Rateable Deal",
            description = "Desc",
            originalPrice = 100.0,
            discountedPrice = 80.0,
            discountPercentage = 20,
            stock = 5,
            categoryId = "cat1",
            expirationTimeMillis = System.currentTimeMillis() + 100000
        )
        dealDao.insertDeal(deal)

        // Submit first review: rating 5
        reviewDao.submitReviewTransaction(
            ReviewEntity(
                id = "r1",
                dealId = "d_rate",
                sellerId = "s_rate",
                userId = "u1",
                userName = "User 1",
                rating = 5,
                comment = "Great",
                timestampMillis = System.currentTimeMillis()
            )
        )

        var updatedSeller = sellerDao.getSellerByIdSync("s_rate")
        assertEquals(5.0, updatedSeller?.rating ?: 0.0, 0.01)

        // Submit second review: rating 3 (Average should be 4.0)
        reviewDao.submitReviewTransaction(
            ReviewEntity(
                id = "r2",
                dealId = "d_rate",
                sellerId = "s_rate",
                userId = "u2",
                userName = "User 2",
                rating = 3,
                comment = "Average",
                timestampMillis = System.currentTimeMillis()
            )
        )

        updatedSeller = sellerDao.getSellerByIdSync("s_rate")
        assertEquals(4.0, updatedSeller?.rating ?: 0.0, 0.01)
    }

    @Test
    fun testRecentSearchDaoLimitAndClear() = runBlocking {
        recentSearchDao.insertRecentSearch(RecentSearchEntity("pizza", 1000L))
        recentSearchDao.insertRecentSearch(RecentSearchEntity("burger", 2000L))

        val searches = recentSearchDao.getRecentSearches().first()
        assertEquals(2, searches.size)
        assertEquals("burger", searches[0].query) // most recent first

        recentSearchDao.clearRecentSearches()
        val emptySearches = recentSearchDao.getRecentSearches().first()
        assertTrue(emptySearches.isEmpty())
    }

    @Test
    fun testWishlistDaoAddAlertAndRemove() = runBlocking {
        val seller = SellerEntity(
            id = "s_w",
            userId = "u_s_w",
            businessName = "Wishlist Store",
            address = "Dhaka",
            latitude = 23.8,
            longitude = 90.4
        )
        sellerDao.insertSeller(seller)

        val deal = DealEntity(
            id = "d_w",
            sellerId = "s_w",
            title = "Wishlist Deal",
            description = "Desc",
            originalPrice = 100.0,
            discountedPrice = 50.0,
            discountPercentage = 50,
            stock = 5,
            categoryId = "cat1",
            expirationTimeMillis = System.currentTimeMillis() + 100000
        )
        dealDao.insertDeal(deal)

        wishlistDao.insertWishlist(
            WishlistEntity(userId = "user_w", dealId = "d_w", isAlertEnabled = true)
        )

        assertTrue(wishlistDao.isWishlisted("user_w", "d_w").first())
        assertTrue(wishlistDao.isAlertEnabled("user_w", "d_w").first())

        wishlistDao.setAlertEnabled("user_w", "d_w", false)
        assertFalse(wishlistDao.isAlertEnabled("user_w", "d_w").first())

        wishlistDao.deleteWishlist("user_w", "d_w")
        assertFalse(wishlistDao.isWishlisted("user_w", "d_w").first())
    }

    @Test
    fun testAnalyticsDaoOperations() = runBlocking {
        analyticsDao.insertEvent(
            AnalyticsEventEntity(name = "deal_view", params = "dealId=d1;sellerId=s1")
        )
        analyticsDao.insertEvent(
            AnalyticsEventEntity(name = "deal_view", params = "dealId=d1;sellerId=s1")
        )
        analyticsDao.insertEvent(
            AnalyticsEventEntity(name = "order_placed", params = "dealId=d1;orderId=o1")
        )

        val totalViews = analyticsDao.getEventCount("deal_view").first()
        assertEquals(2, totalViews)

        val totalOrders = analyticsDao.getEventCount("order_placed").first()
        assertEquals(1, totalOrders)

        val d1Views = analyticsDao.getEventCountWithParam("deal_view", "dealId=d1").first()
        assertEquals(2, d1Views)

        val allEvents = analyticsDao.getAllEvents().first()
        assertEquals(3, allEvents.size)

        analyticsDao.clearAll()
        val emptyEvents = analyticsDao.getAllEvents().first()
        assertTrue(emptyEvents.isEmpty())
    }
}
