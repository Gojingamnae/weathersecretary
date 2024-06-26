//DayViewPagerAdpater
package com.ilsa1000ri.weatherSecretary.ui.calendar.day

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.databinding.ItemDayDateBinding
import com.ilsa1000ri.weatherSecretary.ui.calendar.day.DayRecyclerViewAdapter
import java.util.*
import android.util.Log
import java.text.SimpleDateFormat

class DayViewPagerAdapter(
    private val dates: List<Date?>,
    private val db: FirebaseFirestore,
    private val currentUser: FirebaseUser,
    private val onScheduleClickListener: (Schedule) -> Unit
) : RecyclerView.Adapter<DayViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDayDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: Date) {
            binding.date.text = SimpleDateFormat("MM/dd", Locale.KOREA).format(date)

            val recyclerViewAdapter = DayRecyclerViewAdapter(emptyList(), onScheduleClickListener)
            binding.timeSlotsRecyclerView.adapter = recyclerViewAdapter
            binding.timeSlotsRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.timeSlotsRecyclerView.addItemDecoration(DividerItemDecoration(binding.root.context, DividerItemDecoration.VERTICAL))

            fetchScheduleData(date, recyclerViewAdapter)
        }
    }

    private fun fetchScheduleData(date: Date, adapter: DayRecyclerViewAdapter) {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        val userDocument = db.collection(currentUser.uid).document(dateString)

        userDocument.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val schedules = mutableListOf<Schedule>()

                    // Fetch documents in the time collection
                    documentSnapshot.data?.forEach { (key, value) ->
                        if (value is Map<*, *>) {
                            val color = (value["color"] as? Long)?.toInt() ?: 0
                            val description = value["description"] as? String ?: ""
                            val reminderMinute = (value["reminderMinute"] as? Long)?.toInt() ?: 0
                            val summary = value["summary"] as? String ?: ""
                            val timeSlot = ""
                            val startTime = key.substring(0, 4)
                            Log.d("DayViewPagerAdapter", "$startTime")
                            val endTime = key.substring(4, minOf(key.length, 8))
                            Log.d("DayViewPagerAdapter", "$endTime")

                            val startDateTime = "$dateString ${startTime.substring(0, 2)}:${startTime.substring(2, 4)}"
                            val endDateTime = "$dateString ${endTime.substring(0, 2)}:${endTime.substring(2, 4)}"

                            val startDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(startDateTime)
                            Log.d("DayViewPagerAdapter", "startDate:${startDate}")
                            val endDate =SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(endDateTime)
                            Log.d("DayViewPagerAdapter", "endDate:${endDate}")

                            // Assuming Schedule constructor matches these parameters
                            val schedule = Schedule(
                                timeSlot = timeSlot,
                                summary = summary,
                                startDate = startDate,
                                endDate = endDate,
                                color = color,
                                description = description,
                                reminderMinute = reminderMinute
                            )
                            schedules.add(schedule)
                        }
                    }

                    // Update the adapter with the fetched schedule data
                    adapter.updateSchedules(schedules)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchScheduleData", "Error fetching data", exception)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dates[position]!!)
    }

    override fun getItemCount() = dates.size
}