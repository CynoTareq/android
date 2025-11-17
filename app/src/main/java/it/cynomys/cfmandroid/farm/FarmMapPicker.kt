package it.cynomys.cfmandroid.farm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun FarmMapPicker(
    initialPoint: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onCancel: () -> Unit
) {
    // Selected point state
    var selectedPoint by remember { mutableStateOf(initialPoint) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // -------------------- MAP --------------------
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                // Prevent buttons from blocking gestures
                .padding(bottom = 100.dp),
            factory = { context ->

                // REQUIRED osmdroid initialization
                Configuration.getInstance().load(
                    context,
                    context.getSharedPreferences("osmdroid", 0)
                )

                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)

                    // Helper to place / move marker
                    fun placeMarker(point: GeoPoint) {
                        selectedPoint = point

                        marker?.let { overlays.remove(it) }

                        marker = Marker(this).apply {
                            position = point
                            isDraggable = true
                            icon = context.getDrawable(
                                org.osmdroid.library.R.drawable.marker_default
                            )
                            setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )

                            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                override fun onMarkerDrag(marker: Marker) {}
                                override fun onMarkerDragStart(marker: Marker) {}
                                override fun onMarkerDragEnd(marker: Marker) {
                                    selectedPoint = marker.position
                                }
                            })
                        }

                        overlays.add(marker)
                        invalidate()
                    }

                    // If editing, show existing location
                    initialPoint?.let {
                        controller.setCenter(it)
                        placeMarker(it)
                    }

                    // Tap anywhere to place marker
                    overlays.add(
                        MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                placeMarker(p)
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        })
                    )
                }
            }
        )

        // -------------------- ACTION BUTTONS --------------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                // Keep buttons above system navigation bar
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancel
            ) {
                Text("Cancel")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedPoint != null,
                onClick = {
                    selectedPoint?.let(onLocationSelected)
                }
            ) {
                Text("Confirm")
            }
        }
    }
}
