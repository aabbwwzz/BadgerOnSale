package com.cs407.climark.ui.viewModels


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.climark.data.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt


// --------- UI models ---------
enum class InteractionMode { NORMAL, ADDING, DELETING }


data class DayWeather(
    val label: String,          // e.g., MON / TUE
    val precipitationPct: Int,  // 0..100
    val tempMax: Int,           // rounded °C (or °F if you later convert)
    val tempMin: Int,
    val weatherCode: Int,
    val isToday: Boolean
)


data class MapState(
    val markers: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val selectedMarker: LatLng? = null,
    val mode: InteractionMode = InteractionMode.NORMAL,
    val locationPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,


    // Weather panel
    val weatherLocation: LatLng? = null,
    val weatherDays: List<DayWeather> = emptyList(),
    val isWeatherLoading: Boolean = false
)


class MapViewModel : ViewModel() {


    private val _ui = MutableStateFlow(MapState())
    val uiState = _ui.asStateFlow()


    private var fusedLocationClient: FusedLocationProviderClient? = null


    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }


    fun updateLocationPermission(granted: Boolean) {
        _ui.update { it.copy(locationPermissionGranted = granted) }
    }


    // --------- Location ----------
    fun getCurrentLocation() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            try {
                if (!_ui.value.locationPermissionGranted) {
                    _ui.update { it.copy(isLoading = false, error = "Location permission not granted") }
                    return@launch
                }
                val client = fusedLocationClient ?: error("Location client not initialized")
                val token = CancellationTokenSource().token
                val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token).await()


                if (loc != null) {
                    val here = LatLng(loc.latitude, loc.longitude)
                    _ui.update {
                        it.copy(
                            currentLocation = here,
                            // keep a marker at current location if list empty
                            markers = if (it.markers.isEmpty()) listOf(here) else it.markers,
                            isLoading = false
                        )
                    }
                } else {
                    _ui.update { it.copy(isLoading = false, error = "Location unavailable") }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }


    // --------- Marker modes (Milestone 2) ----------
    fun enterAddMode()  = _ui.update { it.copy(mode = InteractionMode.ADDING, selectedMarker = null) }
    fun enterDeleteMode() = _ui.update { it.copy(mode = InteractionMode.DELETING, selectedMarker = null) }
    fun exitMode()       = _ui.update { it.copy(mode = InteractionMode.NORMAL) }


    fun addMarkerOnce(position: LatLng) {
        _ui.update { it.copy(markers = it.markers + position) }
        exitMode()
    }


    fun deleteMarker(position: LatLng) {
        val cur = _ui.value.currentLocation
        if (cur != null && almostEqual(cur, position)) return // current location cannot be deleted
        _ui.update { it.copy(markers = it.markers.filterNot { m -> almostEqual(m, position) }) }
        exitMode()
    }


    // --------- Weather (Milestone 3) ----------
    fun selectMarker(marker: LatLng) {
        _ui.update {
            it.copy(
                selectedMarker = marker,
                weatherLocation = marker,
                isWeatherLoading = true,
                weatherDays = emptyList()
            )
        }
        fetchWeather(marker)
    }


    fun dismissWeather() {
        _ui.update { it.copy(selectedMarker = null) }
    }


    private fun fetchWeather(latLng: LatLng) {
        viewModelScope.launch {
            try {
                val resp = WeatherApi.service.getWeatherData(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )


                val daily = resp.daily
                if (daily == null ||
                    daily.time.isNullOrEmpty() ||
                    daily.tempMax.isNullOrEmpty() ||
                    daily.tempMin.isNullOrEmpty() ||
                    daily.precipMean.isNullOrEmpty() ||
                    daily.weatherCode.isNullOrEmpty()
                ) {
                    _ui.update { it.copy(isWeatherLoading = false, error = "Weather data is empty") }
                    return@launch
                }


                val fmt = DateTimeFormatter.ISO_LOCAL_DATE
                val out = buildList {
                    val today = LocalDate.now()
                    val count = daily.time.size
                    for (i in 0 until count) {
                        val date = runCatching { LocalDate.parse(daily.time[i], fmt) }.getOrNull()
                        val label = date?.dayOfWeek?.name?.take(3) ?: "DAY"
                        add(
                            DayWeather(
                                label = label,
                                precipitationPct = daily.precipMean[i].coerceIn(0, 100),
                                tempMax = daily.tempMax[i].roundToInt(),
                                tempMin = daily.tempMin[i].roundToInt(),
                                weatherCode = daily.weatherCode[i],
                                isToday = date == today
                            )
                        )
                    }
                }


                _ui.update { it.copy(isWeatherLoading = false, weatherDays = out) }
            } catch (e: Exception) {
                _ui.update { it.copy(isWeatherLoading = false, error = e.message ?: "Weather error") }
            }
        }
    }
}


private fun almostEqual(a: LatLng, b: LatLng): Boolean {
    val dLat = abs(a.latitude - b.latitude)
    val dLng = abs(a.longitude - b.longitude)
    return dLat < 0.00002 && dLng < 0.00002
}

