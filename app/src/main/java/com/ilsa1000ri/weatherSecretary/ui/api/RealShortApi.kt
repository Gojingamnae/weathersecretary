package com.ilsa1000ri.weatherSecretary.ui.api

import android.graphics.Point
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

object RealShortApi {
    //통합 완료
    private const val BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"
    private const val SERVICE_KEY = "O%2B4lJjrJFRco0YDo8fLYlNbJXhZ6NSKuoPvWaLmpzuZmjoYuW25lafiElwJtKjjnREZc3AS%2B5SwDFbNbjXQbGg%3D%3D"
    private const val PAGE_NO = "1"
    private const val NUM_OF_ROWS = "1000"
    private var BASE_DATE = ""
    private var BASE_TIME = ""
    private var NX = ""
    private var NY = ""

    suspend fun getRealShortIndex(latitude: Double, longitude: Double): List<String> {
        Log.i("RealShortApi", "RealShortApiIndex에 전달된 latitude, longitude : ${latitude}, ${longitude}")
        setBaseDateTime() // 현재 날짜와 시간으로 BASE_DATE와 BASE_TIME 설정
        val currentTime = Calendar.getInstance().time
        val currentTimeString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime)
        val point = dfs_xy_conv(latitude.toInt(), longitude.toInt())
        NX = point.x.toString()
        NY = point.y.toString()
        return withContext(Dispatchers.IO) {
            val urlBuilder = StringBuilder(BASE_URL)
            urlBuilder.append("?ServiceKey=$SERVICE_KEY")
            urlBuilder.append("&numOfRows=$NUM_OF_ROWS")
            urlBuilder.append("&pageNo=$PAGE_NO")
            urlBuilder.append("&base_date=$BASE_DATE")
            urlBuilder.append("&base_time=$BASE_TIME")
            urlBuilder.append("&nx=$NX")
            urlBuilder.append("&ny=$NY")

            val url = URL(urlBuilder.toString())
            Log.d("RealShortApi", "Request URL: ${url.toString()}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")

            val responseCode = conn.responseCode
            Log.d("RealShortApi", "Response Code: $responseCode")
            if (responseCode >= 200 && responseCode <= 300) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                val xmlString = br.use { it.readText() }
                Log.d("RealShortApi", "Response Body: $xmlString")
                conn.disconnect()
                extractValuesFromXml(xmlString)
            } else {
                // Error handling if necessary
                emptyList()
            }
        }

    }


    private fun setBaseDateTime() {
        val calendar = Calendar.getInstance()

// 현재 시간에서 한 시간을 뺀다
        calendar.add(Calendar.HOUR_OF_DAY, -1)

        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        BASE_DATE = sdfDate.format(calendar.time)

        val sdfTime = SimpleDateFormat("HH", Locale.getDefault())
        BASE_TIME = sdfTime.format(calendar.time) + "00" // 분을 '00'으로 설정

        Log.d("RealShortApi", "Base Date: $BASE_DATE, Base Time: $BASE_TIME")
    }

    fun extractValuesFromXml(xmlString: String): List<String> {
        val categoryRegex = Regex("<category>(.*?)</category>")
        val fcstValueRegex = Regex("<fcstValue>(.*?)</fcstValue>")
        val fcstDateRegex = Regex("<fcstDate>(.*?)</fcstDate>")
        val fcstTimeRegex = Regex("<fcstTime>(.*?)</fcstTime>")

        val itemRegex = Regex("<item>(.*?)</item>")

        val itemMatches = itemRegex.findAll(xmlString)
        val result = mutableListOf<String>()

        for (itemMatch in itemMatches) {
            val itemString = itemMatch.groupValues[1]

            val categoryMatch = categoryRegex.find(itemString)
            val fcstValueMatch = fcstValueRegex.find(itemString)
            val fcstDateMatch = fcstDateRegex.find(itemString)
            val fcstTimeMatch = fcstTimeRegex.find(itemString)

            val category = categoryMatch?.groupValues?.get(1) ?: ""
            val fcstValue = fcstValueMatch?.groupValues?.get(1) ?: ""
            val fcstDate = fcstDateMatch?.groupValues?.get(1) ?: ""
            val fcstTime = fcstTimeMatch?.groupValues?.get(1) ?: ""

            var ptyValue: String? = null // 강수 정보를 확인하기 위해 null로 초기화

            when (category) {
                "PTY" -> {
                    ptyValue = fcstValue // PTY를 추출하여 강수 정보를 설정
                    val ptyString = when (ptyValue) {
                        "0" -> null // 강수 없음
                        "1", "5" -> "비"
                        "2", "3", "6", "7" -> "눈"
                        else -> null
                    }
                    if (ptyString != null) {
                        result.add("PTY: $ptyString, Date: $fcstDate, Time: $fcstTime")
                    }
                }
                "SKY" -> {
// 강수 정보가 없거나 아직 강수 정보가 설정되지 않은 경우에만 SKY 정보를 추가
                    if (ptyValue == null || ptyValue == "0") {
                        val skyString = when (fcstValue) {
                            "1" -> "맑음"
                            "3" -> "구름많음"
                            "4" -> "흐림"
                            else -> ""
                        }
                        result.add("SKY: $skyString, Date: $fcstDate, Time: $fcstTime")
                    }
                }
                "T1H" -> { //온도
                    result.add("T1H: $fcstValue, Date: $fcstDate, Time: $fcstTime")
                }
            }
// Other category cases could be added here...
        }
        return result
    }
    fun dfs_xy_conv(v1: Int, v2:Int) : Point {
        val RE = 6371.00877     // 지구 반경(km)
        val GRID = 5.0          // 격자 간격(km)
        val SLAT1 = 30.0        // 투영 위도1(degree)
        val SLAT2 = 60.0        // 투영 위도2(degree)
        val OLON = 126.0        // 기준점 경도(degree)
        val OLAT = 38.0         // 기준점 위도(degree)
        val XO = 43             // 기준점 X좌표(GRID)
        val YO = 136            // 기준점 Y좌표(GRID)
        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = v2 * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Point(x, y)
    }
}