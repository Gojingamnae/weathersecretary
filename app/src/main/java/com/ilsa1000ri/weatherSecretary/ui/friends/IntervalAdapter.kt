package com.ilsa1000ri.weatherSecretary.ui.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.Schedule

class IntervalAdapter(private val hour: Int, private val minuteInterval: Int, private val schedules: MutableList<Schedule>) : RecyclerView.Adapter<IntervalAdapter.IntervalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntervalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_interval, parent, false)
        return IntervalViewHolder(view)
    }

    override fun onBindViewHolder(holder: IntervalViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.bind(schedule, hour, minuteInterval)
    }

    override fun getItemCount(): Int = schedules.size

    fun setIntervals(newIntervals: List<Schedule>) {
        schedules.clear()
        schedules.addAll(newIntervals.distinctBy { it.id }) // Ensure same schedule is added only once
        notifyDataSetChanged()
    }

    class IntervalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textView_interval)

        fun bind(schedule: Schedule, hour: Int, minuteInterval: Int) {
            val startDate = schedule.startDate.toDate()
            Log.d("친구테이블", "$startDate")
            val isFirstInterval = startDate.hours == hour && (startDate.minutes / 15) == (minuteInterval / 15)
            if (isFirstInterval) {
                textView.text = schedule.summary
                Log.d("IntervalAdapter", "Schedule title displayed: ${schedule.summary} at hour $hour and minute interval $minuteInterval")

                // Set text color to gray if the schedule belongs to the current user
                if (schedule.color == R.color.dark_gray.toLong()) {
                    textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                } else {
                    textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            } else {
                textView.text = ""
            }
            val colorResId = if (schedule.color == R.color.dark_gray.toLong()) {
                R.color.dark_gray // 사용자 일정의 경우 다크 그레이 색상으로 설정
            } else {
                getColorResId(schedule.color.toInt())
            }
            textView.setBackgroundColor(ContextCompat.getColor(itemView.context, colorResId))
        }

        private fun getColorResId(colorIndex: Int): Int {
            return when (colorIndex) {
                1 -> R.color.color1
                2 -> R.color.color2
                3 -> R.color.color3
                4 -> R.color.color4
                5 -> R.color.color5
                6 -> R.color.color6
                7 -> R.color.color7
                8 -> R.color.color8
                9 -> R.color.color9
                10 -> R.color.color10
                else -> R.color.black // 기본 색상
            }
        }
    }
}
