package com.routeoptimizer.app.network

import com.routeoptimizer.app.api.service.OSRMApiService
import com.routeoptimizer.app.data.RouteLocation
import com.routeoptimizer.app.data.osm.OSRMRouteResponse

class OSMRoutingService(private val api: OSRMApiService) {

    suspend fun calculateRoute(waypoints: List<RouteLocation>): OSRMRouteResponse? {
        if (waypoints.size < 2) return null

        val coordinates = waypoints.joinToString(";") { "${it.longitude},${it.latitude}" }

        return try {
            val response = api.getRoute(coordinates)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDistanceMatrix(locations: List<RouteLocation>): Array<DoubleArray> {
        if (locations.isEmpty()) return emptyArray()

        val coordinates = locations.joinToString(";") { "${it.longitude},${it.latitude}" }

        return try {
            val response = api.getDistanceMatrix(coordinates)
            if (response.isSuccessful) {
                val tableResponse = response.body()
                val distances = tableResponse?.distances

                if (distances != null) {
                    Array(locations.size) { i ->
                        DoubleArray(locations.size) { j ->
                            distances[i][j]?.div(1000.0) ?: Double.MAX_VALUE // Convert to km
                        }
                    }
                } else {
                    createFallbackMatrix(locations)
                }
            } else {
                createFallbackMatrix(locations)
            }
        } catch (e: Exception) {
            createFallbackMatrix(locations)
        }
    }

    suspend fun getTimeMatrix(locations: List<RouteLocation>): Array<DoubleArray> {
        if (locations.isEmpty()) return emptyArray()

        val coordinates = locations.joinToString(";") { "${it.longitude},${it.latitude}" }

        return try {
            val response = api.getDistanceMatrix(coordinates)
            if (response.isSuccessful) {
                val tableResponse = response.body()
                val durations = tableResponse?.durations

                if (durations != null) {
                    Array(locations.size) { i ->
                        DoubleArray(locations.size) { j ->
                            durations[i][j]?.div(3600.0) ?: Double.MAX_VALUE // Convert to hours
                        }
                    }
                } else {
                    createFallbackTimeMatrix(locations)
                }
            } else {
                createFallbackTimeMatrix(locations)
            }
        } catch (e: Exception) {
            createFallbackTimeMatrix(locations)
        }
    }

    private fun createFallbackMatrix(locations: List<RouteLocation>): Array<DoubleArray> {
        val network = RoadNetwork()
        return Array(locations.size) { i ->
            DoubleArray(locations.size) { j ->
                if (i == j) 0.0
                else network.calculateDistance(locations[i], locations[j])
            }
        }
    }

    private fun createFallbackTimeMatrix(locations: List<RouteLocation>): Array<DoubleArray> {
        val network = RoadNetwork()
        return Array(locations.size) { i ->
            DoubleArray(locations.size) { j ->
                if (i == j) 0.0
                else network.calculateDistance(locations[i], locations[j]) / 60.0 // Assume 60 km/h
            }
        }
    }
}