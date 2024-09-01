package com.kohan.shared.armeria.converter.request.result

data class AccessDeviceInfo(
    val ip: String,
    val userAgent: String,
    val os: String,
    val device: String,
)
