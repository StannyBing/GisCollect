package com.gt.map

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.*
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.*
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.base.app.AppInfoManager
import com.gt.base.fragment.BaseFragment
import com.gt.base.tool.WHandTool
import com.gt.module_map.listener.MapListener
import com.gt.giscollect.module.main.func.maplayer.GoogleLayer
import com.gt.giscollect.module.main.func.maplayer.TdtLayerTool
import com.gt.giscollect.module.main.func.tool.LayerTool
import com.gt.module_map.tool.MapTool
import com.gt.giscollect.module.main.mvp.contract.MapContract
import com.gt.giscollect.module.main.mvp.model.MapModel
import com.gt.giscollect.module.main.mvp.presenter.MapPresenter
import com.gt.base.manager.UserManager
import com.gt.module_map.tool.PointTool
import com.zx.zxutils.util.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.json.JSONObject
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Create By XB
 * 功能：地图页
 */
class MapFragment : BaseFragment<MapPresenter, MapModel>(), MapContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): MapFragment {
            val fragment = MapFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "map"
    }

    private lateinit var map: ArcGISMap

    private val singleTapList = arrayListOf<MapListener.OnSingleTapCall>()

    private var locationDisplay: LocationDisplay? = null//定位

//    private lateinit var localMap: ArcGISVectorTiledLayer//本地

    private lateinit var vectorLayer: WebTiledLayer//矢量
    private lateinit var vectorLableLayer: WebTiledLayer//矢量-标注
    private lateinit var imageLayer: WebTiledLayer//影像
    private lateinit var imageLabelLayer: WebTiledLayer//影像-标注
    private lateinit var googleMap: ImageTiledLayer//谷歌地图

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_map
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        try {
            ArcGISRuntimeEnvironment.setLicense("runtimestandard,101,rux00000,none,5SKIXc21JlankElJ")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        map_view.isAttributionTextVisible = false
        map_compass.initMap(map_view)

        map = ArcGISMap(SpatialReference.create(3857))
//        map = ArcGISMap()

        initBaseLayers()

        initOnLineLayers()

//        initLocalLayers()

        initOperationalLayers()

        //TODO
        initLocalLayers()

        initStylx()

//        handler.postDelayed({
        doLocation()
//        }, 500)

        Observable.interval(1000, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //                ZXLocationUtil.getLocation(activity)
                getPermission(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    if (ZXLocationUtil.isGpsEnabled() && ZXLocationUtil.isLocationEnabled()) {
                        locationDisplay?.locationDataSource?.startAsync()
                    }
                }
            }

        map_view.map = map

        map_view.setViewpointCenterAsync(
            com.esri.arcgisruntime.geometry.Point(
                11864933.73932961,
                3447878.713329921,
                map.spatialReference
            ),
            50000.0
        )
    }

    private fun initBaseLayers() {

        val spatialReference = map.spatialReference
//        ZXLogUtil.loge("spa_"+spatialReference.wkid)
//        val spatialReference = SpatialReference.create(3857)
        vectorLayer = TdtLayerTool.getTdtLayer(
            TdtLayerTool.ServiceType.VEC_W,
            TdtLayerTool.LayerName.VECTOR,
            TdtLayerTool.TiledFormat.TILES,
            spatialReference
        )
        vectorLableLayer = TdtLayerTool.getTdtLayer(
            TdtLayerTool.ServiceType.CVA_W,
            TdtLayerTool.LayerName.VECTOR_ANNOTATION_CHINESE,
            TdtLayerTool.TiledFormat.TILES,
            spatialReference
        )
        imageLayer = TdtLayerTool.getTdtLayer(
            TdtLayerTool.ServiceType.IMG_W,
            TdtLayerTool.LayerName.IMAGE,
            TdtLayerTool.TiledFormat.TILES,
            spatialReference
        )
        imageLabelLayer = TdtLayerTool.getTdtLayer(
            TdtLayerTool.ServiceType.CIA_W,
            TdtLayerTool.LayerName.IMAGE_ANNOTATION_CHINESE,
            TdtLayerTool.TiledFormat.TILES,
            spatialReference
        )

        googleMap =
            GoogleLayer.getInstance(GoogleLayer.MapType.IMAGE, requireActivity(), spatialReference)

        map.basemap.baseLayers.add(vectorLayer.apply { name = "矢量地图" })
        map.basemap.baseLayers.add(vectorLableLayer.apply { name = "矢量标注" })
        map.basemap.baseLayers.add(googleMap.apply { name = "谷歌影像" })
        map.basemap.baseLayers.add(imageLayer.apply { name = "天地图影像" })
        map.basemap.baseLayers.add(imageLabelLayer.apply { name = "影像标注" })

//        vectorLayer.isVisible = false
//        vectorLableLayer.isVisible = false
        googleMap.isVisible = false
        imageLayer.isVisible = false
        imageLabelLayer.isVisible = false
        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
//                initOperationalLayers()
                if (type == MapTool.ChangeType.OperationalAdd) {
                    map.operationalLayers.add(layer)
                } else if (type == MapTool.ChangeType.OperationalRemove) {
                    map.operationalLayers.remove(layer)
                } else if (type == MapTool.ChangeType.BaseAdd) {
                    map.basemap.baseLayers.add(layer)
                } else if (type == MapTool.ChangeType.BaseRemove) {
                    map.basemap.baseLayers.remove(layer)
                }
            }
        })

//        map_view.map = map
    }

    /**
     * 添加本地图层
     */
    private fun initLocalLayers() {
        val mapList = map.basemap?.baseLayers
        val nameList = arrayListOf<String>()
        if (!mapList.isNullOrEmpty()) {
            mapList.forEach {
                nameList.add(it.name)
            }
        }
        val localFiles = arrayListOf<String>()
        val file = File(ConstStrings.getLocalMapPath())
        if (file.exists() && file.isDirectory && file.listFiles().isNotEmpty()) {
            file.listFiles().forEach {
                if (it.isFile && !nameList.contains(
                        it.name.substring(
                            it.name.lastIndexOf("/") + 1,
                            it.name.lastIndexOf(".")
                        )
                    )
                ) {
                    localFiles.add(it.path)
                } else if (it.isDirectory) {
                    localFiles.add(it.path)
                }
            }
        }

        val fileIn = File(ConstStrings.getInnerLocalMapPath())
        if (fileIn.exists() && fileIn.isDirectory && fileIn.listFiles().isNotEmpty()) {
            fileIn.listFiles().forEach {
                if (it.isFile && !nameList.contains(
                        it.name.substring(
                            it.name.lastIndexOf("/") + 1,
                            it.name.lastIndexOf(".")
                        )
                    )
                ) {
                    localFiles.add(it.path)
                } else if (it.isDirectory) {
                    localFiles.add(it.path)
                }
            }
        }
        localFiles.forEach {
            if (ZXFileUtil.isFileExists(it)) {
                //加载切片文件夹
                if (File(it).isDirectory) {
                    var isTpkFile = false
                    File(it).listFiles().forEach {
                        if (it.name == "conf.cdi" || it.name == "Conf.xml") {
                            isTpkFile = true
                            return@forEach
                        }
                    }
                    if (isTpkFile) {
                        val localCache = TileCache("$it/")
                        val localMap = ArcGISTiledLayer(localCache)
                        localMap.name = it.substring(it.lastIndexOf("/") + 1)
                        map.basemap.baseLayers.add(localMap)
                    }
                } else {
                    LayerTool.addLocalMapLayer(map, File(it))
////                    else -> {
////                        showToast("暂不支持该文件类型")
////                    }
//                    }
                }
            }
        }
    }

    /**
     * 添加网络图层
     */
    private fun initOnLineLayers() {
        AppInfoManager.appInfo?.onlineService?.forEach {
            val obj = JSONObject(it)
            val key = if (obj.has("type")) "type" else if (obj.has("tpye")) "tpye" else ""
            if (key.isNotEmpty()) {
                if (obj.has("enable") && !obj.getBoolean("enable")) {
                    return@forEach
                }
                when (obj.getString(key)) {
                    "FeatureLayer" -> {
                        map.basemap.baseLayers.add(FeatureLayer(ServiceFeatureTable(obj.getString("url"))).apply {
                            name = obj.getString("itemName")
                            isVisible = !obj.has("visible") || obj.getBoolean("visible")
                            if (obj.has("filterField")) {
                                definitionExpression =
                                    "${obj.getString("filterField")}='${UserManager.user?.rnName}'"
                            }
                        })

                    }
                    "DynamicLayer" -> {
                        map.basemap.baseLayers.add(ArcGISMapImageLayer(obj.getString("url")).apply {
                            name = obj.getString("itemName")
                            isVisible = !obj.has("visible") || obj.getBoolean("visible")
                        })
                    }
                    "TileLayer" -> {
                        map.basemap.baseLayers.add(ArcGISTiledLayer(obj.getString("url")).apply {
                            name = obj.getString("itemName")
                            isVisible = !obj.has("visible") || obj.getBoolean("visible")
                        })
                    }
                }
            }
        }
    }

    private fun getLocalMap(path: String): ArrayList<String> {
        val localFiles = arrayListOf<String>()
        val file = File(path)
        if (file.exists() && file.isDirectory && file.listFiles().isNotEmpty()) {
            file.listFiles().forEach {
                if (it.isFile) {
                    localFiles.add(it.path)
                }
            }
        }
        return localFiles
    }

    /**
     * 初始化业务图层
     */
    private fun initOperationalLayers() {
        getPermission(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //加载在线的
            addOnlineOperation()

            //只加载当前用户的
            addOpreationalLayer(ConstStrings.getOperationalLayersPath())
        }
    }

    /**
     * 初始化stylx文件
     */
    private fun initStylx() {
//        val symbolStyle = DictionarySymbolStyle.createFromFile(ConstEntryStrings.getStylxPath() + "mil2525d.stylx")
//        symbolStyle.loadAsync()
//        symbolStyle.addDoneLoadingListener {
//
//        }
    }

    /**
     * 加载在线服务
     */
    private fun addOnlineOperation() {
        AppInfoManager.appInfo?.editService?.forEach {
            val obj = JSONObject(it)
            obj.optString("url").apply {
                val featureTable = ServiceFeatureTable(this)
                featureTable.loadAsync()
                featureTable.addDoneLoadingListener {
                    val onlineFeatureLayer = FeatureLayer(featureTable)
                    onlineFeatureLayer.loadAsync()
                    onlineFeatureLayer.addDoneLoadingListener {
                        map.operationalLayers.add(onlineFeatureLayer)
                    }
                }
            }
        }
    }

    /**
     * 遍历文件夹中的shape
     */
    private fun addOpreationalLayer(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory && file.listFiles().isNotEmpty()) {
            file.listFiles().forEach {
                addOpreationalLayer(it.path)
            }
        } else if (file.exists() && file.isFile && file.name.endsWith(".shp")) {
            val shapefileFeatureTable = ShapefileFeatureTable(file.path)
            shapefileFeatureTable.loadAsync() //异步方式读取文件
            shapefileFeatureTable.addDoneLoadingListener {
                //数据加载完毕后，添加到地图
                val mainShapefileLayer = FeatureLayer(shapefileFeatureTable)
                mainShapefileLayer.renderer = UniqueValueRenderer().apply {
                    defaultSymbol = when (mainShapefileLayer.featureTable.geometryType) {
                        GeometryType.POINT, GeometryType.MULTIPOINT -> {
                            SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, null)
                        }
                        GeometryType.POLYLINE -> {
                            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
                        }
                        GeometryType.POLYGON -> {
                            SimpleFillSymbol(
                                SimpleFillSymbol.Style.SOLID,
                                Color.parseColor("#50FF0000"),
                                null
                            )
                        }
                        else -> {
                            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
                        }
                    }
                }
                map.operationalLayers.add(mainShapefileLayer)
            }
        } else if (file.exists() && file.isFile && file.name.endsWith(".gpkg")) {
            val geoPackage = GeoPackage(file.path)
            geoPackage.loadAsync()
            geoPackage.addDoneLoadingListener {
                if (geoPackage.loadStatus == LoadStatus.LOADED) {
                    val geoTables = geoPackage.geoPackageFeatureTables
                    geoTables.forEach { table ->
                        val featureLayer = FeatureLayer(table)
//                        featureLayer.loadAsync()
//                        featureLayer.addDoneLoadingListener {
//                            featureLayer.name =
//                        }
                        featureLayer.loadAsync()
                        featureLayer.addDoneLoadingListener {
                            featureLayer.name = file.name.substring(0, file.name.lastIndexOf("."))

                            //只添加当前用户对应的采集数据
//                            templateIds?.forEach temp@{ temp ->
//                                temp.layerNames.forEach {
//                                    if (it == featureLayer.name) {
//                                        StyleFileTool.loadRenderer(featureLayer)
//                                        map.operationalLayers.add(featureLayer)
//                                        return@temp
//                                    }
//                                }
//                            }
//
//                            StyleFileTool.loadRenderer(featureLayer)
//
                            map.operationalLayers.add(featureLayer)
                        }
                    }
                }
            }
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        map_view.onTouchListener =
            object : DefaultMapViewOnTouchListener(requireActivity(), map_view) {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
//                    val point = android.graphics.Point(e!!.x.roundToInt(), e.y.roundToInt())
//                    ZXLogUtil.loge(map_view.screenToLocation(point).toJson())
////                    map_view.setViewpointCenterAsync(map_view.screenToLocation(point))
////                        .addDoneListener {
////
////                        }
//
//                    map_view.setViewpointCenterAsync(
//                        com.esri.arcgisruntime.geometry.Point(
//                            11864933.73932961,
//                            3447878.713329921,
//                            map.spatialReference
//                        )
//                    )
//
////                val queryResult = vectorLayer.getfea

                    return true
                }
            }
    }

    fun addSingleTap(singleTap: MapListener.OnSingleTapCall) {
        singleTapList.add(singleTap)
    }

    private var locationListener: LocationDisplay.LocationChangedListener? = null

    fun doLocation() {
        getPermission(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            //            if (!ZXLocationUtil.isGpsEnabled()) {
//                map_view.setViewpointCenterAsync(
//                    com.esri.arcgisruntime.geometry.Point(
//                        11864933.73932961,
//                        3447878.713329921,
//                        map.spatialReference
//                    )
//                )
//                return@getPermission
//            }
            if (WHandTool.isOpen && WHandTool.isRegister()) {
                val info = WHandTool.getDeviceInfoOneTime()
                if (info != null) {
                    map_view.setViewpointCenterAsync(
                        PointTool.change4326To3857(
                            Point(
                                info.longitude,
                                info.latitude,
                                SpatialReference.create(4326)
                            )
                        )
                    )
                    return@getPermission
                }
            }
            if (locationDisplay == null) {
                locationDisplay = map_view.locationDisplay
            }
            if (locationDisplay?.isStarted == true) {
                locationDisplay?.stop()
                return@getPermission
            }
            if (locationListener != null) locationDisplay?.removeLocationChangedListener(
                locationListener
            )
            locationDisplay?.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
//            locationDisplay?.addLocationChangedListener {
//                ZXToastUtil.showToast(it.location.position.toJson())
//            }
//            locationDisplay?.isShowPingAnimation = true
//            locationDisplay?.isShowAccuracy = true
//            locationDisplay?.isShowLocation = true
            locationDisplay?.startAsync()
        }
    }
}
