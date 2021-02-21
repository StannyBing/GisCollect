package com.gt.entrypad.module.project.bean

import com.esri.arcgisruntime.data.Attachment

data class FileInfoBean(
    var name: String = "",
    var path: String = "",
    var pathImage: String = "",
    var createTime: String = "",
    var type: String = "",//camera图片  video视频   record录音
    var attachment: Attachment? = null
) {
}