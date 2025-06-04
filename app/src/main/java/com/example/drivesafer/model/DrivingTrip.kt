package com.example.drivesafer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.drivesafer.util.DateConverter
import java.util.*

/**
 * Entity untuk menyimpan data riwayat perjalanan
 */
@Entity(tableName = "driving_trips")
data class DrivingTrip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val duration: Long,  // dalam milidetik
    val finalScore: Int,
    val accelerationCount: Int,
    val brakeCount: Int,
    val turnCount: Int,
    val speedingCount: Int,
    val noiseCount: Int = 0,
    val notes: String = ""  // Catatan opsional tentang perjalanan
) {
    /**
     * Mengubah durasi dari milidetik menjadi format yang mudah dibaca
     */
    fun getFormattedDuration(): String {
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = (duration % 60000) / 1000

        return when {
            hours > 0 -> String.format("%dh %dm %ds", hours, minutes, seconds)
            minutes > 0 -> String.format("%dm %ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
}