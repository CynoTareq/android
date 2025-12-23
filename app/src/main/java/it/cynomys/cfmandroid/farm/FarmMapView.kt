package it.cynomys.cfmandroid.farm


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.UUID



@Composable
fun FarmMapView(
    farms: List<Farm>,
    isLoading: Boolean,
    onFarmSelected: (UUID) -> Unit
) {
    val context = LocalContext.current

    if (farms.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "No Farms",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No farms to display on map\nAdd your first farm!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                // Initialize OSMDroid configuration
                Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Calculate positions to avoid overlapping
                    val markerPositions = calculateMarkerPositions(farms)

                    // Add markers for each farm with calculated positions
                    farms.forEach { farm ->
                        val markerColor = when (farm.species) {
                            Species.RUMINANT -> android.graphics.Color.GREEN
                            Species.SWINE -> android.graphics.Color.MAGENTA
                            Species.POULTRY -> android.graphics.Color.YELLOW
                          //  Species.EQUINE -> android.graphics.Color.BLUE
                            else -> android.graphics.Color.RED
                        }

                        val position = markerPositions[farm] ?: GeoPoint(farm.coordinateY, farm.coordinateX)

                        val marker = Marker(this).apply {
                            this.position = position
                            title = farm.name
                            snippet = "${farm.address}\n${farm.area} ha - ${farm.species}"

                            // Set custom Google Maps style marker
                            icon = createGoogleMapsMarker(context, farm.name, markerColor)
                            setAnchor(0.5f, 1.0f) // Center horizontally, bottom vertically

                            setOnMarkerClickListener { _, _ ->
                                onFarmSelected(farm.id!!)
                                true
                            }
                        }
                        overlays.add(marker)
                    }

                    // Fit all farms in the map view
                    if (farms.isNotEmpty()) {
                        fitBoundsToFarms(farms)
                    } else {
                        controller.setZoom(10.0)
                    }

                    invalidate()
                }
            },
            update = { mapView ->
                // Clear existing markers
                mapView.overlays.clear()

                // Calculate positions to avoid overlapping
                val markerPositions = calculateMarkerPositions(farms)

                // Add updated markers with calculated positions
                farms.forEach { farm ->
                    val markerColor = when (farm.species) {
                        Species.RUMINANT -> android.graphics.Color.GREEN
                        Species.SWINE -> android.graphics.Color.MAGENTA
                        Species.POULTRY -> android.graphics.Color.YELLOW
                       // Species.EQUINE -> android.graphics.Color.BLUE
                        else -> android.graphics.Color.RED
                    }

                    val position = markerPositions[farm] ?: GeoPoint(farm.coordinateY, farm.coordinateX)

                    val marker = Marker(mapView).apply {
                        this.position = position
                        title = farm.name
                        snippet = "${farm.address}\n${farm.area} ha - ${farm.species}"

                        // Set custom Google Maps style marker
                        icon = createGoogleMapsMarker(context, farm.name, markerColor)
                        setAnchor(0.5f, 1.0f)

                        setOnMarkerClickListener { _, _ ->
                            onFarmSelected(farm.id!!)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // Fit all farms in the map view
                if (farms.isNotEmpty()) {
                    mapView.fitBoundsToFarms(farms)
                }

                mapView.invalidate()
            }
        )
    }
}


// Extension function to fit map bounds to show all farms
fun MapView.fitBoundsToFarms(farms: List<Farm>) {
    if (farms.isEmpty()) return

    // Find min/max coordinates
    val minLat = farms.minOf { it.coordinateY }
    val maxLat = farms.maxOf { it.coordinateY }
    val minLon = farms.minOf { it.coordinateX }
    val maxLon = farms.maxOf { it.coordinateX }

    // Create bounding box
    val boundingBox = BoundingBox(
        maxLat,  // north
        maxLon,  // east
        minLat,  // south
        minLon   // west
    )

    // Fit map to bounding box with padding (100px padding)
    post {
        zoomToBoundingBox(boundingBox, false, 100)
    }
}


// Function to create a Google Maps style marker
fun createGoogleMapsMarker(context: android.content.Context, farmName: String, markerColor: Int = android.graphics.Color.RED): Drawable {
    val density = context.resources.displayMetrics.density
    val markerWidth = (48 * density).toInt()
    val markerHeight = (58 * density).toInt()
    val textPadding = (8 * density).toInt()

    // Create paint for the marker
    val markerPaint = Paint().apply {
        color = markerColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val strokePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3 * density
    }

    // Create paint for the text
    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        isAntiAlias = true
        textSize = 12 * density
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // Measure text
    val textBounds = Rect()
    textPaint.getTextBounds(farmName, 0, farmName.length, textBounds)
    val textWidth = textBounds.width() + textPadding * 2
    val textHeight = textBounds.height() + textPadding

    // Calculate total dimensions
    val totalWidth = maxOf(markerWidth, textWidth)
    val totalHeight = markerHeight + textHeight + textPadding

    // Create bitmap
    val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw text background (rounded rectangle)
    val textBackgroundPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(4 * density, 0f, 2 * density, android.graphics.Color.argb(50, 0, 0, 0))
    }

    val textX = totalWidth / 2f
    val textY = textHeight / 2f
    val textRect = android.graphics.RectF(
        textX - textWidth / 2f,
        0f,
        textX + textWidth / 2f,
        textHeight.toFloat()
    )
    canvas.drawRoundRect(textRect, 8 * density, 8 * density, textBackgroundPaint)

    // Draw text
    canvas.drawText(
        farmName,
        textX,
        textY + textBounds.height() / 2f,
        textPaint
    )

    // Draw marker pin
    val markerCenterX = totalWidth / 2f
    val markerTop = textHeight + textPadding.toFloat()
    val markerBottom = markerTop + markerHeight - (markerHeight * 0.2f) // Adjust for pin point

    // Create marker path (teardrop shape)
    val path = Path().apply {
        val radius = markerWidth / 2f * 0.8f
        val centerY = markerTop + radius

        // Draw circle part
        addCircle(markerCenterX, centerY, radius, Path.Direction.CW)

        // Draw pin point
        moveTo(markerCenterX, markerBottom)
        lineTo(markerCenterX - radius * 0.3f, centerY + radius * 0.7f)
        lineTo(markerCenterX + radius * 0.3f, centerY + radius * 0.7f)
        close()
    }

    // Draw marker with shadow
    val shadowPaint = Paint().apply {
        color = android.graphics.Color.argb(80, 0, 0, 0)
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = android.graphics.BlurMaskFilter(4 * density, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    canvas.save()
    canvas.translate(2 * density, 2 * density)
    canvas.drawPath(path, shadowPaint)
    canvas.restore()

    // Draw main marker
    canvas.drawPath(path, markerPaint)
    canvas.drawPath(path, strokePaint)

    // Draw inner circle
    val innerCirclePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val innerRadius = markerWidth / 2f * 0.4f
    canvas.drawCircle(markerCenterX, markerTop + markerWidth / 2f * 0.8f, innerRadius, innerCirclePaint)

    return BitmapDrawable(context.resources, bitmap)
}


// Function to calculate offset positions for overlapping markers
fun calculateMarkerPositions(farms: List<Farm>): Map<Farm, GeoPoint> {
    val positionMap = mutableMapOf<Farm, GeoPoint>()
    val locationGroups = farms.groupBy { "${it.coordinateY},${it.coordinateX}" }

    locationGroups.forEach { (_, farmsAtLocation) ->
        if (farmsAtLocation.size == 1) {
            // Single farm at location, no offset needed
            val farm = farmsAtLocation.first()
            positionMap[farm] = GeoPoint(farm.coordinateY, farm.coordinateX)
        } else {
            // Multiple farms at same location, create circular offset
            val centerLat = farmsAtLocation.first().coordinateY
            val centerLon = farmsAtLocation.first().coordinateX
            val offsetDistance = 0.001 // About 100 meters offset

            farmsAtLocation.forEachIndexed { index, farm ->
                if (index == 0) {
                    // First farm stays at original position
                    positionMap[farm] = GeoPoint(centerLat, centerLon)
                } else {
                    // Calculate circular offset for other farms
                    val angle = (2 * Math.PI * index) / farmsAtLocation.size
                    val offsetLat = centerLat + (offsetDistance * Math.cos(angle))
                    val offsetLon = centerLon + (offsetDistance * Math.sin(angle))
                    positionMap[farm] = GeoPoint(offsetLat, offsetLon)
                }
            }
        }
    }

    return positionMap
}