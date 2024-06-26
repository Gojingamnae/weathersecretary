//DayRecyclerViewAdapter
package com.ilsa1000ri.weatherSecretary.ui.calendar.day

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.databinding.ItemDayTimeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Schedule(
    val timeSlot: String,
    val summary: String,
    var startDate: Date,  // 변경 가능하도록 var로 수정
    var endDate: Date,
    val color: Int,
    val description: String?,
    val reminderMinute: Int,
)

class DayRecyclerViewAdapter(
    private var schedules: List<Schedule>,
    private val onScheduleClickListener: (Schedule) -> Unit
) : RecyclerView.Adapter<DayRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDayTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedSchedule = schedules[position]
                    onScheduleClickListener(clickedSchedule)
                }
            }
        }

        fun bind(schedule: Schedule) {
            // SimpleDateFormat을 사용하여 HH:mm 형식으로 시간을 변환합니다.
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

// schedule.startDate와 schedule.endDate를 HH:mm 형식으로 변환합니다.
            val startTime = timeFormat.format(schedule.startDate)
            val endTime = timeFormat.format(schedule.endDate)

// 변환된 시간을 사용하여 binding.timeSlot.text를 설정합니다.
            binding.timeSlot.text = "$startTime ~ $endTime : ${schedule.summary}"

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount() = schedules.size

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules = newSchedules.sortedBy { it.startDate } // 시작 시간 기준으로 정렬
        notifyDataSetChanged() // 변경 사항을 UI에 반영
    }
}

