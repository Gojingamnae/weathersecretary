package com.ilsa1000ri.weatherSecretary.ui.weather

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.AirApi
import com.ilsa1000ri.weatherSecretary.ui.api.RealShortApi
import com.ilsa1000ri.weatherSecretary.ui.api.SunApi
import com.ilsa1000ri.weatherSecretary.ui.location.Area
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale


class WeatherFragmentRealShort {
    private lateinit var txtAirIndex: TextView
    private lateinit var txtUVIndex: TextView
    private lateinit var txtTempIndex: TextView
    private lateinit var txtNow1Icon: TextView
    private lateinit var txtNow2Icon: TextView
    private lateinit var txtNow3Icon: TextView
    private lateinit var txtNow4Icon: TextView
    private lateinit var txtNow5Icon: TextView
    private lateinit var txtNow6Icon: TextView
    private lateinit var txtNow1Hour: TextView
    private lateinit var txtNow2Hour: TextView
    private lateinit var txtNow3Hour: TextView
    private lateinit var txtNow4Hour: TextView
    private lateinit var txtNow5Hour: TextView
    private lateinit var txtNow6Hour: TextView
    private lateinit var txtNow1Temp: TextView
    private lateinit var txtNow2Temp: TextView
    private lateinit var txtNow3Temp: TextView
    private lateinit var txtNow4Temp: TextView
    private lateinit var txtNow5Temp: TextView
    private lateinit var txtNow6Temp: TextView
    private lateinit var txtUpTemp: TextView
    private lateinit var txtUpSky: TextView
    private lateinit var txtUpText: TextView
    private val nowPlus1HourText = mutableListOf<String>() // 날씨와 관련된 문자열을 저장하기 위한 리스트

    private var nx:Int = 0
    private var ny:Int = 0
    fun realViews(binding: FragmentWeatherBinding) {
        txtAirIndex = binding.txtAirIndex
        txtUVIndex = binding.txtUVIndex
        txtTempIndex = binding.txtTempIndex
        txtNow1Icon = binding.txtNow1Icon
        txtNow2Icon = binding.txtNow2Icon
        txtNow3Icon = binding.txtNow3Icon
        txtNow4Icon = binding.txtNow4Icon
        txtNow5Icon = binding.txtNow5Icon
        txtNow6Icon = binding.txtNow6Icon
        txtNow1Hour = binding.txtNow1Hour
        txtNow2Hour = binding.txtNow2Hour
        txtNow3Hour = binding.txtNow3Hour
        txtNow4Hour = binding.txtNow4Hour
        txtNow5Hour = binding.txtNow5Hour
        txtNow6Hour = binding.txtNow6Hour
        txtNow1Temp = binding.txtNow1Temp
        txtNow2Temp = binding.txtNow2Temp
        txtNow3Temp = binding.txtNow3Temp
        txtNow4Temp = binding.txtNow4Temp
        txtNow5Temp = binding.txtNow5Temp
        txtNow6Temp = binding.txtNow6Temp
        txtUpTemp = binding.txtUpTemp
        txtUpSky = binding.txtUpSky
        txtUpText = binding.txtUpText
    }
    fun wfetchDataWeatherRealShort(latitude:Double, longitude:Double, resources: Resources) {
        GlobalScope.launch(Dispatchers.Main) {
            val point = dfs_xy_conv(
                latitude.toInt(),
                longitude.toInt()
            )
            val AREA_NO = gridToAreaNum(nx, ny, resources)!!
            nx = point.x
            ny = point.y
            Log.d("WeatherFragmentRealShort", "AREA_NO : ${AREA_NO}")

            val realShortIndex = RealShortApi.getRealShortIndex(latitude, longitude)
            val nowPlus1HourSky = mutableListOf<String>()
            val nowPlus1HourT1H = mutableListOf<String>()
            val nowPlus1HourTime = mutableListOf<String>()
            val airIndex = AirApi.getAirIndex(AREA_NO)
            val uvIndex = SunApi.getUVIndex(AREA_NO)
            airIndex?.let { txtAirIndex.text = airIndex.toString()
                setIndexColor(airIndex)}

            uvIndex?.let { txtUVIndex.text = uvIndex.toString()
                setUvIndexColor(uvIndex)}

            realShortIndex.forEach { data ->
                when {
                    data.startsWith("PTY") -> {
                        val ptyValue = data.split(":")[1].split(",")[0].trim()
                        val weatherIcon = when (ptyValue) {
                            "비" -> R.drawable.ic_weather_rain_fi
                            "눈" -> R.drawable.ic_weather_snow
                            else -> 0 // 강수 없음(맑음, 구름많음, 흐림)
                        }
                        if (weatherIcon != 0) {
                            nowPlus1HourSky.add(weatherIcon.toString())
                            nowPlus1HourText.add(ptyValue)
                        } }

                    data.startsWith("SKY") && (nowPlus1HourSky.isEmpty() || (!nowPlus1HourSky.last().contains("비") && !nowPlus1HourSky.last().contains("눈"))) -> {
                        // SKY 데이터를 처리하기 전에, 마지막 하늘 상태가 비나 눈이 아닌지 확인> "SKY"로 시작하는 데이터를 처리하려고 할 때, nowPlus1HourSky 리스트가 비어 있거나, 리스트가 비어 있지 않은 경우 리스트의 마지막 요소가 "비"나 "눈"을 포함하지 않는 경우에만 해당 로직을 실행
                        val skyValue = data.split(":")[1].split(",")[0].trim()
                        val weatherIcon = when (skyValue) {
                            "맑음" -> R.drawable.ic_weather_sunny_fi
                            "구름많음" -> R.drawable.ic_weather_cloud_fi
                            "흐림" -> R.drawable.ic_weather_cloud_fi
                            else -> 0 // 기본값
                        }
                        if (weatherIcon != 0) {
                            nowPlus1HourSky.add(weatherIcon.toString())
                            nowPlus1HourText.add(skyValue) } }

                    data.startsWith("T1H") -> {
                        val t1hValue = data.split(":")[1].split(",")[0].trim() // T1H 값만 추출
                        nowPlus1HourT1H.add(t1hValue) } }

                val iconViews = listOf(txtNow1Icon, txtNow2Icon, txtNow3Icon, txtNow4Icon, txtNow5Icon, txtNow6Icon)
                iconViews.forEachIndexed { index, textView ->
                    val iconId = nowPlus1HourSky.getOrNull(index)?.toInt() ?: 0 // 아이콘 ID 가져오기
                    textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0) }
                if (data.startsWith("T1H")) {
                    val t1hValue = data.split(":")[1].split(",")[0].trim() // T1H 값만 추출
                    nowPlus1HourT1H.add(t1hValue) }
                if (data.startsWith("T1H")) {
                    val timeValue = data.split("Time: ")[1].trim() // "Time: " 뒤에 오는 값만 추출
                    nowPlus1HourTime.add(timeValue)
                } }

            txtUpSky.text = nowPlus1HourText.getOrNull(0) ?: "" // 배열에 담긴 T1H 값만을 각각의 TextView에 표시
            txtNow1Temp.text = "    ${nowPlus1HourT1H.getOrNull(0) ?: ""}°"
            txtNow2Temp.text = "    ${nowPlus1HourT1H.getOrNull(1) ?: ""}°"
            txtNow3Temp.text = "    ${nowPlus1HourT1H.getOrNull(2) ?: ""}°"
            txtNow4Temp.text = "    ${nowPlus1HourT1H.getOrNull(3) ?: ""}°"
            txtNow5Temp.text = "    ${nowPlus1HourT1H.getOrNull(4) ?: ""}°"
            txtNow6Temp.text = "    ${nowPlus1HourT1H.getOrNull(5) ?: ""}°"

            nowPlus1HourT1H.firstOrNull()?.let {
                txtTempIndex.text = it + "°"
                txtUpTemp.text = it  }

            val upSkyStatus = txtUpSky.text.toString() // 한마디
            val airQualityStatus = txtAirIndex.text.toString()
            val uvStatus = txtUVIndex.text.toString()
            when {
                upSkyStatus == "비" -> txtUpText.text = "비가 내리고 있어요, 우산을 챙기세요!"// 1순위: 비 또는 눈 상태일 때
                upSkyStatus == "눈" -> txtUpText.text = "하얀 눈이 아름답게 내리고 있어요."
                airQualityStatus in listOf("높음", "매우높음") -> txtUpText.text = "미세먼지가 나쁘니 마스크를 착용하세요." // 2순위: 미세먼지 상태가 높음 또는 매우 높음일 때
                uvStatus in listOf("높음", "매우높음", "위험") -> txtUpText.text = "자외선 차단제를 잊지 마세요!" // 3순위: 자외선 지수가 높음 또는 매우 높음 또는 위험일 때
                upSkyStatus == "맑음" -> txtUpText.text = "오늘은 맑은 하늘이 너무 아름답네요." // 4순위: 하늘 상태에 따른 메시지
                upSkyStatus == "흐림" -> txtUpText.text = "햇빛이 가려져 어두운 날씨네요"
                upSkyStatus == "구름많음" -> txtUpText.text = "하늘에 구름이 예쁘게 퍼져있네요."
            }
            val originalFormat = SimpleDateFormat("HHmm", Locale.getDefault())
            val targetFormat = SimpleDateFormat("a h시", Locale.getDefault())
            nowPlus1HourTime.forEachIndexed { index, time ->   // 배열에 담긴 시간 값들을 오전/오후 시간 포맷으로 변경하여 TextView에 설정
                val date = originalFormat.parse(time)  // HHmm 포맷에서 Date 객체로 파싱
                val formattedTime = date?.let { targetFormat.format(it) } // Date 객체를 a h시 포맷으로 변환
                when (index) { // 변환된 시간을 TextView에 설정합니다.
                    0 -> txtNow1Hour.text = formattedTime ?: "" // 배열의 첫 번째 값
                    1 -> txtNow2Hour.text = formattedTime ?: "" // 2
                    2 -> txtNow3Hour.text = formattedTime ?: "" // 3
                    3 -> txtNow4Hour.text = formattedTime ?: "" // 4
                    4 -> txtNow5Hour.text = formattedTime ?: "" // 5
                    5 -> txtNow6Hour.text = formattedTime ?: "" // 6
                } } } }
    fun setIndexColor(airIndex: String) {
                when (airIndex) {
                    "높음", "매우높음" -> txtAirIndex.setTextColor(Color.parseColor("#FF5050"))
                    else -> txtAirIndex.setTextColor(Color.BLACK) // 기본 색상
                }
            } fun setUvIndexColor(uvIndex: String) {
            when (uvIndex) {
                "높음", "매우높음", "위험" -> txtUVIndex.setTextColor(Color.parseColor("#FF5050"))
                else -> txtUVIndex.setTextColor(Color.BLACK) // 기본 색상
        }
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
    fun gridToAreaNum(NX: Int, NY: Int, resources: Resources): String? {
        val inputStream: InputStream = resources.openRawResource(R.raw.output)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val gridX = jsonObject.getInt("격자 X")
            val gridY = jsonObject.getInt("격자 Y")

            if (gridX == NX && gridY == NY) {
                return jsonObject.getString("행정구역코드")
            }
        }

        return "4817074000" // 해당하는 지역이 없을 경우 null 반환
    }

}
