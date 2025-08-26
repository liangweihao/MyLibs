package com.nothing.dnsservice.home.router

import kotlinx.serialization.Serializable

@Serializable
data class InstallAppRouter(var ip: String, var port: Int)
