//CalendarFragment
package com.ilsa1000ri.weatherSecretary.ui.calendar

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentCalendarBinding
import com.ilsa1000ri.weatherSecretary.ui.calendar.day.DayViewPagerAdapter
import com.ilsa1000ri.weatherSecretary.ui.calendar.day.ScheduleEditDialog
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.BaseFragment
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.CustomDividerItemDecoration
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.Dates
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.Dates.generateDatesForMonths
import com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule
import com.ilsa1000ri.weatherSecretary.ui.calendar.month.CalendarAdapter
import com.ilsa1000ri.weatherSecretary.ui.calendar.month.CalendarPagerAdapter
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.MonthDates
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.MonthDates.generateDatesForMonthss
import com.ilsa1000ri.weatherSecretary.ui.calendar.util.MonthDates.generateDatess
import com.ilsa1000ri.weatherSecretary.ui.timetable.ScheduleAddDialog
import java.util.*

class CalendarFragment : BaseFragment<FragmentCalendarBinding>(R.layout.fragment_calendar) {
    private var calendar = Calendar.getInstance()
    private val timeSlots = Dates.generateTimeSlots()
    val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val startCalendar: Calendar = Calendar.getInstance().apply {
        time = Date()
    }
    private lateinit var monthDates : List<List<Date?>>
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser!!.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!
        monthDates = generateDatesForMonthss(calendar, 12, 12)
        startCalendar.time = calendar.time
        val onScheduleClickListener: (Schedule) -> Unit = { schedule ->
            ScheduleEditDialog(schedule).show(childFragmentManager, "UpdateEventDialog")
        }

        binding.dayCalendarViewPager.adapter = DayViewPagerAdapter(
            monthDates.flatten(),
            db, // Firestore 인스턴스 전달
            currentUser, // 현재 사용자 FirebaseUser 전달
            onScheduleClickListener // onScheduleClickListener를 전달해줍니다.
        )
        binding.dayCalendarViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.dayCalendarViewPager.addItemDecoration(CustomDividerItemDecoration(requireContext()))

        binding.calendarViewPager.addItemDecoration(CustomDividerItemDecoration(requireContext()))
        binding.calendarViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.calendarViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // 현재 선택된 페이지의 월을 기준으로 yearMonthTextView를 업데이트합니다.
                val selectedMonth = (calendar.get(Calendar.MONTH) + position) % 12
                val selectedYear = calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + position) / 12 -1
                binding.yearMonthTextView.text = String.format("%d년 %d월", selectedYear, selectedMonth + 1)
            }
        })

        val addEventButton = view.findViewById<FloatingActionButton>(R.id.addEventButton)
        addEventButton.setOnClickListener {
            ScheduleAddDialog().show(childFragmentManager, "TimetableFragment")
        }
        updateCalendar()
        dayTextView()
    }

    private fun dayTextView() {
        val startCalendarClone = startCalendar.clone() as Calendar
        val endCalendar = startCalendarClone.clone() as Calendar
        endCalendar.add(Calendar.DATE, 6)

        val differenceToSunday = (Calendar.SUNDAY + 7) - startCalendarClone.get(Calendar.DAY_OF_WEEK)
        val endSunDayCalendar = startCalendarClone.clone() as Calendar
        endSunDayCalendar.add(Calendar.DATE, differenceToSunday)

        val startYear = startCalendarClone.get(Calendar.YEAR)
        val startMonth = startCalendarClone.get(Calendar.MONTH) + 1
        val startDate = startCalendarClone.get(Calendar.DAY_OF_MONTH)

        binding.dayTextView.text = String.format("%d년 %d월 %d일", startYear, startMonth, startDate)
    }

    private fun updateCalendar() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        fetchMonthlyScheduleData(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)) { scheduleMap, dateColorMap ->
            if (view != null) {
                binding.yearMonthTextView.text = String.format("%d년 %d월", year, month + 1)
                val pagerAdapter = CalendarPagerAdapter(
                    monthDates,
                    scheduleMap,
                    { date: Date ->
                        startCalendar.time = date
                        updateDayCalendarView(date)
                        binding.calendarViewPager.visibility = View.GONE
                        binding.calendarDate.visibility = View.GONE
                        binding.dayCalendarViewPager.visibility = View.VISIBLE
                        binding.calendarDayDate.visibility = View.VISIBLE
                    },
                    dateColorMap
                )
                binding.calendarViewPager.adapter = pagerAdapter
                binding.calendarViewPager.setCurrentItem(12, false)

            } else {
                Log.e("CalendarFragment", "Binding is null in updateCalendar callback")
            }
        }
    }


    private fun updateDayCalendarView(date: Date) {
        val position = monthDates.flatten().indexOf(date)
        Log.d("Calendar", "Position in monthDates: $position")

        if (position != -1) {
            binding.dayCalendarViewPager.setCurrentItem(position, false)
            dayTextView()
        }
    }

    private fun fetchMonthlyScheduleData(
        month: Int,
        year: Int,
        callback: (Map<Date, List<Schedule>>, Map<Date, Int>) -> Unit
    ) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.MONTH,1)
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.MONTH, 12)
        endCalendar.add(Calendar.DAY_OF_MONTH, -1)

        val scheduleMap = mutableMapOf<Date, List<Schedule>>()
        val dateColorMap = mutableMapOf<Date, Int>()
        val daysInMonth = endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val userIdCollection = db.collection(userId)
        userIdCollection.get().addOnSuccessListener { datesDocuments ->
            val validDates = mutableListOf<String>()
            for (document in datesDocuments) {
                //db에 존재하는 YYYY-MM-dd를 validDate로 추가
                val date = document.id
                if (isValidDateFormat(date)) {
                    validDates.add(date)
                }
            }

            for (months in 1..12) {
                for (day in 1..daysInMonth) {
                    val currentDay = startCalendar.clone() as Calendar
                    currentDay.set(Calendar.DAY_OF_MONTH, day)
                    currentDay.set(Calendar.MONTH, months)
                    val dateString = dateFormat.format(currentDay.time)
                    for (validDate in validDates) {
                        if (validDate == dateString) {
                            userIdCollection.document(validDate).get()
                                .addOnSuccessListener { timeCollections ->
                                    val timeCollectionData = timeCollections.data
                                    Log.d("CalendarFragment", "timeCollectionData:${timeCollectionData}")
                                    val schedules = mutableListOf<Schedule>()
                                    if (timeCollectionData != null) {
                                        for ((key, value) in timeCollectionData) {
                                            if (value is Map<*, *>) {
                                                val summary = value["summary"]?.toString() ?: ""
                                                val startTime = key.substring(0, 4)
                                                val endTime = key.substring(4, 8)

                                                val startDateTime = "$validDate ${startTime.substring(0, 2)}:${startTime.substring(2, 4)}"
                                                val endDateTime = "$validDate ${endTime.substring(0, 2)}:${endTime.substring(2, 4)}"

                                                val startDate = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(startDateTime)
                                                Log.d("CalendarFragment", "startDate:${startDate}")
                                                val endDate = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(endDateTime)
                                                Log.d("CalendarFragment", "endDate:${endDate}")

                                                val color =
                                                    value["color"]?.toString()?.toIntOrNull()
                                                        ?: 3  // 기본값은 3으로 설정
                                                val reminderMinuteRaw = value["reminderMinute"]
                                                val reminderMinute = reminderMinuteRaw?.toString()?.toIntOrNull() ?: 0
                                                val description = ""
                                                val timeSlot = ""
                                                val schedule = Schedule(
                                                    timeSlot,
                                                    summary,
                                                    startDate,
                                                    endDate,
                                                    color,
                                                    description,
                                                    reminderMinute,
                                                )
                                                schedules.add(schedule)
                                                Log.d("CalendarFragment", "schedule: ${schedules}")
                                            }
                                        }
                                        if (schedules.isNotEmpty()) {
                                            val date = dateFormat.parse(dateString)
                                            val existingSchedules =
                                                scheduleMap[date]?.toMutableList()
                                                    ?: mutableListOf()
                                            existingSchedules.addAll(schedules)
                                            scheduleMap[date] = existingSchedules.distinct()
                                            if (date !in dateColorMap) {
                                                val color = 3
                                                dateColorMap[date] = color
                                            }
                                        }
                                    }
                                    callback(scheduleMap, dateColorMap)
                                }
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("CalendarFragment", "userId 컬랙션 접근 실패")
        }
    }
    fun isValidDateFormat(dateString: String): Boolean {
        val pattern = Regex("\\d{4}-\\d{2}-\\d{2}")
        return pattern.matches(dateString)
    }

}