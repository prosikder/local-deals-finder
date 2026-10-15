package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.util.HashUtil
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    val users = mutableListOf<User>()
    var currentUser: User? = null

    override suspend fun getCurrentUser(): User? = currentUser

    override suspend fun login(email: String, pass: String): Result<User> {
        val user = users.find { it.email == email }
        return if (user != null) Result.success(user) else Result.failure(Exception("Not found"))
    }

    override suspend fun register(name: String, email: String, pass: String, role: Role): Result<User> {
        val user = User("1", name, email, role = role)
        users.add(user)
        return Result.success(user)
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return Result.success(user)
    }

    override suspend fun logout() {
        currentUser = null
    }

    override fun getUsers(): kotlinx.coroutines.flow.Flow<List<User>> {
        return kotlinx.coroutines.flow.flowOf(users)
    }

    override suspend fun updateUserStatus(userId: String, isBlocked: Boolean) {
        val index = users.indexOfFirst { it.id == userId }
        if (index != -1) {
            users[index] = users[index].copy(isBlocked = isBlocked)
        }
    }

    override suspend fun hasAdmin(): Boolean = users.any { it.role == Role.ADMIN }

    override suspend fun setupOwner(name: String, email: String, pass: String): Result<User> {
        val user = User("admin_1", name, email, role = Role.ADMIN)
        users.add(user)
        currentUser = user
        return Result.success(user)
    }
}

class AuthUseCasesTest {

    private lateinit var registerUseCase: RegisterUserUseCase
    private lateinit var loginUseCase: LoginUserUseCase
    private lateinit var fakeRepo: FakeAuthRepository

    @Before
    fun setup() {
        fakeRepo = FakeAuthRepository()
        registerUseCase = RegisterUserUseCase(fakeRepo)
        loginUseCase = LoginUserUseCase(fakeRepo)
    }

    @Test
    fun `Register validates empty fields`() = runBlocking {
        val res = registerUseCase("", "test@test.com", "password", Role.USER)
        assertTrue(res.isFailure)
        assertEquals("All fields must be filled", res.exceptionOrNull()?.message)
    }

    @Test
    fun `Register validates email`() = runBlocking {
        val res = registerUseCase("John", "invalidemail", "password", Role.USER)
        assertTrue(res.isFailure)
        assertEquals("Invalid email format", res.exceptionOrNull()?.message)
    }

    @Test
    fun `Register validates password length`() = runBlocking {
        val res = registerUseCase("John", "test@test.com", "123", Role.USER)
        assertTrue(res.isFailure)
        assertEquals("Password must be at least 6 characters", res.exceptionOrNull()?.message)
    }

    @Test
    fun `Register success`() = runBlocking {
        val res = registerUseCase("John", "test@test.com", "password", Role.USER)
        assertTrue(res.isSuccess)
    }

    @Test
    fun `HashUtil creates correct hash`() {
        val salt = "randomsalt"
        val hash1 = HashUtil.hashPassword("mysecret", salt)
        val hash2 = HashUtil.hashPassword("mysecret", salt)
        assertEquals(hash1, hash2)
    }
}
