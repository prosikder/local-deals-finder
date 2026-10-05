package com.localdeals.finder.ui

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    object Empty : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}
