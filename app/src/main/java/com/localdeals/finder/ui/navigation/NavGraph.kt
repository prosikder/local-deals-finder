package com.localdeals.finder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.localdeals.finder.ui.screens.*

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
            SplashScreen(onNavigateNext = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateNext = {
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
            RegisterScreen(onBack = { navController.popBackStack() })
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
        composable(Screen.Profile.route) { ProfileScreen() }

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
