package com.ilsa1000ri.weatherSecretary.ui.calendar.util

import com.google.firebase.Timestamp


data class Event(
    val color:Number=1, //색상
    val date:String="", //날짜
    val summary:String="", //제목
    val description: String = "", //설명
    val reminderMinutes: Number=0, //n분 전 알림
    val startDate: String="", //시작 시간
    val endDate: String, //종료 시간
    val location: String //위치
)
