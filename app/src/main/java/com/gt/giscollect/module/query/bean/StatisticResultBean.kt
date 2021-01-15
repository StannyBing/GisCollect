package com.gt.giscollect.module.query.bean

/**
 * mail:87469669@qq.com
 */
data class StatisticResultBean(
    var name: String,
    var value1: String? = null,
    var value2: String? = null,
    var layerName: String = "",
    var isTitle: Boolean = false
)