package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName
data class OSRMRoute(
    @SerializedName("geometry") val geometry: String,
    @SerializedName("legs") val legs: List<OSRMLeg>,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("weight_name") val weightName: String?,
    @SerializedName("weight") val weight: Double?
)