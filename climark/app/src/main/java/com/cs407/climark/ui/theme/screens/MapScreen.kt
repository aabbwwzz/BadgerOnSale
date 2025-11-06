package com.cs407.climark.ui.screens


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.climark.ui.viewModels.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val defaultLocation = LatLng(43.0731, -89.4012) // Madison
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }


    LaunchedEffect(Unit) { viewModel.initializeLocationClient(context) }


    RequestLocationPermission(
        requestNow = !ui.locationPermissionGranted,
        onResult = { granted ->
            viewModel.updateLocationPermission(granted)
            if (granted) viewModel.getCurrentLocation()
        }
    )


    LaunchedEffect(ui.currentLocation) {
        ui.currentLocation?.let { loc ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }


    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {


            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,      // <- zoom in/out buttons
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapClick = { latLng ->
                    when (ui.mode) {
                        InteractionMode.ADDING -> viewModel.addMarkerOnce(latLng)
                        InteractionMode.DELETING -> viewModel.exitMode() // tapping map cancels delete
                        else -> viewModel.dismissWeather()
                    }
                }
            ) {
                // Current location – custom dot
                ui.currentLocation?.let { myLoc ->
                    MarkerComposable(
                        state = MarkerState(myLoc),
                        title = "Your Location",
                        onClick = {
                            viewModel.selectMarker(myLoc)
                            false // keep default info window behavior
                        }
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Color(0xFF1565C0), CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                }


                // User markers
                ui.markers.forEach { m ->
                    Marker(
                        state = MarkerState(m),
                        title = "Marker",
                        snippet = "${"%.5f".format(m.latitude)}, ${"%.5f".format(m.longitude)}",
                        onClick = {
                            if (ui.mode == InteractionMode.DELETING) {
                                viewModel.deleteMarker(m); true
                            } else {
                                viewModel.selectMarker(m); false
                            }
                        }
                    )
                }
            }


            // Action FABs
            Column(
                Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionFab(Icons.Default.Add, "Add marker") {
                    viewModel.dismissWeather()
                    viewModel.enterAddMode()
                    scope.launch { snackbar.showSnackbar("Add mode: tap the map once") }
                }
                ActionFab(Icons.Default.Delete, "Delete marker") {
                    viewModel.dismissWeather()
                    viewModel.enterDeleteMode()
                    scope.launch { snackbar.showSnackbar("Delete mode: tap a marker, or tap map to cancel") }
                }
                ActionFab(Icons.Default.LocationOn, "Re-center") {
                    viewModel.dismissWeather()
                    ui.currentLocation?.let { loc ->
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 15f)) }
                    }
                }
            }


            // Weather panel
            AnimatedVisibility(
                visible = ui.selectedMarker != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
            ) {
                WeatherCard(
                    isLoading = ui.isWeatherLoading,
                    location = ui.weatherLocation,
                    days = ui.weatherDays,
                    onDismiss = { viewModel.dismissWeather() }
                )
            }
        }
    }
}


@Composable
private fun ActionFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Icon(icon, contentDescription = desc)
    }
}


@Composable
private fun RequestLocationPermission(
    requestNow: Boolean,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onResult(fine || coarse)
    }
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) onResult(true)
    }
    LaunchedEffect(requestNow) {
        if (requestNow) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}


/* ----------------------- Weather Card ----------------------- */


@Composable
private fun WeatherCard(
    isLoading: Boolean,
    location: LatLng?,
    days: List<DayWeather>,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (location != null)
                        "Location: ${formatCoord(location.latitude, 'N', 'S')} - ${formatCoord(location.longitude, 'E', 'W')}"
                    else "Location: —",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Dismiss",
                    modifier = Modifier.clickable { onDismiss() }.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Spacer(Modifier.height(10.dp))


            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    items(days) { d -> DayPill(d) }
                }
            }
        }
    }
}


@Composable
private fun DayPill(d: DayWeather) {
    val bg =
        if (d.isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg =
        if (d.isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .width(64.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(d.label, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(weatherEmoji(d.weatherCode), fontSize = MaterialTheme.typography.headlineSmall.fontSize)
        Text("${d.precipitationPct}%", color = fg, style = MaterialTheme.typography.labelSmall)
        Text("${d.tempMax}°", color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("${d.tempMin}°", color = fg, style = MaterialTheme.typography.labelSmall)
    }
}


private fun formatCoord(value: Double, pos: Char, neg: Char): String {
    val hemi = if (value >= 0) pos else neg
    val abs = kotlin.math.abs(value)
    return String.format("%.2f°%c", abs, hemi)
}


private fun weatherEmoji(code: Int): String = when (code) {
    0 -> "☀️"             // clear sky
    1, 2 -> "🌤️"          // mostly clear / partly cloudy
    3 -> "☁️"             // overcast
    in 45..48 -> "🌫️"     // fog
    in 51..67 -> "🌦️"     // drizzle
    in 71..77 -> "❄️"     // snow
    in 80..82 -> "🌧️"     // rain showers
    in 95..99 -> "⛈️"     // thunder
    else -> "🌡️"
}
