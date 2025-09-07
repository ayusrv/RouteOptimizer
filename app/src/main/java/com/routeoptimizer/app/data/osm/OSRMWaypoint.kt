package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

data class OSRMWaypoint(
    @SerializedName("hint") val hint: String?,
    @SerializedName("distance") val distance: Double?,
    @SerializedName("name") val name: String?,
    @SerializedName("location") val location: List<Double>
)