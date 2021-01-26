package com.gt.giscollect.module.query.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.R
import com.gt.giscollect.module.main.func.listener.MapListener
import com.gt.giscollect.module.main.func.tool.GeoPackageTool
import com.gt.giscollect.module.main.func.tool.MapTool
import com.gt.giscollect.module.query.func.adapter.SearchAdapter
import com.gt.giscollect.module.query.func.tool.HighLightLayerTool
import com.gt.giscollect.module.query.mvp.contract.SearchContract
import com.gt.giscollect.module.query.mvp.model.SearchModel
import com.gt.giscollect.module.query.mvp.presenter.SearchPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.util.ZXFormatCheckUtil
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.regex.Pattern

/**
 * Create By XB
 * 功能：查询
 */
class SearchFragment : BaseFragment<SearchPresenter, SearchModel>(), SearchContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "search"
    }

    private val searchList = arrayListOf<Feature>()
    private val searchAdapter = SearchAdapter(searchList)

    private var startNum = 0
    private var featureSize = 10

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_search_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = searchAdapter
            addItemDecoration(SimpleDecoration(requireActivity()))
        }
        sp_search_map_layer
            .setDefaultItem("请选择查询图层")
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        sp_search_map_field
            .setDefaultItem("请选择显示字段")
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(13)
            .showSelectedTextColor(true)
            .build()
        setLayerData()
        super.initView(savedInstanceState)
    }

    private fun setLayerData() {
        sp_search_map_layer?.let {
            val layers = arrayListOf<Layer>()
            layers.addAll(MapTool.mapListener?.getMap()?.operationalLayers ?: arrayListOf())
            layers.addAll(MapTool.mapListener?.getMap()?.basemap?.baseLayers ?: arrayListOf())
            val layerSpinner = arrayListOf<KeyValueEntity>()
            if (!layers.isNullOrEmpty()) {
                layers.forEach {
                    layerSpinner.add(KeyValueEntity(it.name, it))
                }
            }
            sp_search_map_layer.dataList.clear()
            sp_search_map_layer.dataList.addAll(layerSpinner)
            sp_search_map_layer.setDefaultItem("请选择查询图层")
            sp_search_map_layer.notifyDataSetChanged()
        }
        iv_search_do.setOnClickListener {
            try {
                ZXSystemUtil.closeKeybord(requireActivity())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (sp_search_map_layer.selectedItemPosition == 0) {
                showToast("请选择查询图层")
            } else {
                startNum = 0
                searchInMap(sp_search_map_layer.selectedValue as Layer)
            }
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
        sp_search_map_layer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                searchList.clear()
                searchAdapter.notifyDataSetChanged()

                if (sp_search_map_layer.selectedItemPosition == 0) {
                    return
                }

                val fieldSpinner = arrayListOf<KeyValueEntity>()
                val layer = sp_search_map_layer.selectedValue

                if (layer is FeatureLayer && layer.featureTable.fields.isNotEmpty()) {
                    layer.featureTable.loadAsync()
                    layer.featureTable.addDoneLoadingListener {
                        layer.featureTable.fields.forEach {
                            fieldSpinner.add(KeyValueEntity(it.name, it))
                        }
                    }
                } else if (layer is ArcGISVectorTiledLayer) {
                    GeoPackageTool.getFeatureFromGpkg(layer.name) {
                        sp_search_map_layer.selectedEntity.setValue(it)
                        it.featureTable.fields.forEach {
                            fieldSpinner.add(KeyValueEntity(it.name, it))
                        }
                        reloadFieldSpinner(fieldSpinner)
                    }
                }
                reloadFieldSpinner(fieldSpinner)
            }
        }
        //字段切换事件
        sp_search_map_field.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (searchList.isNotEmpty()) {
                    startNum = 0
                    searchInMap(sp_search_map_layer.selectedValue as Layer)
                }
            }
        }
        //搜索事件
        et_search_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ZXSystemUtil.closeKeybord(requireActivity())
                if (sp_search_map_layer.selectedItemPosition == 0) {
                    showToast("请选择查询图层")
                } else {
                    startNum = 0
                    searchInMap(sp_search_map_layer.selectedValue as Layer)
                }
            }
            true
        }
        //列表点击事件
        searchAdapter.setOnItemClickListener { adapter, view, position ->
            clearAllFeatureSelect()
//            val layer = searchList[position].featureTable.featureLayer
//            layer.selectionColor = Color.RED
//            layer.selectFeature(searchList[position])
            HighLightLayerTool.showHighLight(searchList[position])

//            try {
//                MapTool.mapListener?.getMapView()?.setViewpointGeometryAsync(GeometryEngine.buffer(searchList[position].geometry, 1000.0))
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
        searchAdapter.setEnableLoadMore(true)
        searchAdapter.setOnLoadMoreListener({
            startNum += featureSize
            ZXLogUtil.loge("LoadMore：开始加载更多-${startNum}")
            searchInMap(sp_search_map_layer.selectedValue as Layer)
        }, rv_search_list)
    }

    private fun reloadFieldSpinner(fieldSpinner: ArrayList<KeyValueEntity>) {
        sp_search_map_field.dataList.clear()
        sp_search_map_field.dataList.addAll(fieldSpinner)
        sp_search_map_field.setDefaultItem("请选择显示字段")
        sp_search_map_field.notifyDataSetChanged()
    }

    /**
     * 清空所有要素选择
     */
    private fun clearAllFeatureSelect() {
        HighLightLayerTool.clearHighLight()
//        MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
//            if (it is FeatureLayer) {
//                it.clearSelection()
//            }
//        }
    }

    /**
     * 属性查图
     */
    private fun searchInMap(layer: Layer) {
        if (layer is FeatureLayer) {
//            layer.selectionColor = Color.RED
            val query = QueryParameters()
            query.whereClause = getWhereStrFunction(layer, et_search_text.text.toString())
            query.resultOffset = startNum//从第几条开始
            query.maxFeatures = featureSize//每次查多少条
            layer.featureTable.loadAsync()
            layer.featureTable.addDoneLoadingListener {
                if (startNum == 0) {
                    val countQuery = layer.featureTable.queryFeatureCountAsync(QueryParameters().apply { whereClause = getWhereStrFunction(layer, et_search_text.text.toString()) })
                    countQuery.addDoneListener {
                        showToast("查询到${countQuery.get()}个结果")
                    }
                }
                val listenable1 = layer.featureTable.queryFeaturesAsync(query)//查询
                listenable1.addDoneListener {
                    try {
                        val list = listenable1.get()
                        ZXLogUtil.loge("LoadMore：当前获取-${list.toList().size}条")
                        searchAdapter.loadMoreComplete()
                        if (list.toList().size < featureSize) {
                            searchAdapter.loadMoreEnd()
                        }

                        if (startNum == 0) {
                            searchList.clear()
                        }
                        searchList.addAll(list)

                        if (searchList.isNotEmpty()) {
                            var showIndex = 0
                            if (sp_search_map_field.selectedItemPosition != 0) {
                                searchList[0].attributes.keys.forEachIndexed { index, it ->
                                    if (sp_search_map_field.selectedKey == it) {
                                        showIndex = index
                                        return@forEachIndexed
                                    }
                                }
                            }
                            searchAdapter.showIndex = showIndex
                        }
                        //处理adapter
                        searchAdapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            showToast("当前图层不支持查询")
        }
    }

    /**
     * 获取模糊查询字符串
     * @param featureLayer
     * @param search
     * @return
     */
    private fun getWhereStrFunction(featureLayer: FeatureLayer, search: String): String? {
        if (search.isEmpty()) {
            return "1=1"
        }
        val stringBuilder = StringBuilder()
        val fields = featureLayer.featureTable.fields
        val isNumber: Boolean = isNumberFunction(search)
        for (field in fields) {
            when (field.fieldType) {
                Field.Type.TEXT -> {
                    stringBuilder.append(" upper(")
                    stringBuilder.append(field.name)
                    stringBuilder.append(") LIKE '%")
                    stringBuilder.append(search.toUpperCase())
                    stringBuilder.append("%' or")
                }
                Field.Type.SHORT, Field.Type.INTEGER, Field.Type.FLOAT, Field.Type.DOUBLE, Field.Type.OID -> if (isNumber == true) {
                    stringBuilder.append(" upper(")
                    stringBuilder.append(field.name)
                    stringBuilder.append(") = ")
                    stringBuilder.append(search)
                    stringBuilder.append(" or")
                }
                Field.Type.UNKNOWN, Field.Type.GLOBALID, Field.Type.BLOB, Field.Type.GEOMETRY, Field.Type.RASTER, Field.Type.XML, Field.Type.GUID, Field.Type.DATE -> {
                }
            }
        }
        val result = stringBuilder.toString()
        if (result.length > 2) {
            return result.substring(0, result.length - 2)
        } else {
            return "1=1"
        }
    }

    /**
     * 判断是否为数字
     * @param string
     * @return
     */
    fun isNumberFunction(string: String?): Boolean {
        var result = false
        val pattern = Pattern.compile("^[-+]?[0-9]")
        if (pattern.matcher(string).matches()) {
            //数字
            result = true
        } else {
            //非数字
        }
        //带小数的
        val stringBuilder = java.lang.StringBuilder()
        stringBuilder.append('^')
        stringBuilder.append('[')
        stringBuilder.append("-+")
        stringBuilder.append("]?[")
        stringBuilder.append("0-9]+(")
        stringBuilder.append('\\')
        stringBuilder.append(".[0-9")
        stringBuilder.append("]+)")
        stringBuilder.append("?$")
        val pattern1 = Pattern.compile(stringBuilder.toString())
        if (pattern1.matcher(string).matches()) {
            //数字
            result = true
        } else {
            //非数字
        }
        return result
    }
}
