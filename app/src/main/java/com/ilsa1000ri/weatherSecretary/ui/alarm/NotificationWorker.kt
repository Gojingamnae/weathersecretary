package com.ilsa1000ri.weatherSecretary.ui.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ilsa1000ri.weatherSecretary.MainActivity
import com.ilsa1000ri.weatherSecretary.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val currentPTY = inputData.getString("currentPTY") ?: ""
                val nextHourPTY = inputData.getString("nextHourPTY") ?: ""

                Log.d(
                    "NotificationWorker",
                    "Showing notification: Current PTY = $currentPTY, Next Hour PTY = $nextHourPTY"
                )
                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "weather_channel",
                        "Weather Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Weather change alerts"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                // 알림 클릭 시 실행될 인텐트 생성
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(applicationContext, "weather_channel")
                    .setSmallIcon(R.drawable.ic_home_black_24dp)
                    .setContentTitle("날씨비서")
                    .setContentText("1시간 뒤 $nextHourPTY 옵니다.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)  // 알림 클릭 시 실행될 인텐트 설정
                    .setAutoCancel(true)  // 알림 클릭 시 자동으로 알림 제거
                    .build()

                NotificationManagerCompat.from(applicationContext).notify(1, notification)

                Result.success()
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Error showing notification", e)
                Result.failure()
            }
        }
    }

}

