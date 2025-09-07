package com.routeoptimizer.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.routeoptimizer.app.data.OptimizedRoute
import com.routeoptimizer.app.data.RouteLocation
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun RouteMapDialog(
    route: OptimizedRoute,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Route Map",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Route Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.1f", route.totalDistance)} km",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Distance",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.0f", route.totalTime * 60)} min",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${route.locations.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Stops",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Map
                AndroidView(
                    factory = { ctx ->
                        // Configure OSMDroid - Fixed references
                        Configuration.getInstance().userAgentValue = "RouteOptimizer/1.0"

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            // Setup route visualization directly here
                            setupMapWithRoute(this, route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                )
            }
        }
    }
}

// Fixed setupRouteVisualization function
private fun setupMapWithRoute(mapView: MapView, route: OptimizedRoute) {
    val mapController: IMapController = mapView.controller

    if (route.locations.isEmpty()) return

    // Clear existing overlays
    mapView.overlays.clear()

    // Add markers for each location
    route.locations.forEachIndexed { index, location ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(location.latitude, location.longitude)
            title = when (index) {
                0 -> "Start: ${location.name}"
                route.locations.size - 1 -> "End: ${location.name}"
                else -> "Stop ${index}: ${location.name}"
            }

            // Set anchor point for marker
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
    }

    // Add polyline connecting all locations
    val polyline = Polyline().apply {
        val points = route.locations.map { location ->
            GeoPoint(location.latitude, location.longitude)
        }
        setPoints(points)
        outlinePaint.color = android.graphics.Color.BLUE
        outlinePaint.strokeWidth = 8f
    }
    mapView.overlays.add(polyline)

    // Center map on route
    if (route.locations.isNotEmpty()) {
        val bounds = org.osmdroid.util.BoundingBox.fromGeoPointsSafe(
            route.locations.map { GeoPoint(it.latitude, it.longitude) }
        )
        mapView.post {
            mapView.zoomToBoundingBox(bounds, true, 100)
        }
    }

    // Refresh map
    mapView.invalidate()
}