package it.cynomys.cfmandroid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Language(
    val code: String,
    val name: String,
    val nativeName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    currentLocale: String,
    onLanguageSelected: (String) -> Unit,
    navController: NavController
) {
    val languages = remember {
        listOf(
            Language("en", "English", "English"),
            Language("it", "Italian", "Italiano"),
            Language("es", "Spanish", "Español"),
            Language("fr", "French", "Français"),
            Language("de", "German", "Deutsch"),
            Language("pt", "Portuguese", "Português"),
            Language("zh", "Chinese", "中文"),
            Language("ja", "Japanese", "日本語"),
            Language("ko", "Korean", "한국어"),
            Language("ar", "Arabic", "العربية"),
            Language("ru", "Russian", "Русский"),
            Language("hi", "Hindi", "हिन्दी")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Language") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header text
            Text(
                text = "Choose your preferred language",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(languages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = currentLocale == language.code,
                        onLanguageClick = {
                            onLanguageSelected(language.code)
                            // Navigate to login after selecting language
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onLanguageClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}