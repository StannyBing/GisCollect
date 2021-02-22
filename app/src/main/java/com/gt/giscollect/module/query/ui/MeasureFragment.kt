package com.gt.giscollect.module.query.ui

import android.graphics.Color
import android.os.Bundle
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.module_map.tool.GeometrySizeTool
import com.gt.module_map.tool.MapTool
import com.gt.giscollect.module.query.mvp.contract.MeasureContract
import com.gt.giscollect.module.query.mvp.model.MeasureModel
import com.gt.giscollect.module.query.mvp.presenter.MeasurePresenter
import kotlinx.android.synthetic.main.fragment_measure.*

/**
 * Create By XB
 * 功能：
 */
class MeasureFragment : BaseFragment<MeasurePresenter, MeasureModel>(), MeasureContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): MeasureFragment {
            val fragment = MeasureFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    private val sketchEditor = SketchEditor()

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_measure
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {

        sketchEditor.sketchStyle = SketchStyle().apply {
            setVertexSymbol(SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f))
            selectedVertexSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
            fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#6055A4F1"), null)
//            isShowNumbersForVertices = true
        }

        sketchEditor.addGeometryChangedListener {
            val geometry = sketchEditor.geometry
            when (sketchEditor.sketchCreationMode) {
                SketchCreationMode.POLYLINE -> {
                    if (geometry != null) {
                        val size = GeometrySizeTool.getLength(geometry)
                        if (size > 1000.toBigDecimal()) {
                            tv_measure_geometry_size.text = "距离：${String.format("%.5f", size / 1000.toBigDecimal())}公里"
                        } else {
                            tv_measure_geometry_size.text = "距离：${String.format("%.5f", size)}米"
                        }
                    }
                }
                SketchCreationMode.POLYGON -> {
                    if (geometry != null) {
                        val size = GeometrySizeTool.getArea(geometry)
                        if (size > 1000000.toBigDecimal()) {
                            tv_measure_geometry_size.text = "面积：${String.format("%.5f", size / 1000000.toBigDecimal())}平方公里"
                        } else {
                            tv_measure_geometry_size.text = "面积：${String.format("%.5f", size)}平方米"
                        }
                    }
                }
            }
        }

        startMeasure()

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //模式
        tv_measure_feature_type.setOnClickListener {
            tv_measure_feature_type.isSelected = !tv_measure_feature_type.isSelected
            sketchEditor.clearGeometry()
            if (tv_measure_feature_type.isSelected) {
                tv_measure_feature_type.text = "面积"
                tv_measure_geometry_size.text = "面积：0.00平方米"
                sketchEditor.start(SketchCreationMode.POLYGON)
            } else {
                tv_measure_feature_type.text = "距离"
                tv_measure_geometry_size.text = "距离：0.00米"
                sketchEditor.start(SketchCreationMode.POLYLINE)
            }
        }
        //上一步
        tv_measure_feature_undo.setOnClickListener {
            if (sketchEditor.canUndo()) {
                sketchEditor.undo()
            }
        }
        //下一步
        tv_measure_feature_redo.setOnClickListener {
            if (sketchEditor.canRedo()) {
                sketchEditor.redo()
            }
        }
        //清除
        tv_measure_feature_clear.setOnClickListener {
            sketchEditor.clearGeometry()
        }
    }

    fun startMeasure() {
        if (tv_measure_feature_type != null) {
            MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
            sketchEditor.start(SketchCreationMode.POLYLINE)
            tv_measure_feature_type.text = "距离"
        }
    }
}
