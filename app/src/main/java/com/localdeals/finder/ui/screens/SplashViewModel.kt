package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.data.local.DatabaseSeeder
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object GoToOnboarding : SplashState()
    object GoToOwnerSetup : SplashState()
    object GoToLogin : SplashState()
    object GoToLocationPermission : SplashState()
    data class GoToHome(val role: Role) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val databaseSeeder: DatabaseSeeder,
    private val dataStoreManager: DataStoreManager,
    private val authRepository: com.localdeals.finder.domain.repository.AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            databaseSeeder.seedIfNotSeeded()
            val onboardingCompleted = dataStoreManager.isOnboardingCompleted.first()
            if (!onboardingCompleted) {
                _state.value = SplashState.GoToOnboarding
                return@launch
            }

            // On first launch in release mode without an administrator, show one-time Owner Setup
            if (!com.localdeals.finder.BuildConfig.IS_TEST_BUILD && !authRepository.hasAdmin()) {
                _state.value = SplashState.GoToOwnerSetup
                return@launch
            }

            val user = getCurrentUserUseCase()
            if (user == null || user.isBlocked) {
                _state.value = SplashState.GoToLogin
            } else {
                _state.value = SplashState.GoToHome(user.role)
            }
        }
    }
}
