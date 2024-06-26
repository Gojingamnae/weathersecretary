package com.ilsa1000ri.weatherSecretary.ui.calendar.month

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.databinding.ItemDayoftheweekBinding

//요일을 표시하는 RecyclerView
class DayOfTheWeekAdapter(
    private val days: List<String>)
    : RecyclerView.Adapter<DayOfTheWeekAdapter.DayViewHolder>() {
    // 개별 요일 아이템 뷰
    class DayViewHolder(private val binding: ItemDayoftheweekBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: String) {
            binding.dayTextOfWeek.text = day
        }
    }
    // 뷰 홀더를 생성하고 초기화 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayoftheweekBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    // holder에 데이터 바인딩을 함 day 리스트에서 해당 위치의 요일 문자열을 가져와 bind에 메소드에 전달
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }
    // 아이템 개수를 반환함
    override fun getItemCount(): Int = days.size

}