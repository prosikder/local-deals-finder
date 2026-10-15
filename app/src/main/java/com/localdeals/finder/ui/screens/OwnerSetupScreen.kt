package com.localdeals.finder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.ui.components.PrimaryButton

@Composable
fun OwnerSetupScreen(
    onSetupSuccess: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OwnerSetupViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("System Administrator") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is OwnerSetupState.Success) {
            onSetupSuccess()
        }
    }

    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigitOrSpecial = password.any { it.isDigit() || !it.isLetterOrDigit() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Admin Shield",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Owner Setup / অ্যাডমিন সেটআপ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your primary administrative owner account. This one-time setup ensures full ownership of your app with zero hardcoded passwords in release mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Admin Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Admin Email (e.g. admin@yourdomain.com)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Strong Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(if (confirmPasswordVisible) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password requirements checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .padding(12.dp)
        ) {
            Text(
                text = "Password Requirements:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            RequirementRow(text = "At least 8 characters", isMet = hasMinLength)
            RequirementRow(text = "At least 1 uppercase letter (A-Z)", isMet = hasUpper)
            RequirementRow(text = "At least 1 lowercase letter (a-z)", isMet = hasLower)
            RequirementRow(text = "At least 1 number or special character", isMet = hasDigitOrSpecial)
            RequirementRow(text = "Passwords match", isMet = passwordsMatch)
        }

        if (state is OwnerSetupState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (state as OwnerSetupState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state is OwnerSetupState.Loading) {
            CircularProgressIndicator()
        } else {
            PrimaryButton(
                text = "Create Owner Account",
                onClick = {
                    viewModel.setupOwner(name, email, password, confirmPassword)
                },
                enabled = hasMinLength && hasUpper && hasLower && hasDigitOrSpecial && passwordsMatch && email.isNotBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onSkip) {
                Text("Skip for Now (Browse as Guest)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RequirementRow(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Clear,
            contentDescription = null,
            tint = if (isMet) Color(0xFF10B981) else MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
