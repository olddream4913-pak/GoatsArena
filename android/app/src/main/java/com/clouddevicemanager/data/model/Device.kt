package com.clouddevicemanager.data.model

import com.google.firebase.Timestamp

data class Device(
    val id: String = "",
    val name: String = "",
    val osVersion: String = "Android 14",
    val region: String = "US-East",
    val isOnline: Boolean = true,
    val createdAt: Timestamp? = null
)