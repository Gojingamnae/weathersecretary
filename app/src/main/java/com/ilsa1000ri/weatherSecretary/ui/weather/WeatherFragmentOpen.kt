package com.ilsa1000ri.weatherSecretary.ui.weather

import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.OpenApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class WeatherFragmentOpen {
    private lateinit var txtOpenIndex: TextView
    fun openViews(binding: FragmentWeatherBinding) {
        txtOpenIndex = binding.txtOpenIndex
    }

    fun ofetchDataWeather(latitude:Double, longitude:Double) {
        GlobalScope.launch(Dispatchers.Main) {
            val openIndex = OpenApi.getOpenIndex(latitude, longitude)

            openIndex?.let {
                val celsius = kelvinToCelsius(it)
                val df = DecimalFormat("#")
                val feelsLikeText = df.format(celsius)
                txtOpenIndex.text = feelsLikeText
            }
        }

    }
}
private fun kelvinToCelsius(kelvin: Double): Double {
    return kelvin - 273.15
}