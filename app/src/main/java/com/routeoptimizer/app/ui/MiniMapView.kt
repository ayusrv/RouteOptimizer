package com.routeoptimizer.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.routeoptimizer.app.data.RouteLocation
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MiniMapView(
    locations: List<RouteLocation>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = "RouteOptimizer/1.0"

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false) // Disable interaction for mini map

                setupMiniMap(this, locations)
            }
        },
        modifier = modifier
    )
}

private fun setupMiniMap(mapView: MapView, locations: List<RouteLocation>) {
    val mapController: IMapController = mapView.controller

    if (locations.isEmpty()) return

    // Clear existing overlays
    mapView.overlays.clear()

    // Add simple markers
    locations.forEach { location ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(location.latitude, location.longitude)
            title = location.name
        }
        mapView.overlays.add(marker)
    }

    // Center on first location or show all
    if (locations.size == 1) {
        mapController.setCenter(GeoPoint(locations.first().latitude, locations.first().longitude))
        mapController.setZoom(15.0)
    } else {
        val bounds = org.osmdroid.util.BoundingBox.fromGeoPointsSafe(
            locations.map { GeoPoint(it.latitude, it.longitude) }
        )
        mapView.post {
            mapView.zoomToBoundingBox(bounds, true, 50)
        }
    }
}