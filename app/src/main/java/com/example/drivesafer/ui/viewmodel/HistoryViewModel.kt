package com.example.drivesafer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivesafer.data.AppDatabase
import com.example.drivesafer.data.DrivingTripRepository
import com.example.drivesafer.model.DrivingTrip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.*
// Add these imports at the top of HistoryViewModel.kt
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DrivingTripRepository

    // Daftar semua perjalanan
    private val _allTrips = MutableStateFlow<List<DrivingTrip>>(emptyList())
    val allTrips: StateFlow<List<DrivingTrip>> = _allTrips.asStateFlow()

    // Skor rata-rata
    private val _averageScore = MutableStateFlow(0f)
    val averageScore: StateFlow<Float> = _averageScore.asStateFlow()

    // Filter by time period
    private val _selectedPeriod = MutableStateFlow(TimePeriod.ALL)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    // Add this property with other StateFlow declarations
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Selected trip for details
    private val _selectedTrip = MutableStateFlow<DrivingTrip?>(null)
    val selectedTrip: StateFlow<DrivingTrip?> = _selectedTrip.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DrivingTripRepository(
            database.drivingTripDao(),
            database.routePointDao(),
            database.violationDetailDao())

        // Load all trips by default
        loadTrips()

        // Collect average score
        viewModelScope.launch {
            repository.averageScore.collect {
                _averageScore.value = it
            }
        }
    }

    private var tripsJob: Job? = null

    fun loadTrips() {
        tripsJob?.cancel()
        _isLoading.value = true

        tripsJob = viewModelScope.launch {
            try {
                when (selectedPeriod.value) {
                    TimePeriod.ALL -> {
                        repository.allTrips.collect {
                            _allTrips.value = it
                            _isLoading.value = false
                        }
                    }
                    TimePeriod.WEEK -> {
                        val calendar = Calendar.getInstance()
                        val endDate = calendar.time
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        val startDate = calendar.time

                        repository.getTripsByDateRange(startDate, endDate).collect {
                            _allTrips.value = it
                            _isLoading.value = false
                        }
                    }
                    TimePeriod.MONTH -> {
                        val calendar = Calendar.getInstance()
                        val endDate = calendar.time
                        calendar.add(Calendar.MONTH, -1)
                        val startDate = calendar.time

                        repository.getTripsByDateRange(startDate, endDate).collect {
                            _allTrips.value = it
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }


    fun setTimePeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadTrips()
    }

    fun loadTripById(tripId: Long) {
        viewModelScope.launch {
            repository.getTripById(tripId).fold(
                onSuccess = { trip ->
                    _selectedTrip.value = trip
                },
                onFailure = { error ->
                    // Handle error
                    _selectedTrip.value = null
                }
            )
        }
    }

    fun deleteTrip(trip: DrivingTrip) {
        viewModelScope.launch {
            repository.deleteTrip(trip).fold(
                onSuccess = { /* Success - maybe show toast */ },
                onFailure = { error ->
                    // Handle error - show error message to user
                }
            )
            loadTrips()
        }
    }

    // Add these functions after deleteTrip() function

    /**
     * Load route and violation data for maps display
     */
    fun loadRouteAndViolationData(
        tripId: Long,
        onDataLoaded: (List<RoutePoint>, List<ViolationDetail>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Load route points
                val routeResult = repository.getRoutePointsByTripId(tripId)
                val routePoints = routeResult.getOrElse { emptyList() }

                // Load violations
                val violationResult = repository.getViolationsByTripId(tripId)
                val violations = violationResult.getOrElse { emptyList() }

                // Return data via callback
                onDataLoaded(routePoints, violations)

            } catch (e: Exception) {
                // Handle error - return empty data
                onDataLoaded(emptyList(), emptyList())
            }
        }
    }

    /**
     * Get route points for specific trip as Flow
     */
    fun getRoutePointsFlow(tripId: Long): Flow<List<RoutePoint>> {
        return repository.getRoutePointsByTripIdFlow(tripId)
    }

    /**
     * Get violations for specific trip as Flow
     */
    fun getViolationsFlow(tripId: Long): Flow<List<ViolationDetail>> {
        return repository.getViolationsByTripIdFlow(tripId)
    }

    enum class TimePeriod {
        ALL, WEEK, MONTH
    }

    override fun onCleared() {
        super.onCleared()
        tripsJob?.cancel()
        _isLoading.value = false
    }
}