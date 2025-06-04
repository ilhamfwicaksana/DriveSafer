package com.example.drivesafer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan GPS points selama perjalanan
 */
@Entity(tableName = "trip_route_points")
data class RoutePoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float,
    val accuracy: Float = 0f
)