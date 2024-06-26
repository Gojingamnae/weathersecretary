package com.ilsa1000ri.weatherSecretary.ui.friends

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.timetable.Schedules
import com.ilsa1000ri.weatherSecretary.ui.timetable.Time
import com.ilsa1000ri.weatherSecretary.ui.timetable.TimetableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FriendTimetableFragment : Fragment(), TimetableView.OnStickerSelectedListener {
    private var context: Context? = null
    private var timetable: TimetableView? = null
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_timetable, container, false)
        init(view)
        setupTimetableView()
        return view
    }

    private fun init(view: View) {
        context = activity
        timetable = view.findViewById(R.id.friend_timetable)
        initView()
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
                // Handle invalid index
                Log.e("FriendTimetable", "Invalid index: $idx. Schedules size: ${schedules.size}")
            }
        } else {
            // Handle null schedules
            Log.e("FriendTimetable", "Schedules is null")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val friendUid = arguments?.getString("friendUid")
        val friendName = arguments?.getString("friendName") ?: "Unknown"

        if (friendUid == null) {
            fetchFriendUid(friendName) { uid ->
                if (uid != null) {
                    Log.d("FriendTimetableFragment", "Fetched friendUid: $uid")
                    fetchSchedulesForCurrentWeek(uid)
                } else {
                    Log.e("FriendTimetableFragment", "Failed to fetch friendUid for friendName: $friendName")
                }
            }
        } else {
            fetchSchedulesForCurrentWeek(friendUid)
        }

        // Set action bar title and timetable title
        (activity as? AppCompatActivity)?.supportActionBar?.title = "$friendName 의 시간표"
        view.findViewById<TextView>(R.id.textView_friendTimetableTitle)?.text = "$friendName 의 시간표"

        // Button click listeners
        view.findViewById<Button>(R.id.buttonToCalendar)?.setOnClickListener {
            val action = FriendTimetableFragmentDirections.actionFriendTimetableFragmentToFriendCalendarFragment(friendName)
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.button_back)?.setOnClickListener {
            findNavController().navigate(R.id.action_friendTimetableFragment_to_friendFragment)
        }
    }
    private fun fetchFriendUid(friendName: String, callback: (String?) -> Unit) {
        currentUser?.let { user ->
            val userId = user.uid
            val friendDocumentRef = db.collection(userId).document("Friend")

            friendDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsMap = document.data as? Map<String, Map<String, Any>>
                        friendsMap?.forEach { (name, friendData) ->
                            if (name == friendName) {
                                val friendUid = friendData["uid"] as? String
                                if (friendUid != null) {
                                    callback(friendUid)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                    callback(null)
                }
                .addOnFailureListener { e ->
                    Log.e("FriendTimetableFragment", "Error fetching friend UID: ${e.message}")
                    callback(null)
                }
        }
    }

    private suspend fun fetchWeekSchedules(startDate: Date, endDate: Date, userId: String): List<Schedules> {
        val dateRange = getDateRange(startDate, endDate)
        val schedules = mutableListOf<Schedules>()

        userId.let { uid ->
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
                                schedules.add(schedule)
                            }
                        }
                    } else {
                        // 문서가 없을 경우 다음 날짜로 넘어감
                        Log.d("FriendTimetableFragment", "Document does not exist for date: $date")
                    }
                } catch (e: Exception) {
                    // 에러 처리
                    Log.e("FriendTimetableFragment", "Error fetching schedules", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to fetch schedules: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private fun fetchSchedulesForCurrentWeek(friendId: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startDate = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.time

        coroutineScope.launch {
            try {
                val schedules = fetchWeekSchedules(startDate, endDate,friendId)
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
