package com.ilsa1000ri.weatherSecretary.ui.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ilsa1000ri.weatherSecretary.R

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "TTS_NOTIFICATION_CHANNEL"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TTS Notification Channel"
            val descriptionText = "Channel for TTS notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification() {
        // 권한 확인
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notificationIntent = Intent(context, TtsService::class.java)
            val pendingIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("날씨비서")
                .setContentText("오늘의 브리핑을 들으시겠습니까?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(1, notification)
            }
        } else {
            // 권한이 없을 경우 권한 요청 로직을 추가 가능
            // 예: 사용자에게 권한 요청 대화상자를 표시하는 코드 추가
        }
    }
}
