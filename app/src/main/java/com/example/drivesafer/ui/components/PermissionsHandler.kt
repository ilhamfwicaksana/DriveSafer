package com.example.drivesafer.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
fun LocationPermissionsHandler(
    onPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    // Check if permissions are already granted
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val microphoneGranted = ContextCompat.checkSelfPermission(  // ✅ ADD
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        permissionsGranted = (fineLocationGranted || coarseLocationGranted) && microphoneGranted
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val microphoneGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        permissionsGranted = (fineLocationGranted || coarseLocationGranted) && microphoneGranted
    }

    when {
        permissionsGranted -> {
            onPermissionsGranted()
        }
        else -> {
            LocationPermissionsRequest(
                onRequestPermissions = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun LocationPermissionsRequest(
    onRequestPermissions: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DriveSafer needs location access to track your driving, detect speeding, and microphone access to monitor noise levels for safer driving.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRequestPermissions) {
            Text("Grant Permissions")
        }
    }
}