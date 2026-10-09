package com.localdeals.finder.di

import com.localdeals.finder.data.mock.*
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
        mockAuthRepository: MockAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDealRepository(
        mockDealRepository: MockDealRepository
    ): DealRepository

    @Binds
    @Singleton
    abstract fun bindSellerRepository(
        mockSellerRepository: MockSellerRepository
    ): SellerRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        mockCategoryRepository: MockCategoryRepository
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        mockReviewRepository: MockReviewRepository
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        mockChatRepository: MockChatRepository
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        mockOrderRepository: MockOrderRepository
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: com.localdeals.finder.data.location.LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        mockWishlistRepository: MockWishlistRepository
    ): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindStoreFavoriteRepository(
        mockStoreFavoriteRepository: MockStoreFavoriteRepository
    ): StoreFavoriteRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        mockNotificationRepository: MockNotificationRepository
    ): NotificationRepository
}
