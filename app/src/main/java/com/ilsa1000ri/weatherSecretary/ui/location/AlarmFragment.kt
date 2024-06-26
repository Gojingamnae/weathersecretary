package com.ilsa1000ri.weatherSecretary.ui.location

import android.Manifest
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilsa1000ri.weatherSecretary.R
import pub.devrel.easypermissions.EasyPermissions
import java.util.Locale

import com.ilsa1000ri.weatherSecretary.MainActivity

class AlarmFragment : Fragment() {
    private var actionHour: Int = 9 //시
    private var actionMinute: Int = 0 //분
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)

        // Firebase에서 데이터 읽어오기
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser!!.uid

        val alarmRef = db.collection(userId).document("Alarm")
        alarmRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Firebase에서 데이터를 가져온 후 UI에 설정 값을 표시하는 코드를 여기에 추가하세요.
                val isAlarmEnabled = document.getBoolean("isAlarmEnabled") ?: false
                val briefingTime = document.getString("briefingTime") ?: "12:00"
//                val isCalendarSyncEnabled = document.getBoolean("isSetCalendarSyncEnabled") ?: false

                // 설정 값을 UI에 표시
                val briefingAlarmSwitch = view.findViewById<SwitchCompat>(R.id.setAlarmEnabled)
                briefingAlarmSwitch.isChecked = isAlarmEnabled

                val briefingTimeTextView = view.findViewById<TextView>(R.id.TodayBriefingTimeTextView)
                briefingTimeTextView.text = briefingTime
                Log.d("AlarmFragment", "briefingTime:$briefingTime")
                val timeParts = briefingTime.split(":")
                actionHour = timeParts[0].toInt()
                actionMinute = timeParts[1].toInt()


//                val calendarSyncSwitch = view.findViewById<SwitchCompat>(R.id.setCalendarSync)
//                calendarSyncSwitch.isChecked = isCalendarSyncEnabled
            } else {
                Log.d(TAG, "No such document")
                // 데이터가 없는 경우, 모든 설정을 비활성화(off) 상태로 초기화
                val briefingAlarmSwitch = view.findViewById<SwitchCompat>(R.id.setAlarmEnabled)
                briefingAlarmSwitch.isChecked = false

                val briefingTimeTextView = view.findViewById<TextView>(R.id.TodayBriefingTimeTextView)
                briefingTimeTextView.text = ""
//
//                val calendarSyncSwitch = view.findViewById<SwitchCompat>(R.id.setCalendarSync)
//                calendarSyncSwitch.isChecked = false
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }


        view.findViewById<ImageButton>(R.id.goToAreaManage).setOnClickListener {
            val areaManageFragment = AreaManageFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_area_manage_container, areaManageFragment)
                .addToBackStack(null)
                .commit()
        }

        val briefingAlarmSwitch = view.findViewById<SwitchCompat>(R.id.setAlarmEnabled)
        val briefingTimeLayout = view.findViewById<LinearLayout>(R.id.setTodayBriefingTimeLayout)
        briefingTimeLayout.visibility = View.GONE

        briefingAlarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 스위치가 On 상태인 경우에만 시간대 설정 레이아웃을 보여줍니다.
            if (isChecked) {
                briefingTimeLayout.visibility = View.VISIBLE
            } else {
                briefingTimeLayout.visibility = View.GONE
            }
        }

        val editTime = view.findViewById<TextView>(R.id.TodayBriefingTimeTextView)
        editTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                // 시간이 선택되면 실행되는 부분
                val timeText = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                // 선택된 시간을 TextView에 설정
                editTime.text = timeText
            }, actionHour, actionMinute, false)

            timePickerDialog.show()
        }

        val setAlarmConfirmButton = view.findViewById<ImageButton>(R.id.setAlarmConfirmButton)
        setAlarmConfirmButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val userId = currentUser!!.uid

            // 오늘의 브리핑 설정 저장
            val alarmSwitch = view.findViewById<SwitchCompat>(R.id.setAlarmEnabled)
            val briefingTimeTextView = view.findViewById<TextView>(R.id.TodayBriefingTimeTextView)
            val isAlarmEnabled = alarmSwitch.isChecked
            val briefingTime = if (isAlarmEnabled) briefingTimeTextView.text.toString() else null
//            val calendarSyncSwitch = view.findViewById<SwitchCompat>(R.id.setCalendarSync)
//            val isCalendarSyncEnabled = calendarSyncSwitch.isChecked

            val alarmData = hashMapOf(
                "isAlarmEnabled" to isAlarmEnabled,
                "briefingTime" to briefingTime,
//                "isSetCalendarSyncEnabled" to isCalendarSyncEnabled
            )

//            if (calendarSyncSwitch.isChecked) {
//                (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.GET_ACCOUNTS))
//            }

            // Firebase에 데이터 저장
            db.collection(userId).document("Alarm").set(alarmData)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    val areaManageFragment = AreaManageFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_area_manage_container, areaManageFragment)
                        .addToBackStack(null)
                        .commit()

                    // MainActivity의 setupAlarm 호출
                    val mainActivity = activity as? MainActivity
                    mainActivity?.setupAlarm(briefingTime ?: "12:00")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }

        return view
    }

    companion object {
        private const val TAG = "AlarmFragment"
    }
}
