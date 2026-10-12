package com.localdeals.finder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.ui.components.ErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleGuardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _currentRole = MutableStateFlow<Role?>(null)
    val currentRole: StateFlow<Role?> = _currentRole.asStateFlow()

    init {
        checkRole()
    }

    fun checkRole() {
        viewModelScope.launch {
            _currentRole.value = authRepository.getCurrentUser()?.role
        }
    }
}

@Composable
fun RoleGuard(
    requiredRoles: List<Role>,
    currentRole: Role?, // null means not logged in
    content: @Composable () -> Unit
) {
    if (currentRole != null && requiredRoles.contains(currentRole)) {
        content()
    } else {
        ErrorState(message = "Unauthorized. You do not have permission to view this page.")
    }
}

@Composable
fun AuthenticatedRoleGuard(
    requiredRoles: List<Role>,
    viewModel: RoleGuardViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    RoleGuard(
        requiredRoles = requiredRoles,
        currentRole = currentRole,
        content = content
    )
}
