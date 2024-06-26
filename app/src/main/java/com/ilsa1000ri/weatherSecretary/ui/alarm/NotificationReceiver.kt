package com.ilsa1000ri.weatherSecretary.ui.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ilsa1000ri.weatherSecretary.R
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val summary = intent.getStringExtra("summary")
        val startDateMillis = intent.getLongExtra("startDate", 0)
        val endDateMillis = intent.getLongExtra("endDate", 0)

        val startDate = Date(startDateMillis)
        val endDate = Date(endDateMillis)

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)

        val notificationText = "$startDateStr ~ $endDateStr $summary"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "schedule_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "일정 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)  // 알림 아이콘 설정
            .setContentTitle("날씨비서")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}