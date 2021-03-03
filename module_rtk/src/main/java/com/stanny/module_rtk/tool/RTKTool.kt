package com.stanny.module_rtk.tool

import com.gt.base.bean.RtkInfoBean
import java.lang.Exception
import kotlin.math.cos
import kotlin.math.sin
import com.google.android.material.math.MathUtils.dist



object RTKTool {
    /*
	 * 大地坐标系资料WGS-84 长半径a=6378137 短半径b=6356752.3142 扁率f=1/298.2572236
	 */
    /** 长半径a=6378137 */
    private var a = 6378137
    /** 短半径b=6356752.3142 */
    private var  b = 6356752.3142
    /** 扁率f=1/298.2572236 */
    private var  f = 1 / 298.2572236

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
    fun getDegree(vertexPointX:Double,vertexPointY:Double,point0X:Double,point0Y:Double,point1X:Double,point1Y:Double):Double{
        //向量的点乘
        val vector = (point0X - vertexPointX) * (point1X - vertexPointX) + (point0Y - vertexPointY) * (point1Y - vertexPointY)
        //向量的模乘
        var sqrt = Math.sqrt((Math.abs((point0X-vertexPointX)*(point0X-vertexPointX))+Math.abs((point0Y-vertexPointY)*(point0Y-vertexPointY)))*(
                Math.abs((point1X-vertexPointX)*(point1X-vertexPointX))+Math.abs((point1Y-vertexPointY)*(point1Y-vertexPointY))))
        //反余弦计算弧度
        var radian = Math.acos(vector/sqrt)
        //弧度转角度制
        return (180*radian/Math.PI)
    }

    /**
     * 根据一点经纬度 距离 已经方向夹角 计算另外一点坐标
     * @param angle
     * @param distance 距离 单位m
     * @param pointDistance 两个参考点之间的距离
     */
    fun locationByDistanceAndDirectionAndLocation(lon:Double,lat:Double,angle:Double,distance:Double):Array<Double?>{
        var result = arrayOfNulls<Double>(2)
        val alpha1 = angle*Math.PI / 180.0
        var sinAlpha1 = Math.sin(alpha1)
        var cosAlpha1 = Math.cos(alpha1)
        var tanU1 =(1 - f) * Math.tan(lat*Math.PI / 180.0)
        var cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1))
        var sinU1 = tanU1 * cosU1
        var sigma1 = Math.atan2(tanU1, cosAlpha1)
        var sinAlpha = cosU1 * sinAlpha1
        var cosSqAlpha = 1 - sinAlpha * sinAlpha
        var uSq = cosSqAlpha * (a * a - b * b) / (b * b)
        var A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
        var B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))
        var cos2SigmaM=0.0
        var  sinSigma = 0.0
        var  cosSigma = 0.0
        var sigma = distance / (b * A)
        var  sigmaP = 2 * Math.PI
        while (Math.abs(sigma - sigmaP) > 1e-12){
            cos2SigmaM = Math.cos(2 * sigma1 + sigma)
            sinSigma = Math.sin(sigma)
            cosSigma = Math.cos(sigma)
            var  deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
                    - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)))
            sigmaP = sigma
            sigma=distance / (b * A) + deltaSigma
        }
        var tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1
        var lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
            (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp))
        var lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1)
        var C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha))
        var L = lambda - (1 - C) * f * sinAlpha*(sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)))
        var revAz = Math.atan2(sinAlpha, -tmp)
        result[0]=lon+L*180 / Math.PI
            result[1]=lat2*180 / Math.PI
        return result
    }
}