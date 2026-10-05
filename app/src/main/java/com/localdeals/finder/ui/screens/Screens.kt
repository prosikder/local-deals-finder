package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(title: String, onNavigate: (() -> Unit)? = null, buttonText: String? = null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            if (onNavigate != null && buttonText != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNavigate) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) { PlaceholderScreen("Splash Screen", onNavigateNext, "Go to Login") }

@Composable
fun LoginScreen(onNavigateNext: () -> Unit, onGoToRegister: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Login Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNavigateNext) { Text("Login") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onGoToRegister) { Text("Register") }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit) { PlaceholderScreen("Register Screen", onBack, "Back to Login") }

@Composable
fun LocationPermissionScreen(onNavigateNext: () -> Unit) { PlaceholderScreen("Location Permission Screen", onNavigateNext, "Grant Permission") }

@Composable
fun HomeScreen(onNavigateGallery: () -> Unit) { PlaceholderScreen("Home Screen", onNavigateGallery, "Open Component Gallery") }

@Composable
fun MapScreen() { PlaceholderScreen("Map Screen") }

@Composable
fun SearchScreen() { PlaceholderScreen("Search Screen") }

@Composable
fun CategoryScreen(categoryId: String?) { PlaceholderScreen("Category Screen: $categoryId") }

@Composable
fun DealDetailsScreen(dealId: String?) { PlaceholderScreen("Deal Details Screen: $dealId") }

@Composable
fun SellerStoreScreen(sellerId: String?) { PlaceholderScreen("Seller Store Screen: $sellerId") }

@Composable
fun ChatScreen(userId: String?) { PlaceholderScreen("Chat Screen: $userId") }

@Composable
fun OrderRequestScreen(dealId: String?) { PlaceholderScreen("Order Request Screen: $dealId") }

@Composable
fun MyOrdersScreen() { PlaceholderScreen("My Orders Screen") }

@Composable
fun ReviewScreen(dealId: String?) { PlaceholderScreen("Review Screen: $dealId") }

@Composable
fun WishlistScreen() { PlaceholderScreen("Wishlist Screen") }

@Composable
fun NotificationsScreen() { PlaceholderScreen("Notifications Screen") }

@Composable
fun ProfileScreen() { PlaceholderScreen("Profile Screen") }

@Composable
fun SellerDashboardScreen() { PlaceholderScreen("Seller Dashboard Screen") }

@Composable
fun AdminPanelScreen() { PlaceholderScreen("Admin Panel Screen") }
