package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

// Table API for distance matrices
data class OSRMTableResponse(
    @SerializedName("code") val code: String,
    @SerializedName("durations") val durations: List<List<Double?>>,
    @SerializedName("distances") val distances: List<List<Double?>>,
    @SerializedName("sources") val sources: List<OSRMWaypoint>?,
    @SerializedName("destinations") val destinations: List<OSRMWaypoint>?
)