package com.stanny.sketchpad.bean

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import androidx.annotation.ColorInt
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.SketchPointTool
import java.util.*
import kotlin.collections.ArrayList

data class SketchPadFloorBean(var id: UUID = UUID.randomUUID(), var name:String="", var area:String="", var sketchList: ArrayList<SketchPadGraphicBean>, var isChecked:Boolean=false){


}