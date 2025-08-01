package com.nothing.dnsservice.server.bean

import androidx.annotation.Keep

data class FileInfo(
    var path:String,
    var length:Long,
    var modifyTime:Long,
    var mimeType:String
) {
    var displayName: String? = null
    var formattedDes: String? = null
}

@Keep
data class AppDataResponse(
    var rootPath:String,
    var data:List<FileInfo>
)
