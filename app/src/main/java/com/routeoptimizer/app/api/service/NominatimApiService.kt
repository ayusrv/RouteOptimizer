package com.routeoptimizer.app.api.service

import com.routeoptimizer.app.data.osm.NominatimSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Nominatim API for geocoding (converting addresses to coordinates)
interface NominatimApiService {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("addressdetails") addressDetails: Int = 1
    ): Response<List<NominatimSearchResponse>>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): Response<NominatimSearchResponse>
}