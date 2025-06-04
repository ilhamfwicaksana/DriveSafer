package com.example.drivesafer.model

import java.util.*

/**
 * Data class untuk mencatat peristiwa mengemudi
 */
data class DrivingData(
    val type: String,          // Jenis pelanggaran: "Acceleration", "Brake", "Turn", "Speeding"
    val xValue: Float,         // Nilai sensor X
    val yValue: Float,         // Nilai sensor Y
    val zValue: Float,         // Nilai sensor Z
    val speed: Float,          // Kecepatan saat ini (m/s)
    val timestamp: Long,       // Timestamp kejadian
    val latitude: Double,      // Koordinat latitude
    val longitude: Double,     // Koordinat longitude
    val date: String,          // Tanggal dalam format string
    val noiseLevel: Float = 0f // Level Kebisingan dalam desibel
) {
    companion object {
        const val TYPE_ACCELERATION = "Acceleration"
        const val TYPE_BRAKE = "Brake"
        const val TYPE_TURN = "Turn"
        const val TYPE_SPEEDING = "Speeding"
        const val TYPE_NOISE = "Noise"
    }

    // Format timestamp menjadi string waktu yang dapat dibaca
    fun getTimeString(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return String.format(
            "%02d:%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    // Mendapatkan kecepatan dalam km/h
    fun getSpeedKmh(): Float {
        return speed * 3.6f
    }

    // Mendapatkan Level Noise
    fun getNoiseDescription(): String {
        return when {
            noiseLevel < 60 -> "Quiet"
            noiseLevel < 75 -> "Normal"
            noiseLevel < 85 -> "Loud"
            noiseLevel < 95 -> "Very Loud"
            else -> "Excessive"
        }
    }
}