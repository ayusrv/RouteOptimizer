package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

// OSRM API models for routing
data class OSRMRouteResponse(
    @SerializedName("code") val code: String,
    @SerializedName("routes") val routes: List<OSRMRoute>,
    @SerializedName("waypoints") val waypoints: List<OSRMWaypoint>?
)