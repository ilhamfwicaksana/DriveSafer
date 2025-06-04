package com.example.drivesafer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan detail lokasi pelanggaran
 */
@Entity(tableName = "violation_details")
data class ViolationDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val type: String, // "Acceleration", "Brake", "Turn", "Speeding", "Noise"
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val severity: Float, // Sensor value atau noise level
    val speed: Float = 0f
) {
    companion object {
        const val TYPE_ACCELERATION = "Acceleration"
        const val TYPE_BRAKE = "Brake"
        const val TYPE_TURN = "Turn"
        const val TYPE_SPEEDING = "Speeding"
        const val TYPE_NOISE = "Noise"
    }

    fun getViolationColor(): Int {
        return when (type) {
            TYPE_ACCELERATION -> 0xFF2196F3.toInt() // Blue
            TYPE_BRAKE -> 0xFFF44336.toInt() // Red
            TYPE_TURN -> 0xFF4CAF50.toInt() // Green
            TYPE_SPEEDING -> 0xFFFF9800.toInt() // Orange
            TYPE_NOISE -> 0xFF9C27B0.toInt() // Purple
            else -> 0xFF757575.toInt() // Gray
        }
    }
}