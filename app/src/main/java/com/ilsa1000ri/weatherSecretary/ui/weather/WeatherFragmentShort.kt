package com.ilsa1000ri.weatherSecretary.ui.weather

import android.util.Log
import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.ShortApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherFragmentShort {
    private lateinit var txtDayRain: TextView
    private lateinit var txt1DayRain: TextView
    private lateinit var txt2DayRain: TextView
    private lateinit var txtDayIcon: TextView
    private lateinit var txt1DayIcon: TextView
    private lateinit var txt2DayIcon: TextView
    private lateinit var txtRainIndex: TextView
    private lateinit var txtDayMax: TextView
    private lateinit var txt1DayMax: TextView
    private lateinit var txt2DayMax: TextView
    private lateinit var txtDayMin: TextView
    private lateinit var txt1DayMin: TextView
    private lateinit var txt2DayMin: TextView

    fun shortViews(binding: FragmentWeatherBinding) {
        txtDayRain = binding.txtDayRain
        txt1DayRain = binding.txt1DayRain
        txt2DayRain = binding.txt2DayRain
        txtDayIcon = binding.txtDayIcon
        txt1DayIcon = binding.txt1DayIcon
        txt2DayIcon = binding.txt2DayIcon
        txtRainIndex = binding.txtRainIndex
        txtDayMax = binding.txtDayMax
        txt1DayMax = binding.txt1DayMax
        txt2DayMax = binding.txt2DayMax
        txtDayMin = binding.txtDayMin
        txt1DayMin = binding.txt1DayMin
        txt2DayMin = binding.txt2DayMin
    }
    fun fetchDataWeatherShort(latitude:Double, longitude:Double) {
        GlobalScope.launch(Dispatchers.Main) {
            val shortIndex = ShortApi.getShortIndex(latitude, longitude)             // 단기- 최고, 최저
            setTMXValues(shortIndex)
            setTMNValues(shortIndex)
            val popData = ShortApi.getShortIndex(latitude, longitude) // 단기- 강수량, 하늘상태
            val currentDate = Date()
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

            val maxPopValues = mutableListOf<String>()
            for (i in 1..3) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)  // 하루 뒤로 이동
                val futureDateString = formatter.format(calendar.time)
                var maxPopValue = 0  // 최대 POP 값을 가지기 위한 변수, 초기값은 0

                // 해당 날짜의 데이터를 순회하면서 최대 POP 값을 찾습니다.
                for (data in popData) {
                    if (data.contains("POP") && data.contains("Date: $futureDateString")) {
                        val popValue = data.split(": ")[1].split(",")[0].toInt()
                        if (popValue > maxPopValue) {
                            maxPopValue = popValue  // 새로운 최대값 발견 시 업데이트
                        }
                    }
                }
                Log.d("shortpopdata", "popdata ${popData}")
                maxPopValues.add(maxPopValue.toString())  // 찾은 최대값을 리스트에 추가
                when (i) { //if문은 강수량 일의자리, 십의 자리 숫자가 될때, 값이 밀려나기에 '공백' 삽입
                    1 -> txtDayRain.text = if (maxPopValues[0].toInt() < 10) "  ${maxPopValues[0]}%  " else "  ${maxPopValues[0]}%"
                    2 -> txt1DayRain.text = if (maxPopValues[1].toInt() < 10) "  ${maxPopValues[1]}%  " else "  ${maxPopValues[1]}%"
                    3 -> txt2DayRain.text = if (maxPopValues[2].toInt() < 10) "  ${maxPopValues[2]}%  " else "  ${maxPopValues[2]}%"

                }

                if (maxPopValues.isNotEmpty()) {
                    txtRainIndex.text = maxPopValues.maxOrNull() + "%"  // 가장 큰 강수 확률을 전체 인덱스로 설정
                }

                // 여기까진 강수량
                // 지금부턴 아이콘

                var weatherIcon = 0
                var priorityWeatherFound = false
                for (data in popData) {
                    if (data.contains("Date: $futureDateString")) {
                        val skyValue = data.split(": ")[1].split(",")[0].trim()
                        if (skyValue == "비" || skyValue == "눈") {
                            weatherIcon = when (skyValue) {
                                "눈" -> R.drawable.ic_weather_snow
                                "비" -> R.drawable.ic_weather_rain_fi
                                else -> 0
                            }
                            priorityWeatherFound = true
                            break
                        }
                    }
                }
                if (!priorityWeatherFound) { // 눈이나 비가 아닐때
                    for (data in popData) {
                        if (data.contains("SKY") && data.contains("Date: $futureDateString")) {
                            val weatherCondition = data.split(": ")[1].split(",")[0].trim()
                            weatherIcon = when (weatherCondition) {
                                "맑음" -> R.drawable.ic_weather_sunny_fi
                                "흐림", "구름많음" -> R.drawable.ic_weather_cloud_fi
                                else -> 0
                            }
                            break
                        }
                    }
                }
                if (weatherIcon != 0) {
                    when (i) {
                        1 -> txtDayIcon.setCompoundDrawablesWithIntrinsicBounds(
                            weatherIcon, 0,0, 0
                        )
                        2 -> txt1DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                            weatherIcon, 0, 0, 0
                        )
                        3 -> txt2DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                            weatherIcon, 0, 0, 0
                        )
                    }
                } else {
                    if (i == 3) {
                        txt2DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_weather_cloud_fi, 0, 0, 0
                        )
                    } } } } }

    private fun setTMXValues(shortIndex: List<String>) {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val df = DecimalFormat("#") // 소수점 없이 표현하도록 포맷 설정

        for (i in 0 until 3) { // 현재 날짜부터 3일간의 TMX 값을 찾아서 TextView에 설정
            calendar.add(Calendar.DAY_OF_MONTH, i)
            val futureDateString = formatter.format(calendar.time)
            for (index in shortIndex.indices) {
                val data = shortIndex[index]
                if (data.startsWith("TMX") && data.contains("Date: $futureDateString")) {
                    val tmxValue = data.split(": ")[1].split(",")[0]
                    val formattedTMX = df.format(tmxValue.toDouble()) // 소수점 없이 형식화
                    when (i) {
                        0 -> {txtDayMax.text = formattedTMX + "°"
                            Log.d("TMXValues", "Day 0 TMX: ${txtDayMax.text}")
                        }
                        1 -> {
                            txt1DayMax.text = formattedTMX + "°"
                            Log.d("TMXValues", "Day 1 TMX: ${txt1DayMax.text}")
                        }

                        2 -> {txt2DayMax.text = formattedTMX + "°"
                            Log.d("TMXValues", "Day 2 TMX: ${txt2DayMax.text}")}
                    }
                    break }
                if (txt2DayMax.text.isEmpty()) {
                    txt2DayMax.text = "27°"
                }
            }
            calendar.time = currentDate // 다음 날짜를 위해 현재 날짜로 다시 초기화
        } }

    private fun setTMNValues(shortIndex: List<String>) {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        Log.d("realchorttimecheck", "calendar.time check ${calendar.time}")

        val df = DecimalFormat("#") // 소수점 없이 표현하도록 포맷 설정

        for (i in 0 until 3) { // 현재 날짜부터 3일간의 TMN 값을 찾아서 TextView에 설정
            calendar.add(Calendar.DAY_OF_MONTH, i)
            val futureDateString = formatter.format(calendar.time)
            for (index in shortIndex.indices) {
                val data = shortIndex[index]
                if (data.startsWith("TMN") && data.contains("Date: $futureDateString")) {
                    val tmnValue = data.split(": ")[1].split(",")[0]
                    val formattedTMN = df.format(tmnValue.toDouble()) // 소수점 없이 형식화
                    when (i) {
                        0 -> txtDayMin.text = formattedTMN + "°"
                        1 -> txt1DayMin.text = formattedTMN + "°"
                        2 -> txt2DayMin.text = formattedTMN + "°"
                    }
                    break }
                if (txt2DayMin.text.isEmpty()) {
                    txt2DayMin.text = "18°"
                }
            }
            calendar.time = currentDate // 다음 날짜를 위해 현재 날짜로 다시 초기화
        } } }