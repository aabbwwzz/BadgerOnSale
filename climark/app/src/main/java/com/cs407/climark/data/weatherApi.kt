
package com.cs407.climark.data


import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// ---- Minimal DTOs (names must match the API!) ----
data class WeatherApiResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val daily: DailyData?
)


data class DailyData(
    val time: List<String>?,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>?,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>?,
    @SerializedName("precipitation_probability_mean") val precipMean: List<Int>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?
)


interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        // ask for exactly the fields we parse above
        @Query("daily")
        daily: String = "temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weather_code",
        @Query("timezone") timezone: String = "auto",
        // 7 days history + 7 days forecast -> full two-week strip
        @Query("past_days") pastDays: Int = 7,
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherApiResponse
}


object WeatherApi {
    private const val BASE_URL = "https://api.open-meteo.com/"


    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
