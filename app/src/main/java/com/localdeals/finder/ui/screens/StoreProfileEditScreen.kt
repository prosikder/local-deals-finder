package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.domain.repository.AuthRepository
import com.localdeals.finder.domain.repository.LocationRepository
import com.localdeals.finder.domain.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreProfileState(
    val businessName: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val success: Boolean = false,
    val originalSeller: Seller? = null
)

@HiltViewModel
class StoreProfileEditViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sellerRepository: SellerRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StoreProfileState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null || user.role != com.localdeals.finder.domain.model.Role.SELLER) {
                _state.value = _state.value.copy(isLoading = false, error = "Unauthorized")
                return@launch
            }
            val seller = sellerRepository.getSellerById(user.id)
            if (seller != null) {
                _state.value = StoreProfileState(
                    businessName = seller.businessName,
                    description = seller.description,
                    phoneNumber = seller.phoneNumber,
                    address = seller.address,
                    latitude = seller.latitude,
                    longitude = seller.longitude,
                    isLoading = false,
                    originalSeller = seller
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Seller not found")
            }
        }
    }

    fun updateField(field: String, value: String) {
        val s = _state.value
        _state.value = when (field) {
            "name" -> s.copy(businessName = value)
            "desc" -> s.copy(description = value)
            "phone" -> s.copy(phoneNumber = value)
            "address" -> s.copy(address = value)
            else -> s
        }
    }

    fun setLocationToCurrent() {
        viewModelScope.launch {
            val loc = locationRepository.locationFlow.firstOrNull()
            if (loc != null) {
                _state.value = _state.value.copy(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    address = "Lat: ${"%.4f".format(loc.latitude)}, Lng: ${"%.4f".format(loc.longitude)}"
                )
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val s = _state.value
            if (s.businessName.isBlank()) {
                _state.value = s.copy(error = "Business name cannot be empty")
                return@launch
            }
            if (s.phoneNumber.isBlank()) {
                _state.value = s.copy(error = "Phone number cannot be empty")
                return@launch
            }

            val seller = s.originalSeller?.copy(
                businessName = s.businessName,
                description = s.description,
                phoneNumber = s.phoneNumber,
                address = s.address,
                latitude = s.latitude,
                longitude = s.longitude
            )
            
            if (seller != null) {
                sellerRepository.updateSeller(seller)
                _state.value = s.copy(success = true, error = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProfileEditScreen(
    onBack: () -> Unit,
    viewModel: StoreProfileEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.success) {
        LaunchedEffect(Unit) { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Store Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.businessName,
                    onValueChange = { viewModel.updateField("name", it) },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateField("desc", it) },
                    label = { Text("Store Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { viewModel.updateField("phone", it) },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Store Location", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.updateField("address", it) },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = { viewModel.setLocationToCurrent() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pin to Current Location")
                }

                if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
