package com.routeoptimizer.app.data.osm

import com.google.gson.annotations.SerializedName

// Nominatim API models for geocoding
data class NominatimSearchResponse(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("licence") val licence: String,
    @SerializedName("osm_type") val osmType: String,
    @SerializedName("osm_id") val osmId: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("class") val osmClass: String,
    @SerializedName("type") val type: String,
    @SerializedName("importance") val importance: Double?
)

