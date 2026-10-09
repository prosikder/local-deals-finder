package com.localdeals.finder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
// Removed StarBorder import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("How was your experience?", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.Center) {
                        for (i in 1..5) {
                            IconButton(onClick = { viewModel.updateRating(i) }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= uiState.rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = { viewModel.updateComment(it) },
                        label = { Text("Write a comment (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 5
                    )
                    
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { viewModel.submitReview() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}
