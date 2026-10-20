package com.example.desktopmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(48, 48, 48, 48)

        val title = TextView(this)
        title.text = "Desktop Mate Mobile"
        title.textSize = 22f
        title.gravity = Gravity.CENTER

        val startButton = Button(this)
        startButton.text = "キャラを出す"
        startButton.setOnClickListener { startCharacter() }

        val stopButton = Button(this)
        stopButton.text = "キャラを消す"
        stopButton.setOnClickListener {
            stopService(Intent(this@MainActivity, OverlayService::class.java))
        }

        layout.addView(title)
        layout.addView(startButton)
        layout.addView(stopButton)
        setContentView(layout)
    }

    private fun startCharacter() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "「他のアプリの上に表示」をオンにしてね", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        startForegroundService(Intent(this, OverlayService::class.java))
    }
}
