package com.gt.base.tool

import com.gt.base.bean.RtkInfoBean
import java.lang.Exception

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
                ) - Math.pow(rtkBean2.distance, 2.0))
            -(rtkBean1.pointY - rtkBean2.pointY) * (Math.pow(rtkBean2.pointX, 2.0) - Math.pow(
                rtkBean3.pointX,
                2.0
            ) + Math.pow(rtkBean2.pointY, 2.0) - Math.pow(
                rtkBean3.pointY,
                2.0
            ) - Math.pow(rtkBean2.distance, 2.0) - Math.pow(rtkBean3.distance, 2.0))
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
                ) - Math.pow(rtkBean2.distance, 2.0))
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
                ) - Math.pow(rtkBean3.distance, 2.0))
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
     * @param referencePoint 基准点1
     * @param referencePoint2 以基准点1的所在一条边为基准边，然后再另外一个方向打基准点2
     *
     */
    fun angle(referencePoint:RtkInfoBean,referencePoint2:RtkInfoBean){

    }
}