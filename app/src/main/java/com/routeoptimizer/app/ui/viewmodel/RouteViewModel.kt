package com.routeoptimizer.app.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeoptimizer.app.data.*
import com.routeoptimizer.app.network.NetworkClient
import com.routeoptimizer.app.optimization.RealTimeRouteOptimizer
import kotlinx.coroutines.*
import kotlin.math.pow

class RouteViewModel : ViewModel() {
    private val realTimeOptimizer = RealTimeRouteOptimizer()
    private val geocodingService = NetworkClient.geocodingService

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

    private val _searchResults = mutableStateOf<List<RouteLocation>>(emptyList())
    val searchResults = _searchResults

    private val _isSearching = mutableStateOf(false)
    val isSearching = _isSearching

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage

    // Sample locations for fallback
    private val sampleLocations = listOf(
        RouteLocation(name = "Downtown", latitude = 40.7128, longitude = -74.0060),
        RouteLocation(name = "Airport", latitude = 40.6892, longitude = -74.1745),
        RouteLocation(name = "Mall", latitude = 40.7589, longitude = -73.9851),
        RouteLocation(name = "Hospital", latitude = 40.7282, longitude = -73.7949),
        RouteLocation(name = "University", latitude = 40.8176, longitude = -73.7781),
        RouteLocation(name = "Stadium", latitude = 40.8296, longitude = -73.9262),
        RouteLocation(name = "Beach", latitude = 40.5795, longitude = -73.9707),
        RouteLocation(name = "Park", latitude = 40.7794, longitude = -73.9632)
    )

    fun setStartLocation(location: RouteLocation) {
        _startLocation.value = location
        clearError()
    }

    fun addDestination(location: RouteLocation) {
        if (!_destinations.contains(location) && location != _startLocation.value) {
            _destinations.add(location)
            clearError()
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
        val start = _startLocation.value ?: run {
            _errorMessage.value = "Please select a start location"
            return
        }

        if (_destinations.isEmpty()) {
            _errorMessage.value = "Please add at least one destination"
            return
        }

        _isCalculating.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val route = realTimeOptimizer.optimizeRouteWithOSM(
                    start,
                    _destinations.toList(),
                    _optimizationType.value
                )
                _optimizedRoute.value = route
            } catch (e: Exception) {
                _errorMessage.value = "Failed to calculate route: ${e.message}"
                // Fallback to basic calculation if network fails
                try {
                    val fallbackRoute = calculateFallbackRoute(start, _destinations.toList())
                    _optimizedRoute.value = fallbackRoute
                } catch (fallbackError: Exception) {
                    _errorMessage.value = "Route calculation failed completely"
                }
            } finally {
                _isCalculating.value = false
            }
        }
    }

    fun searchLocations(query: String) {
        if (query.isBlank()) {
            _searchResults.value = sampleLocations
            return
        }

        _isSearching.value = true

        viewModelScope.launch {
            try {
                val results = realTimeOptimizer.searchLocations(query)
                _searchResults.value = if (results.isEmpty()) {
                    // Fallback to sample locations if no results
                    sampleLocations.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                } else {
                    results
                }
            } catch (e: Exception) {
                // Fallback to sample locations on error
                _searchResults.value = sampleLocations.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearRoute() {
        _optimizedRoute.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getAllAvailableLocations(): List<RouteLocation> = sampleLocations

    private suspend fun calculateFallbackRoute(
        start: RouteLocation,
        destinations: List<RouteLocation>
    ): OptimizedRoute = withContext(Dispatchers.Default) {

        // Simple greedy TSP for fallback
        val unvisited = destinations.toMutableList()
        val route = mutableListOf(start)
        var current = start
        var totalDistance = 0.0
        var totalTime = 0.0
        val steps = mutableListOf<RouteStep>()

        while (unvisited.isNotEmpty()) {
            var nearest = unvisited.first()
            var minDistance = calculateHaversineDistance(current, nearest)

            unvisited.forEach { destination ->
                val distance = calculateHaversineDistance(current, destination)
                if (distance < minDistance) {
                    minDistance = distance
                    nearest = destination
                }
            }

            val estimatedTime = minDistance / 50.0 // Assume 50 km/h
            val instruction = "Head toward ${nearest.name}"

            steps.add(RouteStep(current, nearest, minDistance, estimatedTime, instruction))

            totalDistance += minDistance
            totalTime += estimatedTime
            route.add(nearest)
            unvisited.remove(nearest)
            current = nearest
        }

        OptimizedRoute(route, totalDistance, totalTime, steps)
    }

    private fun calculateHaversineDistance(loc1: RouteLocation, loc2: RouteLocation): Double {
        val R = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = kotlin.math.sin(dLat / 2).pow(2) +
                kotlin.math.cos(Math.toRadians(loc1.latitude)) *
                kotlin.math.cos(Math.toRadians(loc2.latitude)) *
                kotlin.math.sin(dLon / 2).pow(2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }
}