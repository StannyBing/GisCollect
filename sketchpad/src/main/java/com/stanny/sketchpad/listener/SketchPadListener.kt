package com.stanny.sketchpad.listener

import android.graphics.PointF
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import java.util.*
import kotlin.collections.ArrayList

interface SketchPadListener {

    fun graphicInsert(graphicBean: SketchPadGraphicBean)

    fun graphicEdit(graphicBean: SketchPadGraphicBean)

    fun closeEdit()

    fun refreshGraphic()

    fun resetCenter()

    fun drawLabel()

    fun saveGraphicInfo()

    fun showSite(isCheck: Boolean)

    fun floorSetting(isCheck: Boolean)

    fun floorEdit(sketchPadFloorBean: SketchPadFloorBean)

    fun saveFloor(sketchPadFloorBean: SketchPadFloorBean)

    fun showSizeInfo(checked: Boolean)

    fun deleteGraphic(id: UUID)
}