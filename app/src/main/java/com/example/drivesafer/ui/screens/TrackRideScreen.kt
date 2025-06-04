package com.example.drivesafer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivesafer.ui.components.LocationPermissionsHandler
import com.example.drivesafer.ui.components.ScoreDisplay
import com.example.drivesafer.ui.components.ViolationCounters
import com.example.drivesafer.viewmodel.TrackRideViewModel
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.ui.graphics.Color

@SuppressLint("MissingPermission")
@Composable
fun TrackRideScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackRideViewModel = viewModel()
) {
    // Collect state from ViewModel
    val score by viewModel.score.collectAsState()
    val accelerationCount by viewModel.accelerationCount.collectAsState()
    val brakeCount by viewModel.brakeCount.collectAsState()
    val turnCount by viewModel.turnCount.collectAsState()
    val speedingCount by viewModel.speedingCount.collectAsState()
    val noiseCount by viewModel.noiseCount.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val locationStatus by viewModel.locationStatus.collectAsState()
    val needsGPSActivation by viewModel.needsGPSActivation.collectAsState()

    // Auto-check GPS status when screen opens
    LaunchedEffect(Unit) {
        viewModel.checkGPSStatus()
    }

    // State baru untuk dialog notes
    val showNotesDialog by viewModel.showNotesDialog.collectAsState()
    val tripNotes by viewModel.tripNotes.collectAsState()

    // State for sensitivity slider
    var sensitivityValue by remember { mutableStateOf(0.5f) }

    // State for mount position
    var selectedMountPosition by remember {
        mutableStateOf(TrackRideViewModel.MountPosition.HORIZONTAL_DASHBOARD)
    }

    LocationPermissionsHandler {
        // Content to show when permissions are granted
        TrackRideContent(
            score = score,
            accelerationCount = accelerationCount,
            brakeCount = brakeCount,
            turnCount = turnCount,
            speedingCount = speedingCount,
            noiseCount = noiseCount,
            isTracking = isTracking,
            locationStatus = locationStatus,
            needsGPSActivation = needsGPSActivation,
            onNavigateBack = onNavigateBack,
            onStartTracking = { viewModel.startTracking() },
            onStopTracking = { viewModel.stopTracking() },
            onResetScore = { viewModel.resetScore() },
            scoreColor = viewModel.getScoreColor(),
            scoreDescription = viewModel.getScoreDescription(),
            // Pass new configuration parameters
            sensitivityValue = sensitivityValue,
            onSensitivityChange = {
                sensitivityValue = it
                viewModel.setSensitivity(0.5f + it)
            },
            selectedMountPosition = selectedMountPosition,
            onMountPositionChange = {
                selectedMountPosition = it
                viewModel.setMountPosition(it)
            },
            viewModel = viewModel
        )

        // Trip Notes Dialog
        if (showNotesDialog) {
            TripNotesDialog(
                notes = tripNotes,
                onNotesChange = { viewModel.updateNotes(it) },
                onDismiss = { viewModel.dismissNotesDialog() },
                onSave = { viewModel.saveNotesAndCloseDialog() }
            )
        }
    }
}

// Dialog untuk menambahkan catatan perjalanan
@Composable
fun TripNotesDialog(
    notes: String,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catatan Perjalanan") },
        text = {
            Column {
                Text(
                    "Tambahkan catatan tentang perjalanan ini (opsional)",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("mis. Kondisi jalan, cuaca, kemacetan...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Lewati")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackRideContent(
    score: Int,
    accelerationCount: Int,
    brakeCount: Int,
    turnCount: Int,
    speedingCount: Int,
    noiseCount: Int,
    isTracking: Boolean,
    locationStatus: String,
    needsGPSActivation: Boolean,
    onNavigateBack: () -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onResetScore: () -> Unit,
    scoreColor: androidx.compose.ui.graphics.Color,
    scoreDescription: String,
    // Add new parameters for configuration
    sensitivityValue: Float,
    onSensitivityChange: (Float) -> Unit,
    selectedMountPosition: TrackRideViewModel.MountPosition,
    onMountPositionChange: (TrackRideViewModel.MountPosition) -> Unit,
    viewModel: TrackRideViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driving Score") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Display
            ScoreDisplay(
                score = score,
                scoreColor = scoreColor,
                scoreDescription = scoreDescription,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Add this after ScoreDisplay
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        locationStatus == "GPS Active" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        needsGPSActivation -> Color(0xFFFF5722).copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (locationStatus == "GPS Active") Icons.Default.GpsFixed else Icons.Default.GpsOff,
                            contentDescription = "GPS Status",
                            tint = when {
                                locationStatus == "GPS Active" -> Color(0xFF4CAF50)
                                needsGPSActivation -> Color(0xFFFF5722)
                                else -> Color.Gray
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = locationStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                locationStatus == "GPS Active" -> Color(0xFF4CAF50)
                                needsGPSActivation -> Color(0xFFFF5722)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    if (needsGPSActivation) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.enableGPSAutomatically() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5722)
                            )
                        ) {
                            Text("Enable GPS Now")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Violations Summary
            Text(
                text = "Driving Violations",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Violation Counters
            ViolationCounters(
                accelerationCount = accelerationCount,
                brakeCount = brakeCount,
                turnCount = turnCount,
                speedingCount = speedingCount,
                noiseCount = noiseCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Configuration Section
            ConfigurationSection(
                sensitivityValue = sensitivityValue,
                onSensitivityChange = onSensitivityChange,
                selectedMountPosition = selectedMountPosition,
                onMountPositionChange = onMountPositionChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons
            Button(
                onClick = { if (isTracking) onStopTracking() else onStartTracking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isTracking) "Stop Tracking" else "Start Tracking",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onResetScore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Reset Score",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ConfigurationSection(
    sensitivityValue: Float,
    onSensitivityChange: (Float) -> Unit,
    selectedMountPosition: TrackRideViewModel.MountPosition,
    onMountPositionChange: (TrackRideViewModel.MountPosition) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Mount Position Selection
            Text(
                text = "Phone Mount Position",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedMountPosition == TrackRideViewModel.MountPosition.HORIZONTAL_DASHBOARD,
                        onClick = {
                            onMountPositionChange(TrackRideViewModel.MountPosition.HORIZONTAL_DASHBOARD)
                        }
                    )
                    Text("Horizontal (on dashboard/flat surface)")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedMountPosition == TrackRideViewModel.MountPosition.VERTICAL_MOUNT,
                        onClick = {
                            onMountPositionChange(TrackRideViewModel.MountPosition.VERTICAL_MOUNT)
                        }
                    )
                    Text("Vertical (mounted upright)")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedMountPosition == TrackRideViewModel.MountPosition.HORIZONTAL_MOUNT,
                        onClick = {
                            onMountPositionChange(TrackRideViewModel.MountPosition.HORIZONTAL_MOUNT)
                        }
                    )
                    Text("Horizontal (mounted sideways)")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sensitivity Slider
            Text(
                text = "Detection Sensitivity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column {
                // Display current sensitivity value
                val sensitivityText = when {
                    sensitivityValue < 0.3f -> "Low (Less Sensitive)"
                    sensitivityValue < 0.7f -> "Medium"
                    else -> "High (More Sensitive)"
                }

                Text(
                    text = sensitivityText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = sensitivityValue,
                    onValueChange = onSensitivityChange,
                    valueRange = 0f..1f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Less Sensitive", fontSize = 12.sp)
                    Text("More Sensitive", fontSize = 12.sp)
                }
            }
        }
    }
}