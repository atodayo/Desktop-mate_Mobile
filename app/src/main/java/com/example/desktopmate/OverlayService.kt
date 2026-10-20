package com.example.desktopmate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private var characterView: ImageView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var dragging = false
    private var direction = 1
    private val speed = 4

    private val walkRunnable = object : Runnable {
        override fun run() {
            val view = characterView ?: return
            if (!dragging) {
                val width = if (view.width > 0) view.width else 300
                val maxX = resources.displayMetrics.widthPixels - width
                params.x += speed * direction
                if (params.x >= maxX) {
                    params.x = maxX
                    direction = -1
                } else if (params.x <= 0) {
                    params.x = 0
                    direction = 1
                }
                view.scaleX = if (direction == 1) 1f else -1f
                windowManager.updateViewLayout(view, params)
            }
            handler.postDelayed(this, 30)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        showCharacter()
    }

    private fun startAsForeground() {
        val channelId = "overlay_character"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(channelId, "キャラ表示", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Desktop Mate Mobile")
            .setContentText("キャラが歩いてるよ")
            .setSmallIcon(R.drawable.character)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun showCharacter() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val view = ImageView(this)
        view.setImageResource(R.drawable.character)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = resources.displayMetrics.heightPixels / 2

        view.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dragging = true
                        startX = params.x
                        startY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(v, params)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        dragging = false
                    }
                }
                return true
            }
        })

        windowManager.addView(view, params)
        characterView = view
        handler.post(walkRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(walkRunnable)
        characterView?.let { windowManager.removeView(it) }
        characterView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
