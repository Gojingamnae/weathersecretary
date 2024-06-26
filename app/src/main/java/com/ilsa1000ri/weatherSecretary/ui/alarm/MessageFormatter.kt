package com.ilsa1000ri.weatherSecretary.ui.alarm

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

fun formatBriefingMessage(temperature: Int, summary:String): String {
    val currentDateTime = Date()
    val formatter = SimpleDateFormat("MM월 dd일 EEEE", Locale.KOREAN)
    val formattedDate = formatter.format(currentDateTime)

    Log.d("MessageFormatter", "currentDate: $formattedDate")
    Log.d("MessageFormatter", "summaryy: $summary")

    // 활동 추천 로직
    val season = when {
        temperature >= 20 -> "summer"
        temperature <= 0 -> "winter"
        else -> "Spring"
    }
    val recommendedActivity = getRandomActivities(season)

    val scheduleMessage = if (summary.isNotEmpty() && summary != "[]") {
        "오늘의 일정은 ${summary} 입니다."
    } else {
        "오늘은 일정이 없습니다."
    }

    val message = "오늘은 $formattedDate 입니다. 오늘의 기온은 ${temperature}도 입니다. 오늘은 $recommendedActivity 를 해보시는게 어떨까요? $scheduleMessage "
    Log.d("MessageFormatter", "Created Message: $message")

    return message
}

fun getRandomActivities(season: String): String {
    val activities = when (season) {
        "Spring" -> listOf("산책", "자전거", "낚시", "조깅", "등산", "피크닉", "꽃구경", "캠프파이어", "요가", "필라테스",
            "GYM", "테니스", "스쿼시", "수영", "골프", "야구", "볼링", "탁구", "당구", "배드민턴", "홈트",
            "복싱", "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍", "사격", "방탈출", "양궁", "승마",
            "VR", "루지"
        )
        "summer" -> listOf("산책", "자전거", "낚시", "조깅", "등산", "수영", "서핑", "비치발리볼", "카누/카약", "수상레저",
            "수상스키", "요트", "요가", "필라테스", "GYM", "테니스", "스쿼시", "수영", "골프", "야구", "볼링",
            "탁구", "당구", "배드민턴", "홈트", "복싱", "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍",
            "사격", "방탈출", "양궁", "승마", "VR", "루지"
        )
        "winter" -> listOf("산책", "자전거", "낚시", "조깅", "등산", "스케이트", "스키장", "온천", "요가", "필라테스", "GYM",
            "테니스", "스쿼시", "수영", "골프", "야구", "볼링", "탁구", "당구", "배드민턴", "홈트", "복싱",
            "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍", "사격", "방탈출", "양궁", "승마", "VR",
            "루지"
        )
        else -> emptyList()
    }

    // activities 리스트에서 랜덤하게 3개의 활동을 선택하여 반환
    return if (activities.isNotEmpty()) activities.shuffled().take(3).joinToString(", ") else "No Activities Available"
}
