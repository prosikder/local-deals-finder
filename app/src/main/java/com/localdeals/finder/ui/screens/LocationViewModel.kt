package com.localdeals.finder.ui.screens

import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.CurrentLocation
import com.localdeals.finder.domain.repository.LocationRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<CurrentLocation?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.locationFlow.collect { loc ->
                _currentLocation.value = loc
            }
        }
    }

    fun startTracking() {
        locationRepository.startLocationUpdates()
    }

    fun stopTracking() {
        locationRepository.stopLocationUpdates()
    }

    fun fetchFreshLocation() {
        viewModelScope.launch {
            val loc = locationRepository.getFreshLocation()
            if (loc != null) {
                // flow should auto-update if tracking is on, but we can rely on startTracking
                // locationRepository.getFreshLocation returns a location but the flow handles UI
            }
        }
    }

    fun saveManualLocation(location: CurrentLocation) {
        viewModelScope.launch {
            locationRepository.setManualLocation(location)
        }
    }
}
