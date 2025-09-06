package com.routeoptimizer.app.algorithms

import com.routeoptimizer.app.data.RouteLocation
import com.routeoptimizer.app.data.OptimizationType
import com.routeoptimizer.app.network.RoadNetwork
import java.util.*
import kotlin.math.*

// Priority Queue Node for Dijkstra's Algorithm
data class PriorityNode(
    val locationId: String,
    val cost: Double
) : Comparable<PriorityNode> {
    override fun compareTo(other: PriorityNode): Int = cost.compareTo(other.cost)
}

// A* Algorithm Implementation
class AStarPathfinder(private val network: RoadNetwork) {

    data class AStarNode(
        val locationId: String,
        val gScore: Double, // Cost from start
        val fScore: Double, // gScore + heuristic
        val parent: String? = null
    ) : Comparable<AStarNode> {
        override fun compareTo(other: AStarNode): Int = fScore.compareTo(other.fScore)
    }

    fun findPath(startId: String, goalId: String): List<String> {
        val openSet = PriorityQueue<AStarNode>()
        val closedSet = mutableSetOf<String>()
        val gScores = mutableMapOf<String, Double>()
        val parents = mutableMapOf<String, String?>()

        val startLocation = network.getLocation(startId) ?: return emptyList()
        val goalLocation = network.getLocation(goalId) ?: return emptyList()

        gScores[startId] = 0.0
        openSet.add(AStarNode(startId, 0.0, heuristic(startLocation, goalLocation)))

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.locationId == goalId) {
                return reconstructPath(parents, goalId)
            }

            closedSet.add(current.locationId)

            network.getNeighbors(current.locationId).forEach { edge ->
                if (edge.to in closedSet) return@forEach

                val tentativeGScore = gScores[current.locationId]!! + edge.distance

                if (tentativeGScore < (gScores[edge.to] ?: Double.MAX_VALUE)) {
                    parents[edge.to] = current.locationId
                    gScores[edge.to] = tentativeGScore

                    val neighborLocation = network.getLocation(edge.to)!!
                    val fScore = tentativeGScore + heuristic(neighborLocation, goalLocation)

                    openSet.add(AStarNode(edge.to, tentativeGScore, fScore, current.locationId))
                }
            }
        }

        return emptyList()
    }

    private fun heuristic(from: RouteLocation, to: RouteLocation): Double {
        return network.calculateDistance(from, to)
    }

    private fun reconstructPath(parents: Map<String, String?>, goalId: String): List<String> {
        val path = mutableListOf<String>()
        var current: String? = goalId

        while (current != null) {
            path.add(0, current)
            current = parents[current]
        }

        return path
    }
}