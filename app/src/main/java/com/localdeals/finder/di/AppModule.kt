package com.localdeals.finder.di

import com.localdeals.finder.data.repository.*
import com.localdeals.finder.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        roomAuthRepository: RoomAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDealRepository(
        roomDealRepository: RoomDealRepository
    ): DealRepository

    @Binds
    @Singleton
    abstract fun bindSellerRepository(
        roomSellerRepository: RoomSellerRepository
    ): SellerRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        roomCategoryRepository: RoomCategoryRepository
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        roomReviewRepository: RoomReviewRepository
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        roomChatRepository: RoomChatRepository
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        roomOrderRepository: RoomOrderRepository
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: com.localdeals.finder.data.location.LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        roomWishlistRepository: RoomWishlistRepository
    ): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindStoreFavoriteRepository(
        roomStoreFavoriteRepository: RoomStoreFavoriteRepository
    ): StoreFavoriteRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        roomNotificationRepository: RoomNotificationRepository
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(
        roomRecentSearchRepository: RoomRecentSearchRepository
    ): RecentSearchRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        roomAnalyticsTracker: com.localdeals.finder.data.analytics.RoomAnalyticsTracker
    ): com.localdeals.finder.domain.analytics.AnalyticsTracker
}
