package com.gt.giscollect.module.layer.bean

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.esri.arcgisruntime.layers.Layer

data class LayerBean(
    var name: String = "",
    var path: String = "",
    var layer: Layer? = null,
    @DrawableRes
    var res: Int? = null,
    var isChecked: Boolean = false,
    var alpha: Int = 100
) {
}