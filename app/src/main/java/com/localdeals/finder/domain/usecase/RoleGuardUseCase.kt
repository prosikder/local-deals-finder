package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User

class RoleGuardUseCase {
    fun canAccessSellerRoutes(user: User?): Boolean {
        return user != null && !user.isBlocked && user.role == Role.SELLER
    }

    fun canAccessAdminRoutes(user: User?): Boolean {
        return user != null && !user.isBlocked && user.role == Role.ADMIN
    }
    
    fun canAccessUserRoutes(user: User?): Boolean {
        return user != null && !user.isBlocked
    }
}
