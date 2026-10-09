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
                onNavigateToOrders = onNavigateToOrders
            )
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(user: User, onSave: (User) -> Unit, onLogout: () -> Unit, onNavigateToOrders: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var radius by remember { mutableStateOf(user.searchRadiusKm) }
    var language by remember { mutableStateOf(user.language) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Search Radius Dropdown
        Text("Search Radius (km)")
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf(1, 3, 5, 10).forEach { km ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = radius == km, onClick = { radius = km })
                    Text("$km km")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Language") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(text = "Save Profile", onClick = {
            onSave(user.copy(name = name, phone = phone, searchRadiusKm = radius, language = language))
        })
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onNavigateToOrders,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Orders")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
