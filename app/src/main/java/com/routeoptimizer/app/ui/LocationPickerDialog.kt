package com.routeoptimizer.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.routeoptimizer.app.data.RouteLocation

@Composable
fun LocationPickerDialog(
    locations: List<RouteLocation>,
    onLocationSelected: (RouteLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            LazyColumn {
                items(locations.size) { index ->
                    val location = locations[index]
                    TextButton(
                        onClick = { onLocationSelected(location) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = location.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}