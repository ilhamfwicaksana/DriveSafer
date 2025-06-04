package com.example.drivesafer.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.PowerManager
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivesafer.data.AppDatabase
import com.example.drivesafer.data.DrivingTripRepository
import com.example.drivesafer.model.DrivingScore
import com.example.drivesafer.model.DrivingTrip
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import android.location.LocationManager
import android.content.Intent
// 🎯 TAMBAHKAN IMPORT INI:
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail


class TrackRideViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // Tambahkan Repository
    private val repository: DrivingTripRepository

    // Tambahkan variabel untuk melacak waktu perjalanan
    private var tripStartTime: Long = 0
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // State untuk teks notes
    private val _tripNotes = MutableStateFlow("")
    val tripNotes: StateFlow<String> = _tripNotes.asStateFlow()

    // State untuk menampilkan dialog notes
    private val _showNotesDialog = MutableStateFlow(false)
    val showNotesDialog: StateFlow<Boolean> = _showNotesDialog.asStateFlow()

    // ADD GPS STATUS PROPERTIES HERE - setelah showNotesDialog
    private val _needsGPSActivation = MutableStateFlow(false)
    val needsGPSActivation: StateFlow<Boolean> = _needsGPSActivation.asStateFlow()

    private val _locationStatus = MutableStateFlow("Checking...")
    val locationStatus: StateFlow<String> = _locationStatus.asStateFlow()

    // ADD ERROR STATE HERE - setelah dialog states
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State untuk ID trip saat ini
    private var currentTripId: Long = -1

    // ADD these properties after currentTripId declaration
    private val routePoints = mutableListOf<RoutePoint>()
    private val violationDetails = mutableListOf<ViolationDetail>()
    private var lastRoutePointTime = 0L
    private val ROUTE_POINT_INTERVAL = 2000L // Save GPS point every 2 seconds

    // Enum untuk mounting position
    enum class MountPosition {
        HORIZONTAL_DASHBOARD, // Ponsel diletakkan datar di dashboard
        VERTICAL_MOUNT,       // Ponsel dipasang vertikal di mount
        HORIZONTAL_MOUNT      // Ponsel dipasang horizontal di mount
    }

    private val context = application.applicationContext
    private val sensorManager = context.getSystemService(SensorManager::class.java)

    // Gunakan LINEAR_ACCELERATION sebagai prioritas
    private val linearAccelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // fallback

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    private val drivingScore = DrivingScore()

    // Expose score information as StateFlow
    val score = drivingScore.score
    val accelerationCount = drivingScore.accelerationCount
    val brakeCount = drivingScore.brakeCount
    val turnCount = drivingScore.turnCount
    val speedingCount = drivingScore.speedingCount
    val noiseCount = drivingScore.noiseCount

    // Tracking state
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Sensor availability state
    private val _sensorAvailable = MutableStateFlow(false)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    // Sensor values
    private var xValue: Float = 0f
    private var yValue: Float = 0f
    private var zValue: Float = 0f

    // Low pass filter
    private val alpha = 0.2f
    private var filteredX: Float = 0f
    private var filteredY: Float = 0f
    private var filteredZ: Float = 0f

    // ADD PERFORMANCE PROPERTIES HERE - setelah low pass filter variables
    private var lastSensorProcessTime = 0L
    private val SENSOR_PROCESS_INTERVAL = 100L // Process every 100ms

    // Calibration
    private var xOffset: Float = 0f
    private var yOffset: Float = 0f
    private var zOffset: Float = 0f
    private var calibrationCount = 50
    private var calibrationStep = 0
    private var xCalibrationSum: Float = 0f
    private var yCalibrationSum: Float = 0f
    private var zCalibrationSum: Float = 0f
    private var isCalibrated = false

    // Moving average
    private var xSum: Float = 0f
    private var ySum: Float = 0f
    private var zSum: Float = 0f
    private var sampleCount = 0
    private val SAMPLES_FOR_AVERAGE = 10

    // Thresholds for violations - Lebih toleran
    private val ACCELERATION_THRESHOLD = -3.5f
    private val BRAKE_THRESHOLD = 4.5f
    private val TURN_THRESHOLD = 3.0f
    private var speedLimitKmh = 50 // Configurable speed limit in km/h
    private val SPEED_LIMIT_MS get() = speedLimitKmh / 3.6f // Convert to m/s

    // NOISE THRESHOLDS
    private val NOISE_VIOLATION_THRESHOLD = 80.0f
    private val NOISE_WARNING_THRESHOLD = 75.0f
    private val NOISE_SUSTAINED_DURATION = 3000L

    // Sensitivitas konfigurasi
    private var sensitivityMultiplier = 1.0f

    // Posisi mounting
    private var mountPosition = MountPosition.HORIZONTAL_DASHBOARD

    // Cooldown period
    private var lastViolationTime = 0L
    private val COOLDOWN_PERIOD = 3000L // 3 detik

    // Location
    private var lastLocation: Location? = null
    private var currentSpeed = 0f

    private var speedHistory = mutableListOf<Float>()
    private val SPEED_HISTORY_SIZE = 3

    //NOISE TRACKING
    private var lastNoiseLevel = 0f
    private var noiseViolationStartTime = 0L
    private var lastNoiseViolationTime = 0L
    private var noiseDetectionThread: Thread? = null

    // Wakelock
    private var wakeLock: PowerManager.WakeLock? = null

    // SharedPreferences constants
    private val PREFS_NAME = "DriveSaferPrefs"
    private val KEY_SCORE = "drivingScore"
    private val KEY_ACC_COUNT = "accelerationCount"
    private val KEY_BRAKE_COUNT = "brakeCount"
    private val KEY_TURN_COUNT = "turnCount"
    private val KEY_SPEED_COUNT = "speedCount"
    private val KEY_NOISE_COUNT = "noiseCount"

    // Location callback
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                updateLocation(location)
            }
        }
    }

    init {
        // Inisialisasi repository
        val database = AppDatabase.getDatabase(application)
        repository = DrivingTripRepository(
            database.drivingTripDao(),
            database.routePointDao(),
            database.violationDetailDao())

        // Check if sensors are available
        _sensorAvailable.value = (linearAccelerometer != null || accelerometer != null)

        // Load saved data
        loadSavedData()
    }

    private fun loadSavedData() {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedScore = sharedPrefs.getInt(KEY_SCORE, DrivingScore.INITIAL_SCORE)
        val savedAccCount = sharedPrefs.getInt(KEY_ACC_COUNT, 0)
        val savedBrakeCount = sharedPrefs.getInt(KEY_BRAKE_COUNT, 0)
        val savedTurnCount = sharedPrefs.getInt(KEY_TURN_COUNT, 0)
        val savedSpeedCount = sharedPrefs.getInt(KEY_SPEED_COUNT, 0)
        val savedNoiseCount = sharedPrefs.getInt(KEY_NOISE_COUNT, 0)

        // Set values to DrivingScore
        drivingScore.setValues(savedScore, savedAccCount, savedBrakeCount, savedTurnCount, savedSpeedCount, savedNoiseCount)
    }

    private fun saveData() {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putInt(KEY_SCORE, score.value)
            putInt(KEY_ACC_COUNT, accelerationCount.value)
            putInt(KEY_BRAKE_COUNT, brakeCount.value)
            putInt(KEY_TURN_COUNT, turnCount.value)
            putInt(KEY_SPEED_COUNT, speedingCount.value)
            putInt(KEY_NOISE_COUNT, drivingScore.noiseCount.value)
            apply()
        }
    }

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    ])
    fun startTracking() {
        if (_isTracking.value) return

        // Step 1: Check permissions
        if (!checkLocationPermissions()) {
            _errorMessage.value = "Location permission required"
            return
        }

        // Step 2: Auto-check GPS and prompt if needed
        if (!checkAndRequestGPS()) {
            // GPS not enabled - auto prompt user
            enableGPSAutomatically()
            return
        }

        // Step 3: All good - start tracking
        _locationStatus.value = "Starting GPS..."
        tripStartTime = System.currentTimeMillis()

        // ✅ ADD: Initialize route and violation collections
        routePoints.clear()
        violationDetails.clear()
        lastRoutePointTime = 0L

        // Simpan waktu mulai perjalanan
        tripStartTime = System.currentTimeMillis()

        // Check if sensor is available
        if (linearAccelerometer == null && accelerometer == null) {
            // Cannot track without accelerometer
            return
        }

        _isTracking.value = true
        calibrationStep = 0
        isCalibrated = false
        xCalibrationSum = 0f
        yCalibrationSum = 0f
        zCalibrationSum = 0f

        // Reset filtered values
        filteredX = 0f
        filteredY = 0f
        filteredZ = 0f

        // Reset cooldown
        lastViolationTime = 0L

        // Register sensor listener - prioritas ke LINEAR_ACCELERATION
        sensorManager?.registerListener(
            this,
            linearAccelerometer ?: accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

//        // Start location updates
//        try {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                context.mainLooper
//            )
//        } catch (e: SecurityException) {
//            // Handle permission not granted
//        }

        // Acquire wake lock to keep CPU running
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DriveSafer::DrivingWakeLock"
        )
        wakeLock?.acquire(60*60*1000L /*1 hour*/)

        if (hasMicrophonePermission()) {
            startNoiseDetection()
        }

        // Step 4: Start location monitoring
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
            _locationStatus.value = "GPS Active"
        } catch (e: SecurityException) {
            _errorMessage.value = "Location permission denied"
        }
    }

    fun stopTracking() {
        if (!_isTracking.value) return

        _isTracking.value = false

        // Unregister sensor listener
        sensorManager?.unregisterListener(this)

        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        stopNoiseDetection()

        // Release wake lock
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null

        // Tampilkan dialog notes
        _showNotesDialog.value = true

        // Simpan trip tanpa notes dulu
        saveTripWithoutNotes()

        // Save data
        saveData()
    }

    fun resetScore() {
        drivingScore.reset()
        saveData()
    }

    fun getScoreColor(): Color = drivingScore.getScoreColor()

    fun getScoreDescription(): String = drivingScore.getScoreDescription()

    // Set mount position
    fun setMountPosition(position: MountPosition) {
        mountPosition = position
    }

    // Set sensitivity level
    fun setSensitivity(sensitivity: Float) {
        // Validate range (0.5 - 1.5)
        sensitivityMultiplier = sensitivity.coerceIn(0.5f, 1.5f)
    }

    fun setSpeedLimit(kmh: Int) {
        speedLimitKmh = kmh.coerceIn(30, 120) // 30-120 km/h range
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER ||
            event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            processSensorData(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    // ✅ FIXED: Better axis mapping for all mount positions
    private fun getAdjustedSensorValues(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        return when (mountPosition) {
            // Phone flat on dashboard - standard mapping
            MountPosition.HORIZONTAL_DASHBOARD -> Triple(
                x,  // Vehicle left/right (turns)
                y,  // Vehicle forward/backward (accel/brake)
                z   // Vehicle up/down (bumps)
            )

            // Phone upright/portrait in mount (screen facing driver)
            MountPosition.VERTICAL_MOUNT -> Triple(
                x,   // Vehicle left/right (turns) - unchanged
                -z,  // Vehicle forward/backward (accel/brake) - Z becomes Y, inverted
                y    // Vehicle up/down (bumps) - Y becomes Z
            )

            // Phone landscape/sideways in mount (rotated 90° clockwise)
            MountPosition.HORIZONTAL_MOUNT -> Triple(
                y,   // Vehicle left/right (turns) - Y becomes X
                -x,  // Vehicle forward/backward (accel/brake) - X becomes Y, inverted
                z    // Vehicle up/down (bumps) - unchanged
            )
        }
    }

    private fun processSensorData(x: Float, y: Float, z: Float) {
        val currentTime = System.currentTimeMillis()

        // Throttle sensor processing to improve performance
        if (currentTime - lastSensorProcessTime < SENSOR_PROCESS_INTERVAL) {
            return
        }
        lastSensorProcessTime = currentTime

        // Adjust for mounting position
        val (adjustedX, adjustedY, adjustedZ) = getAdjustedSensorValues(x, y, z)

        // Apply low-pass filter
        filteredX = filteredX + alpha * (adjustedX - filteredX)
        filteredY = filteredY + alpha * (adjustedY - filteredY)
        filteredZ = filteredZ + alpha * (adjustedZ - filteredZ)

        if (!isCalibrated) {
            // Calibration phase
            xCalibrationSum += filteredX
            yCalibrationSum += filteredY
            zCalibrationSum += filteredZ

            calibrationStep++

            if (calibrationStep >= calibrationCount) {
                xOffset = xCalibrationSum / calibrationCount
                yOffset = yCalibrationSum / calibrationCount
                zOffset = zCalibrationSum / calibrationCount
                isCalibrated = true
            }

            return
        }

        // Apply calibration offset
        xValue = filteredX - xOffset
        yValue = filteredY - yOffset
        zValue = filteredZ - zOffset

        // Add to moving average
        xSum += xValue
        ySum += yValue
        zSum += zValue
        sampleCount++

        // Check if we have enough samples for the moving average
        if (sampleCount >= SAMPLES_FOR_AVERAGE) {
            val xAvg = xSum / sampleCount
            val yAvg = ySum / sampleCount
            val zAvg = zSum / sampleCount

            // Reset for next average
            xSum = 0f
            ySum = 0f
            zSum = 0f
            sampleCount = 0

            // Check for violations
            checkForViolations(xAvg, yAvg, zAvg)
        }
    }

    private fun checkForViolations(xAvg: Float, yAvg: Float, zAvg: Float) {
        val currentTime = System.currentTimeMillis()

        // ✅ ADD: Only detect violations if vehicle is moving
        val minimumSpeedForDetection = 1.0f // 3.6 km/h minimum
        if (currentSpeed < minimumSpeedForDetection) {
            return // Skip violation detection when stationary
        }

        // Adjust thresholds with sensitivity multiplier
        val adjustedAccelThreshold = ACCELERATION_THRESHOLD / sensitivityMultiplier
        val adjustedBrakeThreshold = BRAKE_THRESHOLD / sensitivityMultiplier
        val adjustedTurnThreshold = TURN_THRESHOLD / sensitivityMultiplier

        viewModelScope.launch {
            // Check for sudden acceleration with cooldown
            if (yAvg < adjustedAccelThreshold && (currentTime - lastViolationTime) > COOLDOWN_PERIOD) {
                drivingScore.recordViolation("Acceleration")
                lastViolationTime = currentTime
                collectViolationDetail("Acceleration")
            }
            // Check for sudden braking with cooldown
            else if (yAvg > adjustedBrakeThreshold && (currentTime - lastViolationTime) > COOLDOWN_PERIOD) {
                drivingScore.recordViolation("Brake")
                lastViolationTime = currentTime
                collectViolationDetail("Brake")
            }
            // Check for sharp turns with cooldown
            else if (abs(xAvg) > adjustedTurnThreshold && (currentTime - lastViolationTime) > COOLDOWN_PERIOD) {
                drivingScore.recordViolation("Turn")
                lastViolationTime = currentTime
                collectViolationDetail("Turn")
            }

            // Speed check is handled in updateLocation
        }
    }

    private fun updateLocation(location: Location) {
        val currentTime = System.currentTimeMillis()

        if (lastLocation != null) {
            // Calculate speed more accurately
            val timeSeconds = (location.time - lastLocation!!.time) / 1000f
            if (timeSeconds > 0.5f) { // Minimum 500ms between updates
                // ✅ ADD: Check GPS accuracy
                val currentAccuracy = location.accuracy
                val lastAccuracy = lastLocation?.accuracy ?: Float.MAX_VALUE

                // Only calculate speed if GPS accuracy is reasonable
                if (currentAccuracy <= 20f && lastAccuracy <= 20f) { // Within 20 meters accuracy
                    val distance = location.distanceTo(lastLocation!!)

                    // Filter out unrealistic speeds (>120 km/h = 33.33 m/s)
                    val calculatedSpeed = distance / timeSeconds
                    if (calculatedSpeed <= 33.33f && calculatedSpeed >= 0f) {
                        currentSpeed = getSmoothedSpeed(calculatedSpeed)

                        if (currentSpeed > SPEED_LIMIT_MS && (currentTime - lastViolationTime) > COOLDOWN_PERIOD) {
                            viewModelScope.launch {
                                drivingScore.recordViolation("Speeding")
                                lastViolationTime = currentTime
                                collectViolationDetail("Speeding")
                            }
                        }
                    }
                }
            }
        }


        lastLocation = location

        // 🎯 TAMBAHKAN PEMANGGILAN INI DI AKHIR FUNGSI:
        collectRoutePoint(location)
    }

    // ✅ TAMBAHKAN FUNCTION INI SETELAH updateLocation():
    private fun getSmoothedSpeed(newSpeed: Float): Float {
        speedHistory.add(newSpeed)
        if (speedHistory.size > SPEED_HISTORY_SIZE) {
            speedHistory.removeAt(0)
        }
        return speedHistory.average().toFloat()
    }

    // 🎯 TAMBAHKAN FUNGSI INI SETELAH updateLocation():
    private fun collectRoutePoint(location: Location) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastRoutePointTime > ROUTE_POINT_INTERVAL) {
            val routePoint = RoutePoint(
                tripId = 0, // Will be updated when trip is saved
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = currentTime,
                speed = currentSpeed,
                accuracy = location.accuracy
            )
            routePoints.add(routePoint)
            lastRoutePointTime = currentTime
        }
    }

    // 🎯 TAMBAHKAN FUNGSI INI SETELAH collectRoutePoint():
    private fun collectViolationDetail(violationType: String) {
        lastLocation?.let { location ->
            val violation = ViolationDetail(
                tripId = 0, // Will be updated when trip is saved
                type = violationType,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis(),
                severity = when (violationType) {
                    "Acceleration" -> abs(yValue)
                    "Brake" -> abs(yValue)
                    "Turn" -> abs(xValue)
                    "Speeding" -> currentSpeed
                    "Noise" -> lastNoiseLevel
                    else -> 0f
                },
                speed = currentSpeed
            )
            violationDetails.add(violation)
        }
    }



    // Add this function after updateLocation() function
    private fun checkNoiseViolation(noiseLevel: Float) {
        val currentTime = System.currentTimeMillis()

        when {
            // Immediate violation for very loud noise (>80dB)
            noiseLevel > NOISE_VIOLATION_THRESHOLD -> {
                if ((currentTime - lastNoiseViolationTime) > COOLDOWN_PERIOD) {
                    viewModelScope.launch {
                        drivingScore.recordViolation("Noise")
                        lastNoiseViolationTime = currentTime
                        collectViolationDetail("Noise")
                    }
                }
            }

            // Sustained warning level (75-80dB for >3 seconds)
            noiseLevel > NOISE_WARNING_THRESHOLD -> {
                if (noiseViolationStartTime == 0L) {
                    noiseViolationStartTime = currentTime
                } else if ((currentTime - noiseViolationStartTime) > NOISE_SUSTAINED_DURATION
                    && (currentTime - lastNoiseViolationTime) > COOLDOWN_PERIOD) {
                    viewModelScope.launch {
                        drivingScore.recordViolation("Noise")
                        lastNoiseViolationTime = currentTime
                        noiseViolationStartTime = 0L
                        collectViolationDetail("Noise")
                    }
                }
            }

            // Reset sustained timer if noise drops
            else -> {
                noiseViolationStartTime = 0L
            }
        }

        lastNoiseLevel = noiseLevel
    }

    // Add this function after checkNoiseViolation()
    private fun startNoiseDetection() {
        if (audioRecord != null) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            audioRecord?.startRecording()
            isRecording = true

            // Background thread untuk noise monitoring
            noiseDetectionThread = Thread {
                val buffer = ShortArray(BUFFER_SIZE)
                try {
                    while (isRecording && _isTracking.value && !Thread.currentThread().isInterrupted) {
                        try {
                            val readResult = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                            if (readResult > 0) {
                                val noiseLevel = calculateDecibels(buffer)
                                checkNoiseViolation(noiseLevel)
                            }
                            Thread.sleep(500)
                        } catch (e: InterruptedException) {
                            // Thread interrupted, exit gracefully
                            break
                        } catch (e: Exception) {
                            // Handle other exceptions (audio recording errors)
                            android.util.Log.e("NoiseDetection", "Error in noise detection: ${e.message}")
                            break
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NoiseDetection", "Fatal error in noise detection thread: ${e.message}")
                } finally {
                    // Cleanup resources
                    isRecording = false
                }
            }
            noiseDetectionThread?.start()

        } catch (e: SecurityException) {
            // Handle microphone permission not granted
            _errorMessage.value = "Microphone permission required for noise detection"
        } catch (e: Exception) {
            // Handle other audio recording errors
            _errorMessage.value = "Failed to initialize noise detection"
        }
    }

    private fun stopNoiseDetection() {
        isRecording = false

        // Graceful thread termination
        noiseDetectionThread?.let { thread ->
            thread.interrupt()
            try {
                // Wait for thread to finish (max 1 second)
                thread.join(1000)
            } catch (e: InterruptedException) {
                android.util.Log.w("NoiseDetection", "Thread join interrupted")
            }
        }
        noiseDetectionThread = null

        // Cleanup audio resources
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            android.util.Log.e("NoiseDetection", "Error stopping audio record: ${e.message}")
        }
        audioRecord = null
    }

    private fun calculateDecibels(buffer: ShortArray): Float {
        var sum = 0.0
        for (sample in buffer) {
            sum += (sample * sample).toDouble()
        }
        val rms = kotlin.math.sqrt(sum / buffer.size)

        // Convert to decibels (approximate)
        val decibels = if (rms > 0) {
            20 * kotlin.math.log10(rms / 32767.0) + 90 // Normalize to ~0-100 dB range
        } else {
            0.0
        }

        return maxOf(0f, decibels.toFloat()) // Ensure non-negative
    }

    // Add Microphone Permission Checks()
    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check Location Premission
    private fun checkLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    // Fungsi-fungsi baru untuk fitur history

    // Fungsi untuk menyimpan trip tanpa notes
    private fun saveTripWithoutNotes() {
        val endTime = System.currentTimeMillis()
        val tripDuration = endTime - tripStartTime

        // ✅ ADD DEBUG LOGGING
        android.util.Log.d("DriveSafer", "Saving trip - Duration: ${tripDuration}ms (${tripDuration/1000}s)")
        android.util.Log.d("DriveSafer", "Score: ${score.value}, Violations: A=${accelerationCount.value}, B=${brakeCount.value}")

        if (tripDuration < 30_000) {
            android.util.Log.w("DriveSafer", "Trip too short: ${tripDuration/1000}s - NOT SAVED")
            _showNotesDialog.value = false
            return
        }

        val trip = DrivingTrip(
            date = Date(),
            startTime = dateFormat.format(Date(tripStartTime)),
            endTime = dateFormat.format(Date(endTime)),
            duration = tripDuration,
            finalScore = score.value,
            accelerationCount = accelerationCount.value,
            brakeCount = brakeCount.value,
            turnCount = turnCount.value,
            speedingCount = speedingCount.value,
            noiseCount = noiseCount.value
        )

        // ✅ ADD VALIDATION LOGGING
        val validationError = validateTripData(trip)
        if (validationError != null) {
            android.util.Log.e("DriveSafer", "Validation error: $validationError")
            _errorMessage.value = validationError
            _showNotesDialog.value = false
            return
        }

        android.util.Log.d("DriveSafer", "Attempting to save trip to database...")

        viewModelScope.launch {
            repository.insertTrip(trip).fold(
                onSuccess = { id ->
                    android.util.Log.d("DriveSafer", "Trip saved successfully with ID: $id")
                    currentTripId = id
                    _errorMessage.value = null
                    saveRouteAndViolationData()
                },
                onFailure = { error ->
                    android.util.Log.e("DriveSafer", "Failed to save trip: ${error.message}")
                    currentTripId = -1L
                    _errorMessage.value = when (error) {
                        is IllegalArgumentException -> "Invalid trip data: ${error.message}"
                        else -> "Failed to save trip. Please try again."
                    }
                }
            )
        }
    }

    // Fungsi untuk mengubah nilai notes
    fun updateNotes(notes: String) {
        // Limit notes to reasonable length and trim whitespace
        val trimmedNotes = notes.take(500).trim()
        _tripNotes.value = trimmedNotes
    }

    // Fungsi untuk menyimpan notes dan menutup dialog
    fun saveNotesAndCloseDialog() {
        if (currentTripId != -1L) {
            viewModelScope.launch {
                repository.getTripById(currentTripId).fold(
                    onSuccess = { currentTrip ->
                        currentTrip?.let {
                            val updatedTrip = it.copy(notes = tripNotes.value)
                            repository.updateTrip(updatedTrip).fold(
                                onSuccess = {
                                    // Success - reset values
                                    _tripNotes.value = ""
                                    _showNotesDialog.value = false
                                    currentTripId = -1L
                                },
                                onFailure = { error ->
                                    // Handle error
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        // Handle error getting trip
                    }
                )
            }
        } else {
            // Jika tidak ada ID trip, hanya tutup dialog
            _showNotesDialog.value = false
            _tripNotes.value = ""
        }
    }

    // Add this function after saveNotesAndCloseDialog()
    private suspend fun saveRouteAndViolationData() {
        if (currentTripId == -1L) return

        try {
            // Save all collected route points
            routePoints.forEach { routePoint ->
                val updatedRoutePoint = routePoint.copy(tripId = currentTripId)
                repository.insertRoutePoint(updatedRoutePoint)
            }

            // Save all collected violation details
            violationDetails.forEach { violation ->
                val updatedViolation = violation.copy(tripId = currentTripId)
                repository.insertViolationDetail(updatedViolation)
            }

            // Clear temporary collections
            routePoints.clear()
            violationDetails.clear()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to save route data: ${e.message}"
        }
    }

    // Fungsi untuk menutup dialog tanpa menyimpan notes
    fun dismissNotesDialog() {
        _showNotesDialog.value = false
        _tripNotes.value = ""
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // ✅ ADD GPS MANAGEMENT FUNCTIONS HERE - setelah clearErrorMessage()
    private fun checkAndRequestGPS(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        when {
            isGpsEnabled -> {
                _locationStatus.value = "GPS Ready"
                _needsGPSActivation.value = false
                return true
            }
            isNetworkEnabled -> {
                _locationStatus.value = "Network Location Ready"
                _needsGPSActivation.value = false
                return true
            }
            else -> {
                _locationStatus.value = "Location Disabled"
                _needsGPSActivation.value = true
                return false
            }
        }
    }

    fun enableGPSAutomatically() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

            _errorMessage.value = "Please enable Location and return to app"
        } catch (e: Exception) {
            _errorMessage.value = "Please enable GPS in Settings → Location"
        }
    }

    fun checkGPSStatus() {
        checkAndRequestGPS()
    }

    private fun validateTripData(trip: DrivingTrip): String? {
        return when {
            trip.duration < 30_000 -> "Trip too short (minimum 30 seconds)"
            trip.duration > 24 * 60 * 60 * 1000 -> "Trip too long (maximum 24 hours)"
            trip.finalScore < 0 -> "Invalid score"
            trip.accelerationCount < 0 -> "Invalid violation count"
            trip.brakeCount < 0 -> "Invalid violation count"
            trip.turnCount < 0 -> "Invalid violation count"
            trip.speedingCount < 0 -> "Invalid violation count"
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
        saveData()
    }
}