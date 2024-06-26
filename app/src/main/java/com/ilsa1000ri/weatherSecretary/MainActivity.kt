//MainActivity
package com.ilsa1000ri.weatherSecretary

import java.lang.System
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.ui.api.LocationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.Settings.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.ilsa1000ri.weatherSecretary.databinding.ActivityMainBinding
import com.ilsa1000ri.weatherSecretary.ui.alarm.NotificationReceiver
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Calendar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import android.provider.Settings
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ilsa1000ri.weatherSecretary.ui.alarm.WeatherWorker
import java.util.concurrent.TimeUnit
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.PutDataMapRequest
import com.ilsa1000ri.weatherSecretary.ui.alarm.AlarmReceiver
import com.ilsa1000ri.weatherSecretary.ui.alarm.NotificationHelper
import java.util.*

class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private var locationResult : String = ""
    private val REQUEST_CODE_PERMISSIONS = 1001
    private var todaysSchedule: List<Pair<String, Date>>? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupNotifications()
            setupBriefingAlarm()
            initLocation()
        } else {
            // 권한이 거부된 경우 처리
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    setupNotifications()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

                }
            }
        } else {
            setupNotifications()
        }

        if (checkLocationPermission()) {
            initLocation()
        } else {
            requestLocationPermission()
        }

        // ActivityMainBinding을 사용하여 레이아웃 인플레이트
        binding = ActivityMainBinding.inflate(layoutInflater)

        // root를 setContentView에 전달하여 레이아웃 설정
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_weather,
                R.id.navigation_home,
                R.id.navigation_timetable,
                R.id.navigation_calendar,
                R.id.navigation_friends
            )
        )
        binding.navView.setupWithNavController(navController)

        // 위치 권한이 있는지 확인
        if (checkLocationPermission()) {
            initLocation()
            // 위치 권한이 허용되어 있으면 위치 요청 초기화
        } else {
            // 위치 권한을 요청
            requestLocationPermission()
        }
    }

    private fun setupBriefingAlarm() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        // 현재 사용자가 null이 아닌지 확인
        if (currentUser == null) {
            Log.d("로그인 안됨", "User not logged in")
            return
        }
        val userId = currentUser.uid
        db.collection(userId).document("Alarm")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val isAlarmEnabled = document.getBoolean("isAlarmEnabled") ?: false
                    val briefingTime = document.getString("briefingTime") ?: "12:00"
                    if (isAlarmEnabled) {
                        setupAlarm(briefingTime)
                        // 오늘의 일정 가져오기
                        fetchTodaysSchedule { scheduleList ->
                            // 일정을 가져왔으면 sendToWatchTTS 함수를 호출하여 일정과 briefingTime을 전달
                            sendToWatchTTS(scheduleList, briefingTime)
                        }
                    }
                } else {
                    Log.d("흠냐", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("흠냐", "get failed with ", exception)
            }
    }
    private fun fetchTodaysSchedule(completion: (List<Pair<String, Date>>) -> Unit) {
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d("오늘 날짜", "$todayDateString")
        currentUser?.let { user ->
            val userId = user.uid
            val userDocument = db.collection(userId).document(todayDateString)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // 해당 날짜의 모든 서브 컬렉션에 대한 쿼리 생성
                        val collectionQueries = (0..23).flatMap { hour ->
                            (0..59).map { minute ->
                                val hourString =
                                    String.format("%02d:%02d", hour, minute) // 시간을 두 자리 숫자로 포맷
                                userDocument.collection(hourString)
                            }
                        }

                        val scheduleList = mutableListOf<Pair<String, Date>>()

                        // 각 컬렉션에 대해 일정을 가져오고 출력
                        val fetchTasks = collectionQueries.map { collection ->
                            collection.get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        val summary = document.getString("summary")
                                        val startDate = document.getTimestamp("startDate")?.toDate()

                                        Log.d("일정", "Summary: $summary")
                                        Log.d("일정", "Start Date: $startDate")

                                        // 가져온 일정을 리스트에 추가
                                        if (summary != null && startDate != null) {
                                            scheduleList.add(summary to startDate)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "ScheduleManager",
                                        "Error fetching schedule data for collection: ${e.message}"
                                    )
                                }
                        }

                        // 모든 일정 가져오기가 완료되면 콜백을 통해 일정 데이터 반환
                        Tasks.whenAllComplete(fetchTasks)
                            .addOnSuccessListener {
                                completion(scheduleList.toList())
                            }
                            .addOnFailureListener { e ->
                                Log.e("ScheduleManager", "Error fetching all schedule data: ${e.message}")
                            }
                    } else {
                        Log.d("일정", "No documents found for $todayDateString")
                        completion(emptyList()) // 일정이 없는 경우 빈 리스트 반환
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "ScheduleManager",
                        "Error fetching document for $todayDateString: ${e.message}"
                    )
                }
        }
    }
    private fun setupNotifications() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            // 현재 날짜를 형식에 맞게 가져오기
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val userDocument = db.collection(userId).document(today)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // 해당 날짜의 모든 서브 컬렉션에 대한 쿼리 생성
                        val collectionQueries = (0..23).flatMap { hour ->
                            (0..59).map { minute ->
                                val hourString = String.format("%02d:%02d", hour, minute) // 시간을 두 자리 숫자로 포맷
                                userDocument.collection(hourString)
                            }
                        }

                        // 각 컬렉션에 대해 일정을 가져오고 알림 설정
                        collectionQueries.forEach { collection ->
                            Log.d("일정", "Collection Query: ${collection.path}")
                            collection.get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        val summary = document.getString("summary")
                                        val startDate = document.getDate("startDate")
                                        val endDate = document.getDate("endDate")
                                        val reminderMinute = document.getLong("reminderMinutes")

                                        Log.d("일정", "Summary: $summary")
                                        Log.d("일정", "Start Date: $startDate")
                                        Log.d("일정", "End Date: $endDate")
                                        Log.d("일정", "Reminder Minutes: $reminderMinute")

                                        // 알림 예약
                                        if (summary != null && startDate != null && endDate != null && reminderMinute != null) {
                                            scheduleNotification(startDate, reminderMinute, endDate, summary)
                                            sendToWatch(summary, startDate, endDate, reminderMinute)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error fetching schedule data for collection: ${e.message}")
                                }
                        }
                    } else {
                        Log.d("Firestore", "No documents found for $today")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching document for $today: ${e.message}")
                }
        } else {
            // currentUser가 null인 경우 처리
            Log.e("Firestore", "currentUser가 null입니다.")
        }
    }
    private fun scheduleNotification(startDate: Date, reminderMinute: Long, endDate:Date, summary: String) {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        // 알림을 예약할 시간 설정
        calendar.time = startDate
        calendar.add(Calendar.MINUTE, -reminderMinute.toInt())

        // 예약된 시간 확인을 위한 로그 출력
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val scheduledTime = dateFormat.format(calendar.time)
        Log.d("NotificationScheduler", "알림이 예약된 시간: $scheduledTime")

        // 알림 예약 시간이 현재 시간보다 이후인지 확인
        if (calendar.timeInMillis > currentTime) {
            val intent = Intent(this, NotificationReceiver::class.java)
            intent.putExtra("startDate", startDate.time)
            intent.putExtra("endDate", endDate.time)
            intent.putExtra("summary", summary)

            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 안드로이드 12 이상에서는 SCHEDULE_EXACT_ALARM 권한 요청 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                if (!canScheduleExactAlarms) {
                    // 권한이 없으면 권한 요청
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }
            // 이전 알림을 취소하고 새 알림 예약
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            Log.d("NotificationScheduler", "알림 예약 시간이 현재 시간보다 이전입니다. 알림을 예약하지 않습니다.")
        }
    }
    fun setupAlarm(briefingTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val timeParts = briefingTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // 현재 시간보다 이전이면 다음 날로 설정
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun sendToWatchTTS(scheduleList: List<Pair<String, Date>>, briefingTime: String) {
        // 일정 목록을 문자열로 변환
        val scheduleText = generateScheduleText(scheduleList)

        // Wear OS 장치에 전달할 데이터를 준비합니다.
        val dataClient = Wearable.getDataClient(this)
        val putDataReq = PutDataMapRequest.create("/tts").run {
            dataMap.putString("scheduleText", scheduleText)
            dataMap.putString("briefingTime", briefingTime)
            asPutDataRequest()
        }

        // 데이터를 전송합니다.
        dataClient.putDataItem(putDataReq)
            .addOnSuccessListener {
                Log.d("sendToWatchTTS", "Data sent successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("sendToWatchTTS", "Failed to send data to watch: $exception")
            }
    }

    private fun generateScheduleText(scheduleList: List<Pair<String, Date>>): String {
        val stringBuilder = StringBuilder()

        // 일정 목록을 문자열로 변환
        for ((summary, startDate) in scheduleList) {
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startDate)
            stringBuilder.append("$formattedDate: $summary\n")
        }

        return stringBuilder.toString()
    }



    private fun sendToWatch(summary: String, startDate: Date, endDate: Date, reminderMinutes: Long) {
        val dataClient = Wearable.getDataClient(this)
        val putDataReq = PutDataMapRequest.create("/schedule").run {
            dataMap.putString("summary", summary)
            dataMap.putLong("startDate", startDate.time)
            dataMap.putLong("endDate", endDate.time)
            dataMap.putLong("reminderMinutes", reminderMinutes)
            asPutDataRequest()
        }
        dataClient.putDataItem(putDataReq)
    }


    // 위치 권한을 확인하는 함수
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한을 요청하는 함수
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 위치 요청 초기화 함수
    private fun initLocation() {
        // 위치 권한이 있는지 확인
        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // 위치 업데이트 요청
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                // 최근 위치를 가져왔을 때 실행됩니다.
                location?.let {
                    // 위치 정보를 사용하여 TextView에 표시합니다.
                    val latitude = it.latitude
                    val longitude = it.longitude

                    with(sharedPreferences.edit())
                    {
                        putString("latitude", latitude.toString())
                        putString("longitude", longitude.toString())
                        apply()
                    }                }
            }.addOnFailureListener { e ->
                // 위치 정보를 가져오지 못했을 때 실행됩니다.
                Log.e("MainActivity", "위치 가져오기 실패: ${e.message}")
                // 실패에 대한 처리를 수행할 수 있습니다.
            }
        } else {
            // 위치 권한이 없는 경우 위치 권한을 요청
            requestLocationPermission()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_PERMISSIONS)
        } else {
            // 권한이 이미 부여된 경우 작업 예약
            scheduleWeatherWorker()
        }
    }
    private fun scheduleWeatherWorker() {
        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(weatherWorkRequest)
    }
    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 부여된 경우 위치 요청 초기화
            } else {
                // 위치 권한이 거부된 경우 설정으로 위치 권한을 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            }
        }
    }

    private fun convertLocationToAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.let {
                Log.d("HomeFragment", "addresses : $addresses")
                if (it.isNotEmpty()) {
                    val address = it[0]
                    Log.d("HomeFragment", "val address : $address")
                    val addressText = address.thoroughfare
                    if (addressText != null) {
                        with(sharedPreferences.edit())
                        {
                            putString("address", addressText.toString())
                        }
                    } else {
                        var fullAddress = address.getAddressLine(0)

                        fullAddress = fullAddress.replaceFirst("대한민국", "").trim()

                        val addressComponents = fullAddress.split(" ")

                        // 4번째와 5번째 값을 추출하여 결합
                        val trimmedAddressText = if (addressComponents.size >= 4) {
                            "${addressComponents[2]} ${addressComponents[3]}"
                        } else {
                            "주소 정보 부족"
                        }

                        // 변환된 주소를 LocationApi로 보냅니다.
                        sendAddressToLocationApi(trimmedAddressText)
                    }
                }
            }
        } catch (e: Exception) {
            // 예외 처리
            e.printStackTrace()
        }
    }
    private fun sendAddressToLocationApi(address: String) {
        GlobalScope.launch {
            locationResult = LocationApi.extractValuesFromXml(LocationApi.doInBackground(address))
            // UI 스레드에서 TextView에 결과를 설정합니다.
            withContext(Dispatchers.Main) {
                // 결과를 TextView에 표시합니다.
                Log.d("Main", "Edited Location Result : ${locationResult}")
                with(sharedPreferences.edit()) {
                    putString("address", locationResult)
                    apply()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
