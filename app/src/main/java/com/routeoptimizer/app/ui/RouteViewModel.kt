package com.routeoptimizer.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.routeoptimizer.app.data.*
import com.routeoptimizer.app.network.RoadNetwork
import com.routeoptimizer.app.optimization.RouteOptimizer
import kotlinx.coroutines.*
import kotlin.random.Random

class RouteViewModel : ViewModel() {
    private val network = RoadNetwork()
    private val optimizer = RouteOptimizer(network)

    private val _destinations = mutableStateListOf<RouteLocation>()
    val destinations: List<RouteLocation> = _destinations

    private val _startLocation = mutableStateOf<RouteLocation?>(null)
    val startLocation = _startLocation

    private val _optimizedRoute = mutableStateOf<OptimizedRoute?>(null)
    val optimizedRoute = _optimizedRoute

    private val _optimizationType = mutableStateOf(OptimizationType.DISTANCE)
    val optimizationType = _optimizationType

    private val _isCalculating = mutableStateOf(false)
    val isCalculating = _isCalculating

    init {
        initializeNetwork()
    }

    private fun initializeNetwork() {
        // Sample locations for demo
        val locations = listOf(
            RouteLocation(name = "Downtown", latitude = 40.7128, longitude = -74.0060),
            RouteLocation(name = "Airport", latitude = 40.6892, longitude = -74.1745),
            RouteLocation(name = "Mall", latitude = 40.7589, longitude = -73.9851),
            RouteLocation(name = "Hospital", latitude = 40.7282, longitude = -73.7949),
            RouteLocation(name = "University", latitude = 40.8176, longitude = -73.7781),
            RouteLocation(name = "Stadium", latitude = 40.8296, longitude = -73.9262),
            RouteLocation(name = "Beach", latitude = 40.5795, longitude = -73.9707),
            RouteLocation(name = "Park", latitude = 40.7794, longitude = -73.9632)
        )

        locations.forEach { location ->
            network.addLocation(location)
        }

        // Add sample edges (roads) between locations
        locations.forEach { from ->
            locations.forEach { to ->
                if (from != to) {
                    val distance = network.calculateDistance(from, to)
                    val time = distance / 60.0 // Assume 60 km/h average
                    val trafficMultiplier = 0.8 + (1.5 - 0.8) * Random.nextDouble()
                    network.addEdge(Edge(from.id, to.id, distance, time, trafficMultiplier))
                }
            }
        }
    }

    fun setStartLocation(location: RouteLocation) {
        _startLocation.value = location
    }

    fun addDestination(location: RouteLocation) {
        if (!_destinations.contains(location) && location != _startLocation.value) {
            _destinations.add(location)
        }
    }

    fun removeDestination(index: Int) {
        if (index in _destinations.indices) {
            _destinations.removeAt(index)
        }
    }

    fun setOptimizationType(type: OptimizationType) {
        _optimizationType.value = type
    }

    fun calculateOptimalRoute() {
        val start = _startLocation.value ?: return
        if (_destinations.isEmpty()) return

        _isCalculating.value = true

        // Simulate calculation delay
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val route = optimizer.solveTSP(start, _destinations.toList())
            withContext(Dispatchers.Main) {
                _optimizedRoute.value = route
                _isCalculating.value = false
            }
        }
    }

    fun clearRoute() {
        _optimizedRoute.value = null
    }

    fun getAllAvailableLocations(): List<RouteLocation> = network.getAllLocations()
}