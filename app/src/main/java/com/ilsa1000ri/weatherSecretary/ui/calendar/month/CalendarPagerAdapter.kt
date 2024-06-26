package com.ilsa1000ri.weatherSecretary.ui.calendar.month

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.databinding.CalendarPageBinding
import java.util.Date

class CalendarPagerAdapter(
    private var dates: List<List<Date?>>,
    private var scheduleMap: Map<Date, List<com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule>>,
    private val onDateClickListener: (Date) -> Unit,
    private var dateColorMap: Map<Date, Int>
) : RecyclerView.Adapter<CalendarPagerAdapter.ViewHolder>() {  // 개별 페이지 뷰

    inner class ViewHolder(val binding: CalendarPageBinding) : RecyclerView.ViewHolder(binding.root) {
        val calendarRecyclerView: RecyclerView = binding.calendarViewPager
        val dayOfTheWeekRecyclerView: RecyclerView = binding.dayOfTheWeekRecyclerView
    }

    // 레이아웃 뷰 홀더를 생성하고 초기화 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CalendarPagerAdapter", "onCreateViewHolder called")
        val binding = CalendarPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    // 해당하는 날짜 리스트를 가져와 "캘린더 어댑터" 생성하고 설정함
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CalendarPagerAdapter", "onBindViewHolder called for position: $position")

        val adapter = CalendarAdapter(dates[position], scheduleMap, onDateClickListener, dateColorMap)

        holder.calendarRecyclerView.adapter = adapter
        holder.calendarRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 7)

        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        val dayOfWeekAdapter = DayOfTheWeekAdapter(daysOfWeek)
        holder.dayOfTheWeekRecyclerView.adapter = dayOfWeekAdapter
        holder.dayOfTheWeekRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 7)
    }

    // 데이터 변경 메서드 추가
    fun updateData(newDates: List<List<Date?>>, newScheduleMap: Map<Date, List<com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule>>, newDateColorMap: Map<Date, Int>) {
        this.dates = newDates
        this.scheduleMap = newScheduleMap
        this.dateColorMap = newDateColorMap
        notifyDataSetChanged()
    }

    // 아이템 개수를 반환함
    override fun getItemCount(): Int {
        return dates.size
    }
}
