package com.ilsa1000ri.weatherSecretary.ui.friends

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.ilsa1000ri.weatherSecretary.R
import com.lsg.friendpage.FriendCalendarAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class FriendCalendarFragment : Fragment() {

    companion object {
        private const val ARG_MONTH_POSITION = "month_position"

        fun newInstance(monthPosition: Int): FriendCalendarFragment {
            val fragment = FriendCalendarFragment()
            val args = Bundle()
            args.putInt(ARG_MONTH_POSITION, monthPosition)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: FriendCalendarAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val friendDaysWithSchedules = mutableSetOf<String>()
    private val userDaysWithSchedules = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FriendCalendarFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_friend_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FriendCalendarFragment", "onViewCreated called")

        // RecyclerView 설정
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 7)
        Log.d("FriendCalendarFragment", "RecyclerView 설정 완료")

        val monthPosition = arguments?.getInt(ARG_MONTH_POSITION) ?: 0
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, monthPosition)
        Log.d("FriendCalendarFragment", "calendar 설정 완료: monthPosition=$monthPosition")

        calendarAdapter = FriendCalendarAdapter(requireContext())
        recyclerView.adapter = calendarAdapter
        Log.d("FriendCalendarFragment", "calendarAdapter 설정 완료")

        val friendName = arguments?.getString("friendName") ?: "Default Name"
        val friendUid = arguments?.getString("friendUid")
        Log.d("FriendCalendarFragment", "friendName: $friendName, friendUid: $friendUid")

        if (friendUid == null) {
            fetchFriendUid(friendName) { uid ->
                if (uid != null) {
                    Log.d("FriendCalendarFragment", "Fetched friendUid: $uid")
                    fetchAndDisplaySchedules(uid)
                } else {
                    Log.e("FriendCalendarFragment", "Failed to fetch friendUid for friendName: $friendName")
                }
            }
        } else {
            fetchAndDisplaySchedules(friendUid)
        }

        // 기존 시간표 버튼 클릭 이벤트 처리
        val timetableButton = view.findViewById<Button>(R.id.buttonToTimetable)
        timetableButton.setOnClickListener {
            val action = FriendCalendarFragmentDirections.actionFriendCalendarFragmentToFriendTimetableFragment(friendName)
            Log.d("FriendCalendarFragment", "Navigating to FriendTimetableFragment")
            findNavController().navigate(action)
        }
        view.findViewById<Button>(R.id.button_back_to_friends).setOnClickListener {
            Log.d("FriendCalendarFragment", "Navigating back to friendFragment")
            findNavController().navigate(R.id.action_friendCalendarFragment_to_friendFragment)
        }

        // 현재 월 가져오기
        val currentMonth = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MM월", Locale.getDefault())
        val currentMonthName = monthFormat.format(currentMonth.time)

        // 텍스트뷰에 이름과 월을 표시
        val monthTextView = view.findViewById<TextView>(R.id.textView_friendCalendar)
        monthTextView.text = "$friendName 의 $currentMonthName"
        Log.d("FriendCalendarFragment", "Received friend name: $friendName, currentMonthName: $currentMonthName")
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
                    Log.e("FriendCalendarFragment", "Error fetching friend UID: ${e.message}")
                    callback(null)
                }
        }
    }

    private fun fetchAndDisplaySchedules(friendUid: String) {
        fetchDaysWithSchedules(friendUid) { daysWithSchedules ->
            friendDaysWithSchedules.addAll(daysWithSchedules)
            Log.d("FriendCalendarFragment", "friendDaysWithSchedules: $friendDaysWithSchedules")
            fetchDaysWithSchedules(currentUser?.uid ?: "") { userDaysWithSchedules ->
                this.userDaysWithSchedules.addAll(userDaysWithSchedules)
                Log.d("FriendCalendarFragment", "userDaysWithSchedules: $userDaysWithSchedules")
                calendarAdapter.setList(java.util.Calendar.getInstance(), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), friendDaysWithSchedules, userDaysWithSchedules)
            }
        }
    }

    private fun fetchDaysWithSchedules(userId: String, callback: (Set<String>) -> Unit) {
        val calendar = Calendar.getInstance()

        // 이번 달의 첫 날을 기준으로 날짜를 설정합니다.
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val daysWithSchedules = mutableSetOf<String>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysOfMonth = (0 until daysInMonth).map {
            val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            day
        }

        var completedQueries = 0

        for (day in daysOfMonth) {
            val dayDocument = db.collection(userId).document(day)
            dayDocument.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        daysWithSchedules.add(day)
                        Log.d("FriendCalendarFragment", "Data for $day: ${document.data}")
                    } else {
                        Log.d("FriendCalendarFragment", "No data for $day")
                    }
                    completedQueries++
                    Log.d("FriendCalendarFragment", "Completed query $completedQueries/${daysOfMonth.size} for userId: $userId")
                    if (completedQueries == daysOfMonth.size) {
                        callback(daysWithSchedules)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FriendCalendarFragment", "Error fetching data from collection: ${e.message}")
                    completedQueries++
                    if (completedQueries == daysOfMonth.size) {
                        callback(daysWithSchedules)
                    }
                }
        }
    }
}
