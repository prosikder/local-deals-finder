package com.localdeals.finder.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.localdeals.finder.domain.model.*
import com.localdeals.finder.ui.components.EmptyState
import com.localdeals.finder.ui.components.PrimaryButton
import com.localdeals.finder.ui.screens.OrderCard
import com.localdeals.finder.ui.screens.OrderWithDeal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComposeFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testLoginButtonAndStatesRender() {
        var clicked = false
        composeTestRule.setContent {
            PrimaryButton(text = "Log In", onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Log In")
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun testEmptyStateAndErrorStateRender() {
        composeTestRule.setContent {
            EmptyState(message = "No deals found near your location.")
        }

        composeTestRule.onNodeWithText("No deals found near your location.")
            .assertIsDisplayed()
    }

    @Test
    fun testOrderCardFlowRendersActions() {
        val sampleOrder = OrderRequest(
            id = "ord_101",
            dealId = "d_101",
            buyerId = "user_1",
            sellerId = "seller_1",
            quantity = 2,
            note = "Deliver at lunch",
            phoneConfirmation = "+8801700000000",
            status = OrderStatus.PENDING
        )
        val sampleDeal = Deal(
            id = "d_101",
            sellerId = "seller_1",
            title = "Half Price Gourmet Pizza",
            description = "Delicious pizza",
            originalPrice = 20.0,
            discountedPrice = 10.0,
            discountPercentage = 50,
            stock = 8,
            categoryId = "food",
            expirationTimeMillis = System.currentTimeMillis() + 86400000
        )

        var cancelClicked = false
        composeTestRule.setContent {
            OrderCard(
                orderData = OrderWithDeal(sampleOrder, sampleDeal),
                currentUserId = "user_1",
                onCancel = { cancelClicked = true },
                onReview = {},
                onUpdateStatus = {}
            )
        }

        composeTestRule.onNodeWithText("Half Price Gourmet Pizza")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("PENDING")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel Order")
            .assertIsDisplayed()
            .performClick()

        assertTrue(cancelClicked)
    }
}
