package com.ilsa1000ri.weatherSecretary.ui.weather

import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.Days5Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherFragmentDay5 {
    private lateinit var txt3DayIcon: TextView
    private lateinit var txt4DayIcon: TextView
    private lateinit var txt5DayIcon: TextView

    fun day5Views(binding: FragmentWeatherBinding) {
        txt3DayIcon = binding.txt3DayIcon
        txt4DayIcon = binding.txt4DayIcon
        txt5DayIcon = binding.txt5DayIcon
    }


    fun fetchDataWeatherDay5(latitude:Double, longitude:Double) {
        GlobalScope.launch(Dispatchers.Main) {

            val days5Data = Days5Api.getDays5Data(latitude, longitude)

            days5Data?.let {
                val currentDate = Date()
                val calendar = Calendar.getInstance()
                calendar.time = currentDate

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                for (i in 3..5) {
                    calendar.add(Calendar.DAY_OF_MONTH, i)
                    val futureDateString = formatter.format(calendar.time)

                    val weatherIcon: Int
                    for ((dtTxt, weatherDescription) in it) {
                        if (dtTxt.contains(futureDateString)) {
                            weatherIcon = when {
                                "Snow" in weatherDescription -> R.drawable.ic_weather_snow
                                "Rain" in weatherDescription -> R.drawable.ic_weather_rain_fi
                                "Clouds" in weatherDescription -> R.drawable.ic_weather_cloud_fi
                                else -> R.drawable.ic_weather_sunny_fi
                            }

                            // 이미지 리소스를 설정
                            when (i) {
                                3 -> txt3DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                                    weatherIcon,
                                    0,
                                    0,
                                    0
                                )

                                4 -> txt4DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                                    weatherIcon,
                                    0,
                                    0,
                                    0
                                )

                                5 -> txt5DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                                    weatherIcon,
                                    0,
                                    0,
                                    0
                                )
                            }
                            break // 첫 번째 발견된 정보만 사용하고 반복문 종료
                        }
                    }
                    calendar.time = currentDate // 날짜를 다시 현재로 초기화
                }
                if (txt5DayIcon.compoundDrawables[0] == null) {
                    txt5DayIcon.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_weather_cloud_fi,
                        0,
                        0,
                        0
                    )
                }
            }
        }
    }
}