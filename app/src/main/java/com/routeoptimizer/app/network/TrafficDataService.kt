package com.routeoptimizer.app.network

import kotlin.random.Random
class TrafficDataService {

    data class TrafficCondition(
        val roadId: String,
        val congestionLevel: Double, // 0.0 to 1.0
        val averageSpeed: Double, // km/h
        val incidents: List<TrafficIncident> = emptyList()
    )

    data class TrafficIncident(
        val type: IncidentType,
        val severity: Severity,
        val description: String,
        val estimatedClearTime: Long // milliseconds
    )

    enum class IncidentType {
        ACCIDENT, CONSTRUCTION, ROAD_CLOSURE, WEATHER
    }

    enum class Severity {
        LOW, MODERATE, HIGH, SEVERE
    }

    // Mock traffic data - in production, this would fetch from real APIs
    fun getTrafficConditions(roadIds: List<String>): Map<String, TrafficCondition> {
        return roadIds.associateWith { roadId ->
            TrafficCondition(
                roadId = roadId,
                congestionLevel = 0.0 + (1.0 - 0.0) * Random.nextDouble(),
                averageSpeed = (20..80).random().toDouble(),
                incidents = if ((0..10).random() == 0) {
                    listOf(
                        TrafficIncident(
                            type = IncidentType.values().random(),
                            severity = Severity.values().random(),
                            description = "Traffic incident on $roadId",
                            estimatedClearTime = System.currentTimeMillis() + (10..120).random() * 60000L
                        )
                    )
                } else emptyList()
            )
        }
    }

    fun getTrafficMultiplier(roadId: String): Double {
        val condition = getTrafficConditions(listOf(roadId))[roadId]
        return condition?.let { 1.0 + it.congestionLevel * 2.0 } ?: 1.0
    }
}