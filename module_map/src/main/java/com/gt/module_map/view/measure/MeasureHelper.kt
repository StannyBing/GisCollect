package com.gt.module_map.view.measure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.MapView
import com.zx.zxutils.util.ZXLogUtil
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 图层绘制处理器
 */
class MeasureHelper(var context: Context, private var mapView: MapView) : MeaListener {

    private var change: () -> Unit = {}

    private var layerManager: MeaLayerManager =
        MeaLayerManager(context, this)
    private var optManager: MeaOptManager =
        MeaOptManager(context, this)

    private var geoType: MeasureView.GeoType = MeasureView.GeoType.POLYLINE
    private var pointList = arrayListOf<Point>()
    private val tempPointList = arrayListOf<Point>()
    private var editPoint = -1 //现在正编辑的点的index
    private var movePoint: Point? = null//移动的初始点坐标
    private var transStartPoint: Point? = null//平移的初始点坐标
    private var isMeaTranslate = false //当前是否为平移
    private var isLongClick = false //是否为在长按
    private var isMapLongClick = false //是否当前为map的长按事件（非touch）
    private var longClickPoint: PointF? = null//长按的起始点屏幕坐标
    private var clickGraphic: Graphic? = null//长按时的feature
    private var timer: Timer? = null


    /**
     * 开始
     */
    @SuppressLint("ClickableViewAccessibility")
    fun startMea() {
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(context, mapView) {
            //地图点击事件
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                //如果开启弹窗，只关闭弹窗，不添加点
                if (optManager.closeMarker()) {
                    return super.onSingleTapConfirmed(e)
                }
                //平移不允许添加点
                if (isMeaTranslate) {
                    return super.onSingleTapConfirmed(e)
                }
                //添加上一步操作序列
                val point = mapView.screenToLocation(android.graphics.Point(e!!.x.roundToInt(), e.y.roundToInt()))
                optManager.optAdd(point)
                onMapReAdd()
                return true
            }

            //地图长按事件
            override fun onLongPress(e: MotionEvent?) {
                val type = if (isMeaTranslate) {
                    "polygon"
                } else {
                    "polyline,polygon"
                }
                getTopGraphic(e, type)?.let {
                    clickGraphic = it
                    isMapLongClick = true
                    var point = mapView.screenToLocation(android.graphics.Point(e!!.x.toInt(), e.y.toInt()))
                    if (it.geometry is Polyline) {
                        val points = (it.geometry as Polyline).parts.partsAsPoints
                        point = MeaUtil.getProjectivePoint(points.first(), points.last(), point)
                        editPoint = it.attributes["lineIndex"] as Int + 1
                        optManager.funcIndex = editPoint
                    }
                    optManager.popMenuPoint = point
                    handler.sendEmptyMessage(0)
//                    optManager.showMeaMenu(it)
//                    (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(200)
//                    timer?.cancel()
                }
            }

            //地图按下事件
            override fun onDown(e: MotionEvent?): Boolean {
                return super.onDown(e)
            }

            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isLongClick = false
                        //平移时不允许对点进行操作,只允许操作面图层
                        val type = if (isMeaTranslate) "polygon" else "point"
                        getTopGraphic(event, type).let {
                            if (it != null) {
                                longClickPoint = PointF(event.x, event.y)
                                timer = Timer()
                                timer?.schedule(object : TimerTask() {
                                    override fun run() {
                                        handler.sendEmptyMessage(0)
                                        isLongClick = true
                                    }
                                }, 700)
                                clickGraphic = it
                                if (it.geometry is Point) {
                                    editPoint = it.attributes["pointIndex"] as Int
                                    optManager.funcIndex = editPoint
                                    movePoint = pointList.get(editPoint)
                                    optManager.popMenuPoint = movePoint
                                } else if (it.geometry is Polygon) {
                                    transStartPoint = mapView.screenToLocation(android.graphics.Point(event.x.roundToInt(), event.y.roundToInt()))
                                    optManager.popMenuPoint =
                                        mapView.screenToLocation(android.graphics.Point(event.x.roundToInt(), event.y.roundToInt()))
                                    pointList.forEach {
                                        tempPointList.add(Point(it.x, it.y, it.spatialReference))
                                    }
                                }
                                return true
                            } else {
                                editPoint = -1
                                optManager.popMenuPoint = null
                            }
                        }
                        return super.onTouch(view, event)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //计算移动距离
                        if (timer != null && longClickPoint != null) {
                            val movex = sqrt(
                                abs(event.x - longClickPoint!!.x).pow(2)
                                        + abs(event.y - longClickPoint!!.y).toDouble().pow(2)
                            )
                            if (movex > 20) {
                                isLongClick = false
                                timer?.cancel()
                                timer = null
                            }
                        }
                        //地图平移事件
                        if (isMeaTranslate) {
                            if (isLongClick) {
                                return true
                            }
                            if (transStartPoint != null) {
                                if (tempPointList.isEmpty()) {
                                    return true
                                }
                                val resultPoint = mapView.screenToLocation(android.graphics.Point(event.x.roundToInt(), event.y.roundToInt()))
                                val transLon = resultPoint.x - transStartPoint!!.x
                                val transLat = resultPoint.y - transStartPoint!!.y
                                val list = arrayListOf<Point>()
                                tempPointList.forEachIndexed { index, it ->
                                    val point =
                                        Point(tempPointList[index].x + transLon, tempPointList[index].y + transLat, resultPoint.spatialReference)
                                    list.add(point)
                                }
                                pointList.clear()
                                pointList.addAll(list)
                                updateGraphic()
                                return true
                            }
                            return super.onTouch(view, event)
                        }
                        //地图长按事件
                        if (isMapLongClick) {
                            return super.onTouch(view, event)
                        }
                        //如果为普通长按，将不再执行移动的操作
                        if (isLongClick) {
                            return true
                        }
                        if (editPoint != -1 && movePoint != null) {
                            val touchPoint = mapView.screenToLocation(android.graphics.Point(event.x.roundToInt(), event.y.roundToInt()))
                            pointList.set(editPoint, touchPoint)
                            updateGraphic()
                            optManager.closeMarker()
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (timer != null) {
                            timer!!.cancel()
                            timer = null
                        }
                        //平移事件
                        if (isMeaTranslate) {
                            return if (transStartPoint != null && !tempPointList.isEmpty()) {
                                optManager.optTrans(
                                    transStartPoint!!,
                                    mapView.screenToLocation(android.graphics.Point(event.x.roundToInt(), event.y.roundToInt()))
                                )
                                tempPointList.clear()
                                transStartPoint = null
                                true
                            } else {
                                transStartPoint = null
                                false
                            }
                        }
                        //长按事件
                        isLongClick = false
                        if (isMapLongClick) {
                            isMapLongClick = false
                            //非touch移动
                            return super.onTouch(view, event)
                        }
                        if (editPoint != -1) {
                            optManager.optMove(editPoint, movePoint!!, pointList.get(editPoint))
                            editPoint = -1
                            onMapReAdd()
                        }
                        movePoint = null
                    }
                }
                return super.onTouch(view, event)
            }

            //地图松手事件
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return super.onSingleTapUp(e)
            }
        }
    }

    /**
     * 获取最上层的graphic
     */
    private fun getTopGraphic(e: MotionEvent?, type: String = ""): Graphic? {
        val graphicListenable = mapView.identifyGraphicsOverlayAsync(
            layerManager.meaLayer,
            android.graphics.Point(e!!.x.roundToInt(), e.y.roundToInt()),
            20.0,
            false,
            100
        )
        var graphic: Graphic? = null
        val resultGraphic = graphicListenable.get().graphics
        if (resultGraphic.isNotEmpty()) {
            when (type) {
                "point" -> {
                    resultGraphic.forEach {
                        if (it.geometry is Point) {
                            graphic = it
                            return@forEach
                        }
                    }
                }
                "polyline" -> {
                    resultGraphic.forEach {
                        if (it.geometry is Polyline) {
                            graphic = it
                            return@forEach
                        }
                    }
                }
                "polygon" -> {
                    resultGraphic.forEach {
                        if (it.geometry is Polygon) {
                            graphic = it
                            return@forEach
                        }
                    }
                }
                "polyline,polygon" -> {
                    resultGraphic.forEach {
                        if (it.geometry is Polygon) {
                            graphic = it
                            return@forEach
                        }
                    }
                    resultGraphic.forEach {
                        if (it.geometry is Polyline) {
                            graphic = it
                            return@forEach
                        }
                    }
                }
                else -> {
                    resultGraphic.forEach {
                        if (it.geometry is Point) {
                            graphic = it
                            return@forEach
                        }
                    }
                    resultGraphic.forEach {
                        if (it.geometry is Polyline) {
                            graphic = it
                            return@forEach
                        }
                    }
                    resultGraphic.forEach {
                        if (it.geometry is Polygon) {
                            graphic = it
                            return@forEach
                        }
                    }
//                    graphic = resultGraphic[0]
                }
            }
        }
        return graphic
    }

    /**
     * 上一步
     */
    fun postUndo(): Boolean {
        optManager.closeMarker()
        return optManager.postUndo()
    }

    /**
     * 下一步
     */
    fun postRedo(): Boolean {
        optManager.closeMarker()
        return optManager.postRedo()
    }

    /**
     * 设置绘制类型
     */
    fun setGeoType(type: MeasureView.GeoType) {
        stopTranslate()
        this.geoType = type
    }

    /**
     * 清理
     */
    fun clear() {
        optManager.clear()
        layerManager.clear()
        pointList.clear()
        tempPointList.clear()
        stopTranslate()
        editPoint = -1
        movePoint = null
        longClickPoint = null
        optManager.popMenuPoint = null
        clickGraphic = null
        optManager.closeMarker()
    }

    /**
     * 退出
     */
    fun exit() {
        clear()
        layerManager.clear()
        optManager.closeMarker()
    }

    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) { //showMenu
                tempPointList.clear()
                (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(200)
                //执行长按事件-弹出操作菜单
                optManager.showMeaMenu(clickGraphic!!)
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
            }
        }
    }

    /**
     * 监听绘制状态改变
     */
    fun setMapStatusChanged(change: () -> Unit) {
        this.change = change
    }

    override fun getMapView(): MapView {
        return mapView
    }

    override fun updateGraphic() {
        layerManager.updateGraphic()
    }

    override fun onPointChange() {

    }

    /**
     * 重新执行了点击事件，更新undo与redo的变化
     */
    override fun onMapReAdd() {
        change()
    }

    override fun getGeoType(): MeasureView.GeoType {
        return geoType
    }

    override fun getPointList(): ArrayList<Point> {
        return pointList
    }

    override fun startTranslate() {
        isMeaTranslate = true
        layerManager.startTranslate()
    }

    override fun stopTranslate() {
        if (isMeaTranslate) {
            isMeaTranslate = false
            layerManager.stopTranslate()
        }
    }

    override fun isTranslate(): Boolean {
        return isMeaTranslate
    }

}