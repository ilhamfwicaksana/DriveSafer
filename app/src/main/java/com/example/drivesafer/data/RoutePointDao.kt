package com.example.drivesafer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.drivesafer.model.RoutePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insertRoutePoint(routePoint: RoutePoint): Long

    @Query("SELECT * FROM trip_route_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getRoutePointsByTripId(tripId: Long): List<RoutePoint>

    @Query("SELECT * FROM trip_route_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getRoutePointsByTripIdFlow(tripId: Long): Flow<List<RoutePoint>>

    @Query("DELETE FROM trip_route_points WHERE tripId = :tripId")
    suspend fun deleteRoutePointsByTripId(tripId: Long)
}