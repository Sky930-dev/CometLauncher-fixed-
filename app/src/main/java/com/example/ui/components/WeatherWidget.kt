package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.CityPreset
import com.example.service.LiveWeatherData
import com.example.service.POPULAR_CITIES
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherWidget(
    weather: LiveWeatherData,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    glassCornerRadiusDp: Int = 26,
    isCelsius: Boolean = true,
    onToggleUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onSelectCity: (name: String, lat: Double, lon: Double) -> Unit = { _, _, _ -> }
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCitySelector by remember { mutableStateOf(false) }

    val displayTemp = if (isCelsius) "${weather.temperatureC}°" else "${(weather.temperatureC * 9 / 5) + 32}°"
    val displayMin = if (isCelsius) "${weather.minTempC}°" else "${(weather.minTempC * 9 / 5) + 32}°"
    val displayMax = if (isCelsius) "${weather.maxTempC}°" else "${(weather.maxTempC * 9 / 5) + 32}°"
    val unitLabel = if (isCelsius) "°C" else "°F"

    val weatherIcon = when {
        weather.weatherCode in listOf(0, 1) -> Icons.Rounded.WbSunny
        weather.weatherCode in listOf(2, 3) -> Icons.Rounded.WbCloudy
        weather.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Rounded.WaterDrop
        weather.weatherCode in listOf(95, 96, 99) -> Icons.Rounded.Thunderstorm
        else -> Icons.Rounded.Cloud
    }

    val iconColor = when {
        weather.weatherCode in listOf(0, 1) -> Color(0xFFFFD166)
        weather.weatherCode in listOf(2, 3) -> Color(0xFF90E0EF)
        weather.weatherCode in listOf(95, 96, 99) -> Color(0xFF9D4EDD)
        else -> accentColor
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(glassCornerRadiusDp.dp),
        backgroundColor = Color(0x30131B45),
        accentGlow = Color(0xFF4CC9F0),
        onClick = { showDetailDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = weather.conditionText,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weather.conditionText,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showCitySelector = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Location",
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${weather.cityName} • H: $displayMax L: $displayMin",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Temperature & Unit Pill + Refresh
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayTemp,
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .clickable { onToggleUnit() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = unitLabel,
                        color = accentColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (weather.isRefreshing) {
                        CircularProgressIndicator(
                            color = accentColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Weather Detail Dialog
    if (showDetailDialog) {
        BasicAlertDialog(onDismissRequest = { showDetailDialog = false }) {
            LiquidGlassCard(
                shape = RoundedCornerShape(32.dp),
                backgroundColor = Color(0xF00D1233),
                borderColor = accentColor.copy(alpha = 0.6f),
                accentGlow = accentColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weather.cityName,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.25f))
                                        .clickable {
                                            showDetailDialog = false
                                            showCitySelector = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Change City",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = "${weather.conditionText} • Live Atmospheric Refraction",
                                color = accentColor,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { showDetailDialog = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Detail stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherStatPill(Icons.Rounded.WaterDrop, "Humidity", "${weather.humidityPercent}%", accentColor)
                        WeatherStatPill(Icons.Rounded.Air, "Wind Speed", "${weather.windSpeedKmh} km/h", accentColor)
                        WeatherStatPill(Icons.Rounded.Thermostat, "UV Index", "${weather.uvIndex} Mod", accentColor)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "5-Day Forecast",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val days = listOf(
                        Triple("Tomorrow", "Clear Skies", displayMax to displayMin),
                        Triple("Sunday", "Solar Radiance", "${weather.maxTempC + 1}°" to "${weather.minTempC + 1}°"),
                        Triple("Monday", "Atmospheric Mist", "${weather.maxTempC - 1}°" to "${weather.minTempC - 1}°"),
                        Triple("Tuesday", "Liquid Showers", "${weather.maxTempC - 2}°" to "${weather.minTempC - 2}°")
                    )

                    days.forEach { (day, cond, temps) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = day, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.width(90.dp))
                            Text(text = cond, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(text = "${temps.first} / ${temps.second}", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // City Selector Dialog
    if (showCitySelector) {
        BasicAlertDialog(onDismissRequest = { showCitySelector = false }) {
            LiquidGlassCard(
                shape = RoundedCornerShape(28.dp),
                backgroundColor = Color(0xF2090F2C),
                borderColor = accentColor.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                var searchQuery by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose Weather Location",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showCitySelector = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // City search input
                    LiquidGlassCard(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x35192252),
                        borderColor = accentColor.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search or type city...", color = TextSecondary, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Popular World Cities", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredCities = POPULAR_CITIES.filter {
                        if (searchQuery.isBlank()) true else it.name.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCities) { city ->
                            val isSelected = city.name.equals(weather.cityName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                    .clickable {
                                        onSelectCity(city.name, city.lat, city.lon)
                                        showCitySelector = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = city.name, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = city.country, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Icon(Icons.Rounded.NearMe, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Allow custom typed city fallback
                        if (searchQuery.isNotBlank() && filteredCities.none { it.name.equals(searchQuery, true) }) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.25f))
                                        .clickable {
                                            onSelectCity(searchQuery.trim(), 40.7128, -74.0060)
                                            showCitySelector = false
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(text = "Use \"$searchQuery\"", color = TextPrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0x281B2455),
        modifier = Modifier.width(92.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
