package com.routeoptimizer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routeoptimizer.app.data.OptimizationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizerApp() {
    val viewModel: RouteViewModel = viewModel()
    val startLocation by viewModel.startLocation
    val destinations = viewModel.destinations
    val optimizedRoute by viewModel.optimizedRoute
    val optimizationType by viewModel.optimizationType
    val isCalculating by viewModel.isCalculating

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
                            Text(
                                text = "${index + 1}. ${destination.name}",
                                modifier = Modifier.weight(1f)
                            )

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

        // Optimization Options
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Optimization",
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
            }
        }

        // Calculate Button
        Button(
            onClick = { viewModel.calculateOptimalRoute() },
            modifier = Modifier.fillMaxWidth(),
            enabled = startLocation != null && destinations.isNotEmpty() && !isCalculating
        ) {
            if (isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isCalculating) "Calculating..." else "Calculate Optimal Route")
        }

        // Results Section
        optimizedRoute?.let { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Optimized Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total Distance: ${String.format("%.1f", route.totalDistance)} km",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Estimated Time: ${String.format("%.1f", route.totalTime * 60)} minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Route Steps:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        itemsIndexed(route.steps) { index, step ->
                            Text(
                                text = "${index + 1}. ${step.instruction} (${String.format("%.1f", step.distance)} km)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
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
            onDismiss = { showLocationPicker = false }
        )
    }
}