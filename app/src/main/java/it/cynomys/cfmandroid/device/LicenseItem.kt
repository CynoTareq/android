package it.cynomys.cfmandroid.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LicenseItem(
    license: License,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine card colors based on 'inUse' status
    val cardContainerColor = if (license.inUse)
        MaterialTheme.colorScheme.surface // Use standard surface color for uniform design
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Fixed height for uniform size
            .clickable(enabled = !license.inUse) { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor // Apply uniform card color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Reduced elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            // Align contents to the top so we can align IN USE to the top right corner
            verticalAlignment = Alignment.Top
        ) {
            // Radio Button is centered
            RadioButton(
                selected = isSelected,
                onClick = { if (!license.inUse) onSelect() },
                enabled = !license.inUse,
                modifier = Modifier.align(Alignment.CenterVertically) // Center Radio Button
            )

            // Text Column is centered and takes remaining width
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically) // Center Text Column
            ) {
                Text(
                    text = license.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    // MODIFIED: Removed maxLines/overflow to show full text
                    color = if (license.inUse)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Type: ${license.type}",
                    style = MaterialTheme.typography.labelMedium,
                    // MODIFIED: Removed maxLines/overflow to show full text
                    color = if (license.inUse)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Status Tag / Icon is right-aligned
            if (license.inUse) {
                Text(
                    text = "IN USE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(top = 2.dp) // Adjusted top padding
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp) // MODIFIED: Thinner padding
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterVertically) // Keep icon centered
                )
            }
        }
    }
}