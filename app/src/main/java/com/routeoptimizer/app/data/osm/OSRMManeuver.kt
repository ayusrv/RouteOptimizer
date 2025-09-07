package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

data class OSRMManeuver(
    @SerializedName("bearing_after") val bearingAfter: Int?,
    @SerializedName("bearing_before") val bearingBefore: Int?,
    @SerializedName("location") val location: List<Double>,
    @SerializedName("modifier") val modifier: String?,
    @SerializedName("type") val type: String,
    @SerializedName("instruction") val instruction: String?
)