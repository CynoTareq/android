package it.cynomys.cfmandroid.farm
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun FarmListView(
    farms: List<Farm>,
    isLoading: Boolean,
    onFarmSelected: (UUID) -> Unit,
    onEdit: (Farm) -> Unit,
    onDelete: (Farm) -> Unit
) {
    if (farms.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "No Farms",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No farms found\nAdd your first farm!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 96.dp
            )
        ) {
            items(farms) { farm ->
                FarmItem(
                    farm = farm,
                    onClick = { onFarmSelected(farm.id!!) },
                    onEdit = { onEdit(farm) },
                    onDelete = { onDelete(farm) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}