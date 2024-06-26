package com.ilsa1000ri.weatherSecretary.ui.api


import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SunApi {
    //지역번호 통합 필요
    private const val BASE_URL = "http://apis.data.go.kr/1360000/LivingWthrIdxServiceV4/getUVIdxV4"
    private const val SERVICE_KEY = "O%2B4lJjrJFRco0YDo8fLYlNbJXhZ6NSKuoPvWaLmpzuZmjoYuW25lafiElwJtKjjnREZc3AS%2B5SwDFbNbjXQbGg%3D%3D"
    private const val AREA_NO = "1100000000"
    //    private var TIME = ""
    private const val DATA_TYPE = "xml"

    suspend fun getUVIndex(areaNum:String): String {
//        setBaseTime() // 현재 시간으로 TIME 설정
        return withContext(Dispatchers.IO) {
            val currentTime = Calendar.getInstance().time
            val currentTimeString = SimpleDateFormat("yyyyMMddHH", Locale.getDefault()).format(currentTime)

            val urlBuilder = StringBuilder(BASE_URL) /* URL */
            urlBuilder.append("?ServiceKey=$SERVICE_KEY")
            urlBuilder.append("&areaNo=$areaNum")
            urlBuilder.append("&time=$currentTimeString")
            urlBuilder.append("&dataType=$DATA_TYPE")

            val url = URL(urlBuilder.toString())
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")

            val responseCode = conn.responseCode
            if (responseCode >= 200 && responseCode <= 300) {
                val inputStream = conn.inputStream
                val xmlString = inputStream.bufferedReader().use { it.readText() }

                val regex = Regex("<h0>(.*?)</h0>")
                val uvIndexValue = regex.find(xmlString)?.groupValues?.get(1) ?: ""

                // Convert UV index value to risk level text
                getUVIndexRiskLevelText(uvIndexValue)
            } else {
                // Error handling if necessary
                ""
            }
        }
    }

//    private fun setBaseTime() {
//        val calendar = Calendar.getInstance()
//        val sdfTime = SimpleDateFormat("yyyyMMdd00", Locale.getDefault())
//        TIME = sdfTime.format(calendar.time)
//    }

    private fun getUVIndexRiskLevelText(uvIndexValue: String): String {
        val uvIndex = uvIndexValue.toIntOrNull() ?: return ""

        return when {
            uvIndex >= 11 -> "위험"
            uvIndex in 8..10 -> "매우높음"
            uvIndex in 6..7 -> "높음"
            uvIndex in 3..5 -> "보통"
            else -> "낮음"
        }
    }
}
