package com.localdeals.finder.ui.navigation

import androidx.compose.runtime.Composable
import com.localdeals.finder.domain.model.Role
import com.localdeals.finder.ui.components.ErrorState

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
