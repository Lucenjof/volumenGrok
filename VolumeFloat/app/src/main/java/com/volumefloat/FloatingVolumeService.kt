package com.volumefloat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class FloatingVolumeService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var volumeBarContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var volumeText: TextView
    private lateinit var audioManager: AudioManager

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideVolumeBar() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(1, createNotification())

        setupFloatingButton()
        setupVolumeBar()
    }

    private fun setupFloatingButton() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 400
        }

        val button = floatingView.findViewById<ImageView>(R.id.btnVolume)

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = abs(event.rawX - initialTouchX)
                    val dy = abs(event.rawY - initialTouchY)
                    if (dx < 20 && dy < 20) {
                        showVolumeBar()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingView, params)
    }

    private fun setupVolumeBar() {
        volumeBarContainer = LayoutInflater.from(this).inflate(R.layout.volume_bar, null)
        progressBar = volumeBarContainer.findViewById(R.id.progressVolume)
        volumeText = volumeBarContainer.findViewById(R.id.txtVolume)

        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        progressBar.max = max

        volumeBarContainer.findViewById<View>(R.id.btnUp).setOnClickListener {
            changeVolume(AudioManager.ADJUST_RAISE)
        }
        volumeBarContainer.findViewById<View>(R.id.btnDown).setOnClickListener {
            changeVolume(AudioManager.ADJUST_LOWER)
        }
        volumeBarContainer.findViewById<View>(R.id.btnMute).setOnClickListener {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (current > 0) {
                // flags = 0 → NO se muestra el fader del sistema
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            } else {
                val half = max / 2
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, half, 0)
            }
            updateVolumeBar()
            resetHideTimer()
        }
    }

    private fun showVolumeBar() {
        if (volumeBarContainer.parent != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        updateVolumeBar()
        windowManager.addView(volumeBarContainer, params)
        resetHideTimer()
    }

    private fun hideVolumeBar() {
        if (volumeBarContainer.parent != null) {
            try {
                windowManager.removeView(volumeBarContainer)
            } catch (_: Exception) {
            }
        }
    }

    private fun changeVolume(direction: Int) {
        // flags = 0 → NO aparece la barra de volumen del sistema
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            0
        )
        updateVolumeBar()
        resetHideTimer()
    }

    private fun updateVolumeBar() {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        progressBar.progress = current
        volumeText.text = "$current / $max"
    }

    private fun resetHideTimer() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 2500)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "volume_float",
                "Control de volumen flotante",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el botón flotante activo"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "volume_float")
            .setContentTitle("VolumeFloat activo")
            .setContentText("Botón flotante de volumen")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideHandler.removeCallbacks(hideRunnable)
        try {
            if (::floatingView.isInitialized && floatingView.parent != null) {
                windowManager.removeView(floatingView)
            }
        } catch (_: Exception) {
        }
        hideVolumeBar()
    }
}
