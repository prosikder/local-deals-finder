package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.domain.usecase.GetCurrentUserUseCase
import com.localdeals.finder.domain.usecase.LogoutUseCase
import com.localdeals.finder.domain.usecase.UpdateProfileUseCase
import com.localdeals.finder.ui.BaseViewModel
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<User>() {

    init {
        loadProfile()
    }

    private fun loadProfile() {
        emitLoading()
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user != null) {
                emitSuccess(user)
            } else {
                emitError("User not found")
            }
        }
    }

    fun updateProfile(user: User) {
        emitLoading()
        viewModelScope.launch {
            val result = updateProfileUseCase(user)
            if (result.isSuccess) {
                emitSuccess(result.getOrNull()!!)
            } else {
                emitError(result.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLogoutComplete()
        }
    }
}
