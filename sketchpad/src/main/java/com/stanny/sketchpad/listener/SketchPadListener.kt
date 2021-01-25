package com.stanny.sketchpad.listener

import android.graphics.PointF
import com.stanny.sketchpad.bean.SketchPadGraphicBean

interface SketchPadListener {

    fun graphicInsert(graphicBean: SketchPadGraphicBean)

    fun graphicEdit(graphicBean: SketchPadGraphicBean)

    fun closeEdit()

    fun refreshGraphic()

    fun resetCenter()

    fun drawLabel()

    fun saveGraphicInfo()

    fun showSite()

    fun floorSetting()

    fun finish()
}