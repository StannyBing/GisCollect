package com.gt.giscollect.module.system.bean

import java.io.Serializable

data class DataResBean(
    val appUrl: String = "",
    val materialId: String = "",
    val catalogId: String = "",
    val created: Long = 0L,
    val el: String = "",
    val fileExt: String = "",
    val fileJson: String? = "",
    val fileName: String = "",
    val filePath: String = "",
    val fileUri: String = "",
    val maType: String = "",
    val maUrl: String = "",
    val maYear: Int = 0,
    val materialName: String = "",
    val materialPid: String = "",
    val refTable: String = "",
    val remark: String = "",
    val rnCode: String = "",
    val rowno: Int = 0,
    val scale: String = "",
    val srid: Int = 0,
    val userId: String = "",
    val userName: String = "",
    var isDownload: Boolean = false
) : Serializable