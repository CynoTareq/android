package it.cynomys.cfmandroid.auth.signup

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.MainActivity
import it.cynomys.cfmandroid.R
import it.cynomys.cfmandroid.Screen
import it.cynomys.cfmandroid.auth.AuthViewModel
import it.cynomys.cfmandroid.auth.Settings // Import Settings from the auth package


// --- START: Helper Data Classes and Functions for Dropdowns (to ensure code completeness) ---

data class LanguageOption(val value: String, val label: String)
data class CurrencyOption(val value: String, val label: String)
data class UnitOption(val value: String, val label: String)

fun getLanguageOptions(): List<LanguageOption> = listOf(
    LanguageOption(value = "en", label = "English"),
    LanguageOption(value = "es", label = "Español"),
    LanguageOption(value = "fr", label = "Français"),
    LanguageOption(value = "it", label = "Italian"),
    LanguageOption(value = "ca", label = "Catalan")
)

fun getCurrencyOptions(): List<CurrencyOption> = listOf(
    CurrencyOption(value = "USD", label = "US Dollar ($)"),
    CurrencyOption(value = "EUR", label = "Euro (€)"),
    CurrencyOption(value = "GBP", label = "British Pound (£)"),
    CurrencyOption(value = "JPY", label = "Japanese Yen (¥)")
)

fun getUnitOptions(): List<UnitOption> = listOf(
    UnitOption(value = "metric", label = "metric"),
    UnitOption(value = "imperial", label = "imperial")
)


// --- END: Helper Data Classes and Functions ---

// --- START: New Password Validation Logic ---

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val isPasswordValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar
}

fun validatePassword(password: String): PasswordValidationState {
    return PasswordValidationState(
        hasMinLength = password.length >= 8,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasNumber = password.any { it.isDigit() },
        hasSpecialChar = password.any { it in "!@#\$%^&*(),.?\":{}|<>".toList() }
    )
}

@Composable
fun PasswordRequirement(text: String, isMet: Boolean) {
    val color = if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error // Green or Red
    val icon = if (isMet) Icons.Filled.Check else Icons.Filled.Close

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// --- END: New Password Validation Logic ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(viewModel: AuthViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    // We will use the detailed password validation state instead of a simple error string for the main password
    var passwordValidationState by remember { mutableStateOf(PasswordValidationState()) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) } // Error for confirmation field

    val isLoading by viewModel.isLoading.collectAsState()
    val isError by viewModel.isError.collectAsState()

    // Settings Dropdown States
    val languageOptions = remember { getLanguageOptions() }
    val currencyOptions = remember { getCurrencyOptions() }
    val unitOptions = remember { getUnitOptions() }

    // Set initial values to the first option
    var selectedLanguage by remember { mutableStateOf(languageOptions.first()) }
    var selectedCurrency by remember { mutableStateOf(currencyOptions.first()) }
    var selectedUnit by remember { mutableStateOf(unitOptions.first()) }

    var languageExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }


    val context = androidx.compose.ui.platform.LocalContext.current
    val genericActivity = context.findActivity()
    val activity = genericActivity as? MainActivity


    LaunchedEffect(Unit) {
        // Collect the SharedFlow of single-time events
        viewModel.signupEvents.collect { isSuccess ->
            if (isSuccess) {
                // This code runs only ONCE when the event is emitted.
                navController.navigate(Screen.Login.route) {
                    // Clear the back stack to prevent going back to signup
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }
            }
        }
    }
    val config = LocalConfiguration.current
    val locale = config.locales[0]

    Text(
        text = "DEBUG LOCALE: ${locale.language}",
        color = Color.Red
    )




    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            stringResource(R.string.create1) ,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text = stringResource(R.string.create2),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    "Invalid email format"
                } else null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = { if (emailError != null) Text(emailError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordValidationState = validatePassword(it)
                // Also check confirmation password on primary password change
                confirmPasswordError = if (confirmPassword.isNotBlank() && confirmPassword != it) "Passwords do not match" else null
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Password Requirements Display
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)) {
            Text("Password must contain:", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            PasswordRequirement("At least 8 characters", passwordValidationState.hasMinLength)
            PasswordRequirement("An uppercase letter", passwordValidationState.hasUppercase)
            PasswordRequirement("A lowercase letter", passwordValidationState.hasLowercase)
            PasswordRequirement("A number (0-9)", passwordValidationState.hasNumber)
            PasswordRequirement("A special character (!@#\$...)", passwordValidationState.hasSpecialChar)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = if (confirmPassword != password) "Passwords do not match" else null
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            isError = confirmPasswordError != null,
            supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        // --- Preferences Section ---

        Text(
            "Account Preferences",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        // Language Dropdown
        ExposedDropdownMenuBox(
            expanded = languageExpanded,
            onExpandedChange = { languageExpanded = !languageExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLanguage.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Preferred Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = languageExpanded,
                onDismissRequest = { languageExpanded = false }
            ) {
                languageOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedLanguage = option
                            languageExpanded = false
                            activity?.setAppLocale(option.value)

                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Currency Dropdown
        ExposedDropdownMenuBox(
            expanded = currencyExpanded,
            onExpandedChange = { currencyExpanded = !currencyExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCurrency.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = currencyExpanded,
                onDismissRequest = { currencyExpanded = false }
            ) {
                currencyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedCurrency = option
                            currencyExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Unit Dropdown
        ExposedDropdownMenuBox(
            expanded = unitExpanded,
            onExpandedChange = { unitExpanded = !unitExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedUnit.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit of Measurement") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = unitExpanded,
                onDismissRequest = { unitExpanded = false }
            ) {
                unitOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedUnit = option
                            unitExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = {
                // Final validation check before calling ViewModel
                emailError = if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    "Invalid email format"
                } else null

                // Ensure all password validation requirements are met
                val isPasswordGloballyValid = passwordValidationState.isPasswordValid

                // Check for empty password separately before checking equality,
                // as passwordValidationState assumes the password is being typed.
                confirmPasswordError = if (password.isBlank()) {
                    "Password cannot be empty"
                } else if (!isPasswordGloballyValid) {
                    // Prevent submission if password requirements aren't met
                    "Password does not meet all requirements"
                } else if (password != confirmPassword) {
                    "Passwords do not match"
                } else {
                    null
                }

                if (emailError == null && confirmPasswordError == null && isPasswordGloballyValid) {
                    // --- CONSTRUCT SETTINGS OBJECT ---
                    val settings = Settings(
                        language = selectedLanguage.value,
                        currency = selectedCurrency.value,
                        unit = selectedUnit.value,
                        hasAverage = true // Defaulting to true
                    )
                    // CALL SIGNUP OWNER WITH SETTINGS
                    viewModel.signupOwner(name, email, password, settings)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // Button is enabled only if loading is false, email is valid, passwords match, and all password requirements are met
            enabled = !isLoading && emailError == null && confirmPasswordError == null && passwordValidationState.isPasswordValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Sign Up")
            }
        }

        if (isError) {
            Text(
                text = "Signup failed. Please try again.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(Screen.Login.route) } // Corrected route usage
        ) {
            Text("Already have an account? Login")
        }
    }
}
// Helper function (put outside Composable functions in SignupScreen.kt)
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper){
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}