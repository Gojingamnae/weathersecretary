package com.ilsa1000ri.weatherSecretary.ui.calendar.util
import java.util.Calendar
import java.util.Date

object MonthDates {
    fun generateDatesForMonthss(calendar: Calendar, monthsBefore: Int, monthsAfter: Int): List<List<Date?>> {
        val datesList = mutableListOf<List<Date?>>()
        val today = Calendar.getInstance()

        for (i in -monthsBefore..monthsAfter) {
            val cal = today.clone() as Calendar
            cal.add(Calendar.MONTH, i)
            datesList.add(generateDatess(cal))
        }
        return datesList
    }

    fun generateDatess(calendar: Calendar): List<Date?> {
        val dates = mutableListOf<Date?>()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Add nulls for days of the week before the first day of the month
        for (i in 0 until firstDayOfWeek) {
            dates.add(null)
        }

        for (i in 0 until daysInMonth) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Add nulls to fill the last week of the month
        while (dates.size % 7 != 0) {
            dates.add(null)
        }

        return dates
    }

    fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        for (hour in 0..23) {
            timeSlots.add(String.format("%02d:00", hour))
        }
        return timeSlots
    }
}
