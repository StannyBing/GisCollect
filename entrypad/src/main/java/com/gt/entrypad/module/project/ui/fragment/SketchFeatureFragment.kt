package com.gt.entrypad.module.project.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.gt.base.app.AppInfoManager
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.manager.UserManager
import com.gt.base.app.TempIdsBean
import com.gt.entrypad.R
import com.gt.entrypad.module.project.func.SketchFeatureAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchFeatureContract
import com.gt.entrypad.module.project.mvp.model.SketchFeatureModel
import com.gt.entrypad.module.project.mvp.presenter.SketchFeaturePresenter
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.*
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_sketch_feature.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*

/**
 * Create By XB
 * 功能：
 */
class SketchFeatureFragment : BaseFragment<SketchFeaturePresenter, SketchFeatureModel>(),
    SketchFeatureContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SketchFeatureFragment {
            val fragment = SketchFeatureFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private var currentLayer: FeatureLayer? = null

    private val sketchEditor = SketchEditor()

    private var isInEdit = false
    private var editPosition = 0

    private var tempSize = 0.0
    private var resetSize = true

    private var isOverlay = false

    private var startNum = 0
    private var featureSize = 10

    private val featureList = arrayListOf<Feature>()
    private val featureAdapter = SketchFeatureAdapter(featureList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_feature
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_sketch_filed_features.apply {
            layoutManager = ZXInScrollRecylerManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = featureAdapter
        }

        sp_collect_feature_showfield.showUnderineColor(false)
            .showSelectedTextColor(true)
            .setItemHeightDp(30)
            .setItemTextSizeSp(12)
            .build()

        sketchEditor.sketchStyle = SketchStyle().apply {
            vertexSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
            selectedVertexSymbol =
                SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
            fillSymbol =
                SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#6055A4F1"), null)
        }
        sketchEditor.addGeometryChangedListener {
            val geometry = sketchEditor.geometry
       /*     tv_collect_geometry_size.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.default_text_color
                )
            )*/
            when (sketchEditor.sketchCreationMode) {
                SketchCreationMode.POLYLINE -> {
                    if (geometry != null && resetSize) {
                        val size = GeometrySizeTool.getLength(geometry)
                        tempSize = size.toDouble()
                        if (size > 1000.toBigDecimal()) {
                            /*tv_collect_geometry_size.text =
                                "长度：${DecimalFormat("#0.00").format(size / 1000.toBigDecimal())}公里"*/
                        } else {
                           /* tv_collect_geometry_size.text =
                                "长度：${DecimalFormat("#0.00").format(size)}米"*/
                        }
                    }
                }
                SketchCreationMode.POLYGON -> {
                    if (geometry != null && resetSize) {
                        val size = GeometrySizeTool.getArea(geometry)
                        tempSize = size.toDouble()
                        if (size > 1000000.toBigDecimal()) {
                          /*  tv_collect_geometry_size.text =
                                "面积：${DecimalFormat("#0.00").format(size / 1000000.toBigDecimal())}平方公里"*/
                        } else {
                          /*  tv_collect_geometry_size.text =
                                "面积：${DecimalFormat("#0.00").format(size)}平方米"*/
                        }
                    }
                }
            }
            isOverlay = false
//            checkOverlay(geometry)
        }
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
        super.initView(savedInstanceState)
    }

    /**
     * 监测压盖
     */
    private val overlayInfoBuilder = StringBuilder()
    private val overlayList = arrayListOf<KeyValueEntity>()
    private var checkGemetry: Geometry? = null
    private var checkCount = 0

    private fun checkOverlay(geometry: Geometry?) {
        showLoading("正在检测图层压盖。。。")

        overlayInfoBuilder.clear()
        overlayList.clear()
        checkGemetry = geometry
        checkCount = 0
        geometry?.let {
            try {
                AppInfoManager.appInfo?.layerstyle?.forEach {
                    val obj = JSONObject(it)
                    if (obj.has("checkOverlay") && obj.getBoolean("checkOverlay")) {
                        MapTool.mapListener?.getMap()?.basemap?.baseLayers?.forEach map@{ layer ->
                            if (layer is FeatureLayer && layer.featureTable.tableName == obj.getString(
                                    "itemName"
                                )
                            ) {
                                checkCount++
                                excuteInfo(
                                    layer,
                                    obj.getString("itemName")
                                )
                            } else if ((layer is ArcGISVectorTiledLayer && (layer as ArcGISVectorTiledLayer).name == obj.getString(
                                    "itemName"
                                ))
//                                || (layer is ArcGISTiledLayer && (layer as ArcGISTiledLayer).name == obj.getString(
//                                    "itemName"
//                                ))
                            ) {
                                checkCount++
                                GeoPackageTool.getFeatureFromGpkgWithNull(obj.getString("itemName")) { layer2 ->
                                    if (layer2 == null) {
                                        checkCount--
                                    }
                                    excuteInfo(
                                        layer2,
                                        obj.getString("itemName")
                                    )
                                }
                            }
                            return@map
                        }
                    }
                }
                postOverlayStatus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun excuteInfo(
        featureLayer: FeatureLayer?,
        style: String
    ) {
        featureLayer?.let {
            //获取所有相关的feature
            val queryParameters = QueryParameters()
            queryParameters.spatialRelationship = QueryParameters.SpatialRelationship.INTERSECTS
            queryParameters.geometry = checkGemetry
            val listenable = it.featureTable.queryFeaturesAsync(queryParameters)
            listenable.addDoneListener {
                val features = arrayListOf<Feature>()
                features.addAll(listenable.get())
                var overlayBean = KeyValueEntity(style, 0.0)
                if (features.size > 0) {
                    overlayList.find {
                        it.key == style
                    }.apply {
                        if (this != null) {
                            overlayBean = this
                        } else {
                            overlayList.add(overlayBean)
                        }
                    }
                }
                features.forEach {
                    //获取裁剪的 feature
                    if (it.geometry != null && sketchEditor.geometry != null) {
                        val cutGeometry =
                            GeometryEngine.intersection(it.geometry, sketchEditor.geometry)
                        if (cutGeometry != null && cutGeometry.geometryType == checkGemetry!!.geometryType) {
                            //被裁减部分
                            when (cutGeometry.geometryType) {
                                GeometryType.POLYLINE -> {
                                    overlayBean.value =
                                        (overlayBean.value as Double) + GeometrySizeTool.getLength(
                                            cutGeometry
                                        ).toDouble()
                                }
                                GeometryType.POLYGON -> {
                                    overlayBean.value =
                                        (overlayBean.value as Double) + GeometrySizeTool.getArea(
                                            cutGeometry
                                        ).toDouble()
                                }
                            }
                        }
                    }
                }
                checkCount--
                postOverlayStatus()
            }
        }
//        postOverlayStatus()
    }

    /**
     * 通知压盖状态
     */
    private fun postOverlayStatus() {
//        if (overlayList.isNotEmpty()) {
        if (checkCount <= 0) {
            dismissLoading()
            isOverlay = true
//            tv_collect_geometry_size.setTextColor(ContextCompat.getColor(mContext, R.color.red))
            overlayList.forEachIndexed { index, it ->
                overlayInfoBuilder.append("${it.key}(${(it.value as Double).let {
                    when (checkGemetry!!.geometryType) {
                        GeometryType.POLYLINE -> {
                            if (it > 1000.0) {
                                "${DecimalFormat("#0.00").format(it / 1000.0)}公里"
                            } else {
                                "${DecimalFormat("#0.00").format(this)}米"
                            }
                        }
                        GeometryType.POLYGON -> {
                            if (it > 1000000.0) {
                                "${DecimalFormat("#0.00").format(it / 1000000.0)}平方公里"
                            } else {
                                "${DecimalFormat("#0.00").format(it)}平方米"
                            }
                        }
                        else -> ""
                    }
                }})")
                if (index < overlayList.size - 1) {
                    overlayInfoBuilder.append("，")
                }
            }
//            tv_collect_geometry_size.text = builder.toString()
//        }
            if (overlayInfoBuilder.isEmpty()) {
                createFeature("")
            } else {
                ZXDialogUtil.showYesNoDialog(
                    mContext,
                    "提示",
                    "监测到图层压盖：${overlayInfoBuilder.toString()}，是否继续创建？"
                ) { dialog, which ->
                    createFeature(overlayInfoBuilder.toString())
                }
            }
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        featureAdapter.setEnableLoadMore(true)
        featureAdapter.setOnLoadMoreListener({
            startNum += featureSize
            getFeatureList()
        }, rv_sketch_filed_features)
        ZXRecyclerDeleteHelper(activity, rv_sketch_filed_features)
            .setSwipeOptionViews(R.id.tv_edit, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_edit -> {
                        showToast("开启图形编辑")
                        resetSize = true
                        isInEdit = true
                        editPosition = pos
                        sv_collect_feature.smoothScrollTo(0, 0)
                        currentLayer?.clearSelection()
                        MapTool.mapListener?.getMapView()
                            ?.setViewpointGeometryAsync(featureList[pos].geometry, 80.0)
                        sketchEditor.start(
                            featureList[pos].geometry,
                            when (currentLayer?.featureTable?.geometryType) {
                                GeometryType.POINT -> SketchCreationMode.POINT
//                        GeometryType.MULTIPOINT -> SketchCreationMode.MULTIPOINT
                                GeometryType.POLYLINE -> SketchCreationMode.POLYLINE
                                GeometryType.POLYGON -> SketchCreationMode.POLYGON
                                else -> SketchCreationMode.POLYGON
                            }
                        )
                    }
                    R.id.tv_delete -> {
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否删除该要素，这将同时删除该要素的相关采集数据？"
                        ) { dialog, which ->
                            isInEdit = false
                            DeleteLayerFileTool.deleteFileByFeature(
                                ConstStrings.getOperationalLayersPath() + featureList[pos].featureTable.featureLayer.name + "/file/",
                                featureList[pos]
                            )
                            currentLayer?.featureTable?.deleteFeatureAsync(featureList[pos])
                                ?.addDoneListener {
                                    applyLayerUpdateInfo()
                                }
                            featureList.removeAt(pos)
//                            featureAdapter.notifyDataSetChanged()
                            featureAdapter.notifyItemRemoved(pos)
                            featureAdapter.notifyItemRangeChanged(pos, 5)
                        }
                    }
                }
            }.setClickable {
                isInEdit = false
                currentLayer?.clearSelection()
                HighLightLayerTool.showHighLight(featureList[it])
                currentLayer?.selectFeature(featureList[it])
                fragChangeListener?.onFragGoto(SketchMainFragment.Sketch_Field)
            }

        //要素显示名
        sp_collect_feature_showfield.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    featureAdapter.showName = sp_collect_feature_showfield.selectedKey
                    featureAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun renameLayer(beforeName: String?, afterName: String) {
        val files = FileUtils.getFilesByName(
            ConstStrings.getOperationalLayersPath(),
            currentLayer?.name
        )
        files.forEach {

            ZXFileUtil.rename(
                it, if (it.isFile) {
                    et_sketch_rename.text.toString() + ".gpkg"
                } else {
                    et_sketch_rename.text.toString()
                }
            )
        }
        val filesAfter = FileUtils.getFilesByName(
            ConstStrings.getOperationalLayersPath(),
            et_sketch_rename.text.toString()
        )
        var gpkgFile = filesAfter.first {
            it.isFile
        }

        //添加模板id
        val templateIds =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
        templateIds.forEach outFor@{ temp ->
            var hasTemp = false
            temp.layerNames.forEach inFor@{
                if (it == beforeName) {
                    hasTemp = true
                    return@inFor
                }
            }
            if (hasTemp) {
                temp.layerNames.remove(beforeName)
                temp.layerNames.add(afterName)
            }
        }
        mSharedPrefUtil.putList(ConstStrings.TemplateIdList, templateIds)

        val geoPackage = GeoPackage(gpkgFile?.path)
        geoPackage.loadAsync()
        geoPackage.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                val geoTables = geoPackage.geoPackageFeatureTables
                geoTables.forEach { table ->
                    val featureLayer = FeatureLayer(table)
                    featureLayer.loadAsync()
                    featureLayer.addDoneLoadingListener {
                        featureLayer.name =
                            gpkgFile?.name?.substring(0, gpkgFile?.name?.lastIndexOf(".") ?: 0)
                        MapTool.mapListener?.getMap()?.operationalLayers?.add(featureLayer)
                    }
                }
            }
        }


        currentLayer?.name = et_sketch_rename.text.toString()
        MapTool.mapListener?.getMap()?.operationalLayers?.remove(currentLayer)
        showToast("修改成功！")
        fragChangeListener?.onFragBack("")
    }

    private fun createFeature(remark: String) {
        var feature: Feature? = null
        if (isInEdit) {
            featureList[editPosition].geometry = sketchEditor.geometry
            AppInfoManager.appInfo?.identifystyle?.forEach {
                val obj = JSONObject(it)
                if (obj.getString("itemName") == featureList[editPosition].featureTable?.tableName) {
                    if (obj.has("size")) {
                        val key = obj.getString("size")
                        if (featureList[editPosition].attributes?.containsKey(key) == true) {
                            featureList[editPosition].attributes?.put(
                                key,
                                DecimalFormat("#0.00").format(tempSize).toDouble()
                            )
                        }
                    }
                    return@forEach
                }
            }
            currentLayer?.featureTable?.updateFeatureAsync(featureList[editPosition])
                ?.addDoneListener {
                    applyLayerUpdateInfo()
                    sketchEditor.clearGeometry()
                    sketchEditor.stop()
                    feature?.refresh()
                }
            return
        }
        feature = currentLayer?.featureTable?.createFeature()
        val pointCollection = PointCollection(arrayListOf<Point>().apply {
            add(Point(106.5635978468597, 29.575631491622985))
            add(Point(106.58894815535211, 29.57216102606911))
            add(Point(106.56553432975977, 29.554704814709538))
            add(Point(106.5635978468597, 29.575631491622985))
        })
        feature?.geometry = Polygon(pointCollection)

        //获取筛选内容
        try {
            var spinnerMap = hashMapOf<String, List<String>>()
            AppInfoManager.appInfo?.identifystyle?.forEach {
                val obj = JSONObject(it)
                if (obj.getString("itemName") == feature?.featureTable?.tableName) {
                    if (obj.has("size")) {
                        val key = obj.getString("size")
                        if (feature?.attributes?.containsKey(key) == true) {
                            feature?.attributes?.put(
                                key,
                                DecimalFormat("#0.00").format(tempSize).toDouble()
                            )
                        }
                    }
                    return@forEach
                }
            }
            feature?.featureTable?.fields?.forEach OUT@{ field ->
                if (field.fieldType == Field.Type.DATE) {
                    feature.attributes?.keys?.forEach IN@{ name ->
                        if (name == field.name) {
                            val calendar = Calendar.getInstance()
                            feature.attributes?.set(
                                name,
                                GregorianCalendar(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                            )
                            feature.attributes?.put("uuid", UUID.randomUUID().toString())
                            return@IN
                        }
                    }
                }
            }
            if (feature?.attributes?.containsKey("uuid") == true) {
                feature.attributes?.put("uuid", UUID.randomUUID().toString())
            }
            if (feature?.attributes?.containsKey("乡镇名称") == true) {
                feature.attributes?.put("乡镇名称", UserManager.user?.rnName)
            }
            if (feature?.attributes?.containsKey("乡镇街道") == true) {
                feature.attributes?.put("乡镇街道", UserManager.user?.rnName)
            }
            if (feature?.attributes?.containsKey("采集人") == true) {
                feature.attributes?.put("采集人", UserManager.user?.userName)
            }
            if (feature?.attributes?.containsKey("备注") == true) {
                feature.attributes?.put("备注", "压盖：$remark")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        resetSize = false
        currentLayer?.featureTable?.addFeatureAsync(feature)?.addDoneListener {
            applyLayerUpdateInfo()
            sketchEditor.clearGeometry()
            sketchEditor.stop()
            feature?.refresh()
            tv_collect_feature_title.text =
                "要素列表(${currentLayer?.featureTable?.totalFeatureCount})"
        }
        featureList.add(feature!!)
        featureAdapter.notifyDataSetChanged()
    }

    /**
     * 处理图层
     */
    fun excuteLayer(featureLayer: FeatureLayer, isEdit: Boolean, canRename: Boolean) {
        startNum = 0
        isInEdit = false
        et_sketch_rename.isEnabled = false
        tv_collect_rename.visibility = View.GONE

        et_sketch_rename.setText(featureLayer.name)
        currentLayer = featureLayer
        currentLayer?.clearSelection()
        try {
            AppInfoManager.appInfo?.identifystyle?.forEach {
                val obj = JSONObject(it)
                if (obj.getString("itemName") == featureLayer.featureTable.tableName) {
                    val defaultKey = obj.getString("default")
                    featureAdapter.showName = defaultKey
                    return@forEach
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //设置字段显示筛选项
        sp_collect_feature_showfield.dataList.clear()
        var showIndex = 0
        featureLayer.featureTable.fields.forEachIndexed { index, it ->
            if (showIndex == 0 && it.name == featureAdapter.showName) {
                showIndex = index
            }
            sp_collect_feature_showfield.dataList.add(KeyValueEntity(it.name, index))
        }

        sp_collect_feature_showfield.notifyDataSetChanged()
        sp_collect_feature_showfield.setSelection(showIndex)

        featureAdapter.editable = isEdit
        ll_collect_edit_bar.visibility = if (isEdit) View.VISIBLE else View.GONE

        featureList.clear()
       // getFeatureList()
        createFeature("")
    }

    private fun getFeatureList() {
        currentLayer?.featureTable?.loadAsync()
        currentLayer?.featureTable?.addDoneLoadingListener {
            val queryGet = currentLayer?.featureTable?.queryFeaturesAsync(QueryParameters().apply {
                whereClause = "1=1"
                this.resultOffset = startNum//从第几条开始
                this.maxFeatures = featureSize//每次查多少条
            })
            queryGet?.addDoneListener {
                val list = queryGet.get()
                featureAdapter.loadMoreComplete()
                if (list.toList().size < featureSize) {
                    featureAdapter.loadMoreEnd()
                }
                featureList.addAll(list)
                if (startNum > 0) {
                    featureAdapter.notifyItemInserted(startNum)
                }
                tv_collect_feature_title.text =
                    "要素列表(${currentLayer!!.featureTable.totalFeatureCount})"
            }
            tv_collect_feature_title.text = "要素列表(${currentLayer!!.featureTable.totalFeatureCount})"
            dismissLoading()
        }
    }

    fun reInit() {
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
        HighLightLayerTool.clearHighLight()
        currentLayer?.clearSelection()
        featureAdapter.notifyDataSetChanged()
    }

    private fun moveToLayer(layer: FeatureLayer) {
        try {
            val extent = layer.fullExtent
            if (extent.xMin == 0.0 || extent.xMax == 0.0 || extent.yMin == 0.0 || extent.yMax == 0.0) {
                MapTool.mapListener?.getMapView()?.setViewpointCenterAsync(extent.center, 100000.0)
            } else {
                MapTool.mapListener?.getMapView()?.setViewpointAsync(Viewpoint(extent), 1f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyLayerUpdateInfo() {
        if (currentLayer?.featureTable is ServiceFeatureTable) {
            (currentLayer?.featureTable as ServiceFeatureTable).applyEditsAsync()
        }
    }

    /**
     * 检测图层重复
     */
    private fun checkLayerExist(name: String): Boolean {
        MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
            if (it.name == name) {
                return true
            }
        }
        ConstStrings.checkList.forEach {
            if (it.getFileName().replace(".gpkg", "") == name) {
                return true
            }
        }
        return false
    }
}
