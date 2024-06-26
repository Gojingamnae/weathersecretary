package com.ilsa1000ri.weatherSecretary.ui.api
// 3-5일뒤 강수확률
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MidLandApi {
    private const val BASE_URL = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst"
    private const val SERVICE_KEY = "O%2B4lJjrJFRco0YDo8fLYlNbJXhZ6NSKuoPvWaLmpzuZmjoYuW25lafiElwJtKjjnREZc3AS%2B5SwDFbNbjXQbGg%3D%3D"
    private const val PAGE_NO = "1"
    private const val NUMBER_OR_ROWS = "1"
    private const val AREA_NO = "11B10101"
    private const val num = "06"

    suspend fun getLandIndex(): List<String> {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            if (currentHour == 0 || currentHour == 1 || currentHour == 2 || currentHour == 3 || currentHour == 4 || currentHour == 5 ) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            val yearMonthDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
            val currentTimeString = "$yearMonthDay" // 현재 날짜와 고정된 시간 "06"


            val urlBuilder = StringBuilder(BASE_URL) /* URL */
            urlBuilder.append("?ServiceKey=$SERVICE_KEY")
            urlBuilder.append("&numOfRows=$NUMBER_OR_ROWS")
            urlBuilder.append("&pageNo=$PAGE_NO")
            urlBuilder.append("&regId=$AREA_NO")
            urlBuilder.append("&tmFc=$currentTimeString+$num")


            val url = URL(urlBuilder.toString())
            Log.d("MidLandApi", "Request URL: ${url.toString()}")

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")

            val responseCode = conn.responseCode
            if (responseCode >= 200 && responseCode <= 300) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                val xmlString = br.use { it.readText() }
                conn.disconnect()
                extractValuesFromXml(xmlString)
            } else {
                // Error handling if necessary
                emptyList()
            }
        }
    }

    fun extractValuesFromXml(xmlString: String): List<String> {
        // 강수예보
        val day3am = Regex("<rnSt3Am>(.*?)</rnSt3Am>")
        val matchResultday3am = day3am.find(xmlString)
        val rday3amValue = matchResultday3am?.groupValues?.get(1) ?: ""

        val day3pm = Regex("<rnSt3Pm>(.*?)</rnSt3Pm>")
        val matchResultday3pm = day3pm.find(xmlString)
        val rday3pmValue = matchResultday3pm?.groupValues?.get(1) ?: ""

        val day4am = Regex("<rnSt4Am>(.*?)</rnSt4Am>")
        val matchResultday4am = day4am.find(xmlString)
        val rday4amValue = matchResultday4am?.groupValues?.get(1) ?: ""

        val day4pm = Regex("<rnSt4Pm>(.*?)</rnSt4Pm>")
        val matchResultday4pm = day4pm.find(xmlString)
        val rday4pmValue = matchResultday4pm?.groupValues?.get(1) ?: ""

        val day5am = Regex("<rnSt5Am>(.*?)</rnSt5Am>")
        val matchResultday5am = day5am.find(xmlString)
        val rday5amValue = matchResultday5am?.groupValues?.get(1) ?: ""

        val day5pm = Regex("<rnSt5Pm>(.*?)</rnSt5Pm>")
        val matchResultday5pm = day5pm.find(xmlString)
        val rday5pmValue = matchResultday5pm?.groupValues?.get(1) ?: ""

        return listOf(rday3amValue, rday3pmValue, rday4amValue, rday4pmValue, rday5amValue, rday5pmValue)
    }
}