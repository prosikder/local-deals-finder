package com.localdeals.finder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.*
import com.localdeals.finder.domain.repository.*
import com.localdeals.finder.domain.usecase.DealValidationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditDealState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val dealId: String? = null,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val discount: String = "",
    val stock: String = "",
    val categoryId: String = "",
    val categories: List<Category> = emptyList(),
    val expiryTimeMillis: Long = System.currentTimeMillis() + 86400000,
    val imageUrl: String = ""
)

@HiltViewModel
class AddEditDealViewModel @Inject constructor(
    private val dealRepository: DealRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditDealState())
    val state = _state.asStateFlow()
    
    private val validationUseCase = DealValidationUseCase()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = categoryRepository.getCategories().firstOrNull() ?: emptyList()
            _state.value = _state.value.copy(categories = cats)
            if (cats.isNotEmpty() && _state.value.categoryId.isEmpty()) {
                _state.value = _state.value.copy(categoryId = cats.first().id)
            }
        }
    }

    fun loadDeal(dealId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val deal = dealRepository.getDealById(dealId)
            if (deal != null) {
                _state.value = _state.value.copy(
                    dealId = deal.id,
                    title = deal.title,
                    description = deal.description,
                    price = deal.originalPrice.toString(),
                    discount = deal.discountPercentage.toString(),
                    stock = deal.stock.toString(),
                    categoryId = deal.categoryId,
                    expiryTimeMillis = deal.expirationTimeMillis,
                    imageUrl = deal.imageUrl,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Deal not found")
            }
        }
    }

    fun onTitleChange(value: String) { _state.value = _state.value.copy(title = value) }
    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value) }
    fun onPriceChange(value: String) { _state.value = _state.value.copy(price = value) }
    fun onDiscountChange(value: String) { _state.value = _state.value.copy(discount = value) }
    fun onStockChange(value: String) { _state.value = _state.value.copy(stock = value) }
    fun onCategoryChange(value: String) { _state.value = _state.value.copy(categoryId = value) }
    fun onExpiryChange(value: Long) { _state.value = _state.value.copy(expiryTimeMillis = value) }
    fun onImagePicked(uri: String) { _state.value = _state.value.copy(imageUrl = uri) }

    fun saveDeal() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val user = authRepository.getCurrentUser()
            if (user == null || user.role != Role.SELLER) {
                _state.value = _state.value.copy(isLoading = false, error = "Unauthorized")
                return@launch
            }

            val priceVal = _state.value.price.toDoubleOrNull() ?: 0.0
            val discountVal = _state.value.discount.toIntOrNull() ?: 0
            val stockVal = _state.value.stock.toIntOrNull() ?: 0

            val validation = validationUseCase.execute(
                title = _state.value.title,
                price = priceVal,
                discount = discountVal,
                stock = stockVal,
                expiryTimeMillis = _state.value.expiryTimeMillis
            )

            if (validation.isFailure) {
                _state.value = _state.value.copy(isLoading = false, error = validation.exceptionOrNull()?.message)
                return@launch
            }

            val dealPrice = priceVal * (1 - (discountVal / 100.0))

            val cleanTitle = com.localdeals.finder.util.SanitizationUtil.sanitizeText(_state.value.title, 100)
            val cleanDesc = com.localdeals.finder.util.SanitizationUtil.sanitizeText(_state.value.description, 1000)

            val deal = Deal(
                id = _state.value.dealId ?: UUID.randomUUID().toString(),
                sellerId = user.id,
                title = cleanTitle,
                description = cleanDesc,
                originalPrice = priceVal,
                discountedPrice = dealPrice,
                discountPercentage = discountVal,
                stock = stockVal,
                categoryId = _state.value.categoryId,
                expirationTimeMillis = _state.value.expiryTimeMillis,
                imageUrl = _state.value.imageUrl
            )

            if (_state.value.dealId == null) {
                dealRepository.addDeal(deal)
            } else {
                dealRepository.updateDeal(deal)
            }

            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
        }
    }
}
