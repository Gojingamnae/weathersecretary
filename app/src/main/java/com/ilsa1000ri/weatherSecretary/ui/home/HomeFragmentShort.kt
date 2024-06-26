package com.ilsa1000ri.weatherSecretary.ui.home

import android.widget.TextView
import com.ilsa1000ri.weatherSecretary.databinding.FragmentHomeBinding
import com.ilsa1000ri.weatherSecretary.ui.api.ShortApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragmentShort {
    private lateinit var txtHomeRain: TextView

    fun initializeViews2(binding: FragmentHomeBinding) {
        txtHomeRain = binding.txtHomeRain
    }

    fun fetchDataWeatherShort(latitude:Double, longitude:Double) {
        GlobalScope.launch(Dispatchers.Main) {
            //short- 강수량
            val popData = ShortApi.getShortIndex(latitude, longitude)

            // POP 강수량
            var popValue = ""
            val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val futureDateString = formatter.format(calendar.time)

            for (data in popData) {
                if (data.contains("POP") && data.contains("Date: $futureDateString")) {
                    popValue = data.split(": ")[1].split(",")[0]
                    break
                }
            }
            popValue?.let {
                txtHomeRain.text = popValue.toString() + "%"
            }
        }
    }
}