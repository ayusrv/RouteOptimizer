package com.routeoptimizer.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routeoptimizer.app.data.OptimizationType
import com.routeoptimizer.app.ui.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizerApp() {
    val viewModel: RouteViewModel = viewModel()
    val startLocation by viewModel.startLocation
    val destinations = viewModel.destinations
    val optimizedRoute by viewModel.optimizedRoute
    val optimizationType by viewModel.optimizationType
    val isCalculating by viewModel.isCalculating
    val errorMessage by viewModel.errorMessage

    var showLocationPicker by remember { mutableStateOf(false) }
    var pickingFor by remember { mutableStateOf("start") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Route Optimizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Start Location Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Start Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = {
                        pickingFor = "start"
                        showLocationPicker = true
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(startLocation?.name ?: "Select Start Location")
                }

                startLocation?.let { location ->
                    Text(
                        text = "Lat: ${String.format("%.6f", location.latitude)}, " +
                                "Lon: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Destinations Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Destinations (${destinations.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = {
                            pickingFor = "destination"
                            showLocationPicker = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Destination")
                    }
                }

                if (destinations.isEmpty()) {
                    Text(
                        text = "No destinations added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        itemsIndexed(destinations) { index, destination ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}. ${destination.name}",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${String.format("%.6f", destination.latitude)}, " +
                                                "${String.format("%.6f", destination.longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeDestination(index) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Optimization Options
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Optimization Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    FilterChip(
                        onClick = { viewModel.setOptimizationType(OptimizationType.DISTANCE) },
                        label = { Text("Shortest Distance") },
                        selected = optimizationType == OptimizationType.DISTANCE,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    FilterChip(
                        onClick = { viewModel.setOptimizationType(OptimizationType.TIME) },
                        label = { Text("Fastest Time") },
                        selected = optimizationType == OptimizationType.TIME
                    )
                }

                Text(
                    text = when (optimizationType) {
                        OptimizationType.DISTANCE -> "Minimize total driving distance"
                        OptimizationType.TIME -> "Minimize total travel time (considers traffic)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Calculate Button
        Button(
            onClick = {
                viewModel.clearError()
                viewModel.calculateOptimalRoute()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = startLocation != null && destinations.isNotEmpty() && !isCalculating
        ) {
            if (isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isCalculating) "Calculating Route..." else "Calculate Optimal Route")
        }

        // Results Section
        optimizedRoute?.let { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Optimized Route Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", route.totalDistance)} km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.0f", route.totalTime * 60)} min",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = "Stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${route.locations.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (route.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Route Steps:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp).padding(top = 4.dp)
                        ) {
                            itemsIndexed(route.steps) { index, step ->
                                Text(
                                    text = "${index + 1}. ${step.instruction}\n" +
                                            "   ${String.format("%.1f", step.distance)} km, " +
                                            "${String.format("%.0f", step.time * 60)} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Location Picker Dialog
    if (showLocationPicker) {
        LocationPickerDialog(
            locations = viewModel.getAllAvailableLocations(),
            onLocationSelected = { location ->
                when (pickingFor) {
                    "start" -> viewModel.setStartLocation(location)
                    "destination" -> viewModel.addDestination(location)
                }
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false },
            viewModel = viewModel
        )
    }
}