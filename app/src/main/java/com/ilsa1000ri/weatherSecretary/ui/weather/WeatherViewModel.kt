package com.ilsa1000ri.weatherSecretary.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WeatherViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "날씨 페이지"
    }
    val text: LiveData<String> = _text
}