package com.ilsa1000ri.weatherSecretary.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "홈 페이지"
    }
    val text: LiveData<String> = _text
}