package com.routeoptimizer.app.api.service

import com.routeoptimizer.app.data.osm.NominatimSearchResponse
import com.routeoptimizer.app.data.osm.OSRMRouteResponse
import com.routeoptimizer.app.data.osm.OSRMTableResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// OSRM API for routing
interface OSRMApiService {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @retrofit2.http.Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("steps") steps: Boolean = true
    ): Response<OSRMRouteResponse>

    @GET("table/v1/driving/{coordinates}")
    suspend fun getDistanceMatrix(
        @retrofit2.http.Path("coordinates") coordinates: String,
        @Query("sources") sources: String? = null,
        @Query("destinations") destinations: String? = null
    ): Response<OSRMTableResponse>
}