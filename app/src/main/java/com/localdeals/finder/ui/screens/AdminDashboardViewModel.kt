package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import com.localdeals.finder.domain.usecase.*
import com.localdeals.finder.ui.BaseViewModel
import com.localdeals.finder.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val stats: AdminStats? = null,
    val users: List<User> = emptyList(),
    val sellers: List<Seller> = emptyList(),
    val deals: List<Deal> = emptyList(),
    val categories: List<Category> = emptyList(),
    val reportedReviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getAdminAnalyticsUseCase: GetAdminAnalyticsUseCase,
    private val manageUsersUseCase: ManageUsersUseCase,
    private val manageSellersUseCase: ManageSellersUseCase,
    private val moderateReviewUseCase: ModerateReviewUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val authRepository: AuthRepository,
    private val sellerRepository: SellerRepository,
    private val dealRepository: DealRepository,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                getAdminAnalyticsUseCase().collectLatest { stats ->
                    _state.value = _state.value.copy(stats = stats, isLoading = false)
                }
            }
            launch {
                authRepository.getUsers().collectLatest { users ->
                    _state.value = _state.value.copy(users = users)
                }
            }
            launch {
                sellerRepository.getSellers().collectLatest { sellers ->
                    _state.value = _state.value.copy(sellers = sellers)
                }
            }
            launch {
                dealRepository.getDeals().collectLatest { deals ->
                    _state.value = _state.value.copy(deals = deals)
                }
            }
            launch {
                categoryRepository.getCategories().collectLatest { cats ->
                    _state.value = _state.value.copy(categories = cats)
                }
            }
            launch {
                reviewRepository.getReportedReviews().collectLatest { reviews ->
                    _state.value = _state.value.copy(reportedReviews = reviews)
                }
            }
        }
    }

    fun toggleUserBlock(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            if (isBlocked) manageUsersUseCase.blockUser(userId)
            else manageUsersUseCase.unblockUser(userId)
        }
    }

    fun toggleSellerSuspend(sellerId: String, suspend: Boolean) {
        viewModelScope.launch {
            if (suspend) manageSellersUseCase.suspendSeller(sellerId)
            else manageSellersUseCase.approveSeller(sellerId)
        }
    }

    fun removeDeal(dealId: String) {
        viewModelScope.launch {
            dealRepository.deleteDeal(dealId)
        }
    }

    fun dismissReviewReport(reviewId: String) {
        viewModelScope.launch {
            moderateReviewUseCase.dismissReport(reviewId)
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            moderateReviewUseCase.deleteReview(reviewId)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.addCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            deleteCategoryUseCase(categoryId)
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLogoutComplete()
        }
    }
}
