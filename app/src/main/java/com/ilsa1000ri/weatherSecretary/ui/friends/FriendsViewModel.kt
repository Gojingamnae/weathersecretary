package com.ilsa1000ri.weatherSecretary.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsViewModel : ViewModel() {
    private val _selectedFriend = MutableLiveData<Friend>()
    val selectedFriend: LiveData<Friend>
        get() = _selectedFriend

    fun selectFriend(friend: Friend) {
        _selectedFriend.value = friend
    }
}