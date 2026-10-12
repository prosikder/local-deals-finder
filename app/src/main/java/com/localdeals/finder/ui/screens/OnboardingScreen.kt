package com.localdeals.finder.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.data.local.DataStoreManager
import com.localdeals.finder.ui.theme.PrimaryOrange
import com.localdeals.finder.ui.theme.SecondaryTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
            onFinished()
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val badge: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlide(
            title = "Discover Deals Near You",
            description = "Explore hot discounts, special retail offers, and restaurant promotions around Dhaka sorted by exact distance.",
            icon = Icons.Default.LocationOn,
            iconColor = PrimaryOrange,
            badge = "Explore & Save"
        ),
        OnboardingSlide(
            title = "Direct Chat & Easy Ordering",
            description = "Chat directly with local verified shopkeepers, request orders without intermediaries, and track order stages seamlessly.",
            icon = Icons.Default.ShoppingCart,
            iconColor = SecondaryTeal,
            badge = "Direct Connection"
        ),
        OnboardingSlide(
            title = "100% Offline & Private",
            description = "Zero external tracking and no cloud leaks. All your browsing, searches, and chats stay safely on your device.",
            icon = Icons.Default.Check,
            iconColor = PrimaryOrange,
            badge = "Safe & Offline"
        )
    )

    fun finish() {
        viewModel.completeOnboarding(onFinished)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local Deals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
                TextButton(
                    onClick = { finish() },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val slide = slides[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = slide.iconColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(140.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = slide.title,
                                    tint = slide.iconColor,
                                    modifier = Modifier.size(68.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = slide.iconColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = slide.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = slide.iconColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Dots Indicator
                Row(
                    modifier = Modifier
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) PrimaryOrange else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                // Bottom Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    if (pagerState.currentPage < 2) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Text(
                                text = "Next",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { finish() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
