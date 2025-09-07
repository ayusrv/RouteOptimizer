package com.routeoptimizer.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.routeoptimizer.app.data.OptimizationType
import com.routeoptimizer.app.ui.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteOptimizerApp() {
    val context = LocalContext.current
    val viewModel: RouteViewModel = viewModel { RouteViewModel(context) }

    val startLocation by viewModel.startLocation
    val destinations = viewModel.destinations
    val optimizedRoute by viewModel.optimizedRoute
    val optimizationType by viewModel.optimizationType
    val isCalculating by viewModel.isCalculating
    val errorMessage by viewModel.errorMessage
    val isGettingLocation by viewModel.isGettingLocation
    val showMap by viewModel.showMap

    var showLocationPicker by remember { mutableStateOf(false) }
    var pickingFor by remember { mutableStateOf("start") }

    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

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
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Start Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Current Location Button
                    OutlinedButton(
                        onClick = {
                            if (locationPermissionsState.allPermissionsGranted) {
                                viewModel.getCurrentLocation()
                            } else {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        enabled = !isGettingLocation
                    ) {
                        if (isGettingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Getting...")
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Current Location",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current")
                        }
                    }
                }

                Button(
                    onClick = {
                        pickingFor = "start"
                        showLocationPicker = true
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
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
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Straighten,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shortest Distance")
                            }
                        },
                        selected = optimizationType == OptimizationType.DISTANCE,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    FilterChip(
                        onClick = { viewModel.setOptimizationType(OptimizationType.TIME) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fastest Time")
                            }
                        },
                        selected = optimizationType == OptimizationType.TIME
                    )
                }

                Text(
                    text = when (optimizationType) {
                        OptimizationType.DISTANCE -> "Minimize total driving distance using real road networks"
                        OptimizationType.TIME -> "Minimize total travel time considering traffic conditions"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Calculate Button
            Button(
                onClick = {
                    viewModel.clearError()
                    viewModel.calculateOptimalRoute()
                },
                modifier = Modifier.weight(1f),
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
                    Icon(Icons.Default.Route, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCalculating) "Calculating..." else "Calculate Route")
            }

            // Show Map Button
            optimizedRoute?.let {
                OutlinedButton(
                    onClick = { viewModel.showMapView() }
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Map")
                }
            }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row {
                            TextButton(
                                onClick = { viewModel.showMapView() }
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = "Show Map",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Map")
                            }

                            TextButton(
                                onClick = { viewModel.clearRoute() }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear Route",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Straighten,
                                contentDescription = "Distance",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", route.totalDistance)} km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.0f", route.totalTime * 60)} min",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = "Stops",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${route.locations.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (route.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Turn-by-Turn Directions:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp).padding(top = 4.dp)
                        ) {
                            itemsIndexed(route.steps) { index, step ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${step.instruction}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${String.format("%.1f", step.distance)} km • ${String.format("%.0f", step.time * 60)} min",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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

    // Map Dialog
    if (showMap && optimizedRoute != null) {
        RouteMapDialog(
            route = optimizedRoute!!,
            onDismiss = { viewModel.hideMapView() }
        )
    }

    // Permission handling
    if (!locationPermissionsState.allPermissionsGranted) {
        LaunchedEffect(locationPermissionsState.shouldShowRationale) {
            if (locationPermissionsState.shouldShowRationale) {
                // Show rationale if needed
            }
        }
    }
}