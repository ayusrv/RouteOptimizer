package com.routeoptimizer.app.optimization

import com.routeoptimizer.app.data.*
import com.routeoptimizer.app.network.RoadNetwork
import com.routeoptimizer.app.algorithms.AStarPathfinder
import kotlin.math.*
import java.util.*

class RouteOptimizer(private val network: RoadNetwork) {

    // Dijkstra's Algorithm Implementation
    fun findShortestPath(startId: String, endId: String, optimizeFor: OptimizationType): List<String> {
        val distances = mutableMapOf<String, Double>()
        val previous = mutableMapOf<String, String?>()
        val priorityQueue = PriorityQueue<PriorityNode>()
        val visited = mutableSetOf<String>()

        // Initialize distances
        network.getAllLocations().forEach { location ->
            distances[location.id] = Double.MAX_VALUE
            previous[location.id] = null
        }
        distances[startId] = 0.0
        priorityQueue.add(PriorityNode(startId, 0.0))

        while (priorityQueue.isNotEmpty()) {
            val current = priorityQueue.poll()

            if (current.locationId in visited) continue
            visited.add(current.locationId)

            if (current.locationId == endId) break

            network.getNeighbors(current.locationId).forEach { edge ->
                val cost = when (optimizeFor) {
                    OptimizationType.DISTANCE -> edge.distance
                    OptimizationType.TIME -> edge.time * edge.trafficMultiplier
                }

                val newDistance = distances[current.locationId]!! + cost

                if (newDistance < distances[edge.to]!!) {
                    distances[edge.to] = newDistance
                    previous[edge.to] = current.locationId
                    priorityQueue.add(PriorityNode(edge.to, newDistance))
                }
            }
        }

        // Reconstruct path
        val path = mutableListOf<String>()
        var current: String? = endId

        while (current != null) {
            path.add(0, current)
            current = previous[current]
        }

        return if (path.isNotEmpty() && path.first() == startId) path else emptyList()
    }

    // Simplified TSP using Greedy approach
    fun solveTSP(startLocation: RouteLocation, destinations: List<RouteLocation>): OptimizedRoute {
        val allLocations = listOf(startLocation) + destinations
        val n = allLocations.size

        if (n <= 1) {
            return OptimizedRoute(allLocations, 0.0, 0.0, emptyList())
        }

        return solveGreedyTSP(startLocation, destinations)
    }

    private fun solveGreedyTSP(start: RouteLocation, destinations: List<RouteLocation>): OptimizedRoute {
        val unvisited = destinations.toMutableList()
        val route = mutableListOf(start)
        var current = start
        var totalDistance = 0.0
        var totalTime = 0.0
        val steps = mutableListOf<RouteStep>()

        while (unvisited.isNotEmpty()) {
            var nearest = unvisited.first()
            var minDistance = network.calculateDistance(current, nearest)

            unvisited.forEach { destination ->
                val distance = network.calculateDistance(current, destination)
                if (distance < minDistance) {
                    minDistance = distance
                    nearest = destination
                }
            }

            // Create route step
            val estimatedTime = minDistance / 50.0 // Assume 50 km/h average speed
            val instruction = generateInstruction(current, nearest)

            steps.add(RouteStep(current, nearest, minDistance, estimatedTime, instruction))

            totalDistance += minDistance
            totalTime += estimatedTime
            route.add(nearest)
            unvisited.remove(nearest)
            current = nearest
        }

        return OptimizedRoute(route, totalDistance, totalTime, steps)
    }

    private fun generateInstruction(from: RouteLocation, to: RouteLocation): String {
        val bearing = calculateBearing(from, to)
        val direction = when {
            bearing < 45 || bearing >= 315 -> "north"
            bearing < 135 -> "east"
            bearing < 225 -> "south"
            else -> "west"
        }
        return "Head $direction toward ${to.name}"
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

// Priority Queue Node for algorithms
data class PriorityNode(
    val locationId: String,
    val cost: Double
) : Comparable<PriorityNode> {
    override fun compareTo(other: PriorityNode): Int = cost.compareTo(other.cost)
}
