package com.example.drivesafer

import android.content.Context
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Object berisi fungsi utilitas untuk aplikasi DriveSafer
 */
object Utils {
    // Format dua angka di belakang koma
    val decimalFormat = DecimalFormat("0.00")

    // Format tanggal
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    /**
     * Menghitung jarak antara dua koordinat GPS
     */
    fun getDistanceBetween(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        // Validate coordinates
        if (!isValidLatitude(lat1) || !isValidLatitude(lat2)) {
            return 0.0
        }
        if (!isValidLongitude(long1) || !isValidLongitude(long2)) {
            return 0.0
        }

        val result = FloatArray(1)
        Location.distanceBetween(lat1, long1, lat2, long2, result)
        return result[0].toDouble()
    }

    // ADD VALIDATION FUNCTIONS HERE - setelah getDistanceBetween()
    /**
     * Validasi latitude (-90 to 90)
     */
    private fun isValidLatitude(latitude: Double): Boolean {
        return latitude in -90.0..90.0
    }

    /**
     * Validasi longitude (-180 to 180)
     */
    private fun isValidLongitude(longitude: Double): Boolean {
        return longitude in -180.0..180.0
    }

    /**
     * Menghitung kecepatan berdasarkan jarak dan waktu
     */
    fun calculateSpeed(distance: Double, timeSeconds: Long): Double {
        return when {
            timeSeconds <= 0 -> 0.0
            distance < 0 -> 0.0
            distance > 1000 -> 0.0 // Sanity check: max 1km per calculation
            else -> distance / timeSeconds
        }
    }

    /**
     * Mendapatkan tanggal saat ini dalam format "dd-MM-yyyy"
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Konversi kecepatan dari m/s ke km/h
     */
    fun msToKmh(speedMs: Double): Double = speedMs * 3.6

    /**
     * Konversi kecepatan dari km/h ke m/s
     */
    fun kmhToMs(speedKmh: Double): Double = speedKmh / 3.6
}