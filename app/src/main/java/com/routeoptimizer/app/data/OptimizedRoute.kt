package com.routeoptimizer.app.data

data class OptimizedRoute(
    val locations: List<RouteLocation>,
    val totalDistance: Double,
    val totalTime: Double,
    val steps: List<RouteStep>
)