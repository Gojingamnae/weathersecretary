package com.ilsa1000ri.weatherSecretary.ui.weather

import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.ThisWeekApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WeatherFragmentWeek {
    private lateinit var txt3DayMax: TextView
    private lateinit var txt3DayMin: TextView
    private lateinit var txt4DayMax: TextView
    private lateinit var txt4DayMin: TextView
    private lateinit var txt5DayMax: TextView
    private lateinit var txt5DayMin: TextView
    fun weekViews(binding: FragmentWeatherBinding) {
        txt3DayMax = binding.txt3DayMax
        txt3DayMin = binding.txt3DayMin
        txt4DayMax = binding.txt4DayMax
        txt4DayMin = binding.txt4DayMin
        txt5DayMax = binding.txt5DayMax
        txt5DayMin = binding.txt5DayMin
    }

    fun fetchDataWeatherWeek() {
        GlobalScope.launch(Dispatchers.Main) {
            val maxmin5Data = ThisWeekApi.getWEEKIndex()
            maxmin5Data?.let { maxMinData ->
                // maxMinData에서 최소/최대 온도 데이터를 추출하여 각각의 텍스트뷰에 할당
                txt3DayMin.text = maxMinData[0] + "°"
                txt3DayMax.text = maxMinData[1] + "°"
                txt4DayMin.text = maxMinData[2] + "°"
                txt4DayMax.text = maxMinData[3] + "°"
                txt5DayMin.text = maxMinData[4] + "°"
                txt5DayMax.text = maxMinData[5] + "°"
        }

            }
        }
}