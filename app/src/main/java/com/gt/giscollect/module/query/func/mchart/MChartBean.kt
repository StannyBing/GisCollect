package com.gt.giscollect.module.query.func.mchart

/**
 * Created by Xiangb on 2019/10/11.
 * 功能：
 */
data class MChartBean(
    var name: String,
    var num: Double,
    var percent: Double = 0.0,
    var showCenter: Boolean = false
) {
}