package com.localdeals.finder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.User
import com.localdeals.finder.ui.UiState
import com.localdeals.finder.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((state as UiState.Error).message)
            }
        }
        is UiState.Success -> {
            val user = (state as UiState.Success<User>).data
            ProfileContent(
                user = user, 
                onSave = { viewModel.updateProfile(it) }, 
                onLogout = { viewModel.logout(onLogout) },
                onResetDemoData = { viewModel.resetDemoData() },
                onNavigateToOrders = onNavigateToOrders
            )
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    user: User,
    onSave: (User) -> Unit,
    onLogout: () -> Unit,
    onResetDemoData: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var radius by remember { mutableStateOf(user.searchRadiusKm) }
    var language by remember { mutableStateOf(user.language) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Demo Data") },
            text = { Text("Are you sure you want to clear all data and restore demo defaults? This will restore original deals, categories, and sellers.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetDemoData()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 600.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Search Radius Dropdown
        Text("Search Radius", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf(1, 3, 5, 10).forEach { km ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    RadioButton(selected = radius == km, onClick = { radius = km })
                    Text("$km km")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Language Selector
        Text("Language / ভাষা", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("English" to "English", "Bengali" to "বাংলা").forEach { (code, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    RadioButton(
                        selected = language.equals(code, ignoreCase = true) || (code == "Bengali" && language.contains("বাংলা")),
                        onClick = { language = code }
                    )
                    Text(label)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Save Profile",
            onClick = {
                val cleanName = com.localdeals.finder.util.SanitizationUtil.sanitizeText(name, 50)
                val cleanPhone = com.localdeals.finder.util.SanitizationUtil.sanitizePhone(phone)
                onSave(user.copy(name = cleanName, phone = cleanPhone, searchRadiusKm = radius, language = language))
            },
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onNavigateToOrders,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
            Text("My Orders")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
            Text("Reset Demo Data / Clear All")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
            Text("Logout")
        }
    }
}
