package com.example.drivesafer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.drivesafer.ui.screens.*
import com.example.drivesafer.ui.theme.DriveSaferTheme

class MainActivity : ComponentActivity() {

    // Request permissions at app start
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true // Not needed for older versions
        }
        val microphoneGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        when {
            (locationGranted || coarseLocationGranted) && microphoneGranted -> {
                // At least one location permission granted - app can function
                // You could show a toast: "Location permission granted"
            }
            else -> {
                // No location permissions granted - show explanation
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        // Create an AlertDialog to explain why permissions are needed
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.apply {
            setTitle("Permissions Required")
            setMessage("DriveSafer needs location access to track your driving speed and microphone access to monitor noise levels. Without these permissions, the app cannot function properly.\\n\\nPlease grant all permissions in Settings.")
            setPositiveButton("Open Settings") { _, _ ->
                // Open app settings
                openAppSettings()
            }
            setNegativeButton("Exit App") { _, _ ->
                // Close the app
                finish()
            }
            setCancelable(false)
        }
        builder.create().show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }

        setContent {
            DriveSaferTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onStartRideClick = { navController.navigate("trackRide") },
                                onStatisticsClick = { navController.navigate("statistics") },
                                // Tambahkan navigasi ke history
                                onHistoryClick = { navController.navigate("history") }
                            )
                        }

                        composable("trackRide") {
                            TrackRideScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("statistics") {
                            StatisticsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Tambahkan rute untuk History
                        composable("history") {
                            HistoryScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDetail = { tripId ->
                                    navController.navigate("tripDetail/$tripId")
                                }
                            )
                        }

                        // Tambahkan rute untuk TripDetail
                        composable(
                            "tripDetail/{tripId}",
                            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
                            TripDetailScreen(
                                tripId = tripId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}