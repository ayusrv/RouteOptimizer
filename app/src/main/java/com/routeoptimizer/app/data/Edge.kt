package com.routeoptimizer.app.data

data class Edge(
    val from: String,
    val to: String,
    val distance: Double,
    val time: Double,
    val trafficMultiplier: Double = 1.0
)