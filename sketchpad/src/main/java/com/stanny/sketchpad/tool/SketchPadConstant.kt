package com.stanny.sketchpad.tool

import android.graphics.Color
import androidx.annotation.ColorInt
import com.zx.zxutils.util.ZXSystemUtil

object SketchPadConstant {

    //背景虚线颜色
    @ColorInt
    var backgroundGridColor = Color.parseColor("#EEEEEE")

    //图形默认边框颜色
    @ColorInt
    var graphicLineColor = Color.parseColor("#90b1c2")

    //图形高亮边框颜色
    @ColorInt
    var graphicHightLightColor = Color.parseColor("#e16531")

    //图形高亮边框标记颜色
    @ColorInt
    var graphicMarkNumColor = Color.parseColor("#0e932e")

    //图形边框宽度
    var graphicLineWidth = 2f

    //图形缩放比率（每***米占一格）
    var graphicRatio = 1.6f

    //背景虚线网格间隔
    var backgroundGridSpace = 50f

    //自动贴边阈值
    var autoWeltLimit = 10f

    //工具-图形栏高度
    var toolbarGraphicHeight = ZXSystemUtil.dp2px(60f)

    //工具-功能栏高度
    var toolbarFuncHeight = ZXSystemUtil.dp2px(60f)
}