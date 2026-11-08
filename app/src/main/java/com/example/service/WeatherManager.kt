package com.example.service

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class CityPreset(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

val POPULAR_CITIES = listOf(
    CityPreset("New York", "USA", 40.7128, -74.0060),
    CityPreset("London", "UK", 51.5074, -0.1278),
    CityPreset("Tokyo", "Japan", 35.6762, 139.6503),
    CityPreset("Paris", "France", 48.8566, 2.3522),
    CityPreset("San Francisco", "USA", 37.7749, -122.4194),
    CityPreset("Berlin", "Germany", 52.5200, 13.4050),
    CityPreset("Sydney", "Australia", -33.8688, 151.2093),
    CityPreset("Singapore", "Singapore", 1.3521, 103.8198),
    CityPreset("Sofia", "Bulgaria", 42.6977, 23.3219),
    CityPreset("Toronto", "Canada", 43.6532, -79.3832)
)

data class LiveWeatherData(
    val cityName: String = "Aurora City",
    val temperatureC: Int = 21,
    val conditionText: String = "Clear Refraction",
    val weatherCode: Int = 0,
    val minTempC: Int = 16,
    val maxTempC: Int = 24,
    val humidityPercent: Int = 45,
    val windSpeedKmh: Int = 12,
    val uvIndex: Int = 4,
    val isRefreshing: Boolean = false,
    val lastUpdated: String = "Just now"
)

class WeatherManager(private val context: Context, private val scope: CoroutineScope) {

    private val prefs = context.getSharedPreferences("comet_weather_cache", Context.MODE_PRIVATE)

    private val _weather = MutableStateFlow(
        LiveWeatherData(
            cityName = prefs.getString("city", "Aurora City") ?: "Aurora City",
            temperatureC = prefs.getInt("temp", 21),
            conditionText = prefs.getString("condition", "Clear Refraction") ?: "Clear Refraction",
            weatherCode = prefs.getInt("code", 0),
            minTempC = prefs.getInt("min", 16),
            maxTempC = prefs.getInt("max", 24)
        )
    )
    val weather: StateFlow<LiveWeatherData> = _weather.asStateFlow()

    init {
        // Initial refresh
        fetchWeather(_weather.value.cityName, 40.7128, -74.0060)
    }

    fun setLocation(city: String, lat: Double, lon: Double) {
        prefs.edit().putString("city", city).apply()
        fetchWeather(city, lat, lon)
    }

    fun refresh(city: String, lat: Double, lon: Double) {
        fetchWeather(city, lat, lon)
    }

    private fun fetchWeather(city: String, lat: Double, lon: Double) {
        scope.launch(Dispatchers.IO) {
            _weather.value = _weather.value.copy(isRefreshing = true, cityName = city)
            try {
                val urlString = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto",
                    lat, lon
                )
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val current = json.getJSONObject("current")
                    val daily = json.getJSONObject("daily")

                    val temp = current.getDouble("temperature_2m").toInt()
                    val humidity = current.getInt("relative_humidity_2m")
                    val code = current.getInt("weather_code")
                    val wind = current.getDouble("wind_speed_10m").toInt()

                    val maxTemps = daily.getJSONArray("temperature_2m_max")
                    val minTemps = daily.getJSONArray("temperature_2m_min")
                    val maxT = if (maxTemps.length() > 0) maxTemps.getDouble(0).toInt() else temp + 3
                    val minT = if (minTemps.length() > 0) minTemps.getDouble(0).toInt() else temp - 4

                    val condition = decodeWeatherCode(code)

                    prefs.edit()
                        .putString("city", city)
                        .putInt("temp", temp)
                        .putString("condition", condition)
                        .putInt("code", code)
                        .putInt("min", minT)
                        .putInt("max", maxT)
                        .apply()

                    _weather.value = LiveWeatherData(
                        cityName = city,
                        temperatureC = temp,
                        conditionText = condition,
                        weatherCode = code,
                        minTempC = minT,
                        maxTempC = maxT,
                        humidityPercent = humidity,
                        windSpeedKmh = wind,
                        uvIndex = 4,
                        isRefreshing = false,
                        lastUpdated = "Updated just now"
                    )
                    return@launch
                }
            } catch (e: Exception) {
                // If offline or network error, keep cached or fallback gracefully
            }
            _weather.value = _weather.value.copy(isRefreshing = false)
        }
    }

    private fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy Horizon"
            51, 53, 55 -> "Light Drizzle"
            61, 63 -> "Liquid Rain"
            65 -> "Heavy Downpour"
            71, 73, 75 -> "Snow Flurries"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Electric Thunderstorm"
            else -> "Atmospheric Flow"
        }
    }
}
