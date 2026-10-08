package com.localdeals.finder.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import com.localdeals.finder.ui.components.DealCard
import com.localdeals.finder.util.MapProjection
import kotlin.math.hypot

data class MapPin(
    val deal: Deal,
    val seller: Seller,
    val isCluster: Boolean,
    val count: Int,
    val lat: Double,
    val lng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateToDeal: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Bottom sheet state
    var selectedDeal by remember { mutableStateOf<Deal?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleViewMode() }) {
                Icon(
                    if (uiState.isListView) Icons.Default.LocationOn else Icons.Default.List,
                    contentDescription = "Toggle View"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isListView) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.deals, key = { it.id }) { deal ->
                        DealCard(deal = deal, onClick = { onNavigateToDeal(deal.id) })
                    }
                }
            } else {
                MapCanvas(
                    uiState = uiState,
                    onPinTapped = { deal -> selectedDeal = deal }
                )
            }

            // Top overlay: Category filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.setCategoryFilter(category.id) },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }

    if (selectedDeal != null) {
        val deal = selectedDeal!!
        val seller = uiState.sellers[deal.sellerId]
        
        ModalBottomSheet(
            onDismissRequest = { selectedDeal = null },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(deal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                seller?.let {
                    Text(it.businessName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("৳${deal.discountedPrice}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${deal.discountPercentage}% OFF", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF57C00))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            seller?.let {
                                val uri = Uri.parse("geo:${it.latitude},${it.longitude}?q=${it.latitude},${it.longitude}(${Uri.encode(it.businessName)})")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "No maps app installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Directions")
                    }
                    Button(
                        onClick = {
                            selectedDeal = null
                            onNavigateToDeal(deal.id)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Deal")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun MapCanvas(
    uiState: MapData,
    onPinTapped: (Deal) -> Unit
) {
    // Initial map center defaults to user location or Dhaka center
    val defaultLat = 23.8103
    val defaultLng = 90.4125
    
    var centerLat by remember { mutableDoubleStateOf(uiState.userLocation?.latitude ?: defaultLat) }
    var centerLng by remember { mutableDoubleStateOf(uiState.userLocation?.longitude ?: defaultLng) }
    var zoom by remember { mutableFloatStateOf(0.5f) } // Initial zoom (smaller is zoomed out)

    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val userLocColor = Color.Blue
    val radiusColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    // Pre-allocate the pin path to avoid allocations in the draw loop
    val pinPath = remember {
        Path().apply {
            moveTo(0f, 0f)
            lineTo(-15f, -30f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(-15f, -45f, 15f, -15f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(0f, 0f)
            close()
        }
    }

    // Derived clustered points for performance
    // Automatically re-calculates when deals or zoom changes
    val clusteredPins by remember(uiState.deals, uiState.sellers, centerLat, centerLng, zoom) {
        derivedStateOf {
            val pins = mutableListOf<MapPin>()
            val clusterThresholdPixels = 50f
            
            // width and height aren't easily available here, but we can do a local pixel projection
            // using 0 as a dummy canvas dimension since we just need relative pixel distances
            uiState.deals.forEach { deal ->
                val seller = uiState.sellers[deal.sellerId] ?: return@forEach
                val (px, py) = MapProjection.latLngToPixel(
                    lat = seller.latitude, lng = seller.longitude,
                    centerLat = centerLat, centerLng = centerLng,
                    zoom = zoom, canvasWidth = 0f, canvasHeight = 0f
                )
                
                var added = false
                for (i in pins.indices) {
                    val cluster = pins[i]
                    val (cx, cy) = MapProjection.latLngToPixel(
                        lat = cluster.lat, lng = cluster.lng,
                        centerLat = centerLat, centerLng = centerLng,
                        zoom = zoom, canvasWidth = 0f, canvasHeight = 0f
                    )
                    
                    if (hypot(px - cx, py - cy) < clusterThresholdPixels) {
                        pins[i] = cluster.copy(
                            isCluster = true,
                            count = cluster.count + 1,
                            lat = (cluster.lat * cluster.count + seller.latitude) / (cluster.count + 1),
                            lng = (cluster.lng * cluster.count + seller.longitude) / (cluster.count + 1)
                        )
                        added = true
                        break
                    }
                }
                if (!added) {
                    pins.add(MapPin(deal, seller, false, 1, seller.latitude, seller.longitude))
                }
            }
            pins
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9)) // Light green for land
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoomChange, _ ->
                        zoom = (zoom * zoomChange).coerceIn(0.1f, 5.0f)
                        
                        // Pan handling: move centerLat/Lng based on pixel pan
                        // If we move screen right (pan.x > 0), the map center moves LEFT (longitude decreases)
                        if (pan.x != 0f || pan.y != 0f) {
                            val newCenter = MapProjection.pixelToLatLng(
                                x = size.width / 2f - pan.x,
                                y = size.height / 2f - pan.y,
                                centerLat = centerLat,
                                centerLng = centerLng,
                                zoom = zoom,
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat()
                            )
                            centerLat = newCenter.first
                            centerLng = newCenter.second
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            // Zoom in and recenter
                            val newCenter = MapProjection.pixelToLatLng(
                                x = offset.x,
                                y = offset.y,
                                centerLat = centerLat,
                                centerLng = centerLng,
                                zoom = zoom,
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat()
                            )
                            centerLat = newCenter.first
                            centerLng = newCenter.second
                            zoom = (zoom * 1.5f).coerceAtMost(5.0f)
                        },
                        onTap = { offset ->
                            // Find closest pin within touch radius (~40px)
                            var closestPin: MapPin? = null
                            var minDistance = Float.MAX_VALUE
                            
                            clusteredPins.forEach { pin ->
                                val (x, y) = MapProjection.latLngToPixel(
                                    lat = pin.lat,
                                    lng = pin.lng,
                                    centerLat = centerLat,
                                    centerLng = centerLng,
                                    zoom = zoom,
                                    canvasWidth = size.width.toFloat(),
                                    canvasHeight = size.height.toFloat()
                                )
                                
                                val dist = hypot(offset.x - x, offset.y - y)
                                if (dist < 60f && dist < minDistance) { // 60px tap radius
                                    minDistance = dist
                                    closestPin = pin
                                }
                            }
                            
                            // For clusters, just pick the underlying deal for now, or zoom in
                            closestPin?.let {
                                if (it.isCluster) {
                                    // zoom in on the cluster
                                    centerLat = it.lat
                                    centerLng = it.lng
                                    zoom = (zoom * 1.5f).coerceAtMost(5.0f)
                                } else {
                                    onPinTapped(it.deal)
                                }
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            
            // Draw procedural grid (representing roads/blocks)
            // We draw lines based on modulo of projected lat/lng
            val gridSize = 100f * zoom
            val xOffset = ((centerLng * 10000) % gridSize).toFloat()
            val yOffset = ((centerLat * 10000) % gridSize).toFloat()
            
            for (x in 0..width.toInt() step 100) {
                val lineX = x + (gridSize - xOffset % gridSize)
                drawLine(gridColor, Offset(lineX, 0f), Offset(lineX, height), strokeWidth = 2f)
            }
            for (y in 0..height.toInt() step 100) {
                val lineY = y + (gridSize - yOffset % gridSize)
                drawLine(gridColor, Offset(0f, lineY), Offset(width, lineY), strokeWidth = 2f)
            }

            // Draw Search Radius if user location exists
            if (uiState.userLocation != null) {
                val loc = uiState.userLocation
                val (locX, locY) = MapProjection.latLngToPixel(
                    lat = loc.latitude, lng = loc.longitude,
                    centerLat = centerLat, centerLng = centerLng,
                    zoom = zoom,
                    canvasWidth = width, canvasHeight = height
                )
                
                // Radius in pixels (approximate based on projection constants)
                val metersPerDegreeLat = 111320.0
                val radiusPx = (uiState.searchRadiusMeters / metersPerDegreeLat * metersPerDegreeLat * zoom).toFloat()
                
                if (locX in -radiusPx..(width + radiusPx) && locY in -radiusPx..(height + radiusPx)) {
                    drawCircle(radiusColor, radiusPx, Offset(locX, locY))
                    drawCircle(primaryColor, 10f, Offset(locX, locY))
                    drawCircle(Color.White, 6f, Offset(locX, locY))
                    
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "You are here",
                        topLeft = Offset(locX + 15f, locY - 10f),
                        style = TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, background = Color.White.copy(alpha=0.7f))
                    )
                }
            }

            // Draw Pins
            clusteredPins.forEach { pin ->
                val (x, y) = MapProjection.latLngToPixel(
                    lat = pin.lat, lng = pin.lng,
                    centerLat = centerLat, centerLng = centerLng,
                    zoom = zoom,
                    canvasWidth = width, canvasHeight = height
                )

                // Cull off-screen pins
                if (x in -50f..(width + 50f) && y in -50f..(height + 50f)) {
                    translate(left = x, top = y) {
                        drawPath(pinPath, primaryColor)
                        drawCircle(surfaceColor, 8f, Offset(0f, -30f))
                    }
                    
                    if (pin.isCluster) {
                        // Draw cluster count badge
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "${pin.count}",
                            topLeft = Offset(x + 10f, y - 45f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                background = Color.Blue
                            )
                        )
                    } else {
                        // Draw discount badge
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "-${pin.deal.discountPercentage}%",
                            topLeft = Offset(x + 12f, y - 45f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                background = Color.Red
                            )
                        )
                    }
                }
            }
        }

        // Recenter Button
        if (uiState.userLocation != null) {
            val loc = uiState.userLocation
            SmallFloatingActionButton(
                onClick = {
                    centerLat = loc.latitude
                    centerLng = loc.longitude
                    zoom = 0.8f
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp, 88.dp) // Above main FAB
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Recenter")
            }
        }
    }
}
