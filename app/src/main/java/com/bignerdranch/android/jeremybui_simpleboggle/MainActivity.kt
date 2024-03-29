package com.bignerdranch.android.jeremybui_simpleboggle

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class MainActivity : AppCompatActivity(), GameFragment.GameInteractionListener {
    private lateinit var sensorManager: SensorManager
    private var shakeDetector: ShakeDetector? = null
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector { onRestartGame() }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.gameFragmentContainer, GameFragment())
                replace(R.id.scoreFragmentContainer, ScoreFragment())
                commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector?.also { detector ->
            sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        shakeDetector?.also { detector ->
            sensorManager.unregisterListener(detector)
        }
    }

    override fun onScoreUpdated(score: Int) {
        val scoreFragment = supportFragmentManager.findFragmentById(R.id.scoreFragmentContainer) as? ScoreFragment
        scoreFragment?.updateScore(score)
    }

    fun onRestartGame() {
        val gameFragment = supportFragmentManager.findFragmentById(R.id.gameFragmentContainer) as? GameFragment
        gameFragment?.resetGame()
        onScoreUpdated(0)
    }

    private class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
        private var lastTime: Long = 0
        private var lastX: Float = 0.0f
        private var lastY: Float = 0.0f
        private var lastZ: Float = 0.0f
        private val shakeThreshold = 800

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastTime) > 100) {
                val x = event?.values?.get(0) ?: 0f
                val y = event?.values?.get(1) ?: 0f
                val z = event?.values?.get(2) ?: 0f

                val speed = Math.sqrt(((x - lastX).toDouble().pow(2.0) + (y - lastY).toDouble().pow(2.0) + (z - lastZ).toDouble().pow(2.0))) / (currentTime - lastTime) * 10000
                if (speed > shakeThreshold) {
                    onShake()
                }

                lastTime = currentTime
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }
}

