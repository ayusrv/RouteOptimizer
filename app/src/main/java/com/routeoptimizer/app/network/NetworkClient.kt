package com.routeoptimizer.app.network

import com.routeoptimizer.app.api.service.NominatimApiService
import com.routeoptimizer.app.api.service.OSRMApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "RouteOptimizer/1.0")
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val nominatimRetrofit = Retrofit.Builder()
        .baseUrl(NOMINATIM_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val osrmRetrofit = Retrofit.Builder()
        .baseUrl(OSRM_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val nominatimApi: NominatimApiService = nominatimRetrofit.create(NominatimApiService::class.java)
    val osrmApi: OSRMApiService = osrmRetrofit.create(OSRMApiService::class.java)

    // Service instances
    val geocodingService = OSMGeocodingService(nominatimApi)
    val routingService = OSMRoutingService(osrmApi)
}