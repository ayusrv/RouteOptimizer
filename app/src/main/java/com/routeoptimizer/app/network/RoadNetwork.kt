package com.routeoptimizer.app.network

import com.routeoptimizer.app.data.RouteLocation
import com.routeoptimizer.app.data.Edge
import kotlin.math.*

class RoadNetwork {
    private val adjacencyList = mutableMapOf<String, MutableList<Edge>>()
    private val locations = mutableMapOf<String, RouteLocation>()

    fun addLocation(location: RouteLocation) {
        locations[location.id] = location
        adjacencyList.putIfAbsent(location.id, mutableListOf())
    }

    fun addEdge(edge: Edge) {
        adjacencyList[edge.from]?.add(edge)
        // Add reverse edge for bidirectional roads
        val reverseEdge = Edge(edge.to, edge.from, edge.distance, edge.time, edge.trafficMultiplier)
        adjacencyList[edge.to]?.add(reverseEdge)
    }

    fun getNeighbors(locationId: String): List<Edge> {
        return adjacencyList[locationId] ?: emptyList()
    }

    fun getLocation(id: String): RouteLocation? = locations[id]

    fun getAllLocations(): List<RouteLocation> = locations.values.toList()

    // Calculate distance between two points using Haversine formula
    fun calculateDistance(loc1: RouteLocation, loc2: RouteLocation): Double {
        val R = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(loc1.latitude)) *
                cos(Math.toRadians(loc2.latitude)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}