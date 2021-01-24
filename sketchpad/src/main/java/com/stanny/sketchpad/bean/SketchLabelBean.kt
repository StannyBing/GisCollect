package com.stanny.sketchpad.bean

import android.graphics.PointF

data class SketchLabelBean(
    var key: String = "",
    var value: String = "",
    var isChecked: Boolean = false,
    var pointF: PointF
)