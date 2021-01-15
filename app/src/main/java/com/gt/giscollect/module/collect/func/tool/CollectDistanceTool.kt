package com.gt.giscollect.module.collect.func.tool

import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXToastUtil
import kotlin.math.*

object CollectDistanceTool {

    fun excutePoint(sketchEditor: SketchEditor, angle: String, distance: String): Point? {
        if (sketchEditor.geometry == null) {
            return null
        }
        var endPoint2: Point? = null
        var endPoint: Point? = null
        var endGeometry: Geometry? = null
        when (sketchEditor.geometry.geometryType) {
            GeometryType.POINT -> {
                ZXToastUtil.showToast("点图层无法进行计算打点")
            }
            GeometryType.POLYLINE -> {
                if ((sketchEditor.geometry as Polyline).parts.isEmpty() || (sketchEditor.geometry as Polyline).parts.first().size < 2) {
                    ZXToastUtil.showToast("请先绘制初始边")
                } else {
                    endPoint2 =
                        (sketchEditor.geometry as Polyline).parts.first().getPoint((sketchEditor.geometry as Polyline).parts.first().lastIndex - 1)
                    endPoint = (sketchEditor.geometry as Polyline).parts.first().endPoint
                    endGeometry = PolylineBuilder(PointCollection(arrayListOf<Point>(endPoint, endPoint2))).toGeometry()
                }
            }
            GeometryType.POLYGON -> {
                if ((sketchEditor.geometry as Polygon).parts.isEmpty() || (sketchEditor.geometry as Polygon).parts.first().size < 2) {
                    ZXToastUtil.showToast("请先绘制初始边")
                } else {
                    endPoint2 =
                        (sketchEditor.geometry as Polygon).parts.first().getPoint((sketchEditor.geometry as Polygon).parts.first().lastIndex - 1)
                    endPoint = (sketchEditor.geometry as Polygon).parts.first().endPoint
                    endGeometry = PolylineBuilder(PointCollection(arrayListOf<Point>(endPoint, endPoint2))).toGeometry()
                }
            }
        }
        if (angle.isEmpty()) {
            ZXToastUtil.showToast("请输入角度")
        } else if (distance.isEmpty()) {
            ZXToastUtil.showToast("请输入距离")
        } else if (endPoint != null && endPoint2 != null) {
            var mAngle = 0.0
            var mDistance = 0.0
            try {
                mAngle = angle.toDouble()
            } catch (e: Exception) {
                ZXToastUtil.showToast("角度输入错误")
            }
            if (mAngle < -180 || mAngle > 180) {
                ZXToastUtil.showToast("角度输入错误")
            }
            try {
                mDistance = distance.toDouble()
            } catch (e: Exception) {
                ZXToastUtil.showToast("距离输入错误")
            }
            if (mDistance < 0) {
                ZXToastUtil.showToast("距离输入错误")
            }

            ZXLogUtil.loge("point1:${endPoint2.toJson()}. point2:${endPoint.toJson()}")

            if (mDistance > 0) {
                val p1X = endPoint2.x
                val p1Y = endPoint2.y
                val p2X = endPoint.x
                val p2Y = endPoint.y

                mAngle = Math.toDegrees(Math.atan((p2Y - p1Y) / (p2X - p1X))).let {
                    if (p2Y >= p1Y && p2X >= p1X) {
                        180 + it
                    } else if (p2Y >= p1Y && p2X < p1X) {
                        360 + it
                    } else if (p2Y < p1Y && p2X >= p1X) {
                        180 + it
                    } else {
                        it
                    }
                } - mAngle

//                val mDistance = mDistance * GeometrySizeTool.getLength(endGeometry!!).toDouble() / Math.sqrt(Math.pow(p1X - p2X, 2.0) + Math.pow(p1Y - p2Y, 2.0))
                val mDistance = mDistance * Math.sqrt(Math.pow(p1X - p2X, 2.0) + Math.pow(p1Y - p2Y, 2.0)) / GeometrySizeTool.getLength(endGeometry!!).toDouble()

                val pX = p2X + mDistance * cos(Math.toRadians(mAngle))
                val pY = p2Y + mDistance * sin(Math.toRadians(mAngle))

                return Point(pX, pY)
            }
        }

        return null
    }

    private fun getChangeAngle(cen: Point, first: Point, second: Point): Double {
        val angle: Double
        val dx1: Double = first.x - cen.x
        val dy1: Double = first.y - cen.y
        val dx2: Double = second.x - cen.x
        val dy2: Double = second.y - cen.y
        val c = sqrt(dx1 * dx1 + dy1 * dy1) * sqrt(dx2 * dx2 + dy2 * dy2)
        if (c == 0.0) return -1.0
        angle = acos((dx1 * dx2 + dy1 * dy2) / c)
        return Math.toDegrees(angle);
    }

}