package com.localdeals.finder.data.repository

import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.data.local.dao.UserDao
import com.localdeals.finder.data.local.entity.UserEntity
import com.localdeals.finder.data.local.mapper.toDomain
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.util.HashUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomAuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sellerDao: com.localdeals.finder.data.local.dao.SellerDao,
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override suspend fun getCurrentUser(): User? {
        val userId = dataStoreManager.loggedInUserId.firstOrNull() ?: return null
        val userEntity = userDao.getUserByIdSync(userId) ?: return null
        if (userEntity.isBlocked) {
            dataStoreManager.clearSession()
            return null
        }
        return userEntity.toDomain()
    }

    override suspend fun login(email: String, pass: String): Result<User> {
        val userEntity = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("Invalid email or password"))

        val computedHash = HashUtil.hashPassword(pass, userEntity.passwordSalt)
        if (computedHash != userEntity.passwordHash) {
            return Result.failure(Exception("Invalid email or password"))
        }

        if (userEntity.isBlocked) {
            return Result.failure(Exception("Account is blocked"))
        }

        dataStoreManager.saveSession(userEntity.id)
        return Result.success(userEntity.toDomain())
    }

    override suspend fun register(
        name: String,
        email: String,
        pass: String,
        role: Role
    ): Result<User> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("Email already exists"))
        }

        val salt = HashUtil.generateSalt()
        val hash = HashUtil.hashPassword(pass, salt)
        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            passwordHash = hash,
            passwordSalt = salt,
            role = role
        )

        userDao.insertUser(newUser)
        if (role == Role.SELLER) {
            val sellerEntity = com.localdeals.finder.data.local.entity.SellerEntity(
                id = "s_${newUser.id.take(8)}",
                userId = newUser.id,
                businessName = name,
                address = "Dhaka, Bangladesh",
                latitude = 23.8103,
                longitude = 90.4125,
                rating = 5.0,
                status = "ACTIVE"
            )
            sellerDao.insertSeller(sellerEntity)
        }
        dataStoreManager.saveSession(newUser.id)
        return Result.success(newUser.toDomain())
    }

    override suspend fun updateProfile(user: User): Result<User> {
        val existing = userDao.getUserByIdSync(user.id)
            ?: return Result.failure(Exception("User not found"))

        val updated = existing.copy(
            name = user.name,
            phone = user.phone,
            searchRadiusKm = user.searchRadiusKm,
            language = user.language,
            isBlocked = user.isBlocked
        )
        userDao.updateUser(updated)
        return Result.success(updated.toDomain())
    }

    override suspend fun logout() {
        dataStoreManager.clearSession()
    }

    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun updateUserStatus(userId: String, isBlocked: Boolean) {
        userDao.updateUserStatus(userId, isBlocked)
    }
}
