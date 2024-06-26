package com.lsg.friendpage

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.R
import java.text.SimpleDateFormat
import java.util.*

class FriendCalendarAdapter(private val context: Context) : RecyclerView.Adapter<FriendCalendarAdapter.ItemView>() {
    private val array = ArrayList<Long>()
    private var month = 0
    private val todayCalendar = Calendar.getInstance()
    private val friendDaysWithSchedules = mutableSetOf<String>()
    private val userDaysWithSchedules = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_freind_calendar_item, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = array[position]

        val currentMonth = calendar.get(Calendar.MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val isToday = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                todayCalendar.get(Calendar.MONTH) == currentMonth &&
                todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)

        // 현재 달이 아닌 날짜의 배경색을 회색으로 설정
        if (this.month != currentMonth) {
            holder.background.setBackgroundColor(Color.parseColor("#44cccccc"))
        } else {
            // 현재 달의 날짜의 기본 배경색을 투명으로 설정
            holder.background.setBackgroundColor(Color.TRANSPARENT)

            if (isToday) {
                holder.viewTodayCircle.visibility = View.VISIBLE
                holder.textDay.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                holder.viewTodayCircle.visibility = View.INVISIBLE
                holder.textDay.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            holder.itemView.setOnClickListener {
                Toast.makeText(
                    context,
                    SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(calendar.time),
                    Toast.LENGTH_SHORT).show()
            }
        }

        holder.textDay.text = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
        Log.d("FriendCalendarAdapter", "Binding item at position $position, dateKey: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)}")

        when (dayOfWeek) {
            Calendar.SUNDAY -> holder.textDay.setTextColor(Color.RED)
            Calendar.SATURDAY -> holder.textDay.setTextColor(Color.BLUE)
            else -> holder.textDay.setTextColor(Color.BLACK)
        }

        // 친구와 나의 일정을 확인하여 날짜 배경색 변경
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val friendHasSchedule = friendDaysWithSchedules.contains(dateKey)
        val userHasSchedule = userDaysWithSchedules.contains(dateKey)

        if (this.month == currentMonth) {
            when {
                friendHasSchedule -> holder.background.setBackgroundColor(ContextCompat.getColor(context, R.color.main))
                userHasSchedule -> holder.background.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray))
            }
        }
    }

    override fun getItemCount(): Int {
        return array.size
    }

    fun setList(calendar: Calendar, month: Int, friendDaysWithSchedules: Set<String>, userDaysWithSchedules: Set<String>) {
        this.month = month
        this.friendDaysWithSchedules.clear()
        this.friendDaysWithSchedules.addAll(friendDaysWithSchedules)
        this.userDaysWithSchedules.clear()
        this.userDaysWithSchedules.addAll(userDaysWithSchedules)
        array.clear()

        val firstDayOfTheMonth = calendar.clone() as Calendar
        firstDayOfTheMonth.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeek = firstDayOfTheMonth.get(Calendar.DAY_OF_WEEK) - firstDayOfTheMonth.firstDayOfWeek
        if (firstDayOfWeek < 0) firstDayOfWeek += 7
        firstDayOfTheMonth.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lastDayOfTheMonth = calendar.clone() as Calendar
        lastDayOfTheMonth.set(Calendar.DAY_OF_MONTH, daysInMonth)

        var lastDayOfWeek = lastDayOfTheMonth.get(Calendar.DAY_OF_WEEK) - firstDayOfTheMonth.firstDayOfWeek
        if (lastDayOfWeek < 0) lastDayOfWeek += 7
        lastDayOfWeek = 6 - lastDayOfWeek
        lastDayOfTheMonth.add(Calendar.DAY_OF_MONTH, lastDayOfWeek)

        while (firstDayOfTheMonth.before(lastDayOfTheMonth) || firstDayOfTheMonth == lastDayOfTheMonth) {
            array.add(firstDayOfTheMonth.timeInMillis)
            firstDayOfTheMonth.add(Calendar.DAY_OF_MONTH, 1)
        }

        Log.d("FriendCalendarAdapter", "setList called, array size: ${array.size}")
        notifyDataSetChanged()
    }

    class ItemView(view: View) : RecyclerView.ViewHolder(view) {
        val textDay: TextView = view.findViewById(R.id.text_day)
        val background: ConstraintLayout = view.findViewById(R.id.background)
        val viewTodayCircle: View = view.findViewById(R.id.view_today_circle)
    }
}
