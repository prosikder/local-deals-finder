package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import org.junit.Assert.*
import org.junit.Test

class RoleGuardUseCaseTest {

    private val useCase = RoleGuardUseCase()

    @Test
    fun `blocked user cannot access any routes`() {
        val user = User("1", "Test", "t@t.com", role = Role.ADMIN, isBlocked = true)
        
        assertFalse(useCase.canAccessAdminRoutes(user))
        assertFalse(useCase.canAccessSellerRoutes(user))
        assertFalse(useCase.canAccessUserRoutes(user))
    }

    @Test
    fun `null user cannot access routes`() {
        assertFalse(useCase.canAccessAdminRoutes(null))
        assertFalse(useCase.canAccessSellerRoutes(null))
        assertFalse(useCase.canAccessUserRoutes(null))
    }

    @Test
    fun `seller user can access seller and user routes but not admin`() {
        val user = User("1", "Test", "t@t.com", role = Role.SELLER, isBlocked = false)
        
        assertFalse(useCase.canAccessAdminRoutes(user))
        assertTrue(useCase.canAccessSellerRoutes(user))
        assertTrue(useCase.canAccessUserRoutes(user))
    }

    @Test
    fun `user role can access user routes but not seller or admin`() {
        val user = User("1", "Test", "t@t.com", role = Role.USER, isBlocked = false)
        
        assertFalse(useCase.canAccessAdminRoutes(user))
        assertFalse(useCase.canAccessSellerRoutes(user))
        assertTrue(useCase.canAccessUserRoutes(user))
    }

    @Test
    fun `admin user can access admin and user routes`() {
        val user = User("1", "Test", "t@t.com", role = Role.ADMIN, isBlocked = false)
        
        assertTrue(useCase.canAccessAdminRoutes(user))
        assertTrue(useCase.canAccessUserRoutes(user))
    }
}
