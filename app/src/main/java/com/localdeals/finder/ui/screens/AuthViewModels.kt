package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.usecase.LoginUserUseCase
import com.localdeals.finder.domain.usecase.RegisterUserUseCase
import com.localdeals.finder.ui.BaseViewModel
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : BaseViewModel<Role>() {

    init {
        emitEmpty()
    }

    fun login(email: String, pass: String) {
        emitLoading()
        viewModelScope.launch {
            val result = loginUserUseCase(email, pass)
            if (result.isSuccess) {
                emitSuccess(result.getOrNull()!!.role)
            } else {
                emitError(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
    
    fun resetState() {
        emitEmpty()
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : BaseViewModel<Role>() {

    init {
        emitEmpty()
    }

    fun register(name: String, email: String, pass: String, role: Role) {
        emitLoading()
        viewModelScope.launch {
            val result = registerUserUseCase(name, email, pass, role)
            if (result.isSuccess) {
                emitSuccess(result.getOrNull()!!.role)
            } else {
                emitError(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        emitEmpty()
    }
}
