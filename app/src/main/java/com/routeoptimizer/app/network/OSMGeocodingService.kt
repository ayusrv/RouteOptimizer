package com.routeoptimizer.app.network

import com.routeoptimizer.app.api.service.NominatimApiService
import com.routeoptimizer.app.data.RouteLocation

// Implementation classes
class OSMGeocodingService(private val api: NominatimApiService) {

    suspend fun searchLocations(query: String): List<RouteLocation> {
        return try {
            val response = api.searchLocation(query)
            if (response.isSuccessful) {
                response.body()?.map { result ->
                    RouteLocation(
                        id = result.placeId,
                        name = result.displayName,
                        latitude = result.lat.toDouble(),
                        longitude = result.lon.toDouble()
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLocationName(latitude: Double, longitude: Double): String? {
        return try {
            val response = api.reverseGeocode(latitude, longitude)
            if (response.isSuccessful) {
                response.body()?.displayName
            } else null
        } catch (e: Exception) {
            null
        }
    }
}