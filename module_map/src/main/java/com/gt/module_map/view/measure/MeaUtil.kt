package com.gt.module_map.view.measure

import android.content.Context
import android.util.TypedValue
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference

object MeaUtil {
    fun removeLast(pointList: ArrayList<Point>, point: Point) {
        for (i in pointList.size - 1 downTo 0) {
            if (pointList[i] == point) {
                pointList.removeAt(i)
                return
            }
        }
    }

    /**
     * 求pOut在pLine以及pLine2所连直线上的投影点
     *
     * @param pLine
     * @param pLine2
     * @param pOut
     */
    fun getProjectivePoint(
        pLine: Point,
        pLine2: Point,
        pOut: Point
    ): Point? {
        var k = 0.0
        k = try {
            getSlope(pLine.x, pLine.y, pLine2.x, pLine2.y)
        } catch (e: Exception) {
            0.0
        }
        val pProject: Point
        if (k == 0.0) { //垂线斜率不存在情况
            pProject = Point(pOut.x, pLine.y, pOut.spatialReference)
        } else {
            val x = (k * pLine.x + pOut.x / k + pOut.y - pLine.y) / (1 / k + k)
            pProject = Point(
                x,
                -1 / k * (x - pOut.x) + pOut.y,
                pOut.spatialReference
            )
        }
        return pProject
    }

    /**
     * 通过两个点坐标计算斜率
     * 已知A(x1,y1),B(x2,y2)
     * 1、若x1=x2,则斜率不存在；
     * 2、若x1≠x2,则斜率k=[y2－y1]/[x2－x1]
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @throws Exception 如果x1==x2,则抛出该异常
     */
    @Throws(Exception::class)
    private fun getSlope(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        if (x1 == x2) {
            throw Exception("error")
        }
        return (y2 - y1) / (x2 - x1)
    }

    fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, context.resources.displayMetrics
        ).toInt()
    }
}