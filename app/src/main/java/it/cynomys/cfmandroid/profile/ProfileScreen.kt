package it.cynomys.cfmandroid.profile

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.cynomys.cfmandroid.auth.AuthViewModel
import it.cynomys.cfmandroid.auth.Settings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// Define data classes for dropdown options
data class LanguageOption(val value: String, val label: String)
data class CurrencyOption(val value: String, val label: String)
data class UnitOption(val value: String, val label: String)


// Constants for dropdowns
val languages: List<LanguageOption> = listOf(
    LanguageOption(value = "en", label = "English"),
    LanguageOption(value = "es", label = "Español"),
    LanguageOption(value = "fr", label = "Français"),
    LanguageOption(value = "it", label = "Italian"),
    LanguageOption(value = "ca", label = "Catalan")
)

val currencies: List<CurrencyOption> = listOf(
    CurrencyOption(value = "USD", label = "US Dollar ($)"),
    CurrencyOption(value = "EUR", label = "Euro (€)"),
    CurrencyOption(value = "GBP", label = "British Pound (£)"),
    CurrencyOption(value = "JPY", label = "Japanese Yen (¥)")
)

// You might want to define units similarly if they are predefined
val units: List<UnitOption> = listOf(
    UnitOption(value = "metric", label = "metric"),
    UnitOption(value = "imperial", label = "imperial") // Corrected typo here
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    ownerId: UUID,
    onNavigateBack: () -> Unit,
    // ADDED: AuthViewModel for logout functionality
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val owner by profileViewModel.owner.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isError by profileViewModel.isError.collectAsState()
    // ADDED: Observe user session state to navigate after logout
    val userSession by authViewModel.userSession.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    // Form states
    var name by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf<Date?>(null) }
    var language by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // State for dropdown menus
    var languageExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }


    // Fetch settings when the screen loads
    LaunchedEffect(ownerId) {
        profileViewModel.fetchOwnerSettings(ownerId)
    }

    // ADDED: Effect to navigate back when userSession becomes null (i.e., after logout)
    LaunchedEffect(userSession) {
        if (userSession == null) {
            // Navigate back, which should lead to the login/auth screen
            onNavigateBack()
        }
    }

    // Update UI states when owner data changes
    LaunchedEffect(owner) {
        owner?.let { ownerData ->
            Log.d("ProfileScreen", "Owner data received: $ownerData")
            Log.d("ProfileScreen", "Settings: ${ownerData.settings}")

            name = ownerData.name
            birthday = ownerData.birthday
            language = ownerData.settings?.language ?: ""
            currency = ownerData.settings?.currency ?: ""
            unit = ownerData.settings?.unit?.trim() ?: "" // Apply trim here

            Log.d("ProfileScreen", "Updated UI states - Language: $language, Currency: $currency, Unit: $unit")
        }
    }

    // Reset form when edit mode is cancelled
    fun resetForm() {
        owner?.let { ownerData ->
            Log.d("ProfileScreen", "Resetting form with owner data: ${ownerData.settings}")
            name = ownerData.name
            birthday = ownerData.birthday
            language = ownerData.settings?.language ?: ""
            currency = ownerData.settings?.currency ?: ""
            unit = ownerData.settings?.unit?.trim() ?: "" // Apply trim here
            Log.d("ProfileScreen", "Reset complete - Language: $language, Currency: $currency, Unit: $unit")
        } ?: Log.w("ProfileScreen", "Cannot reset form - owner data is null")
    }

    fun saveChanges() {
        val updatedSettings = Settings(
            language = language,
            currency = currency,
            unit = unit,
            hasAverage = owner?.settings?.hasAverage ?: false
        )

        val updatedOwner = owner?.copy(
            name = name,
            email = owner!!.email,
            birthday = birthday,
            settings = updatedSettings
        ) ?: return

        profileViewModel.updateOwnerSettings(ownerId, updatedOwner)
        isEditMode = false
    }

    // ADDED: Logout function
    fun logout() {
        authViewModel.logoutOwner()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ADDED: Logout Icon is always available when not loading
                    if (!isLoading) {
                        IconButton(onClick = { logout() }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }

                    // Original Edit Icon is available only when not in edit mode and not loading
                    if (!isEditMode && !isLoading) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            resetForm()
                            isEditMode = false
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    FloatingActionButton(
                        onClick = { saveChanges() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading profile...")
                }
            } else if (isError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Failed to load profile. Please try again.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Personal Information Section
                ProfileSection(
                    title = "Personal Information",
                    content = {
                        ProfileField(
                            label = "Name",
                            value = name,
                            onValueChange = { name = it },
                            icon = Icons.Default.Person,
                            isEditMode = isEditMode
                        )

                        ProfileField(
                            label = "Email",
                            value = owner?.email ?: "",
                            onValueChange = { },
                            icon = Icons.Default.Email,
                            isEditMode = false, // Email is never editable
                            keyboardType = KeyboardType.Email
                        )

                        // Birthday field with date picker
// Replace the birthday field section in your code with this:

// Birthday field with date picker
                        if (isEditMode) {
                            OutlinedTextField(
                                value = birthday?.let { dateFormatter.format(it) } ?: "Select Birthday",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Birthday") },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Birthday") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val calendar = Calendar.getInstance()
                                        birthday?.let { calendar.time = it }

                                        DatePickerDialog(
                                            context,
                                            { _: DatePicker, year: Int, month: Int, day: Int ->
                                                birthday = Calendar.getInstance().apply {
                                                    set(year, month, day)
                                                }.time
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                enabled = false
                            )
                        } else {
                            ProfileDisplayField(
                                label = "Birthday",
                                value = birthday?.let { dateFormatter.format(it) } ?: "Not set",
                                icon = Icons.Default.DateRange
                            )
                        }
                    }
                )

                // Settings Section
                ProfileSection(
                    title = "Preferences",
                    content = {
                        if (isEditMode) {
                            // Language Dropdown
                            ExposedDropdownMenuBox(
                                expanded = languageExpanded,
                                onExpandedChange = { languageExpanded = !languageExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = languages.find { it.value == language }?.label ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Language") },
                                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Language") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = languageExpanded,
                                    onDismissRequest = { languageExpanded = false }
                                ) {
                                    languages.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                language = option.value
                                                languageExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp)) // Add spacing between dropdowns

                            // Currency Dropdown
                            ExposedDropdownMenuBox(
                                expanded = currencyExpanded,
                                onExpandedChange = { currencyExpanded = !currencyExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currencies.find { it.value == currency }?.label ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Currency") },
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Currency") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = currencyExpanded,
                                    onDismissRequest = { currencyExpanded = false }
                                ) {
                                    currencies.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                currency = option.value
                                                currencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // Add spacing between dropdowns

                            // Unit Dropdown (example, if you have predefined units)
                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = !unitExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = units.find { it.value == unit }?.label ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unit") },
                                    leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = "Unit") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false }
                                ) {
                                    units.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                unit = option.value.trim() // Apply trim here
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }


                        } else {
                            ProfileDisplayField(
                                label = "Language",
                                value = languages.find { it.value == language }?.label ?: "Not set",
                                icon = Icons.Default.Language
                            )

                            ProfileDisplayField(
                                label = "Currency",
                                value = currencies.find { it.value == currency }?.label ?: "Not set",
                                icon = Icons.Default.AttachMoney
                            )

                            ProfileDisplayField(
                                label = "Unit",
                                value = units.find { it.value == unit }?.label ?: "Not set",
                                icon = Icons.Default.SquareFoot
                            )
                        }
                    }
                )

                // Add bottom spacing for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEditMode: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    if (isEditMode) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = label) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        ProfileDisplayField(
            label = label,
            value = value.ifEmpty { "Not set" },
            icon = icon
        )
    }
}

@Composable
private fun ProfileDisplayField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}