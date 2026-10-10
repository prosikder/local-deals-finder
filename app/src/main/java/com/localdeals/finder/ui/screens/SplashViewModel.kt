package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object GoToLogin : SplashState()
    object GoToLocationPermission : SplashState()
    data class GoToHome(val role: Role) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null || user.isBlocked) {
                if (user?.isBlocked == true) {
                    // we could theoretically logout here, but getCurrentUserUseCase just fetches it
                }
                _state.value = SplashState.GoToLogin
            } else {
                // In a real app we'd check if location permission is granted. 
                // For now, if user exists, route to Home with their role.
                _state.value = SplashState.GoToHome(user.role)
            }
        }
    }
}
