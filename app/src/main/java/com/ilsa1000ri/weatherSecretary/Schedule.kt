package com.ilsa1000ri.weatherSecretary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import com.google.firebase.Timestamp

@Parcelize
data class Schedule(
    val id: String,
    val summary: String,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val color: Number,
    val reminderMinutes: Number,
    val description: String = "",
    val isAllday: Boolean
) : Parcelable {

}