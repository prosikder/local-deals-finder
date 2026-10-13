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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

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
                    is SplashState.GoToOnboarding -> {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
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
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateNext = { role ->
                    val nextRoute = when (role) {
                        com.localdeals.finder.domain.model.Role.ADMIN -> Screen.AdminPanel.route
                        com.localdeals.finder.domain.model.Role.SELLER -> Screen.SellerDashboard.route
                        else -> Screen.LocationPermission.route
                    }
                    navController.navigate(nextRoute) {
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
                    val nextRoute = when (role) {
                        com.localdeals.finder.domain.model.Role.ADMIN -> Screen.AdminPanel.route
                        com.localdeals.finder.domain.model.Role.SELLER -> Screen.SellerDashboard.route
                        else -> Screen.LocationPermission.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LocationPermission.route) {
            LocationScreen(onLocationDetermined = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.LocationPermission.route) { inclusive = true }
                }
            })
        }
        
        // Bottom Nav Routes
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateGallery = {
                    navController.navigate(Screen.ComponentGallery.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToLocation = {
                    navController.navigate(Screen.LocationPermission.route)
                },
                onNavigateToCategory = { categoryId ->
                    navController.navigate(Screen.Category.createRoute(categoryId))
                },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                },
                onNavigateToStore = { storeId ->
                    navController.navigate(Screen.SellerStore.createRoute(storeId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                }
            )
        }
        composable(Screen.Wishlist.route) { 
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                }
            )
        }
        composable(Screen.Profile.route) { 
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.MyOrders.route)
                }
            )
        }

        // Details & Deep Links
        composable(Screen.Category.route) { backStackEntry ->
            CategoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                }
            )
        }
        composable(
            route = Screen.DealDetails.route,
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "localdeals://app/deal/{dealId}" }),
            enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
        ) { backStackEntry ->
            DealDetailsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSeller = { sellerId ->
                    navController.navigate(Screen.SellerStore.createRoute(sellerId))
                },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Screen.Chat.createRoute(userId))
                },
                onNavigateToOrder = { dealId ->
                    navController.navigate(Screen.OrderRequest.createRoute(dealId))
                }
            )
        }
        composable(Screen.SellerStore.route) { backStackEntry ->
            SellerStoreScreen(
                sellerId = backStackEntry.arguments?.getString("sellerId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDeal = { dealId ->
                    navController.navigate(Screen.DealDetails.createRoute(dealId))
                },
                onNavigateToChat = { sellerId ->
                    navController.navigate(Screen.Chat.createRoute(sellerId))
                }
            )
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onNavigateToChat = { userId ->
                    navController.navigate(Screen.Chat.createRoute(userId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Chat.route,
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "localdeals://app/chat/{userId}" })
        ) { backStackEntry ->
            ChatScreen(
                userId = backStackEntry.arguments?.getString("userId"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OrderRequest.route) { backStackEntry ->
            OrderRequestScreen(
                dealId = backStackEntry.arguments?.getString("dealId"),
                onBack = { navController.popBackStack() },
                onOrderSuccess = {
                    navController.navigate(Screen.MyOrders.route) {
                        popUpTo(Screen.OrderRequest.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.MyOrders.route,
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "localdeals://app/orders" })
        ) { 
            MyOrdersScreen(
                onBack = { navController.popBackStack() },
                onNavigateToReview = { dealId ->
                    navController.navigate(Screen.Review.createRoute(dealId))
                }
            )
        }
        composable(Screen.Review.route) { backStackEntry ->
            ReviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notifications.route) { 
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Seller Dashboard
        composable(Screen.SellerDashboard.route) {
            AuthenticatedRoleGuard(requiredRoles = listOf(Role.SELLER, Role.ADMIN)) {
                SellerDashboardScreen(
                    onNavigateToAddDeal = {
                        navController.navigate(Screen.AddEditDeal.createRoute(null))
                    },
                    onNavigateToOrders = {
                        navController.navigate(Screen.MyOrders.route)
                    },
                    onNavigateToInbox = {
                        navController.navigate(Screen.ChatList.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.StoreProfileEdit.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Screen.StoreProfileEdit.route) {
            AuthenticatedRoleGuard(requiredRoles = listOf(Role.SELLER, Role.ADMIN)) {
                com.localdeals.finder.ui.screens.StoreProfileEditScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = Screen.AddEditDeal.route,
            arguments = listOf(androidx.navigation.navArgument("dealId") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            AuthenticatedRoleGuard(requiredRoles = listOf(Role.SELLER, Role.ADMIN)) {
                AddEditDealScreen(
                    dealId = backStackEntry.arguments?.getString("dealId"),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Admin Panel
        composable(Screen.AdminPanel.route) { 
            AuthenticatedRoleGuard(requiredRoles = listOf(Role.ADMIN)) {
                val adminViewModel: com.localdeals.finder.ui.screens.AdminDashboardViewModel = hiltViewModel()
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        adminViewModel.logout {
                            navController.navigate(Screen.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        // Debug Gallery (only available in DEBUG builds)
        if (com.localdeals.finder.BuildConfig.DEBUG) {
            composable(Screen.ComponentGallery.route) {
                ComponentGalleryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
