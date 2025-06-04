package com.example.drivesafer.model

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Model data untuk tracking skor mengemudi.
 * Menyimpan skor saat ini dan menangani perubahan berdasarkan deteksi pelanggaran.
 */
class DrivingScore {
    // Constant penalties for violations
    companion object {
        const val INITIAL_SCORE = 100
        const val ACCELERATION_PENALTY = 5
        const val BRAKE_PENALTY = 5
        const val TURN_PENALTY = 3
        const val SPEEDING_PENALTY = 8
        const val NOISE_PENALTY = 4

        // Score ranges
        const val EXCELLENT_THRESHOLD = 80
        const val GOOD_THRESHOLD = 60
        const val FAIR_THRESHOLD = 40
    }

    // Current score as StateFlow for reactivity
    private val _score = MutableStateFlow(INITIAL_SCORE)
    val score: StateFlow<Int> = _score.asStateFlow()

    // Violation counters
    private val _accelerationCount = MutableStateFlow(0)
    val accelerationCount: StateFlow<Int> = _accelerationCount.asStateFlow()

    private val _brakeCount = MutableStateFlow(0)
    val brakeCount: StateFlow<Int> = _brakeCount.asStateFlow()

    private val _turnCount = MutableStateFlow(0)
    val turnCount: StateFlow<Int> = _turnCount.asStateFlow()

    private val _speedingCount = MutableStateFlow(0)
    val speedingCount: StateFlow<Int> = _speedingCount.asStateFlow()

    private val _noiseCount = MutableStateFlow(0)
    val noiseCount: StateFlow<Int> = _noiseCount.asStateFlow()

    // Record a violation and update score
    fun recordViolation(violationType: String) {
        when (violationType) {
            "Acceleration" -> {
                _accelerationCount.value = _accelerationCount.value + 1
                reduceScore(ACCELERATION_PENALTY)
            }
            "Brake" -> {
                _brakeCount.value = _brakeCount.value + 1
                reduceScore(BRAKE_PENALTY)
            }
            "Turn" -> {
                _turnCount.value = _turnCount.value + 1
                reduceScore(TURN_PENALTY)
            }
            "Speeding" -> {
                _speedingCount.value = _speedingCount.value + 1
                reduceScore(SPEEDING_PENALTY)
            }
            "Noise" -> {  // ✅ ADD: Handle noise violations
                _noiseCount.value = _noiseCount.value + 1
                reduceScore(NOISE_PENALTY)
            }
        }
    }

    // Reduce score with limit at 0
    private fun reduceScore(penalty: Int) {
        _score.value = (_score.value - penalty).coerceAtLeast(0)
    }

    // Reset score to initial value
    fun reset() {
        _score.value = INITIAL_SCORE
        _accelerationCount.value = 0
        _brakeCount.value = 0
        _turnCount.value = 0
        _speedingCount.value = 0
        _noiseCount.value = 0
    }

    // Get color based on current score
    fun getScoreColor(): Color {
        return when (_score.value) {
            in EXCELLENT_THRESHOLD..INITIAL_SCORE -> Color(0xFF4CAF50) // Green
            in GOOD_THRESHOLD until EXCELLENT_THRESHOLD -> Color(0xFFFFEB3B) // Yellow
            in FAIR_THRESHOLD until GOOD_THRESHOLD -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFF44336) // Red
        }
    }

    // Get text description of driving quality
    fun getScoreDescription(): String {
        return when (_score.value) {
            in EXCELLENT_THRESHOLD..INITIAL_SCORE -> "Excellent"
            in GOOD_THRESHOLD until EXCELLENT_THRESHOLD -> "Good"
            in FAIR_THRESHOLD until GOOD_THRESHOLD -> "Fair"
            else -> "Poor"
        }
    }

    fun setValues(savedScore: Int, savedAccCount: Int, savedBrakeCount: Int, savedTurnCount: Int, savedSpeedCount: Int, savedNoiseCount: Int = 0) {
        _score.value = savedScore
        _accelerationCount.value = savedAccCount
        _brakeCount.value = savedBrakeCount
        _turnCount.value = savedTurnCount
        _speedingCount.value = savedSpeedCount
        _noiseCount.value = savedNoiseCount
    }
}