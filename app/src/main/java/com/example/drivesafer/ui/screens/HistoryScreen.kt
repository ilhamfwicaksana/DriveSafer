package com.example.drivesafer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivesafer.model.DrivingTrip
import com.example.drivesafer.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val trips by viewModel.allTrips.collectAsState()
    val averageScore by viewModel.averageScore.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Perjalanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ringkasan Mengemudi",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Skor Rata-rata: ${averageScore.toInt()}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total Perjalanan: ${trips.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Filter Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedPeriod == HistoryViewModel.TimePeriod.ALL,
                    onClick = { viewModel.setTimePeriod(HistoryViewModel.TimePeriod.ALL) },
                    label = { Text("Semua") }
                )

                FilterChip(
                    selected = selectedPeriod == HistoryViewModel.TimePeriod.WEEK,
                    onClick = { viewModel.setTimePeriod(HistoryViewModel.TimePeriod.WEEK) },
                    label = { Text("Minggu Ini") }
                )

                FilterChip(
                    selected = selectedPeriod == HistoryViewModel.TimePeriod.MONTH,
                    onClick = { viewModel.setTimePeriod(HistoryViewModel.TimePeriod.MONTH) },
                    label = { Text("Bulan Ini") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trip List
            if (trips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada perjalanan tercatat",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(trips) { trip ->
                        TripItem(
                            trip = trip,
                            dateFormat = dateFormat,
                            onTripClick = { onNavigateToDetail(trip.id) },
                            onDeleteClick = { viewModel.deleteTrip(trip) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripItem(
    trip: DrivingTrip,
    dateFormat: SimpleDateFormat,
    onTripClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTripClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trip date and time info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dateFormat.format(trip.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${trip.startTime} - ${trip.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Duration in minutes and seconds
                val hours = trip.duration / 3600000
                val minutes = (trip.duration % 3600000) / 60000
                val seconds = (trip.duration % 60000) / 1000
                Text(
                    text = "Durasi: ${if (hours > 0) "${hours}j " else ""}${minutes}m ${seconds}d",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${trip.finalScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        trip.finalScore >= 80 -> MaterialTheme.colorScheme.primary
                        trip.finalScore >= 60 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                Text(
                    text = "Skor",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Perjalanan") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan perjalanan ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}