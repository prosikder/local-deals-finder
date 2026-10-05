package com.localdeals.finder.domain.usecase

import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, pass: String, role: Role): Result<User> {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("All fields must be filled"))
        }
        if (!email.contains("@")) {
            return Result.failure(Exception("Invalid email format"))
        }
        if (pass.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        return authRepository.register(name, email, pass, role)
    }
}

class LoginUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Email and password are required"))
        }
        return authRepository.login(email, pass)
    }
}

class UpdateProfileUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(user: User): Result<User> {
        if (user.name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }
        return authRepository.updateProfile(user)
    }
}

class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}

class LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
