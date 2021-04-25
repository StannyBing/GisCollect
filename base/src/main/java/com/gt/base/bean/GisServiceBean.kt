package com.gt.base.bean

import java.io.Serializable

data class GisServiceBean(
    var id: String = "",
    var sname: String = "",
    var surl: String = "",
    var type: String = "",
    var visible: Boolean = false,
    var category: String = "",
    var notes: String = "",
    var sseq: Int = 0, //排序,
    var children: List<GisServiceBean>? = null
) : Serializable {
    data class OnlineBean(
        var url: String = "",
        var visible: Boolean = false,
        var type: String = "",
        var itemName: String = ""
    ) : Serializable
}