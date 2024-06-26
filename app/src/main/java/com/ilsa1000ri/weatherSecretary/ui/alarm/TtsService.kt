package com.ilsa1000ri.weatherSecretary.ui.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import com.ilsa1000ri.weatherSecretary.ui.api.RealShortApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TtsService : Service() {
    private var ttsManager: TextToSpeechManager? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    val summaryList = mutableListOf<String?>()
    private lateinit var sharedPreferences: SharedPreferences
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TtsService", "Service started")
        sharedPreferences =
            getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        latitude = sharedPreferences.getString("latitude", "0.0")?.toDoubleOrNull() ?: 0.0
        longitude = sharedPreferences.getString("longitude", "0.0")?.toDoubleOrNull() ?: 0.0

        // 비동기 작업 수행을 위해 코루틴 사용
        CoroutineScope(Dispatchers.Main).launch {
            val t1hValue = getCurrentTemperature(latitude, longitude)
            Log.d("TtsService", "Current temperature (T1H): $t1hValue")

            // fetchScheduleData를 비동기적으로 호출
            fetchScheduleData {
                // 데이터 로드 후 summaryList 사용
                val summary = summaryList.toString()
                Log.d("tts", "써머리: $summary")

                // TTS 초기화 및 음성 출력
                ttsManager = TextToSpeechManager(this@TtsService, onInitialized = { initialized ->
                    if (initialized) {
                        val message = formatBriefingMessage(t1hValue, summary)
                        ttsManager?.speak(message)
                    } else {
                        Log.e("TTS", "Text-to-Speech 초기화 실패")
                    }
                }, onSpeakCompleted = {
                    stopSelf() // 음성 출력이 완료되면 서비스 종료
                })
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun getCurrentTemperature(latitude: Double, longitude: Double): Int {
        return withContext(Dispatchers.IO) {
            try {
                val responseList = RealShortApi.getRealShortIndex(latitude, longitude)
                val t1hString = responseList.find { it.startsWith("T1H:") }?.split(",")?.get(0)?.split(":")?.get(1)?.trim()
                Log.e("Tts", "t1hs")

                t1hString?.toInt() ?: 0
            } catch (e: Exception) {
                Log.e("TtsService", "Error fetching temperature", e)
                0
            }
        }
    }

    private fun formatTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm   ", Locale.getDefault())
        return format.format(date)
    }

    private fun fetchScheduleData(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            Log.d("오늘 날짜", "$todayDateString")
            currentUser?.let { user ->
                val userId = user.uid
                val userDocument = db.collection(userId).document(todayDateString)
                val documentSnapshot = Tasks.await(userDocument.get())

                if (documentSnapshot.exists()) {
                    // 해당 날짜의 모든 서브 컬렉션에 대한 쿼리 생성
                    val collectionQueries = (0..23).flatMap { hour ->
                        (0..59).map { minute ->
                            val hourString =
                                String.format("%02d:%02d", hour, minute) // 시간을 두 자리 숫자로 포맷
                            userDocument.collection(hourString)
                        }
                    }

                    // 각 컬렉션에 대해 일정을 가져오고 출력 (병렬 처리)
                    collectionQueries.map { collection ->
                        async {
                            Log.d("일정", "Collection Query: ${collection.path}")
                            val documents = Tasks.await(collection.get())
                            for (document in documents) {
                                val summary = document.getString("summary")
                                Log.d("일정", "lo: $summary")

                                // summary 값을 리스트에 추가
                                summaryList.add(summary)
                                Log.d("summaryList", "Added Summary: $summary")
                                Log.d("summaryList", "Current Summary List: $summaryList")
                            }
                        }
                    }.awaitAll()
                } else {
                    Log.d("일정", "No documents found for $todayDateString")
                }
            }

            // ensure onComplete is called on the main thread
            withContext(Dispatchers.Main) {
                Log.d("fetchScheduleData", "Schedule data fetch completed")
                onComplete()
            }
        }
    }

    override fun onDestroy() {
        Log.d("TtsService", "Service destroyed") // 서비스 종료 로그 추가
        ttsManager?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
