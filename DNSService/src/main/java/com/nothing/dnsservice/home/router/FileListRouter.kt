package com.nothing.dnsservice.home.router

import kotlinx.serialization.Serializable


@Serializable
data class FileListRouter(
    var ip: String,
    var port: Int,
    var currentSubDir: String,
    var external: Boolean,
    var isAppStorage: Boolean,
    var isRoot:Boolean,
)