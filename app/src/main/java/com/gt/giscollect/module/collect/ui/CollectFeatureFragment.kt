package com.gt.giscollect.module.collect.ui

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
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
import com.gt.giscollect.R
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.base.AppInfoManager
import com.gt.base.fragment.BaseFragment
import com.gt.base.tool.WHandTool
import com.gt.giscollect.base.FragChangeListener
import com.gt.giscollect.module.collect.func.adapter.CollectFeatureAdapter
import com.gt.giscollect.module.collect.func.tool.CollectDistanceTool
import com.gt.giscollect.module.collect.func.tool.DeleteLayerFileTool
import com.gt.giscollect.module.collect.mvp.contract.CollectFeatureContract
import com.gt.giscollect.module.collect.mvp.model.CollectFeatureModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectFeaturePresenter
import com.gt.giscollect.module.main.func.tool.FileUtils
import com.gt.giscollect.module.main.func.tool.GeoPackageTool
import com.gt.giscollect.module.main.func.tool.MapTool
import com.gt.giscollect.module.query.func.tool.HighLightLayerTool
import com.gt.giscollect.tool.SimpleDecoration
import com.gt.base.manager.UserManager
import com.gt.giscollect.module.collect.func.tool.GeometrySizeTool
import com.gt.giscollect.module.system.bean.TempIdsBean
import com.gt.module_map.tool.PointTool
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_collect_feature.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*

/**
 * Create By XB
 * 功能：
 */
class CollectFeatureFragment : BaseFragment<CollectFeaturePresenter, CollectFeatureModel>(),
    CollectFeatureContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectFeatureFragment {
            val fragment = CollectFeatureFragment()
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
    private val featureAdapter = CollectFeatureAdapter(featureList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_feature
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_collect_filed_features.apply {
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
//            isShowNumbersForVertices = true
        }
        sketchEditor.addGeometryChangedListener {
            val geometry = sketchEditor.geometry
            tv_collect_geometry_size.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.default_text_color
                )
            )
            when (sketchEditor.sketchCreationMode) {
                SketchCreationMode.POLYLINE -> {
                    if (geometry != null && resetSize) {
                        val size = GeometrySizeTool.getLength(geometry)
                        tempSize = size.toDouble()
                        if (size > 1000.toBigDecimal()) {
                            tv_collect_geometry_size.text =
                                "长度：${DecimalFormat("#0.00").format(size / 1000.toBigDecimal())}公里"
                        } else {
                            tv_collect_geometry_size.text =
                                "长度：${DecimalFormat("#0.00").format(size)}米"
                        }
                    }
                }
                SketchCreationMode.POLYGON -> {
                    if (geometry != null && resetSize) {
                        val size = GeometrySizeTool.getArea(geometry)
                        tempSize = size.toDouble()
                        if (size > 1000000.toBigDecimal()) {
                            tv_collect_geometry_size.text =
                                "面积：${DecimalFormat("#0.00").format(size / 1000000.toBigDecimal())}平方公里"
                        } else {
                            tv_collect_geometry_size.text =
                                "面积：${DecimalFormat("#0.00").format(size)}平方米"
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

//        Observable.timer(2, TimeUnit.SECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                dismissLoading()
//                if (overlayInfo.isNotEmpty()) {
//                    ZXDialogUtil.showYesNoDialog(
//                        mContext,
//                        "提示",
//                        "$overlayInfo，是否继续创建？"
//                    ) { dialog, which ->
//                        createFeature(overlayInfo)
//                    }
//                } else {
//                    createFeature("")
//                }
//            }

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
        }, rv_collect_filed_features)
        ZXRecyclerDeleteHelper(activity, rv_collect_filed_features)
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
                //                MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
//                    if (it is FeatureLayer) {
//                        it.clearSelection()
//                    }
//                }
//                MapTool.mapListener?.getMapView()?.setViewpointGeometryAsync(featureList[it].geometry)
                currentLayer?.clearSelection()
                HighLightLayerTool.showHighLight(featureList[it])
                currentLayer?.selectFeature(featureList[it])
                fragChangeListener?.onFragGoto(
                    CollectMainFragment.Collect_Field,
                    featureList[it] to (ll_collect_edit_bar.visibility == View.VISIBLE)
                )
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
        //修改名称
        tv_collect_rename.setOnClickListener {
            if (et_collect_rename.text.toString().isNotEmpty()) {
                val beforeName = currentLayer?.name
                val afterName = et_collect_rename.text.toString()
                if (checkLayerExist(et_collect_rename.text.toString())) {
                    showToast("该名称已被占用，请更换")
                    return@setOnClickListener
                }
                var nowTemplateId = ConstStrings.bussinessId
                val templateIds =
                    mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
                templateIds.forEach OUT@{ temp ->
                    temp.layerNames.forEach {
                        if (it == beforeName) {
                            nowTemplateId = temp.templateId
                            return@OUT
                        }
                    }
                }
                mPresenter.checkMultiName(
                    hashMapOf(
                        "templateId" to nowTemplateId,
                        "layerName" to et_collect_rename.text.toString()
                    ), beforeName, afterName
                )

//                renameLayer(beforeName, afterName)
            } else {
                showToast("请填写图层名")
            }
        }
        //新增
        tv_collect_feature_create.setOnClickListener {
            isOverlay = true
            if (sketchEditor.geometry?.isEmpty == false) {
                ZXDialogUtil.showYesNoDialog(
                    mContext,
                    "提示",
                    "当前正在编辑中，是否清除并重新编辑？"
                ) { dialog, which ->
                    isInEdit = false
                    sketchEditor.clearGeometry()
                    sketchEditor.start(
                        when (currentLayer?.featureTable?.geometryType) {
                            GeometryType.POINT -> SketchCreationMode.POINT
//                            GeometryType.MULTIPOINT -> SketchCreationMode.MULTIPOINT
                            GeometryType.POLYLINE -> SketchCreationMode.POLYLINE
                            GeometryType.POLYGON -> SketchCreationMode.POLYGON
                            else -> SketchCreationMode.POLYGON
                        }
                    )
                    showToast("请开始采集要素")
                }
            } else {
                resetSize = true
                sketchEditor.start(
                    when (currentLayer?.featureTable?.geometryType) {
                        GeometryType.POINT -> SketchCreationMode.POINT
//                        GeometryType.MULTIPOINT -> SketchCreationMode.MULTIPOINT
                        GeometryType.POLYLINE -> SketchCreationMode.POLYLINE
                        GeometryType.POLYGON -> SketchCreationMode.POLYGON
                        else -> SketchCreationMode.POLYGON
                    }
                )
                showToast("请开始采集要素")
            }
        }
        //上一步
        tv_collect_feature_undo.setOnClickListener {
            if (sketchEditor.canUndo()) {
                sketchEditor.undo()
            }
        }
        //下一步
        tv_collect_feature_redo.setOnClickListener {
            if (sketchEditor.canRedo()) {
                sketchEditor.redo()
            }
        }
        //GPS
        tv_collect_feature_gps.setOnClickListener {
            getPermission(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val location = if (WHandTool.isRegister() && WHandTool.isOpen) {
                    val info = WHandTool.getDeviceInfoOneTime()
                    if (info == null) {
                        null
                    } else {
                        PointTool.change4326To3857(Point(info.longitude, info.latitude, SpatialReference.create(4326)))
                    }
                } else {
                    MapTool.mapListener?.getMapView()?.locationDisplay?.mapLocation
                }
                if (sketchEditor.isVisible && location != null) {
                    sketchEditor.insertVertexAfterSelectedVertex(location)
                } else {
                    showToast("GPS打点失败")
                }
//                if (!WHandTool.isRegister()) {
//                    WHandTool.registerWHand(requireActivity())
//                    WHandTool.setDeviceInfoListener(object :
//                        WHandTool.WHandDeviceListener {
//                        override fun onDeviceInfoCallBack(info: WHandInfo?) {
////                            ZXToastUtil.showToast("获取到定位信息：${info?.longitude},${info?.latitude}")
//                        }
//                    })
//                } else {
//                    val info = WHandTool.getDeviceInfoOneTime()
//                    ZXToastUtil.showToast("获取到定位信息：${info?.longitude},${info?.latitude}")
//                }
            }
        }
        //清除
        tv_collect_feature_clear.setOnClickListener {
            sketchEditor.clearGeometry()
        }
        //距离打点
        tv_collect_feature_distance.setOnClickListener {
            val point = CollectDistanceTool.excutePoint(
                sketchEditor,
                et_collect_angle.text.toString(),
                et_collect_distance.text.toString()
            )
            point?.let {
                sketchEditor.insertVertexAfterSelectedVertex(it)
            }
        }
        //保存
        tv_collect_feature_save.setOnClickListener {
            //            if (isOverlay) {
//                showToast("当前绘制区域已检测到图层压盖，无法保存")
//                return@setOnClickListener
//            }
            if (sketchEditor.geometry?.isEmpty == false) {
                if ((sketchEditor.geometry.geometryType == GeometryType.POLYGON && (sketchEditor.geometry as Polygon).parts.partsAsPoints.toList().size < 3) ||
                    (sketchEditor.geometry.geometryType == GeometryType.POLYLINE && (sketchEditor.geometry as Polyline).parts.partsAsPoints.toList().size < 2)
                ) {
                    showToast("当前绘制范围不全，无法保存")
                    return@setOnClickListener
                }
                //检测图层压盖
                checkOverlay(sketchEditor.geometry)
            } else {
                showToast("暂未添加要素")
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
                    et_collect_rename.text.toString() + ".gpkg"
                } else {
                    et_collect_rename.text.toString()
                }
            )
        }
        val filesAfter = FileUtils.getFilesByName(
            ConstStrings.getOperationalLayersPath(),
            et_collect_rename.text.toString()
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


        currentLayer?.name = et_collect_rename.text.toString()
        MapTool.mapListener?.getMap()?.operationalLayers?.remove(currentLayer)
        showToast("修改成功！")
        fragChangeListener?.onFragBack(CollectMainFragment.Collect_Feature)
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
        feature?.geometry = sketchEditor.geometry

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
        //                if (featureAdapter.showName == "") {//第一条
        //                    featureAdapter.showName = sp_collect_feature_showfield.selectedKey
        //                }
    }

    /**
     * 处理图层
     */
    fun excuteLayer(featureLayer: FeatureLayer, isEdit: Boolean, canRename: Boolean) {
        startNum = 0
        isInEdit = false
//        et_collect_rename.isEnabled = canRename
//        tv_collect_rename.visibility = if (canRename) View.VISIBLE else View.GONE
        et_collect_rename.isEnabled = false
        tv_collect_rename.visibility = View.GONE

        et_collect_rename.setText(featureLayer.name)
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

//        moveToLayer(featureLayer)
        featureList.clear()
        getFeatureList()
    }

    private fun getFeatureList() {
        currentLayer?.featureTable?.loadAsync()
        currentLayer?.featureTable?.addDoneLoadingListener {

            //                if (featureLayer.featureTable.totalFeatureCount > 0) {
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
//                if (list.firstOrNull()?.attributes?.containsKey("OBJECTID") == true) {
//                    featureList.addAll(list.sortedBy {
//                        it.attributes["OBJECTID"] as Long
//                    })
//                } else {
                featureList.addAll(list)
//                }

                if (startNum > 0) {
                    featureAdapter.notifyItemInserted(startNum)
                }
                tv_collect_feature_title.text =
                    "要素列表(${currentLayer!!.featureTable.totalFeatureCount})"
            }
            //                }
            tv_collect_feature_title.text = "要素列表(${currentLayer!!.featureTable.totalFeatureCount})"

            if (currentLayer!!.featureTable.geometryType == GeometryType.POLYGON) {
                tv_collect_geometry_size.visibility = View.VISIBLE
                tv_collect_geometry_size.text = "面积："
            } else if (currentLayer!!.featureTable.geometryType == GeometryType.POLYLINE) {
                tv_collect_geometry_size.visibility = View.VISIBLE
                tv_collect_geometry_size.text = "长度："
            } else {
                tv_collect_geometry_size.visibility = View.GONE
            }
            dismissLoading()
        }
    }

    fun reInit() {
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
        currentLayer?.clearSelection()
        HighLightLayerTool.clearHighLight()
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

    override fun checkMultiNameResult(isMulti: Boolean, beforeName: String?, afterName: String) {
        if (isMulti) {
            showToast("该名称已被占用，请更换")
            return
        }
        renameLayer(beforeName, afterName)
    }
}
