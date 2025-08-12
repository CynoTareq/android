package it.cynomys.cfmandroid.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.view.common.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerLayout(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Hour",
                style = MaterialTheme.typography.labelMedium
            )

            NumberPicker(
                value = hour,
                onValueChange = onHourChange,
                range = 0..23,
                modifier = Modifier.width(80.dp)
            )
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Minute picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Minute",
                style = MaterialTheme.typography.labelMedium
            )

            NumberPicker(
                value = minute,
                onValueChange = onMinuteChange,
                range = 0..59,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}
