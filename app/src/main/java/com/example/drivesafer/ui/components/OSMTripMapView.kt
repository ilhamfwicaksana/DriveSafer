package com.example.drivesafer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OSMTripMapView(
    routePoints: List<RoutePoint>,
    violations: List<ViolationDetail>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (routePoints.isEmpty()) {
        // Show message when no route data
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "No Route Data",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "GPS route was not recorded for this trip",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    // Calculate center point
    val centerLat = routePoints.map { it.latitude }.average()
    val centerLng = routePoints.map { it.longitude }.average()
    val centerPoint = GeoPoint(centerLat, centerLng)

    AndroidView(
        modifier = modifier.height(300.dp),
        factory = { context ->
            // Initialize OSMDroid configuration
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Set map center and zoom
                val mapController: IMapController = controller
                mapController.setZoom(15.0)
                mapController.setCenter(centerPoint)

                // Add route polyline
                if (routePoints.size > 1) {
                    val routeLine = Polyline().apply {
                        outlinePaint.color = Color.Blue.toArgb()
                        outlinePaint.strokeWidth = 8f
                    }

                    routePoints.forEach { point ->
                        routeLine.addPoint(GeoPoint(point.latitude, point.longitude))
                    }

                    overlays.add(routeLine)
                }

                // Add start marker
                if (routePoints.isNotEmpty()) {
                    val startPoint = routePoints.first()
                    val startMarker = Marker(this).apply {
                        position = GeoPoint(startPoint.latitude, startPoint.longitude)
                        title = "Start"
                        snippet = "Trip started here"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(startMarker)
                }

                // Add end marker
                if (routePoints.isNotEmpty()) {
                    val endPoint = routePoints.last()
                    val endMarker = Marker(this).apply {
                        position = GeoPoint(endPoint.latitude, endPoint.longitude)
                        title = "End"
                        snippet = "Trip ended here"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(endMarker)
                }

                // Add violation markers
                violations.forEach { violation ->
                    val violationMarker = Marker(this).apply {
                        position = GeoPoint(violation.latitude, violation.longitude)
                        title = "${violation.type} Violation"
                        snippet = "Severity: ${String.format("%.2f", violation.severity)}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        // Set different colors for different violation types
                        // You can customize marker icons here
                    }
                    overlays.add(violationMarker)
                }
            }
        }
    )
}