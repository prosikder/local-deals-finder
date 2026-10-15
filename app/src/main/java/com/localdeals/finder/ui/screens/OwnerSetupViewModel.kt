package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OwnerSetupState {
    object Idle : OwnerSetupState()
    object Loading : OwnerSetupState()
    object Success : OwnerSetupState()
    data class Error(val message: String) : OwnerSetupState()
}

@HiltViewModel
class OwnerSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<OwnerSetupState>(OwnerSetupState.Idle)
    val state = _state.asStateFlow()

    fun setupOwner(name: String, email: String, password: String, confirmPassword: String) {
        val trimmedEmail = email.trim()
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (trimmedEmail.isBlank() || !trimmedEmail.matches(emailRegex)) {
            _state.value = OwnerSetupState.Error("Please enter a valid administrator email address.")
            return
        }

        if (password.length < 8) {
            _state.value = OwnerSetupState.Error("Password must be at least 8 characters long.")
            return
        }

        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigitOrSpecial = password.any { it.isDigit() || !it.isLetterOrDigit() }

        if (!hasUpper || !hasLower || !hasDigitOrSpecial) {
            _state.value = OwnerSetupState.Error("Password must include uppercase, lowercase, and at least one number or symbol.")
            return
        }

        if (password != confirmPassword) {
            _state.value = OwnerSetupState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _state.value = OwnerSetupState.Loading
            val result = authRepository.setupOwner(
                name = name.trim().ifBlank { "Owner Admin" },
                email = trimmedEmail,
                pass = password
            )
            result.onSuccess {
                _state.value = OwnerSetupState.Success
            }.onFailure { err ->
                _state.value = OwnerSetupState.Error(err.message ?: "Failed to set up administrator.")
            }
        }
    }
}
