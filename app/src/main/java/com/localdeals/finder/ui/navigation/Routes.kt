package com.localdeals.finder.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object LocationPermission : Screen("location_permission")
    object Home : Screen("home")
    object Map : Screen("map")
    object Search : Screen("search")
    object Category : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }
    object DealDetails : Screen("deal_details/{dealId}") {
        fun createRoute(dealId: String) = "deal_details/$dealId"
    }
    object SellerStore : Screen("seller_store/{sellerId}") {
        fun createRoute(sellerId: String) = "seller_store/$sellerId"
    }
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    object OrderRequest : Screen("order_request/{dealId}") {
        fun createRoute(dealId: String) = "order_request/$dealId"
    }
    object MyOrders : Screen("my_orders")
    object Review : Screen("review/{dealId}") {
        fun createRoute(dealId: String) = "review/$dealId"
    }
    object Wishlist : Screen("wishlist")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object SellerDashboard : Screen("seller_dashboard")
    object AddEditDeal : Screen("add_edit_deal?dealId={dealId}") {
        fun createRoute(dealId: String?) = if (dealId != null) "add_edit_deal?dealId=$dealId" else "add_edit_deal"
    }
    object StoreProfileEdit : Screen("store_profile_edit")
    object AdminPanel : Screen("admin_panel")
    object OwnerSetup : Screen("owner_setup")

    object ComponentGallery : Screen("component_gallery")
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Search,
    Screen.Wishlist,
    Screen.Profile
)
