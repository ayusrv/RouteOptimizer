package com.routeoptimizer.app.optimization

import android.location.Location
import com.routeoptimizer.app.algorithms.AStarPathfinder
import com.routeoptimizer.app.algorithms.AdvancedTSPSolver
import com.routeoptimizer.app.data.OptimizationType
import com.routeoptimizer.app.data.OptimizedRoute
import com.routeoptimizer.app.data.RouteLocation
import com.routeoptimizer.app.data.RouteStep
import com.routeoptimizer.app.network.RoadNetwork
import com.routeoptimizer.app.network.TrafficDataService
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class EnhancedRouteOptimizer(
    private val network: RoadNetwork,
    private val trafficService: TrafficDataService
) {
    private val aStarFinder = AStarPathfinder(network)
    private val tspSolver = AdvancedTSPSolver()

    fun optimizeRouteWithTraffic(
        start: RouteLocation,
        destinations: List<RouteLocation>,
        optimizationType: OptimizationType
    ): OptimizedRoute {
        val allLocations = listOf(start) + destinations
        val n = allLocations.size

        // Build distance matrix with real-time traffic data
        val distanceMatrix = Array(n) { i ->
            DoubleArray(n) { j ->
                if (i == j) 0.0
                else calculateRealTimeDistance(allLocations[i], allLocations[j], optimizationType)
            }
        }

        // Use appropriate TSP solver based on problem size
        val (totalCost, optimalOrder) = if (n <= 12) {
            tspSolver.solveTSPDP(distanceMatrix)
        } else {
            tspSolver.solveTSPGenetic(distanceMatrix, populationSize = 200, generations = 1000)
        }

        // Convert indices back to locations and generate route steps
        val optimalRoute = optimalOrder.map { allLocations[it] }
        val steps = generateDetailedSteps(optimalRoute, optimizationType)

        return OptimizedRoute(
            locations = optimalRoute,
            totalDistance = if (optimizationType == OptimizationType.DISTANCE) totalCost else calculateTotalDistance(optimalRoute),
            totalTime = if (optimizationType == OptimizationType.TIME) totalCost else calculateTotalTime(optimalRoute),
            steps = steps
        )
    }

    private fun calculateRealTimeDistance(
        from: RouteLocation,
        to: RouteLocation,
        optimizationType: OptimizationType
    ): Double {
        val path = aStarFinder.findPath(from.id, to.id)
        if (path.isEmpty()) {
            // Fallback to straight-line distance
            val distance = network.calculateDistance(from, to)
            return when (optimizationType) {
                OptimizationType.DISTANCE -> distance
                OptimizationType.TIME -> distance / 50.0 // Assume 50 km/h
            }
        }

        var totalDistance = 0.0
        var totalTime = 0.0

        for (i in 0 until path.size - 1) {
            val edge = network.getNeighbors(path[i]).find { it.to == path[i + 1] }
            if (edge != null) {
                totalDistance += edge.distance
                val trafficMultiplier = trafficService.getTrafficMultiplier("${path[i]}-${path[i + 1]}")
                totalTime += edge.time * trafficMultiplier
            }
        }

        return when (optimizationType) {
            OptimizationType.DISTANCE -> totalDistance
            OptimizationType.TIME -> totalTime
        }
    }

    private fun calculateTotalDistance(route: List<RouteLocation>): Double {
        var total = 0.0
        for (i in 0 until route.size - 1) {
            total += network.calculateDistance(route[i], route[i + 1])
        }
        return total
    }

    private fun calculateTotalTime(route: List<RouteLocation>): Double {
        var total = 0.0
        for (i in 0 until route.size - 1) {
            val distance = network.calculateDistance(route[i], route[i + 1])
            val baseTime = distance / 60.0 // Assume 60 km/h base speed
            val trafficMultiplier = trafficService.getTrafficMultiplier("${route[i].id}-${route[i + 1].id}")
            total += baseTime * trafficMultiplier
        }
        return total
    }

    private fun generateDetailedSteps(route: List<RouteLocation>, optimizationType: OptimizationType): List<RouteStep> {
        val steps = mutableListOf<RouteStep>()

        for (i in 0 until route.size - 1) {
            val from = route[i]
            val to = route[i + 1]
            val distance = network.calculateDistance(from, to)
            val baseTime = distance / 60.0
            val trafficMultiplier = trafficService.getTrafficMultiplier("${from.id}-${to.id}")
            val time = baseTime * trafficMultiplier

            val instruction = generateDetailedInstruction(from, to, trafficMultiplier)

            steps.add(RouteStep(from, to, distance, time, instruction))
        }

        return steps
    }

    private fun generateDetailedInstruction(from: RouteLocation, to: RouteLocation, trafficMultiplier: Double): String {
        val bearing = calculateBearing(from, to)
        val direction = when {
            bearing < 22.5 || bearing >= 337.5 -> "north"
            bearing < 67.5 -> "northeast"
            bearing < 112.5 -> "east"
            bearing < 157.5 -> "southeast"
            bearing < 202.5 -> "south"
            bearing < 247.5 -> "southwest"
            bearing < 292.5 -> "west"
            else -> "northwest"
        }

        val trafficCondition = when {
            trafficMultiplier < 1.2 -> ""
            trafficMultiplier < 1.5 -> " (light traffic)"
            trafficMultiplier < 2.0 -> " (moderate traffic)"
            else -> " (heavy traffic)"
        }

        return "Head $direction toward ${to.name}$trafficCondition"
    }

    private fun calculateBearing(from: RouteLocation, to: RouteLocation): Double {
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }
}