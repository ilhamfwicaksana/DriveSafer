package com.example.drivesafer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.drivesafer.model.ViolationDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface ViolationDetailDao {
    @Insert
    suspend fun insertViolationDetail(violation: ViolationDetail): Long

    @Query("SELECT * FROM violation_details WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getViolationsByTripId(tripId: Long): List<ViolationDetail>

    @Query("SELECT * FROM violation_details WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getViolationsByTripIdFlow(tripId: Long): Flow<List<ViolationDetail>>

    @Query("SELECT * FROM violation_details WHERE tripId = :tripId AND type = :type")
    suspend fun getViolationsByTypeAndTripId(tripId: Long, type: String): List<ViolationDetail>

    @Query("DELETE FROM violation_details WHERE tripId = :tripId")
    suspend fun deleteViolationsByTripId(tripId: Long)
}