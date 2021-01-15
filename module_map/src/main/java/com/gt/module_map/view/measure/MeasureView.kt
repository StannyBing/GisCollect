package com.gt.module_map.view.measure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.mapping.view.MapView
import com.gt.module_map.R
import kotlinx.android.synthetic.main.map_measure_layout.view.*

/**
 * 绘制工具界面
 */
class MeasureView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attr, defStyle),
    View.OnClickListener {

    enum class GeoType {
        POLYGON,
        POLYLINE
    }

    private var meaType = GeoType.POLYLINE//绘制类型
    private var defaultListener: MapView.OnTouchListener? = null//默认地图点击监听
    private var mapView: MapView? = null
    private var helper: MeasureHelper? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.map_measure_layout, this, true)
        mea_undo.setOnClickListener(this)
        mea_redo.setOnClickListener(this)
        mea_delete.setOnClickListener(this)
        mea_exit.setOnClickListener(this)
        mea_rg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.mea_line -> {//线
                    mea_line.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    mea_poly.setTextColor(ContextCompat.getColor(context, R.color.white))
                    helper?.setGeoType(GeoType.POLYLINE)
                    delete()
                }
                R.id.mea_poly -> {//面
                    mea_line.setTextColor(ContextCompat.getColor(context, R.color.white))
                    mea_poly.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    helper?.setGeoType(GeoType.POLYGON)
                    delete()
                }
            }
        }
        mea_rg.check(R.id.mea_line)
        mea_delete.imageAlpha = 100
        visibility = View.GONE
//        mea_exit.visibility = View.GONE
        setOnTouchListener(null)
        setOnClickListener(null)
    }

    /**
     * 初始化绘制控件
     */
    fun initMea(defaultListener: MapView.OnTouchListener, mapView: MapView) {
        if (this.mapView == null) {
            this.defaultListener = defaultListener
            this.mapView = mapView
            helper = MeasureHelper(context, mapView)
            helper?.setMapStatusChanged {
                mea_undo.setBackgroundResource(R.drawable.goback_on)
                mea_redo.setBackgroundResource(R.drawable.mea_recover_off)
                mea_delete.imageAlpha = 255
            }
        }
    }

    /**
     *开始绘制
     */
    fun startMeasure(meaType: GeoType? = null, funcs: List<FuncBean> = arrayListOf()) {
        if (visibility == View.GONE) {
            visibility = View.VISIBLE
            startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        }
        //设置默认绘制类型
        if (meaType == null) {
            mea_rg.visibility = View.VISIBLE
        } else {
            mea_rg.visibility = View.GONE
            if (meaType == GeoType.POLYLINE) {
                mea_rg.check(R.id.mea_line)
            } else if (meaType == GeoType.POLYGON) {
                mea_rg.check(R.id.mea_poly)
            }
        }
        //添加功能按钮
        if (funcs.isNotEmpty()) {
            mea_funcs.removeAllViews()
            funcs.forEach { func ->
                val view = TextView(context)
                view.layoutParams = LayoutParams(MeaUtil.dp2px(context, 40f), LayoutParams.MATCH_PARENT)
                view.gravity = Gravity.CENTER
                view.text = func.name
                view.textSize = 12f
                view.setTextColor(Color.WHITE)
                view.setOnClickListener {
                    helper?.let {
                        func.callBack(it.getPointList())
                    }
                }
                mea_funcs.addView(view)
            }
        }
        delete()
        helper?.startMea()
    }

    /**
     * 关闭绘制
     */
    @SuppressLint("ClickableViewAccessibility")
    fun closeMeasure() {
        if (visibility == View.VISIBLE) {
            visibility = View.GONE
            startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
        }
        mapView?.onTouchListener = defaultListener
        helper?.exit()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mea_undo -> {//上一步
                if (helper?.postUndo() == true) {
                    mea_undo.setBackgroundResource(R.drawable.mea_goback_off)
                }
                mea_redo.setBackgroundResource(R.drawable.recover_on)
            }
            R.id.mea_redo -> {//下一步
                if (helper?.postRedo() == true) {
                    mea_redo.setBackgroundResource(R.drawable.mea_recover_off)
                }
                if (helper?.getPointList()?.isNotEmpty() == true) {
                    mea_undo.setBackgroundResource(R.drawable.goback_on)
                }
            }
            R.id.mea_delete -> {//清除
                delete()
            }
            R.id.mea_exit -> {//退出
                delete()
                closeMeasure()
            }
        }
    }

    /**
     * 删除
     */
    fun delete() {
        mea_delete.imageAlpha = 100
        mea_undo.setBackgroundResource(R.drawable.mea_goback_off)
        mea_redo.setBackgroundResource(R.drawable.mea_recover_off)
        helper?.clear()
    }

    /**
     * 设置默认数据
     */
    fun reInitPoints(geometry: Geometry?) {
        if (geometry != null) {
            if (geometry is Polygon) {
                mea_rg.check(R.id.mea_poly)
                helper?.getPointList()?.clear()
                helper?.getPointList()?.addAll(geometry.parts.partsAsPoints)
            } else if (geometry is Polyline) {
                mea_rg.check(R.id.mea_line)
                helper?.getPointList()?.clear()
                helper?.getPointList()?.addAll(geometry.parts.partsAsPoints)
            }
            helper?.updateGraphic()
        }
    }

    data class FuncBean(
        var name: String,
        var callBack: (ArrayList<Point>) -> Unit
    )
}