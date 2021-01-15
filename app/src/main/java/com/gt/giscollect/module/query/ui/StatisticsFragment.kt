package com.gt.giscollect.module.query.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.gt.giscollect.R
import com.gt.giscollect.base.BaseFragment
import com.gt.giscollect.module.collect.func.tool.GeometrySizeTool
import com.gt.giscollect.module.main.func.tool.GeoPackageTool
import com.gt.giscollect.module.main.func.tool.MapTool
import com.gt.giscollect.module.query.bean.StatisticResultBean
import com.gt.giscollect.module.query.func.adapter.StatisticsResponseAdapter
import com.gt.giscollect.module.query.func.mchart.ChartView
import com.gt.giscollect.module.query.func.mchart.MChartBean
import com.gt.giscollect.module.query.mvp.contract.StatisticsContract
import com.gt.giscollect.module.query.mvp.model.StatisticsModel
import com.gt.giscollect.module.query.mvp.presenter.StatisticsPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.ZXInScrollRecylerManager
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.collections.ArrayList

/**
 * Create By XB
 * 功能：统计
 */
class StatisticsFragment : BaseFragment<StatisticsPresenter, StatisticsModel>(), StatisticsContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): StatisticsFragment {
            val fragment = StatisticsFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "statistics"
    }

    private val sketchEditor = SketchEditor()

    private val tempChartList = arrayListOf<MChartBean>()

    private val mResponseList = ArrayList<StatisticResultBean>()
    private var mResponseAdapter = StatisticsResponseAdapter(mResponseList)

    private val cutSortList = hashMapOf<String, Double>()

    private var chartType = ChartView.ChartType.PieChart

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_statistics
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_gis_statistics_response.apply {
            layoutManager = ZXInScrollRecylerManager(requireActivity())
            adapter = mResponseAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }

        sp_statitics_layer
            .setDefaultItem("请选择统计图层")
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        sp_statistics_sortfield
            .setDefaultItem("请选择分类字段")
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        sp_statistics_resultfield
            .setDefaultItem("请选择统计字段")
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        sp_statistics_resulttype
            .setData(
                arrayListOf(
                    KeyValueEntity("最大值", StatisticType.MAXIMUM),
                    KeyValueEntity("最小值", StatisticType.MINIMUM),
                    KeyValueEntity("平均值", StatisticType.AVERAGE),
                    KeyValueEntity("总和", StatisticType.SUM),
                    KeyValueEntity("计数", StatisticType.COUNT)
                )
            )
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        setLayerData()

        sketchEditor.sketchStyle = SketchStyle().apply {
            setVertexSymbol(SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f))
            selectedVertexSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
            fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#6055A4F1"), null)
//            isShowNumbersForVertices = true
        }
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
        super.initView(savedInstanceState)
    }

    private fun setLayerData() {
        sp_statitics_layer?.let {
            val layers = arrayListOf<Layer>()
            layers.addAll(MapTool.mapListener?.getMap()?.operationalLayers ?: arrayListOf())
            layers.addAll(MapTool.mapListener?.getMap()?.basemap?.baseLayers ?: arrayListOf())
            val layerSpinner = arrayListOf<KeyValueEntity>()
            if (!layers.isNullOrEmpty()) {
                layers.forEach {
                    //                    if (it is FeatureLayer) {
                    layerSpinner.add(KeyValueEntity(it.name, it))
//                    }
                }
            }
            sp_statitics_layer.dataList.clear()
            sp_statitics_layer.dataList.addAll(layerSpinner)
            sp_statitics_layer.setDefaultItem("请选择统计图层")
            sp_statitics_layer.notifyDataSetChanged()
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                handler.postDelayed({
                    setLayerData()
                }, 500)
            }
        })
        //图层切换事件
        sp_statitics_layer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (sp_statitics_layer.selectedItemPosition == 0) {
                    return
                }

                val sortFields = arrayListOf<KeyValueEntity>()
                val resultFields = arrayListOf<KeyValueEntity>()
                val layer = sp_statitics_layer.selectedValue
                if (layer is FeatureLayer && layer.featureTable.fields.isNotEmpty()) {
                    layer.featureTable.fields.forEach {
                        if (it.fieldType == Field.Type.DOUBLE || it.fieldType == Field.Type.INTEGER || it.fieldType == Field.Type.FLOAT) {
                            resultFields.add(KeyValueEntity(it.name, it))
                        } else {
                            sortFields.add(KeyValueEntity(it.name, it))
                        }
                    }
                    reloadFieldSpinner(sortFields, resultFields)
                } else if (layer is ArcGISVectorTiledLayer) {
                    GeoPackageTool.getFeatureFromGpkg(layer.name) {
                        sp_statitics_layer.selectedEntity.setValue(it)
                        it.featureTable.fields.forEach {
                            if (it.fieldType == Field.Type.DOUBLE || it.fieldType == Field.Type.INTEGER || it.fieldType == Field.Type.FLOAT) {
                                resultFields.add(KeyValueEntity(it.name, it))
                            } else {
                                sortFields.add(KeyValueEntity(it.name, it))
                            }
                        }
                        reloadFieldSpinner(sortFields, resultFields)
                    }
                }
            }
        }
        //开始统计
        btn_statistic_start.setOnClickListener {
            if (sp_statitics_layer.selectedItemPosition == 0) {
                showToast("请选择统计图层")
                return@setOnClickListener
            }
            if (sp_statistics_sortfield.selectedItemPosition == 0) {
                showToast("请选择分类字段")
                return@setOnClickListener
            }
            if (sp_statistics_resultfield.selectedItemPosition == 0) {
                showToast("请选择统计字段")
                return@setOnClickListener
            }
            doStatistcs()
        }
        //图表切换
        tv_statistics_change.setOnClickListener {
            if (tv_statistics_change.text == "柱状图") {
                tv_statistics_change.text = "饼状图"
                chartType = ChartView.ChartType.BarChart
            } else if (tv_statistics_change.text == "饼状图") {
                tv_statistics_change.text = "柱状图"
                chartType = ChartView.ChartType.PieChart
            }
            chart_view.setData(tempChartList, chartType)
        }
        //开始绘制
        tv_statistics_start.setOnClickListener {
            MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
            if (tv_statistics_start.tag == "close") {
                sketchEditor.start(SketchCreationMode.POLYGON)
                tv_statistics_start.text = "停止绘制"
                tv_statistics_start.tag = "open"
                showToast("请开始绘制统计范围")
            } else {
                sketchEditor.stop()
                tv_statistics_start.text = "开始绘制"
                tv_statistics_start.tag = "close"
            }
        }
        //上一步
        tv_statistics_undo.setOnClickListener {
            if (sketchEditor.canUndo()) {
                sketchEditor.undo()
            }
        }
        //下一步
        tv_statistics_redo.setOnClickListener {
            if (sketchEditor.canRedo()) {
                sketchEditor.redo()
            }
        }
        //清除
        tv_statistics_clear.setOnClickListener {
            sketchEditor.clearGeometry()
        }
    }

    private fun reloadFieldSpinner(
        sortFields: ArrayList<KeyValueEntity>,
        resultFields: ArrayList<KeyValueEntity>
    ) {
        sp_statistics_sortfield.dataList.clear()
        sp_statistics_sortfield.dataList.addAll(sortFields)
        sp_statistics_sortfield.setDefaultItem("请选择分类字段")
        sp_statistics_sortfield.notifyDataSetChanged()

        sp_statistics_resultfield.dataList.clear()
        sp_statistics_resultfield.dataList.addAll(resultFields)
        sp_statistics_resultfield.setDefaultItem("请选择统计字段")
        sp_statistics_resultfield.notifyDataSetChanged()
    }

    private fun doStatistcs() {
        showLoading("统计中...")
        mResponseList.clear()
        mResponseList.add(StatisticResultBean("内容", "统计方式", "结果", isTitle = true))

        val statDefinitions: MutableList<StatisticDefinition> = ArrayList()
        statDefinitions.add(
            StatisticDefinition(
                sp_statistics_resultfield.selectedKey,
                sp_statistics_resulttype.selectedValue as StatisticType,
                sp_statistics_resulttype.selectedKey
            )
        )

        val parameters = StatisticsQueryParameters(statDefinitions)
        parameters.groupByFieldNames.add(sp_statistics_sortfield.selectedKey)

        cutSortList.clear()
        if (sketchEditor.geometry?.isEmpty == false) {//如果进行了裁剪
            parameters.spatialRelationship = QueryParameters.SpatialRelationship.INTERSECTS
            parameters.geometry = sketchEditor.geometry
            //获取所有相关的feature
            val queryParameters = QueryParameters()
            queryParameters.spatialRelationship = QueryParameters.SpatialRelationship.INTERSECTS
            queryParameters.geometry = sketchEditor.geometry
            (sp_statitics_layer.selectedValue as FeatureLayer).featureTable.loadAsync()
            (sp_statitics_layer.selectedValue as FeatureLayer).featureTable.addDoneLoadingListener {
                val listenable = (sp_statitics_layer.selectedValue as FeatureLayer).featureTable.queryFeaturesAsync(queryParameters)
                listenable.addDoneListener {
                    val features = arrayListOf<Feature>()
                    features.addAll(listenable.get())
//                if (features.size > 500) {
//                    showToast("分类字段“${sp_statistics_sortfield.selectedKey}”结果过多，请更换")
//                    return@addDoneListener
//                }
                    features.forEach {
                        it.attributes.keys
                        //获取裁剪的 feature
                        val cutGeometry = GeometryEngine.intersection(it.geometry, sketchEditor.geometry)
                        var percent = 1.0
                        if (cutGeometry != null) {
                            //被裁减部分
                            when (cutGeometry.geometryType) {
                                GeometryType.POLYLINE -> {
                                    percent = GeometrySizeTool.getLength(cutGeometry).toDouble() / GeometrySizeTool.getLength(it.geometry).toDouble()
                                }
                                GeometryType.POLYGON -> {
                                    percent = GeometrySizeTool.getArea(cutGeometry).toDouble() / GeometrySizeTool.getArea(it.geometry).toDouble()
                                }
                            }
                        }
                        //找到当前分类字段并对所选统计字段进行百分比计算
                        if (percent < 1.0 && it.attributes.containsKey(sp_statistics_sortfield.selectedKey) && it.attributes.containsKey(
                                sp_statistics_resultfield.selectedKey
                            )
                        ) {
                            try {
                                it.attributes[sp_statistics_sortfield.selectedKey]?.toString().let { name ->
                                    if (cutSortList.containsKey(name)) {
                                        cutSortList[name!!] =
                                            cutSortList[name!!]!! + it.attributes[sp_statistics_resultfield.selectedKey].toString()
                                                .toDouble() * percent
                                    } else {
                                        cutSortList.put(
                                            name!!,
                                            it.attributes[sp_statistics_resultfield.selectedKey].toString().toDouble() * percent
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                    loadStatisticsResult(parameters)
                }
            }
        } else {
            loadStatisticsResult(parameters)
        }
    }

    private fun loadStatisticsResult(parameters: StatisticsQueryParameters) {
        val result = (sp_statitics_layer.selectedValue as FeatureLayer).featureTable.queryStatisticsAsync(parameters)
        result.addDoneListener {
            try {
                val statQueryResult: StatisticsQueryResult = result.get()
                val statisticRecordIterator = statQueryResult.iterator()

                val list = arrayListOf<StatisticRecord>()
                list.addAll(statisticRecordIterator.asSequence())
                if (list.size > 500) {
                    showToast("分类字段“${sp_statistics_sortfield.selectedKey}”结果过多，请更换")
                    return@addDoneListener
                }
                list.forEach { statisticsReccord ->
                    if (statisticsReccord.group.isEmpty()) {
//                        for ((key, value) in statisticsReccord.statistics) {
//                            tempChartList.add(MChartBean(key, num = value as Double))
//                        }
                    } else {
                        for ((groupKey, groupValue) in statisticsReccord.group) {
                            for ((statisticsKey, statisticsValue) in statisticsReccord.statistics) {
                                try {
                                    if (cutSortList[groupValue.toString()] ?: 0.0 > 0.0) {
                                        mResponseList.add(
                                            StatisticResultBean(
                                                groupValue.toString(),
                                                sp_statistics_resultfield.selectedKey,
                                                (cutSortList[groupValue.toString()] ?: 0.0).toString()
                                            )
                                        )
                                    } else {
                                        mResponseList.add(
                                            StatisticResultBean(
                                                groupValue.toString(),
                                                sp_statistics_resultfield.selectedKey,
                                                statisticsValue.toString()
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    mResponseList.add(
                                        StatisticResultBean(
                                            groupValue.toString(),
                                            sp_statistics_resultfield.selectedKey,
                                            statisticsValue.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dismissLoading()
    }

    private fun refreshData() {
        tv_statistics_change.visibility = View.VISIBLE
        mResponseAdapter.notifyDataSetChanged()

        val chartList = arrayListOf<MChartBean>()
        mResponseList.forEach {
            if (!it.isTitle) {
                try {
//                    val num = if (1 == 0) {
//                        it.value1?.toDouble() ?: 0.0
//                    } else {
                    val num = it.value2?.toDouble() ?: 0.0
//                    }
                    chartList.add(MChartBean(it.name, num, showCenter = false))
                } catch (e: Exception) {
                }
            }
        }
        tempChartList.clear()
        tempChartList.addAll(chartList)

        chart_view.setData(tempChartList, chartType)
    }

    fun reInit() {
        tv_statistics_start?.text = "开始绘制"
        tv_statistics_start?.tag = "close"
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
    }
}
