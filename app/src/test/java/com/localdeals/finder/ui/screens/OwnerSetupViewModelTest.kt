package com.localdeals.finder.ui.screens

import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepo: FakeOwnerAuthRepo
    private lateinit var viewModel: OwnerSetupViewModel

    class FakeOwnerAuthRepo : AuthRepository {
        var hasAdminUser = false
        var lastCreatedUser: User? = null

        override suspend fun getCurrentUser(): User? = lastCreatedUser
        override suspend fun login(email: String, pass: String): Result<User> = Result.failure(Exception())
        override suspend fun register(name: String, email: String, pass: String, role: Role): Result<User> = Result.failure(Exception())
        override suspend fun updateProfile(user: User): Result<User> = Result.success(user)
        override suspend fun logout() {}
        override fun getUsers(): Flow<List<User>> = flowOf(emptyList())
        override suspend fun updateUserStatus(userId: String, isBlocked: Boolean) {}
        override suspend fun hasAdmin(): Boolean = hasAdminUser

        override suspend fun setupOwner(name: String, email: String, pass: String): Result<User> {
            val user = User("admin_1", name, email, role = Role.ADMIN)
            lastCreatedUser = user
            hasAdminUser = true
            return Result.success(user)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepo = FakeOwnerAuthRepo()
        viewModel = OwnerSetupViewModel(fakeAuthRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setupOwner with short password sets error`() = runTest {
        viewModel.setupOwner("Admin", "admin@app.com", "Pass1", "Pass1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is OwnerSetupState.Error)
        assertEquals("Password must be at least 8 characters long.", (viewModel.state.value as OwnerSetupState.Error).message)
    }

    @Test
    fun `setupOwner with mismatched passwords sets error`() = runTest {
        viewModel.setupOwner("Admin", "admin@app.com", "StrongPass@123", "WrongPass@123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is OwnerSetupState.Error)
        assertEquals("Passwords do not match.", (viewModel.state.value as OwnerSetupState.Error).message)
    }

    @Test
    fun `setupOwner with valid data sets success`() = runTest {
        viewModel.setupOwner("Super Admin", "owner@localdeals.com", "StrongOwner@123", "StrongOwner@123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is OwnerSetupState.Success)
        assertTrue(fakeAuthRepo.hasAdminUser)
        assertEquals("owner@localdeals.com", fakeAuthRepo.lastCreatedUser?.email)
    }
}
