package com.ilsa1000ri.weatherSecretary.ui.weather

import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.MidLandApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WeatherFragmentMid {
    private lateinit var txt3DayRain: TextView
    private lateinit var txt4DayRain: TextView
    private lateinit var txt5DayRain: TextView
    fun midViews(binding: FragmentWeatherBinding) {
        txt3DayRain = binding.txt3DayRain
        txt4DayRain = binding.txt4DayRain
        txt5DayRain = binding.txt5DayRain

    }

    fun fetchDataWeatherMid() {
        GlobalScope.launch(Dispatchers.Main) {
            val rain5Data = MidLandApi.getLandIndex()

            rain5Data?.let { rainData ->
                // rnSt3Am과 rnSt3Pm을 비교하여 큰 값을 선택, 값이 같으면 rnSt3Am을 선택
                val txt3DayRainValue = if (rainData[0] >= rainData[1]) rainData[0] else rainData[1]
                txt3DayRain.text = "  $txt3DayRainValue%"

                // rnSt4Am과 rnSt4Pm을 비교하여 큰 값을 선택, 값이 같으면 rnSt4Am을 선택
                val txt4DayRainValue = if (rainData[2] >= rainData[3]) rainData[2] else rainData[3]
                txt4DayRain.text = "  $txt4DayRainValue%"

                // rnSt5Am과 rnSt5Pm을 비교하여 큰 값을 선택, 값이 같으면 rnSt5Am을 선택
                val txt5DayRainValue = if (rainData[4] >= rainData[5]) rainData[4] else rainData[5]
                txt5DayRain.text = "  $txt5DayRainValue%"
            }
        }
    }
}