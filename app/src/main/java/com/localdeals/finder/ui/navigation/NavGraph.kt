package com.localdeals.finder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.localdeals.finder.ui.screens.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Role
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            val state by splashViewModel.state.collectAsState()

            LaunchedEffect(state) {
                when (state) {
                    is SplashState.GoToLogin -> {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    is SplashState.GoToLocationPermission -> {
                        navController.navigate(Screen.LocationPermission.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    is SplashState.GoToHome -> {
                        val role = (state as SplashState.GoToHome).role
                        val destination = when (role) {
                            Role.ADMIN -> Screen.AdminPanel.route
                            Role.SELLER -> Screen.SellerDashboard.route
                            Role.USER -> Screen.Home.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
            SplashScreen()
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateNext = { role ->
                    navController.navigate(Screen.LocationPermission.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onNavigateNext = { role ->
                    navController.navigate(Screen.LocationPermission.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LocationPermission.route) {
            LocationPermissionScreen(onNavigateNext = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.LocationPermission.route) { inclusive = true }
                }
            })
        }
        
        // Bottom Nav Routes
        composable(Screen.Home.route) {
            HomeScreen(onNavigateGallery = {
                navController.navigate(Screen.ComponentGallery.route)
            })
        }
        composable(Screen.Map.route) { MapScreen() }
        composable(Screen.Search.route) { SearchScreen() }
        composable(Screen.Wishlist.route) { WishlistScreen() }
        composable(Screen.Profile.route) { 
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                }
            )
        }

        // Details & Deep Links
        composable(Screen.Category.route) { backStackEntry ->
            CategoryScreen(categoryId = backStackEntry.arguments?.getString("categoryId"))
        }
        composable(Screen.DealDetails.route) { backStackEntry ->
            DealDetailsScreen(dealId = backStackEntry.arguments?.getString("dealId"))
        }
        composable(Screen.SellerStore.route) { backStackEntry ->
            SellerStoreScreen(sellerId = backStackEntry.arguments?.getString("sellerId"))
        }
        composable(Screen.Chat.route) { backStackEntry ->
            ChatScreen(userId = backStackEntry.arguments?.getString("userId"))
        }
        composable(Screen.OrderRequest.route) { backStackEntry ->
            OrderRequestScreen(dealId = backStackEntry.arguments?.getString("dealId"))
        }
        composable(Screen.MyOrders.route) { MyOrdersScreen() }
        composable(Screen.Review.route) { backStackEntry ->
            ReviewScreen(dealId = backStackEntry.arguments?.getString("dealId"))
        }
        composable(Screen.Notifications.route) { NotificationsScreen() }
        
        // Seller Dashboard
        composable(Screen.SellerDashboard.route) { SellerDashboardScreen() }

        // Admin Panel
        composable(Screen.AdminPanel.route) { AdminPanelScreen() }

        // Debug Gallery
        composable(Screen.ComponentGallery.route) {
            ComponentGalleryScreen(onBack = { navController.popBackStack() })
        }
    }
}
