package com.ilsa1000ri.weatherSecretary.ui.api

import android.graphics.Point
import android.util.Log
import com.ilsa1000ri.weatherSecretary.ui.api.RealShortApi.dfs_xy_conv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ShortApi {
    //통합 완료
    private const val BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
    private const val SERVICE_KEY = "kdBltm%2F4TCJMOW7AkD21ODMfLn7z0BywAq2GBoKRSWK4AfXGAecYkuF%2Fepz45IMqd6pEGfuPJU9%2ByCo8hFr5xw%3D%3D"
    private const val PAGE_NO = "2"
    private const val NUM_OF_ROWS = "3000"
    private var BASE_TIME = "0200" // TMX와 TMN을 받아오기 위해선 Base_Time의 고정이 필요하다
    private var NX = ""
    private var NY = ""


    suspend fun getShortIndex(latitude: Double, longitude: Double): List<String> {
        return withContext(Dispatchers.IO) {
            // base_time 고정 로직때문에 구현하는 if문
            val calendar = Calendar.getInstance()
            val currentTime = calendar.time
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            Log.d("data", "cureenthour = ${currentHour}")


            // 현재 시간이 0,1,2일때만 하루전으로 bcz base_time의 상수값때문에
            if (currentHour == 0 || currentHour == 1 || currentHour == 2 ) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }

            val currentTimeString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
            Log.d("data", "currentTimeString = ${currentTimeString}")

            Log.i("ShortApi", "ShortApiIndex에 전달된 latitude, longitude : ${latitude}, ${longitude}")
            val point = dfs_xy_conv(latitude.toInt(), longitude.toInt())
            NX = point.x.toString()
            NY = point.y.toString()
            Log.d("ShortApi", "ShortApiIndex에 전달된 NX = ${NX}, NY = ${NY}")

            val urlBuilder = StringBuilder(BASE_URL) /* URL */
            urlBuilder.append("?ServiceKey=$SERVICE_KEY")
            urlBuilder.append("&numOfRows=$NUM_OF_ROWS")
            urlBuilder.append("&pageNo=$PAGE_NO")
            urlBuilder.append("&base_date=$currentTimeString")
            urlBuilder.append("&base_time=$BASE_TIME")
            urlBuilder.append("&nx=$NX")
            urlBuilder.append("&ny=$NY")
            val url = URL(urlBuilder.toString())
            Log.d("ShortApi", "Request URL: ${url.toString()}")
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

            // pty가 0이라면 sky값, 그외라면 다르게 뜨게
            val itemResult = when (category) {
                "POP" -> "POP: $fcstValue, Date: $fcstDate, Time: $fcstTime"
                "TMN" -> "TMN: $fcstValue, Date: $fcstDate, Time: $fcstTime"
                "TMX" -> "TMX: $fcstValue, Date: $fcstDate, Time: $fcstTime"
                "PTY" -> {
                    val ptyString = when (fcstValue) {
                        "0" -> ""
                        "1", "2", "4" -> "비"
                        "3" -> "눈"
                        else -> ""
                    }
                    if (ptyString.isNotEmpty()) {
                        "PTY: $ptyString, Date: $fcstDate, Time: $fcstTime"
                    } else {
                        continue
                    }
                }
                "SKY" -> {
                    val skyString = when (fcstValue) {
                        "1" -> "맑음"
                        "3" -> "구름많음"
                        "4" -> "흐림"
                        else -> ""
                    }

                    val ptyMatch = Regex("<category>PTY</category>.*?<fcstValue>(.*?)</fcstValue>").find(xmlString)
                    val ptyValue = ptyMatch?.groupValues?.get(1) ?: "0"
                    if (ptyValue == "0") {
                        "SKY: $skyString, Date: $fcstDate, Time: $fcstTime"
                    } else {
                        continue
                    }
                }
                else -> ""
            }
            if (itemResult.isNotEmpty()) {
                result.add(itemResult)
            }
        }

        return result
    }
    fun dfs_xy_conv(v1: Int, v2: Int) : Point {
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
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 sn의 값 : ${sn}")

        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 sf의 값 : ${sf}")

        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 ro의 값 : ${ro}")

        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 ra의 값 : ${ra}")

        var theta = v2 * DEGRAD - olon
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 theta의 값 : ${theta}")

        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()
        Log.i("ShortApi", "ShortApiIndex의 dfs_xy_conv 내 x, y의 값 : ${x}, ${y}")

        return Point(x, y)
    }


}