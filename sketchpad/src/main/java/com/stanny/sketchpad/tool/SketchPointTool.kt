package com.stanny.sketchpad.tool

import android.graphics.PointF
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt


object SketchPointTool {

    /**
     *
     * 判断点是否在多边形内
     *
     * @param point 检测点
     *
     * @param pts 多边形的顶点
     *
     * @return 点在多边形内返回true,否则返回false
     */
    fun isPtInPoly(
        point: PointF,
        pts: List<PointF>
    ): Boolean {
        val N = pts.size
        val boundOrVertex = true //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        var intersectCount = 0 //cross points count of x
        val precision = 2e-10 //浮点类型计算时候与0比较时候的容差
        var p1: PointF
        var p2: PointF //neighbour bound vertices
        val p: PointF = point //当前点
        p1 = pts[0] //left vertex
        for (i in 1..N) { //check all rays
            if (p.equals(p1)) {
                return boundOrVertex //p is an vertex
            }
            p2 = pts[i % N] //right vertex
            if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(
                    p1.x,
                    p2.x
                )
            ) { //ray is outside of our interests
                p1 = p2
                continue  //next ray left point
            }
            if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(
                    p1.x,
                    p2.x
                )
            ) { //ray is crossing over by the algorithm (common part of)
                if (p.y <= Math.max(p1.y, p2.y)) { //x is before of ray
                    if (p1.x == p2.x && p.y >= Math.min(
                            p1.y,
                            p2.y
                        )
                    ) { //overlies on a horizontal ray
                        return boundOrVertex
                    }
                    if (p1.y == p2.y) { //ray is vertical
                        if (p1.y == p.y) { //overlies on a vertical ray
                            return boundOrVertex
                        } else { //before ray
                            ++intersectCount
                        }
                    } else { //cross point on the left side
                        val xinters: Double =
                            ((p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y).toDouble() //cross point of y
                        if (Math.abs(p.y - xinters) < precision) { //overlies on a ray
                            return boundOrVertex
                        }
                        if (p.y < xinters) { //before ray
                            ++intersectCount
                        }
                    }
                }
            } else { //special case when ray is crossing through the vertex
                if (p.x == p2.x && p.y <= p2.y) { //p crossing over p2
                    val p3: PointF = pts[(i + 1) % N] //next vertex
                    if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(
                            p1.x,
                            p3.x
                        )
                    ) { //p.x lies between p1.x & p3.x
                        ++intersectCount
                    } else {
                        intersectCount += 2
                    }
                }
            }
            p1 = p2 //next ray left point
        }
        return intersectCount % 2 != 0
    }

    /**
     * 判断两条线是否平行
     */
    fun isLineParallel(poiA1: PointF, poiA2: PointF, poiB1: PointF, poiB2: PointF): Boolean {
        //这个判断条件是为了避免有一条直线平行于y轴，因为他们此时斜率无穷大，但是如果他们都平行y轴，说明他们也是平行的
        if (poiA1.x == poiA2.x || poiB1.x == poiB2.x) {
            if (poiA1.x == poiA2.x && poiB1.x == poiB2.x) {
                return true
            }
        } else {
            val p1 = (poiA2.y - poiA1.y) / (poiA2.x - poiA1.x)
            val p2 = (poiB2.y - poiB1.y) / (poiB2.x - poiB1.x)
            if (p1 == p2) {
                return true
            }
        }
        return false
    }

    /**
     * 计算点与直线的距离（只计算水平与竖直两个方向的贴边）
     */
    fun getLinesDistanceSimple(poiA1x: Float, poiA1y: Float, poiA2x: Float, poiA2y: Float, poiB1x: Float, poiB1y: Float, poiB2x: Float, poiB2y: Float, call: (Float?, Float?) -> Unit) {
        if (poiA1x == poiA2x && poiB1x == poiB2x) {//垂直方向平行
            if (abs(poiA1y - poiA2y) + abs(poiB1y - poiB2y) > abs(arrayListOf(poiA1y, poiA2y, poiB1y, poiB2y).max()!! - arrayListOf(poiA1y, poiA2y, poiB1y, poiB2y).min()!!)) {//两条线有空间重叠部分
                call(poiA1x - poiB1x, null)
            }
        } else if (poiA1y == poiA2y && poiB1y == poiB2y) {//水平方向平行
            if (abs(poiA1x - poiA2x) + abs(poiB1x - poiB2x) > abs(arrayListOf(poiA1x, poiA2x, poiB1x, poiB2x).max()!! - arrayListOf(poiA1x, poiA2x, poiB1x, poiB2x).min()!!)) {//两条线有空间重叠部分
                call(null, poiA1y - poiB1y)
            }
        }
    }

    data class GraphicWeltBean(var type: WeltType, var distance: Float) {
        enum class WeltType {
            X, Y
        }
    }

    /**
     * 计算点与直线的距离（计算两条平行线的距离）
     */
    fun getLinesDistance(
        poiA1x: Float,
        poiA1y: Float,
        poiA2x: Float,
        poiA2y: Float,
        poiB1x: Float,
        poiB1y: Float
    ): Float {
        val ABx = poiA2x - poiA1x
        val ABy = poiA2y - poiA1y
        val APx = poiB1x - poiA1x
        val APy = poiB1y - poiA1y

        val AB_AP = ABx * APx + ABy * APy
        val distAB2 = ABx * ABx + ABy * ABy

        var Dx = poiA1x
        var Dy = poiA1y
        if (distAB2 != 0f) {
            val t = AB_AP / distAB2
            if (t >= 1f) {
                Dx = poiA2x
                Dy = poiA2y
            } else if (t > 0f) {
                Dx = poiA1x + ABx * t
                Dy = poiA1y + ABy * t
            } else {
                Dx = poiA1x
                Dy = poiA1y
            }
        }
        val PDx = Dx - poiB1x
        val PDy = Dy - poiB1y
        return sqrt((PDx * PDx + PDy * PDy).toDouble()).toFloat()
    }
}