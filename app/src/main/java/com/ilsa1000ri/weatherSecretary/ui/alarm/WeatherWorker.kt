package com.ilsa1000ri.weatherSecretary.ui.alarm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.ilsa1000ri.weatherSecretary.ui.api.RealShortApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WeatherWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override suspend fun doWork(): Result {
        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("WeatherWorker", "latitude, longitude = $latitude, $longitude")
                }
            }.await()

            return withContext(Dispatchers.IO) {
                try {
                    val weatherData = RealShortApi.getRealShortIndex(latitude, longitude)
                    Log.d("WeatherWorker", "Weather data: $weatherData")

                    val currentPTY = weatherData.firstOrNull { it.startsWith("PTY:") }
                    val nextHourPTY = weatherData.drop(1).firstOrNull { it.startsWith("PTY:") }

                    Log.d("WeatherWorker", "Current PTY: $currentPTY")
                    Log.d("WeatherWorker", "Next Hour PTY: $nextHourPTY")

                    val currentPTYValue = extractPTYValue(currentPTY)
                    val nextHourPTYValue = extractPTYValue(nextHourPTY)

                    Log.d("WeatherWorker", "Current PTY Value: $currentPTYValue")
                    Log.d("WeatherWorker", "Next Hour PTY Value: $nextHourPTYValue")

                    if (currentPTYValue != null && nextHourPTYValue != null) {
                        val isCurrentPTYRain = currentPTYValue == "비"
                        val isNextHourPTYRain = nextHourPTYValue == "비"

                        if (isCurrentPTYRain xor isNextHourPTYRain) { // 둘 중 하나만 "비"일 때
                            scheduleNotificationWorker(currentPTYValue, nextHourPTYValue)
                            Log.d("WeatherWorker", "One of current or next hour PTY is '비', so notification scheduled")
                        } else if (isCurrentPTYRain or isNextHourPTYRain) {
                            Log.d("WeatherWorker", "Both or neither PTY values are '비', so no notification")
                        }
                    } else {
                        Log.d("WeatherWorker", "PTY values are null, so nothing happens")
                    }

                    Result.success()
                } catch (e: Exception) {
                    Log.e("WeatherWorker", "Error fetching weather data", e)
                    Result.failure()
                }
            }
        } else {
            Log.e("WeatherWorker", "Location permission not granted")
            return Result.failure()
        }
    }

    private fun extractPTYValue(ptyString: String?): String? {
        val regex = Regex("PTY: (\\S+)")
        val matchResult = ptyString?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)?.replace(",", "")
    }

    private fun scheduleNotificationWorker(currentPTY: String, nextHourPTY: String) {
        val inputData = Data.Builder()
            .putString("currentPTY", currentPTY)
            .putString("nextHourPTY", nextHourPTY)
            .build()

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(appContext).enqueue(notificationWorkRequest)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
