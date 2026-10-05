package com.localdeals.finder.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<T> : ViewModel() {
    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun emitLoading() {
        _uiState.value = UiState.Loading
    }

    protected fun emitSuccess(data: T) {
        _uiState.value = UiState.Success(data)
    }

    protected fun emitEmpty() {
        _uiState.value = UiState.Empty
    }

    protected fun emitError(message: String) {
        _uiState.value = UiState.Error(message)
    }
}
