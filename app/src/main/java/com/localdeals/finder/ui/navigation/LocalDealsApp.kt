package com.localdeals.finder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun LocalDealsApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val bottomNavVisible = currentRoute in BottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (bottomNavVisible) {
                LocalDealsBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun LocalDealsBottomBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
        BottomNavItem(Screen.Map, "Map", Icons.Default.LocationOn),
        BottomNavItem(Screen.Search, "Search", Icons.Default.Search),
        BottomNavItem(Screen.Wishlist, "Wishlist", Icons.Default.Favorite),
        BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)
