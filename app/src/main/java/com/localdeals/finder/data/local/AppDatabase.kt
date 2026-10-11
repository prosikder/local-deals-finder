package com.localdeals.finder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.localdeals.finder.data.local.converter.Converters
import com.localdeals.finder.data.local.dao.*
import com.localdeals.finder.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        SellerEntity::class,
        CategoryEntity::class,
        DealEntity::class,
        DealStatsEntity::class,
        ReviewEntity::class,
        WishlistEntity::class,
        StoreFavoriteEntity::class,
        ChatMessageEntity::class,
        OrderRequestEntity::class,
        NotificationEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sellerDao(): SellerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun dealDao(): DealDao
    abstract fun dealStatsDao(): DealStatsDao
    abstract fun reviewDao(): ReviewDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun storeFavoriteDao(): StoreFavoriteDao
    abstract fun chatDao(): ChatDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        const val DATABASE_NAME = "local_deals_finder.db"
    }
}
