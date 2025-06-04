package com.example.drivesafer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartRideClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onHistoryClick: () -> Unit // Tambahkan parameter untuk navigasi ke History
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DriveSafer") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "DriveSafer",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // App Description
            Text(
                text = "Monitor your driving behavior and improve your safety score",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Start Ride Button
            Button(
                onClick = onStartRideClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Start Ride",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Button (Tambahkan tombol ini)
            Button(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Driving History",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Button
            OutlinedButton(
                onClick = onStatisticsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Statistics",
                    fontSize = 18.sp
                )
            }
        }
    }
}