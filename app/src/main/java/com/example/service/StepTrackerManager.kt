package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StepTelemetry(
    val steps: Int = 4280,
    val goal: Int = 8000,
    val distanceKm: Float = 3.26f,
    val caloriesKcal: Int = 171,
    val isHardwareSensorActive: Boolean = false
) {
    val progress: Float
        get() = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
}

class StepTrackerManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val prefs = context.getSharedPreferences("comet_step_cache", Context.MODE_PRIVATE)

    private val todayKey: String
        get() = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private var initialHardwareOffset: Int = -1

    private val _telemetry = MutableStateFlow(
        StepTelemetry(
            steps = prefs.getInt("steps_$todayKey", 4280),
            goal = prefs.getInt("goal", 8000),
            isHardwareSensorActive = false
        )
    )
    val telemetry: StateFlow<StepTelemetry> = _telemetry.asStateFlow()

    fun startListening(goal: Int) {
        val savedSteps = prefs.getInt("steps_$todayKey", 4280)
        updateSteps(savedSteps, goal)

        var registered = false
        if (sensorManager != null) {
            if (stepCounterSensor != null) {
                registered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            } else if (stepDetectorSensor != null) {
                registered = sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        _telemetry.value = _telemetry.value.copy(
            goal = goal,
            isHardwareSensorActive = registered
        )
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (ignored: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                if (initialHardwareOffset < 0) {
                    val savedSteps = prefs.getInt("steps_$todayKey", 0)
                    initialHardwareOffset = (totalSteps - savedSteps).coerceAtLeast(0)
                }
                val todaySteps = (totalSteps - initialHardwareOffset).coerceAtLeast(0)
                updateSteps(todaySteps, _telemetry.value.goal)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                val increment = event.values[0].toInt()
                if (increment > 0) {
                    val newSteps = _telemetry.value.steps + increment
                    updateSteps(newSteps, _telemetry.value.goal)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun addManualSteps(count: Int) {
        val newSteps = _telemetry.value.steps + count
        updateSteps(newSteps, _telemetry.value.goal)
    }

    fun setGoal(newGoal: Int) {
        prefs.edit().putInt("goal", newGoal).apply()
        updateSteps(_telemetry.value.steps, newGoal)
    }

    private fun updateSteps(steps: Int, goal: Int) {
        prefs.edit().putInt("steps_$todayKey", steps).apply()
        val dist = steps * 0.000762f // km
        val cal = (steps * 0.04f).toInt() // kcal
        _telemetry.value = _telemetry.value.copy(
            steps = steps,
            goal = goal,
            distanceKm = (dist * 100).toInt() / 100f,
            caloriesKcal = cal
        )
    }
}
