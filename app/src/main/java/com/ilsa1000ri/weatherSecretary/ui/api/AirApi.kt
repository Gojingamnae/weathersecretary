package com.ilsa1000ri.weatherSecretary.ui.api
// 미세먼지

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.Log
import com.ilsa1000ri.weatherSecretary.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AirApi {
    //지역번호 통합 필요 -> 위도, 경도 전달 -> NX, NY로 변경 -> 이를 통해 지역번호 출력
    private const val BASE_URL =
        "http://apis.data.go.kr/1360000/LivingWthrIdxServiceV4/getAirDiffusionIdxV4"
    private const val SERVICE_KEY =
        "O%2B4lJjrJFRco0YDo8fLYlNbJXhZ6NSKuoPvWaLmpzuZmjoYuW25lafiElwJtKjjnREZc3AS%2B5SwDFbNbjXQbGg%3D%3D"
    private const val DATA_TYPE = "xml"

    suspend fun getAirIndex(areaNum:String): String {
        return withContext(Dispatchers.IO) {
            val currentTime = Calendar.getInstance().time
            val currentTimeString =
                SimpleDateFormat("yyyyMMddHH", Locale.getDefault()).format(currentTime)
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

                val regex = Regex("<h3>(.*?)</h3>")
                val sunIndexValue = regex.find(xmlString)?.groupValues?.get(1) ?: ""

                // Convert UV index value to risk level text
                getSunIndexRiskLevelText(sunIndexValue)
            } else {
                // Error handling if necessary
                ""
            }
        }
    }

    private fun getSunIndexRiskLevelText(uvIndexValue: String): String {
        val sunIndex = uvIndexValue.toIntOrNull() ?: return ""

        return when (sunIndex) {
            100 -> "낮음"
            75 -> "보통"
            50 -> "높음"
            25 -> "매우높음"
            else -> "기타"
        }
    }
}
