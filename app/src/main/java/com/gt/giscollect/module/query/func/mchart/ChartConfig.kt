package com.gt.giscollect.module.query.func.mchart

import com.gt.giscollect.R


object ChartConfig {
    private val colors =
        arrayListOf(
            R.color.Chart_1,
            R.color.Chart_2,
            R.color.Chart_3,
            R.color.Chart_4,
            R.color.Chart_5,
            R.color.Chart_6,
            R.color.Chart_7,
            R.color.Chart_8,
            R.color.Chart_9,
            R.color.Chart_10,
            R.color.Chart_11,
            R.color.Chart_12,
            R.color.Chart_13,
            R.color.Chart_14,
            R.color.Chart_15,
            R.color.Chart_16,
            R.color.Chart_17,
            R.color.Chart_18,
            R.color.Chart_19,
            R.color.Chart_20
        )

    fun getColor(index: Int): Int {
        return colors[index % 20]
//        if (index < colors.size) {
//        } else {
//            return colors[index%20]
//        }
    }
}