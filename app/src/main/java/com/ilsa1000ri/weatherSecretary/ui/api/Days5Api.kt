package com.ilsa1000ri.weatherSecretary.ui.api
// 5일 아이콘- openweathermap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object Days5Api {
    //통합 완료
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/forecast"
    private const val API_KEY = "7e28c8a6e30662e969a7d4e6794cc2b9"

    suspend fun getDays5Data(latitude: Double, longitude: Double): List<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            val urlBuilder = StringBuilder(BASE_URL) /* URL */
            urlBuilder.append("?lat=$latitude")
            urlBuilder.append("&lon=$longitude")
            urlBuilder.append("&appid=$API_KEY")

            val url = URL(urlBuilder.toString())
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")

            val responseCode = conn.responseCode
            if (responseCode >= 200 && responseCode <= 300) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                val response = br.use { it.readText() } // JSON 문자열 전체를 읽어옴
                br.close()
                conn.disconnect()

                val jsonObject = JSONObject(response)
                val listArray = jsonObject.getJSONArray("list")

                val dataList = mutableListOf<Pair<String, String>>()

                for (i in 0 until listArray.length()) {
                    val listItem = listArray.getJSONObject(i)
                    val dt_txt = listItem.getString("dt_txt")
                    val weatherArray = listItem.getJSONArray("weather")
                    val weatherObj = weatherArray.getJSONObject(0)
                    val weatherDescription = weatherObj.getString("main")

                    val dataPair = Pair(dt_txt, weatherDescription)
                    dataList.add(dataPair)
                }

                dataList
            } else {
                // Error handling if necessary
                emptyList()
            }
        }
    }
}
