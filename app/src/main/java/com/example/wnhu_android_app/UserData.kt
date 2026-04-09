package com.example.wnhu_android_app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class UserData : ViewModel() {

    var user by mutableStateOf(
        UserModel(
            firstName = "Sarah",
            lastName = "Svitlik",
            email = "s@unh.newhaven.edu"
        )
    )

    var songs = mutableStateListOf(
        LikedSongModel("No One Noticed", "The Marias"),
    )
}
