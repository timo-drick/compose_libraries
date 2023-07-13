package de.drick.compose.sample.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberRotationVector(): State<FloatArray> {
    val ctx = LocalContext.current
    val attitudeManager = remember {
        AttitudeManager(ctx)
    }
    DisposableEffect(Unit) {
        attitudeManager.start()
        onDispose {
            attitudeManager.stop()
        }
    }
    return attitudeManager.rotationVector
}

class AttitudeManager(ctx: Context) {
    private val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = requireNotNull(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))
    var rotationVector: MutableState<FloatArray> = mutableStateOf(floatArrayOf(1f, 0f, 0f))

    fun start() {
        sensorManager.registerListener(listener, rotationSensor, 10000)
        //SensorManager.getRotationMatrix()
    }
    fun stop() {
        sensorManager.unregisterListener(listener)
    }
    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                rotationVector.value = event.values
                //SensorManager.getRotationMatrixFromVector()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }
    private fun setDisplayRotation(r: Int) {
        /*when (r) {
            android.view.Surface.ROTATION_0 -> {
                axe[0] = 0
                axe[1] = 1
                inverted[0] = false
                inverted[1] = false
                log { "Rotation 0" }
            }
            android.view.Surface.ROTATION_90 -> {
                axe[0] = 1
                axe[1] = 0
                inverted[0] = true
                inverted[1] = false
                log { "Rotation 90" }
            }
            android.view.Surface.ROTATION_180 -> {
                axe[0] = 0
                axe[1] = 1
                inverted[0] = true
                inverted[1] = true
                log { "Rotation 180" }
            }
            android.view.Surface.ROTATION_270 -> {
                axe[0] = 1
                axe[1] = 0
                inverted[0] = false
                inverted[1] = true
                log { "Rotation 270" }
            }
        }*/
    }
}
