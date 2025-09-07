package com.routeoptimizer.app.optimization

import com.routeoptimizer.app.algorithms.AdvancedTSPSolver
import com.routeoptimizer.app.data.*
import com.routeoptimizer.app.network.NetworkClient
import com.routeoptimizer.app.network.OSMRoutingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow

class RealTimeRouteOptimizer {

    private val tspSolver = AdvancedTSPSolver()
    private val routingService = NetworkClient.routingService
    private val geocodingService = NetworkClient.geocodingService

    suspend fun optimizeRouteWithOSM(
        start: RouteLocation,
        destinations: List<RouteLocation>,
        optimizationType: OptimizationType
    ): OptimizedRoute = withContext(Dispatchers.IO) {

        val allLocations = listOf(start) + destinations
        val n = allLocations.size

        // Get real distance/time matrix from OSRM
        val matrix = when (optimizationType) {
            OptimizationType.DISTANCE -> routingService.getDistanceMatrix(allLocations)
            OptimizationType.TIME -> routingService.getTimeMatrix(allLocations)
        }

        // Solve TSP with real data
        val (totalCost, optimalOrder) = if (n <= 12) {
            tspSolver.solveTSPDP(matrix)
        } else {
            tspSolver.solveTSPGenetic(
                matrix,
                populationSize = 200,
                generations = 1000
            )
        }

        // Convert indices back to locations
        val optimalRoute = optimalOrder.map { allLocations[it] }

        // Generate detailed route steps using OSRM
        val steps = generateRealRouteSteps(optimalRoute)

        // Calculate actual totals
        val totalDistance = calculateActualDistance(optimalRoute)
        val totalTime = calculateActualTime(optimalRoute)

        OptimizedRoute(
            locations = optimalRoute,
            totalDistance = totalDistance,
            totalTime = totalTime,
            steps = steps
        )
    }

    private suspend fun generateRealRouteSteps(route: List<RouteLocation>): List<RouteStep> {
        val steps = mutableListOf<RouteStep>()

        for (i in 0 until route.size - 1) {
            val from = route[i]
            val to = route[i + 1]

            // Get detailed route from OSRM
            val osrmResponse = routingService.calculateRoute(listOf(from, to))

            if (osrmResponse?.routes?.isNotEmpty() == true) {
                val osrmRoute = osrmResponse.routes.first()
                val distance = osrmRoute.distance / 1000.0 // Convert to km
                val time = osrmRoute.duration / 3600.0 // Convert to hours

                // Extract turn-by-turn instructions
                val instruction = if (osrmRoute.legs.isNotEmpty() &&
                    osrmRoute.legs.first().steps.isNotEmpty()) {
                    val firstStep = osrmRoute.legs.first().steps.first()
                    firstStep.maneuver.instruction ?: "Head toward ${to.name}"
                } else {
                    "Head toward ${to.name}"
                }

                steps.add(RouteStep(from, to, distance, time, instruction))
            } else {
                // Fallback if OSRM fails
                val fallbackDistance = calculateHaversineDistance(from, to)
                val fallbackTime = fallbackDistance / 60.0 // Assume 60 km/h
                steps.add(RouteStep(
                    from, to, fallbackDistance, fallbackTime,
                    "Head toward ${to.name}"
                ))
            }
        }

        return steps
    }

    private suspend fun calculateActualDistance(route: List<RouteLocation>): Double {
        var totalDistance = 0.0

        for (i in 0 until route.size - 1) {
            val osrmResponse = routingService.calculateRoute(
                listOf(route[i], route[i + 1])
            )

            if (osrmResponse?.routes?.isNotEmpty() == true) {
                totalDistance += osrmResponse.routes.first().distance / 1000.0
            } else {
                totalDistance += calculateHaversineDistance(route[i], route[i + 1])
            }
        }

        return totalDistance
    }

    private suspend fun calculateActualTime(route: List<RouteLocation>): Double {
        var totalTime = 0.0

        for (i in 0 until route.size - 1) {
            val osrmResponse = routingService.calculateRoute(
                listOf(route[i], route[i + 1])
            )

            if (osrmResponse?.routes?.isNotEmpty() == true) {
                totalTime += osrmResponse.routes.first().duration / 3600.0
            } else {
                totalTime += calculateHaversineDistance(route[i], route[i + 1]) / 60.0
            }
        }

        return totalTime
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

    suspend fun searchLocations(query: String): List<RouteLocation> {
        return geocodingService.searchLocations(query)
    }
}