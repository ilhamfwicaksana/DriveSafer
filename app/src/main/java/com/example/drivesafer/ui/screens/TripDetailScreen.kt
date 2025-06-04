package com.example.drivesafer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivesafer.model.DrivingTrip
import com.example.drivesafer.ui.components.ScoreCircle
import com.example.drivesafer.ui.components.ViolationCounters
import com.example.drivesafer.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*
// Add these imports at the top of TripDetailScreen.kt
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail
//import com.example.drivesafer.ui.components.TripMapView
import com.example.drivesafer.ui.components.OSMTripMapView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val selectedTrip by viewModel.selectedTrip.collectAsState()

    // ✅ ADD: States for route and violations
    var routePoints by remember { mutableStateOf<List<RoutePoint>>(emptyList()) }
    var violations by remember { mutableStateOf<List<ViolationDetail>>(emptyList()) }
    var isLoadingMapData by remember { mutableStateOf(true) }

    // Load trip details when screen opens
    LaunchedEffect(tripId) {
        viewModel.loadTripById(tripId)

        // ✅ ADD: Load route and violation data
        viewModel.loadRouteAndViolationData(tripId) { route, violationList ->
            routePoints = route
            violations = violationList
            isLoadingMapData = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Perjalanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        selectedTrip?.let { trip ->
            TripDetailContent(
                trip = trip,
                routePoints = routePoints,           // ✅ ADD
                violations = violations,             // ✅ ADD
                isLoadingMapData = isLoadingMapData, // ✅ ADD
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        } ?: run {
            // Loading or trip not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun TripDetailContent(
    trip: DrivingTrip,
    routePoints: List<RoutePoint>,        // ✅ ADD
    violations: List<ViolationDetail>,    // ✅ ADD
    isLoadingMapData: Boolean,            // ✅ ADD
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date and Time
        Text(
            text = dateFormat.format(trip.date),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${trip.startTime} - ${trip.endTime}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score Circle
        val scoreColor = when {
            trip.finalScore >= 80 -> MaterialTheme.colorScheme.primary
            trip.finalScore >= 60 -> MaterialTheme.colorScheme.tertiary
            trip.finalScore >= 40 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        }

        ScoreCircle(
            score = trip.finalScore,
            scoreColor = scoreColor,
            size = 180.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score Description
        val scoreDescription = when {
            trip.finalScore >= 80 -> "Sangat Baik"
            trip.finalScore >= 60 -> "Baik"
            trip.finalScore >= 40 -> "Cukup"
            else -> "Perlu Perbaikan"
        }

        Text(
            text = "Kualitas Mengemudi: $scoreDescription",
            style = MaterialTheme.typography.titleMedium,
            color = scoreColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ ADD: Maps Section
        Text(
            text = "Rute Perjalanan",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isLoadingMapData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
//                    TripMapView(
//                        routePoints = routePoints,
//                        violations = violations,
//                        modifier = Modifier.fillMaxWidth()
//                    )
                    OSMTripMapView(
                        routePoints = routePoints,
                        violations = violations,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ✅ ADD: Map Statistics
                    if (routePoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Divider()

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GPS Points: ${routePoints.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Violations: ${violations.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Trip Statistics
        Text(
            text = "Pelanggaran Mengemudi",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Violation Counters
        ViolationCounters(
            accelerationCount = trip.accelerationCount,
            brakeCount = trip.brakeCount,
            turnCount = trip.turnCount,
            speedingCount = trip.speedingCount,
            noiseCount = trip.noiseCount
        )

        // ✅ ADD: Detailed Violation Analysis (if violations exist)
        if (violations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Analisis Detail Pelanggaran",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            ViolationAnalysisCard(violations = violations)
        }

        // Trip Duration
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Detail Perjalanan",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration
                val hours = trip.duration / 3600000
                val minutes = (trip.duration % 3600000) / 60000
                val seconds = (trip.duration % 60000) / 1000

                val durationText = when {
                    hours > 0 -> "${hours}j ${minutes}m ${seconds}d"
                    minutes > 0 -> "${minutes}m ${seconds}d"
                    else -> "${seconds}d"
                }

                Text(
                    text = "Durasi: $durationText",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // If there are notes
        if (trip.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Catatan",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = trip.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ✅ ADD: New component for detailed violation analysis
@Composable
fun ViolationAnalysisCard(
    violations: List<ViolationDetail>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Group violations by type
            val violationsByType = violations.groupBy { it.type }

            violationsByType.forEach { (type, typeViolations) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${typeViolations.size} lokasi",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (type != violationsByType.keys.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}