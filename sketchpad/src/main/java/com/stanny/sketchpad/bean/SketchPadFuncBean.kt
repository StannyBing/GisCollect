package com.stanny.sketchpad.bean

import androidx.annotation.DrawableRes

data class SketchPadFuncBean(
    var name: String, @DrawableRes var icon: Int, @DrawableRes var normalIcon: Int,
    var isChecked: Boolean = false
) {
}