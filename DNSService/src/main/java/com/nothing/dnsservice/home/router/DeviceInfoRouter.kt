package com.nothing.dnsservice.home.router

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoRouter(var ip: String, var port: Int)
