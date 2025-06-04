package com.example.drivesafer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivesafer.viewmodel.HistoryViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val trips by viewModel.allTrips.collectAsState()
    val averageScore by viewModel.averageScore.collectAsState()

    // Calculate statistics
    val totalTrips = trips.size
    val totalDrivingTime = trips.sumOf { it.duration }

    // Violation statistics
    val totalAccelerations = trips.sumOf { it.accelerationCount }
    val totalBraking = trips.sumOf { it.brakeCount }
    val totalTurning = trips.sumOf { it.turnCount }
    val totalSpeeding = trips.sumOf { it.speedingCount }
    val totalNoise = trips.sumOf { it.noiseCount }
    val totalViolations = totalAccelerations + totalBraking + totalTurning + totalSpeeding + totalNoise

    // Best and worst trips
    val bestTrip = trips.maxByOrNull { it.finalScore }
    val worstTrip = trips.minByOrNull { it.finalScore }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driving Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            item {
                SummaryStatsSection(
                    totalTrips = totalTrips,
                    averageScore = averageScore,
                    totalDrivingTime = totalDrivingTime,
                    bestScore = bestTrip?.finalScore ?: 0,
                    worstScore = worstTrip?.finalScore ?: 0
                )
            }

            // Violations Bar Chart
            item {
                ViolationsBarChartSection(
                    accelerations = totalAccelerations,
                    braking = totalBraking,
                    turning = totalTurning,
                    speeding = totalSpeeding,
                    noise = totalNoise,
                    totalViolations = totalViolations
                )
            }

            // Violation Breakdown Cards
            item {
                ViolationBreakdownSection(
                    accelerations = totalAccelerations,
                    braking = totalBraking,
                    turning = totalTurning,
                    speeding = totalSpeeding,
                    noise = totalNoise,
                    totalViolations = totalViolations
                )
            }

            // Best/Worst Trips
            if (bestTrip != null && worstTrip != null) {
                item {
                    BestWorstTripsSection(
                        bestTrip = bestTrip,
                        worstTrip = worstTrip
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatsSection(
    totalTrips: Int,
    averageScore: Float,
    totalDrivingTime: Long,
    bestScore: Int,
    worstScore: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Driving Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Total Trips",
                    value = totalTrips.toString(),
                    icon = "🚗"
                )

                StatCard(
                    title = "Avg Score",
                    value = "${averageScore.toInt()}",
                    icon = "⭐"
                )

                StatCard(
                    title = "Best Score",
                    value = bestScore.toString(),
                    icon = "🏆"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Driving Time",
                    value = formatDuration(totalDrivingTime),
                    icon = "⏱️"
                )

                StatCard(
                    title = "Worst Score",
                    value = worstScore.toString(),
                    icon = "⚠️"
                )

                StatCard(
                    title = "Improvement",
                    value = "+${(bestScore - worstScore)}",
                    icon = "📈"
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ViolationsBarChartSection(
    accelerations: Int,
    braking: Int,
    turning: Int,
    speeding: Int,
    noise: Int,
    totalViolations: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Violations Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (totalViolations > 0) {
                // Create chart data
                val chartEntryModel = entryModelOf(
                    0 to accelerations.toFloat(),
                    1 to braking.toFloat(),
                    2 to turning.toFloat(),
                    3 to speeding.toFloat(),
                    4 to noise.toFloat()
                )

                Chart(
                    chart = columnChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Legend
                Spacer(modifier = Modifier.height(16.dp))

                ViolationLegend()

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = "No Violations Yet!",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Keep up the safe driving!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViolationLegend() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF2196F3), label = "Acceleration")
            LegendItem(color = Color(0xFFF44336), label = "Braking")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF4CAF50), label = "Turning")
            LegendItem(color = Color(0xFFFF9800), label = "Speeding")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(color = Color(0xFF9C27B0), label = "Noise")
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(end = 4.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = color)
            ) {}
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ViolationBreakdownSection(
    accelerations: Int,
    braking: Int,
    turning: Int,
    speeding: Int,
    noise: Int,
    totalViolations: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Violation Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (totalViolations > 0) {
                ViolationDetailRow(
                    icon = "⚡",
                    label = "Acceleration",
                    count = accelerations,
                    percentage = (accelerations.toFloat() / totalViolations * 100).toInt(),
                    color = Color(0xFF2196F3)
                )

                ViolationDetailRow(
                    icon = "🛑",
                    label = "Hard Braking",
                    count = braking,
                    percentage = (braking.toFloat() / totalViolations * 100).toInt(),
                    color = Color(0xFFF44336)
                )

                ViolationDetailRow(
                    icon = "🔄",
                    label = "Sharp Turning",
                    count = turning,
                    percentage = (turning.toFloat() / totalViolations * 100).toInt(),
                    color = Color(0xFF4CAF50)
                )

                ViolationDetailRow(
                    icon = "🏃‍♂️",
                    label = "Speeding",
                    count = speeding,
                    percentage = (speeding.toFloat() / totalViolations * 100).toInt(),
                    color = Color(0xFFFF9800)
                )

                ViolationDetailRow(
                    icon = "🔊",
                    label = "Noise",
                    count = noise,
                    percentage = (noise.toFloat() / totalViolations * 100).toInt(),
                    color = Color(0xFF9C27B0)
                )
            } else {
                Text(
                    text = "No violations recorded yet. Start driving to see statistics!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ViolationDetailRow(
    icon: String,
    label: String,
    count: Int,
    percentage: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            modifier = Modifier.width(24.dp)
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )

        Text(
            text = "($percentage%)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun BestWorstTripsSection(
    bestTrip: com.example.drivesafer.model.DrivingTrip,
    worstTrip: com.example.drivesafer.model.DrivingTrip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏆 Best & Worst Trips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripCard(
                    title = "Best Trip",
                    score = bestTrip.finalScore,
                    duration = bestTrip.getFormattedDuration(),
                    icon = "🏆",
                    color = Color(0xFF4CAF50)
                )

                TripCard(
                    title = "Worst Trip",
                    score = worstTrip.finalScore,
                    duration = worstTrip.getFormattedDuration(),
                    icon = "⚠️",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun TripCard(
    title: String,
    score: Int,
    duration: String,
    icon: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = duration,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val hours = durationMs / 3600000
    val minutes = (durationMs % 3600000) / 60000

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}