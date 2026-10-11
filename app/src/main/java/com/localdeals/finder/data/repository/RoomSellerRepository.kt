package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.dao.SellerDao
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.data.local.mapper.toEntity
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.SellerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSellerRepository @Inject constructor(
    private val sellerDao: SellerDao
) : SellerRepository {

    override fun getSellers(): Flow<List<Seller>> {
        return sellerDao.getAllSellers().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getSellerById(id: String): Seller? {
        return sellerDao.getSellerByIdSync(id)?.toDomain()
    }

    override suspend fun updateSeller(seller: Seller) {
        sellerDao.updateSeller(seller.toEntity())
    }

    override suspend fun updateSellerStatus(sellerId: String, status: String) {
        sellerDao.updateSellerStatus(sellerId, status)
    }
}
