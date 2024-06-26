package com.ilsa1000ri.weatherSecretary.ui.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ThisWeekApi {
    //지역번호 통합 필요
    private const val BASE_URL = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa"
    private const val SERVICE_KEY = "O%2B4lJjrJFRco0YDo8fLYlNbJXhZ6NSKuoPvWaLmpzuZmjoYuW25lafiElwJtKjjnREZc3AS%2B5SwDFbNbjXQbGg%3D%3D"
    private const val PAGE_NO = "1"
    private const val NUMBER_OR_ROWS = "1"
    private const val AREA_NO = "11B10101"
    private const val num = "06"


    suspend fun getWEEKIndex(): List<String> {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            if (currentHour == 0 || currentHour == 1 || currentHour == 2 || currentHour == 3 || currentHour == 4 || currentHour == 5 ) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            val yearMonthDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
            val currentTimeString = "$yearMonthDay"

            val urlBuilder = StringBuilder(BASE_URL) /* URL */
            urlBuilder.append("?ServiceKey=$SERVICE_KEY")
            urlBuilder.append("&numOfRows=$NUMBER_OR_ROWS")
            urlBuilder.append("&pageNo=$PAGE_NO")
            urlBuilder.append("&regId=$AREA_NO")
            urlBuilder.append("&tmFc=$currentTimeString+$num")

            val url = URL(urlBuilder.toString())
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

    private fun extractValuesFromXml(xmlString: String): List<String> {
        val regexTaMin3 = Regex("<taMin3>(.*?)</taMin3>")
        val matchResultTaMin3 = regexTaMin3.find(xmlString)
        val taMin3Value = matchResultTaMin3?.groupValues?.get(1) ?: ""

        val regexTaMax3 = Regex("<taMax3>(.*?)</taMax3>")
        val matchResultTaMax3 = regexTaMax3.find(xmlString)
        val taMax3Value = matchResultTaMax3?.groupValues?.get(1) ?: ""

        val regexTaMin4 = Regex("<taMin4>(.*?)</taMin4>")
        val matchResultTaMin4 = regexTaMin4.find(xmlString)
        val taMin4Value = matchResultTaMin4?.groupValues?.get(1) ?: ""

        val regexTaMax4 = Regex("<taMax4>(.*?)</taMax4>")
        val matchResultTaMax4 = regexTaMax4.find(xmlString)
        val taMax4Value = matchResultTaMax4?.groupValues?.get(1) ?: ""

        val regexTaMin5 = Regex("<taMin5>(.*?)</taMin5>")
        val matchResultTaMin5 = regexTaMin5.find(xmlString)
        val taMin5Value = matchResultTaMin5?.groupValues?.get(1) ?: ""

        val regexTaMax5 = Regex("<taMax5>(.*?)</taMax5>")
        val matchResultTaMax5 = regexTaMax5.find(xmlString)
        val taMax5Value = matchResultTaMax5?.groupValues?.get(1) ?: ""

        return listOf(taMin3Value, taMax3Value, taMin4Value, taMax4Value, taMin5Value, taMax5Value)
    }
}