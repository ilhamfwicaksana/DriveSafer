package com.example.drivesafer

import android.app.Application
import org.osmdroid.config.Configuration

class DriveSaferApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        // Set user agent for tile requests
        Configuration.getInstance().userAgentValue = "DriveSafer/1.0"

        // Optional: Set cache path for better performance
        Configuration.getInstance().osmdroidBasePath = filesDir
        Configuration.getInstance().osmdroidTileCache = getExternalFilesDir("osmdroid")
    }
}