package com.gt.giscollect.module.collect.bean

data class VersionBean(
    var versionCode: Int,
    var versionName: String,
    var fileJson: String,
    var created: String,
    var content: String?,
    var versionId: String,
    var catalogId: String,
    var fileUri: String
) {
}