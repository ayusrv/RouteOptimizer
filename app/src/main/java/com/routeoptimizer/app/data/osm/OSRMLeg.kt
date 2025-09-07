package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

data class OSRMLeg(
    @SerializedName("steps") val steps: List<OSRMStep>,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("summary") val summary: String?,
    @SerializedName("weight") val weight: Double?
)