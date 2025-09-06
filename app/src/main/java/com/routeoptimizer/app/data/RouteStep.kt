package com.routeoptimizer.app.data

data class RouteStep(
    val from: RouteLocation,
    val to: RouteLocation,
    val distance: Double,
    val time: Double,
    val instruction: String
)