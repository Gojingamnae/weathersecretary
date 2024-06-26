package com.ilsa1000ri.weatherSecretary.ui.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object OpenApi {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    private const val API_KEY = "7e28c8a6e30662e969a7d4e6794cc2b9"

    suspend fun getOpenIndex(latitude: Double, longitude: Double): Double? { //해당 위치를 전달?
        Log.i("OpenApi", "getOpenIndex에 전달된 latitude, longitude : ${latitude}, ${longitude}")
        return withContext(Dispatchers.IO) {
            val urlBuilder = StringBuilder(BASE_URL) /* URL */

            urlBuilder.append("?lat=$latitude")
            urlBuilder.append("&lon=$longitude")
            urlBuilder.append("&appid=$API_KEY")

            val url = URL(urlBuilder.toString())
            Log.d("OpenApi", "OpenApi의 url : ${url}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")


            val responseCode = conn.responseCode
            if (responseCode >= 200 && responseCode <= 300) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                val response = br.use { it.readText() } // JSON 문자열 전체를 읽어옴
                br.close()
                conn.disconnect()

                // JSON 파싱하여 "feels_like" 값 추출
                val jsonObject = JSONObject(response)
                val mainObject = jsonObject.getJSONObject("main")
                mainObject.optDouble("feels_like") // "feels_like" 값 반환
            } else {
                // Error handling if necessary
                null
            }
        }
    }


    suspend fun getOpenIndexInCelsius(latitude: Double, longitude: Double): Int?{ //해당 위치의 화씨 -> 섭씨
        val kelvinTemperature = getOpenIndex(latitude, longitude) ?: return null
        Log.d("OpenApi", "OpenApi의 kelvinTemperature : ${kelvinTemperature}")
        return kelvinToCelsius(kelvinTemperature)
    }


    private fun kelvinToCelsius(kelvin: Double): Int {
        return (kelvin - 273.15).toInt()
    }

    suspend fun getWeatherStatus(latitude: Double, longitude: Double): String? {
        val feelsLikeTemperature = getOpenIndex(latitude, longitude) ?: return null
        return when {
            feelsLikeTemperature >= 299 -> "temp1" // 26도 이상인 경우 (299K = 26도)
            feelsLikeTemperature in 294.15..298.15 -> "temp2" // 21~25도인 경우 (294.15K = 21도, 298.15K = 25도)
            feelsLikeTemperature in 289.15..293.15 -> "temp3" // 16~20도인 경우 (289.15K = 16도, 293.15K = 20도)
            feelsLikeTemperature in 284.15..288.15 -> "temp4" // 11~15도인 경우 (284.15K = 11도, 288.15K = 15도)
            feelsLikeTemperature in 279.15..283.15 -> "temp5" // 6~10도인 경우 (279.15K = 6도, 283.15K = 10도)
            feelsLikeTemperature < 278.15 -> "temp6" // 5도 미만인 경우 (278.15K = 5도)
            else -> null
        }
    }

    fun getRandomItems(feellike: String?): Pair<String, String>? {
        val weatherStatus = feellike ?: return null
        val itemsMap = mapOf(
            "temp1" to arrayOf("반바지 ", "민소매 ", "반팔 ", "이온음료 "),
            "temp2" to arrayOf("얇은셔츠 ", "반팔 ", "얇은긴팔 ", "면바지 "),
            "temp3" to arrayOf("얇은가디건 ", "후드티", "청바지", "조끼니트 "),
            "temp4" to arrayOf("두꺼운가디건 " , "자켓 ", "셔츠 ", "트렌치코트 "),
            "temp5" to arrayOf("코트 ", "가죽자켓 ", "후리스 ", "니트 ", "기모후드티 "),
            "temp6" to arrayOf("숏패딩 ", "목도리 ", "수면양말 ", "장갑 ", "비니 ", "롱패딩 ")
        )
        val itemsForWeather = itemsMap[weatherStatus] ?: return null
        val randomIndices = (0 until itemsForWeather.size).shuffled().take(2)
        val item1 = itemsForWeather[randomIndices[0]]
        val item2 = itemsForWeather[randomIndices[1]]
        // item1과 item2를 로그에 출력
        Log.d("RandomItems", "Item 1: $item1, Item 2: $item2")
        return Pair(item1, item2)
    }
}
