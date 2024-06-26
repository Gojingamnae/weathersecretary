package com.ilsa1000ri.weatherSecretary.ui.timetable

import android.widget.TextView
import java.io.Serializable


class Sticker : Serializable {
    val view: ArrayList<TextView>
    val schedules: ArrayList<Schedules>

    init {
        view = ArrayList()
        schedules = ArrayList()
    }

    fun addTextView(v: TextView) {
        view.add(v)
    }

    fun addSchedule(schedule: Schedules) {
        schedules.add(schedule)
    }
}