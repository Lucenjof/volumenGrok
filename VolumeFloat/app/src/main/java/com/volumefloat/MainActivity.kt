package com.volumefloat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        btnStart.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startFloatingService()
            } else {
                requestOverlayPermission()
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FloatingVolumeService::class.java))
            Toast.makeText(this, "Botón flotante detenido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
        Toast.makeText(
            this,
            "Activá el permiso 'Mostrar sobre otras apps' y volvé a la app",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingVolumeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Botón flotante activado", Toast.LENGTH_SHORT).show()
        finish()
    }
}
