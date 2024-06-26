package com.ilsa1000ri.weatherSecretary.ui.calendar.month

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule
import com.ilsa1000ri.weatherSecretary.databinding.CalendarItemBinding
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarAdapter(
    private var dates: List<Date?>,  // 날짜 표시 리스트
    private var scheduleMap: Map<Date, List<Schedule>>,  // 각 날짜에 해당하는 일정리스트 저장한 맵
    private val onDateClickListener: (Date) -> Unit,  // 날짜 클릭했을때 호출되는 함수
    private var dateColorMap: Map<Date, Int>  // 각 날짜에 해당하는 색상을 저장한 맵
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>(), Parcelable {

    // 개별 아이템 뷰를 나타내는 클래스
    inner class ViewHolder(val binding: CalendarItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                binding.date?.let { date ->
                    Log.d("CalendarAdapter", "Date clicked: $date")
                    onDateClickListener(date)
                }
            }
        }

        fun bind(date: Date?) {
            Log.d("CalendarAdapter", "Binding date: $date")
            binding.date = date

            date?.let {
                // 날짜 텍스트 설정
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                binding.itemDayText.text = dayFormat.format(it)


                // 요일에 따라 텍스트 색상 설정
                val calendar = Calendar.getInstance()
                calendar.time = it
                when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SATURDAY -> binding.itemDayText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.blue))
                    Calendar.SUNDAY -> binding.itemDayText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                    else -> binding.itemDayText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateOnly = dateFormat.parse(dateFormat.format(it))
                Log.d("CalendarAdapter", "dateOnly:${dateOnly}")

                val schedules = scheduleMap[dateOnly]?.sortedBy { schedule -> schedule.startDate }
                Log.d("CalendarAdapter", " schedules:$schedules")
                binding.scheduleContainer.removeAllViews()   // 일정 컨테이너를 초기화

                schedules?.forEach { schedule ->
                    Log.d("CalendarAdapter", "Schedule found: ${schedule.summary}")
                    val truncatedSummary = if (schedule.summary.length > 4) {
                        schedule.summary.substring(0, 5) // 텍스트를 5글자로 자름
                    } else {
                        schedule.summary // 5글자보다 짧으면 그대로 유지
                    }
                    val scheduleTextView = TextView(binding.root.context).apply {
                        text = truncatedSummary
                        textSize = 12f // 원하는 텍스트 크기로 설정 (예: 12sp)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val colorResId = when (schedule.color.toInt()) {
                            0-> R.color.color1
                            1 -> R.color.color2
                            2 -> R.color.color3
                            3 -> R.color.color4
                            4 -> R.color.color5
                            5 -> R.color.color6
                            6 -> R.color.color7
                            7 -> R.color.color8
                            8 -> R.color.color9
                            9 -> R.color.color10
                            else -> R.color.color4 // 기본값 설정
                        }
                        setBackgroundColor(ContextCompat.getColor(binding.root.context, colorResId))
                    }
                    binding.scheduleContainer.addView(scheduleTextView)
                }

                if (schedules.isNullOrEmpty()) {
                    val emptyTextView = TextView(binding.root.context).apply {
                        text = ""
                        setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                    }
                    binding.scheduleContainer.addView(emptyTextView)
                    Log.d("CalendarAdapter", "Date: $dateOnly has no schedules")
                }
            }
            binding.executePendingBindings()  // Ensure the binding is immediately executed.
        }
    }

    constructor(parcel: Parcel) : this(
        mutableListOf<Date?>().apply {
            parcel.readList(this, Date::class.java.classLoader)
        },
        mutableMapOf<Date, List<com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule>>().apply {
            parcel.readMap(this, Schedule::class.java.classLoader)
        },
        parcel.readSerializable() as (Date) -> Unit,
        mutableMapOf<Date, Int>().apply {
            parcel.readMap(this, Int::class.java.classLoader)
        }
    )

    // 뷰를 생성하고 초기화 함 CalendarItemBinding을 이용해 레이아웃 인플레이트 하고 'ViewHolder'를 반환함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CalendarAdapter", "onCreateViewHolder called")
        val binding = CalendarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // holder 에 데이터를 바인딩함 dates 리스트에서 해당 위치 날짜 가져와서 'bind' 메소드에 전달함
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dates[position]
        Log.d("CalendarAdapter", "onBindViewHolder called for position: $position with date: $date")
        holder.bind(date)
    }

    // 아이템 개수를 반환 dates는 리스트의 크기
    override fun getItemCount(): Int {
        val size = dates.size
        Log.d("CalendarAdapter", "getItemCount called, total items: $size")
        return size
    }


    // 데이터 변경 메서드 추가
    fun updateData(newDates: List<Date?>, newScheduleMap: Map<Date, List<com.ilsa1000ri.weatherSecretary.ui.calendar.day.Schedule>>, newDateColorMap: Map<Date, Int>) {
        this.dates = newDates
        this.scheduleMap = newScheduleMap
        this.dateColorMap = newDateColorMap
        notifyDataSetChanged()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(dates)
        parcel.writeMap(scheduleMap)
        parcel.writeSerializable(onDateClickListener as Serializable)
        parcel.writeMap(dateColorMap)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarAdapter> {
        override fun createFromParcel(parcel: Parcel): CalendarAdapter {
            return CalendarAdapter(parcel)
        }

        override fun newArray(size: Int): Array<CalendarAdapter?> {
            return arrayOfNulls(size)
        }
    }
}
