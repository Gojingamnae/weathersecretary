package com.ilsa1000ri.weatherSecretary.ui.timetable

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.timetable.TimetableView.OnStickerSelectedListener
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Time : Serializable {
    var hour = 0
    var minute = 0
    constructor(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }
    constructor()
}

class Schedules {
    var Title = ""
    var day = 0
    var color = 0
    var reminderMinute = 0
    var description = ""
    var id = ""
    var Date: String? = null // "yyyy-MM-dd" 형식의 날짜 문자열
    var startTime: Time? = null // Time 클래스 인스턴스
    var endTime: Time? = null // Time 클래스 인스턴스

    constructor()

    companion object {
        const val SUN = 0
        const val MON = 1
        const val TUE = 2
        const val WED = 3
        const val THU = 4
        const val FRI = 5
        const val SAT = 6
    }
}

class TimetableFragment : Fragment(), OnStickerSelectedListener {
    private var context: Context? = null
    private var timetable: TimetableView? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        init(view)
        setupTimetableView()

        return view
    }

    private fun init(view: View) {
        context = activity
        timetable = view.findViewById(R.id.timetable)
        initView()
        fetchSchedulesForCurrentWeek()
    }

    private fun setupTimetableView() {
        timetable?.setOnStickerSelectEventListener(this)
    }

    private fun initView() {
        timetable?.setOnStickerSelectEventListener(this)
    }

    override fun OnStickerSelected(idx: Int, schedules: List<Schedules>?) {
        if (schedules != null) {
            if (idx >= 0 && idx < schedules.size) {
                val selectedSchedule = schedules[idx]
            } else {
                Log.e("Timetable", "Invalid index: $idx. Schedules size: ${schedules.size}")
            }
        } else {
            Log.e("Timetable", "Schedules is null")
        }
    }

    private suspend fun fetchWeekSchedules(startDate: Date, endDate: Date): List<Schedules> {
        val dateRange = getDateRange(startDate, endDate)
        val schedules = mutableListOf<Schedules>()

        userId?.let { uid ->
            for (date in dateRange) {
                try {
                    val documentSnapshot = db.collection(uid).document(date).get().await()
                    if (documentSnapshot.exists()) {
                        val events = documentSnapshot.data ?: continue
                        for ((key, value) in events) {
                            if (value is Map<*, *>) {
                                val startTime = Time(key.substring(0, 2).toInt(), key.substring(2, 4).toInt())
                                val endTime = Time(key.substring(4, 6).toInt(), key.substring(6, 8).toInt())
                                val summary = value["summary"] as? String ?: ""
                                val dayOfWeek = getDayOfWeek(date)
                                val description = value["description"] as? String ?: ""
                                val id = value["id"] as? String ?: ""
                                val color = value["color"] as? Int ?: 0
                                val reminderMinute = value["reminderMinute"] as? Int ?: 0
                                val schedule = Schedules().apply {
                                    Title = summary
                                    day = dayOfWeek
                                    this.description = description
                                    this.color = color
                                    this.reminderMinute = reminderMinute
                                    this.startTime = startTime
                                    this.endTime = endTime
                                    this.Date = date
                                }
                                Log.d("schedule", "Title: ${schedule.Title}")
                                Log.d("schedule", "Description: ${schedule.description}")
                                Log.d("schedule", "Day: ${schedule.day}")
                                Log.d("schedule", "Start Time: ${schedule.startTime?.hour}:${schedule.startTime?.minute}")
                                Log.d("schedule", "End Time: ${schedule.endTime?.hour}:${schedule.endTime?.minute}")
                                Log.d("schedule", "Color: ${schedule.color}")
                                Log.d("schedule", "Reminder Minute: ${schedule.reminderMinute}")
                                Log.d("schedule", "Date: ${schedule.Date}")
                                schedules.add(schedule)
                            }
                        }
                    } else {
                        // 문서가 없을 경우 다음 날짜로 넘어감
                        Log.d("Timetable", "Document does not exist for date: $date")
                    }
                } catch (e: Exception) {
                    // 에러 처리
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch schedules: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return schedules
    }
    private fun getDayOfWeek(date: String): Int {
        val calendar = Calendar.getInstance().apply {
            time = dateFormat.parse(date) ?: Date()
        }
        return calendar.get(Calendar.DAY_OF_WEEK) - 1 // SUN=0, MON=1, ...
    }

    private fun getDateRange(start: Date, end: Date): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = start

        while (calendar.time <= end) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }

        return dates
    }

    private fun fetchSchedulesForCurrentWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startDate = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.time

        coroutineScope.launch {
            try {
                val schedules = fetchWeekSchedules(startDate, endDate)
                timetable?.add(schedules) // TimetableView에 일정 추가
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to fetch schedules: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
