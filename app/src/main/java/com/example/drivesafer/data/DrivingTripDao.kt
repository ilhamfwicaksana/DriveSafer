package com.example.drivesafer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.drivesafer.model.DrivingTrip
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object untuk operasi database pada entity DrivingTrip
 */
@Dao
interface DrivingTripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: DrivingTrip): Long

    @Update
    suspend fun updateTrip(trip: DrivingTrip)

    @Delete
    suspend fun deleteTrip(trip: DrivingTrip)

    @Query("SELECT * FROM driving_trips ORDER BY date DESC, startTime DESC")
    fun getAllTrips(): Flow<List<DrivingTrip>>

    @Query("SELECT * FROM driving_trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): DrivingTrip?

    @Query("SELECT AVG(finalScore) FROM driving_trips")
    fun getAverageScore(): Flow<Float>

    @Query("SELECT * FROM driving_trips WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getTripsByDateRange(startDate: Date, endDate: Date): Flow<List<DrivingTrip>>

    @Query("SELECT COUNT(*) FROM driving_trips")
    fun getTripCount(): Flow<Int>

    @Query("SELECT SUM(duration) FROM driving_trips")
    fun getTotalDrivingTime(): Flow<Long?>
}