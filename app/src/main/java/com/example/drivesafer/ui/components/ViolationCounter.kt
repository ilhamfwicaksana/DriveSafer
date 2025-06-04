package com.example.drivesafer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Komponen untuk menampilkan penghitung pelanggaran dengan label dan warna
 */
@Composable
fun ViolationCounter(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(8.dp)
            .width(130.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Grid untuk menampilkan penghitung pelanggaran
 */
@Composable
fun ViolationCounters(
    accelerationCount: Int,
    brakeCount: Int,
    turnCount: Int,
    speedingCount: Int,
    noiseCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ViolationCounter(
                label = "Acceleration",
                count = accelerationCount,
                color = Color(0xFF2196F3) // Blue
            )

            ViolationCounter(
                label = "Braking",
                count = brakeCount,
                color = Color(0xFFF44336) // Red
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ViolationCounter(
                label = "Turning",
                count = turnCount,
                color = Color(0xFF4CAF50) // Green
            )

            ViolationCounter(
                label = "Speeding",
                count = speedingCount,
                color = Color(0xFFFF9800) // Orange
            )
        }

        // ROW 3 - For Noise
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ViolationCounter(
                label = "Noise",
                count = noiseCount,
                color = Color(0xFF9C27B0) // Purple
            )
        }
    }
}