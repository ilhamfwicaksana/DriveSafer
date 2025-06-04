package com.example.drivesafer.data

import com.example.drivesafer.model.DrivingTrip
import kotlinx.coroutines.flow.Flow
import java.util.*
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail

/**
 * Repository untuk mengakses dan mengelola data DrivingTrip
 * Bertindak sebagai lapisan abstraksi antara ViewModel dan sumber data (Room Database)
 */
class DrivingTripRepository(
    private val drivingTripDao: DrivingTripDao,
    private val routePointDao: RoutePointDao,
    private val violationDetailDao: ViolationDetailDao
) {

    // Mendapatkan semua perjalanan, diurutkan dari yang terbaru
    val allTrips: Flow<List<DrivingTrip>> = drivingTripDao.getAllTrips()

    // Mendapatkan rata-rata skor dari semua perjalanan
    val averageScore: Flow<Float> = drivingTripDao.getAverageScore()

    // Mendapatkan jumlah total perjalanan
    val tripCount: Flow<Int> = drivingTripDao.getTripCount()

    // Mendapatkan total waktu berkendara (dalam milidetik)
    val totalDrivingTime: Flow<Long?> = drivingTripDao.getTotalDrivingTime()

    /**
     * Menyimpan perjalanan baru ke database
     * @return Result dengan ID dari perjalanan yang disimpan atau error
     */
    suspend fun insertTrip(trip: DrivingTrip): Result<Long> {
        return try {
            // Validate trip data before insert
            if (trip.duration <= 0) {
                return Result.failure(IllegalArgumentException("Trip duration must be positive"))
            }
            if (trip.finalScore < 0 || trip.finalScore > 100) {
                return Result.failure(IllegalArgumentException("Score must be between 0-100"))
            }

            val id = drivingTripDao.insertTrip(trip)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memperbarui data perjalanan yang sudah ada
     * @return Result sukses atau error
     */
    suspend fun updateTrip(trip: DrivingTrip): Result<Unit> {
        return try {
            drivingTripDao.updateTrip(trip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus perjalanan dari database
     * @return Result sukses atau error
     */
    suspend fun deleteTrip(trip: DrivingTrip): Result<Unit> {
        return try {
            drivingTripDao.deleteTrip(trip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan detail perjalanan berdasarkan ID
     * @return Result dengan DrivingTrip atau error
     */
    suspend fun getTripById(id: Long): Result<DrivingTrip?> {
        return try {
            if (id <= 0) {
                return Result.failure(IllegalArgumentException("Invalid trip ID"))
            }

            val trip = drivingTripDao.getTripById(id)
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan perjalanan dalam rentang tanggal tertentu
     */
    fun getTripsByDateRange(startDate: Date, endDate: Date): Flow<List<DrivingTrip>> {
        return drivingTripDao.getTripsByDateRange(startDate, endDate)
    }

    /**
     * Insert GPS route point
     */
    suspend fun insertRoutePoint(routePoint: RoutePoint): Result<Long> {
        return try {
            val id = routePointDao.insertRoutePoint(routePoint)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Insert violation detail with location
     */
    suspend fun insertViolationDetail(violation: ViolationDetail): Result<Long> {
        return try {
            val id = violationDetailDao.insertViolationDetail(violation)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get route points for specific trip
     */
    suspend fun getRoutePointsByTripId(tripId: Long): Result<List<RoutePoint>> {
        return try {
            val points = routePointDao.getRoutePointsByTripId(tripId)
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get violations for specific trip
     */
    suspend fun getViolationsByTripId(tripId: Long): Result<List<ViolationDetail>> {
        return try {
            val violations = violationDetailDao.getViolationsByTripId(tripId)
            Result.success(violations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get route points as Flow for real-time updates
     */
    fun getRoutePointsByTripIdFlow(tripId: Long): Flow<List<RoutePoint>> {
        return routePointDao.getRoutePointsByTripIdFlow(tripId)
    }

    /**
     * Get violations as Flow for real-time updates
     */
    fun getViolationsByTripIdFlow(tripId: Long): Flow<List<ViolationDetail>> {
        return violationDetailDao.getViolationsByTripIdFlow(tripId)
    }
}