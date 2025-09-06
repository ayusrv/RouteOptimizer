package com.routeoptimizer.app.data

import java.util.UUID

data class RouteLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double
)