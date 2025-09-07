package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

data class OSRMStep(
    @SerializedName("geometry") val geometry: String,
    @SerializedName("maneuver") val maneuver: OSRMManeuver,
    @SerializedName("mode") val mode: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("name") val name: String?,
    @SerializedName("weight") val weight: Double?
)