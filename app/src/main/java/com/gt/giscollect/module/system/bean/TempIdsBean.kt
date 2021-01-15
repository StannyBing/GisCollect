package com.gt.giscollect.module.system.bean

import java.io.Serializable

data class TempIdsBean(
    var name: String,
    var templateId: String,
    var catalogId: String,
    var layerNames: ArrayList<String> = arrayListOf()
) : Serializable {
}