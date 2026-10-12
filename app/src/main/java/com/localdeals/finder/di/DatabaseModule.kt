package com.localdeals.finder.di

import android.content.Context
import androidx.room.Room
import com.localdeals.finder.data.local.AppDatabase
import com.localdeals.finder.data.local.DatabaseMigrations
import com.localdeals.finder.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideSellerDao(db: AppDatabase): SellerDao = db.sellerDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideDealDao(db: AppDatabase): DealDao = db.dealDao()

    @Provides
    fun provideDealStatsDao(db: AppDatabase): DealStatsDao = db.dealStatsDao()

    @Provides
    fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()

    @Provides
    fun provideWishlistDao(db: AppDatabase): WishlistDao = db.wishlistDao()

    @Provides
    fun provideStoreFavoriteDao(db: AppDatabase): StoreFavoriteDao = db.storeFavoriteDao()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideRecentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()

    @Provides
    fun provideAnalyticsDao(db: AppDatabase): AnalyticsDao = db.analyticsDao()
}
