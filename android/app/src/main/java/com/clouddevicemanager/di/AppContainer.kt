package com.clouddevicemanager.di

import com.clouddevicemanager.data.repository.AuthRepository
import com.clouddevicemanager.data.repository.CloudDeviceRepository
import com.clouddevicemanager.data.repository.DeviceRepository

object AppContainer {
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val cloudDeviceRepository: CloudDeviceRepository by lazy { CloudDeviceRepository() }
    val deviceRepository: DeviceRepository by lazy { DeviceRepository() }
}