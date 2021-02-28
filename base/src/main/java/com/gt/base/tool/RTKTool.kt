package com.gt.base.tool

import android.graphics.Point
import android.graphics.PointF
import androidx.annotation.Px
import com.gt.base.bean.RtkInfoBean
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.sin

object RTKTool {
    /**
     * @param rtkBean1 参考点1
     * @param rtkBean2 参考点2
     * @param rtkBean3 参考点3
     */
    fun rtkActualLocation(
        rtkBean1: RtkInfoBean,
        rtkBean2: RtkInfoBean,
        rtkBean3: RtkInfoBean
    ): RtkInfoBean {
        try {
            //分子
            var elementsX =
                (rtkBean2.pointY - rtkBean3.pointY) * (Math.pow(rtkBean1.pointX, 2.0) - Math.pow(
                    rtkBean2.pointX,
                    2.0
                ) + Math.pow(rtkBean1.pointY, 2.0) - Math.pow(rtkBean2.pointY, 2.0) - Math.pow(
                    rtkBean1.distance,
                    2.0
                ) +Math.pow(rtkBean2.distance, 2.0))
            -(rtkBean1.pointY - rtkBean2.pointY) * (Math.pow(rtkBean2.pointX, 2.0) - Math.pow(
                rtkBean3.pointX,
                2.0
            ) + Math.pow(rtkBean2.pointY, 2.0) - Math.pow(
                rtkBean3.pointY,
                2.0
            ) - Math.pow(rtkBean2.distance, 2.0)+ Math.pow(rtkBean3.distance, 2.0))
            //分母
            var denominatorX =
                2 * (rtkBean1.pointX - rtkBean2.pointX) * (rtkBean2.pointY - rtkBean3.pointY) - 2 * (rtkBean2.pointX - rtkBean3.pointX) * (rtkBean1.pointY - rtkBean2.pointY)
            var x = elementsX / denominatorX
            var elementsX1 =
                (rtkBean2.pointX - rtkBean3.pointX) * (Math.pow(rtkBean1.pointX, 2.0) - Math.pow(
                    rtkBean2.pointX,
                    2.0
                ) + Math.pow(rtkBean1.pointY, 2.0) - Math.pow(rtkBean2.pointY, 2.0) - Math.pow(
                    rtkBean1.distance,
                    2.0
                ) + Math.pow(rtkBean2.distance, 2.0))
            var denominatorY1 =
                2 * (rtkBean1.pointY - rtkBean2.pointY) * (rtkBean2.pointX - rtkBean3.pointX) - 2 * (rtkBean2.pointY - rtkBean3.pointY) * (rtkBean1.pointX - rtkBean2.pointX)
            var a = elementsX1 / denominatorY1
            var elementsX2 =
                (rtkBean1.pointX - rtkBean2.pointX) * (Math.pow(rtkBean2.pointX, 2.0) - Math.pow(
                    rtkBean3.pointX,
                    2.0
                ) + Math.pow(rtkBean2.pointY, 2.0) - Math.pow(rtkBean3.pointY, 2.0) - Math.pow(
                    rtkBean2.distance,
                    2.0
                ) +Math.pow(rtkBean3.distance, 2.0))
            var denominatorY2 =
                2 * (rtkBean2.pointX - rtkBean3.pointX) * (rtkBean1.pointY - rtkBean2.pointY) - 2 * (rtkBean2.pointY - rtkBean3.pointY) * (rtkBean1.pointX - rtkBean2.pointX)
            var b = elementsX2 / denominatorY2
            var y = a - b
            return RtkInfoBean(x, y)
        } catch (e: Exception) {
            e.printStackTrace()
            return RtkInfoBean()
        }
    }

    /**
     *
     *@param point0X point0Y 顶点1横纵坐标
     * @param point1X point1Y 顶点2横纵坐标
     * @param vertexPointX vertexPointY 定点横纵坐标
     */
    fun getDegree(vertexPointX:Double,vertexPointY:Double,point0X:Double,point0Y:Double,point1X:Double,point1Y:Double):Int{
        //向量的点乘
        val vector = (point0X - vertexPointX) * (point1X - vertexPointX) + (point0Y - vertexPointY) * (point1Y - vertexPointY)
        //向量的模乘
        var sqrt = Math.sqrt((Math.abs((point0X-vertexPointX)*(point0X-vertexPointX))+Math.abs((point0Y-vertexPointY)*(point0Y-vertexPointY)))*(
                Math.abs((point1X-vertexPointX)*(point1X-vertexPointX))+Math.abs((point1Y-vertexPointY)*(point1Y-vertexPointY))))
        //反余弦计算弧度
        var radian = Math.acos(vector/sqrt)
        //弧度转角度制
        return (180*radian/Math.PI).toInt()
    }

    /**
     * 根据一点经纬度 距离 已经方向夹角 计算另外一点坐标
     * @param angle
     * @param distance 距离 单位m
     * @param pointDistance 两个参考点之间的距离
     */
    fun locationByDistanceAndDirectionAndLocation(angle:Double,startX:Double,startY:Double,endX:Double,endY:Double,distance:Double,pointDistance:Double):Array<Double?>{
      /*  var decimalFormat =DecimalFormat("0.000000")
        val result = arrayOfNulls<Double>(2)
        result[0]=decimalFormat.format(startLat+(distance*Math.cos(angle*Math.PI/180))/111).toDouble()
        result[1]=decimalFormat.format(startLng+(distance*Math.sin(angle*Math.PI/180))/(111*Math.cos(startLat*Math.PI/180))).toDouble()*/
        val result = arrayOfNulls<Double>(2)
        val p1X = startX
        val p1Y = startY
        val p2X =endX
        val p2Y =endY

        val mAngle = Math.toDegrees(Math.atan((p2Y - p1Y) / (p2X - p1X))).let {
            if (p2Y >= p1Y && p2X >= p1X) {
                180 + it
            } else if (p2Y >= p1Y && p2X < p1X) {
                360 + it
            } else if (p2Y < p1Y && p2X >= p1X) {
                180 + it
            } else {
                it
            }
        } - angle

        val mDistance = distance * Math.sqrt(Math.pow(p1X - p2X, 2.0) + Math.pow(p1Y - p2Y, 2.0)) / pointDistance

        val pX = p2X + mDistance * cos(Math.toRadians(mAngle))
        val pY = p2Y + mDistance * sin(Math.toRadians(mAngle))
        result[0]=pX
        result[1]=pY
        return result
    }
}