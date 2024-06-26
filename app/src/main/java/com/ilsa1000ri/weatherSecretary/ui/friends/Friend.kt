package com.ilsa1000ri.weatherSecretary.ui.friends

data class Friend(
    val name: String,
    val uid: String? = null,
    var isFavorite: Boolean = false
)