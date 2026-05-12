package com.clouddevicemanager.data.model

import com.google.firebase.Timestamp

data class CloudDevice(
    val id: String = "",
    val name: String = "",
    val region: String = "",
    val androidVersion: String = "",
    val status: String = "offline",
    val createdAt: Timestamp? = null
)