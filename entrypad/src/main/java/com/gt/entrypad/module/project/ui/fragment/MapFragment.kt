package com.gt.entrypad.module.project.ui.fragment

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.WebTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.stanny.module_rtk.tool.WHandTool
import com.gt.entrypad.R
import com.gt.entrypad.module.project.mvp.contract.MapContract
import com.gt.entrypad.module.project.mvp.model.MapModel
import com.gt.entrypad.module.project.mvp.presenter.MapPresenter
import com.gt.giscollect.module.main.func.maplayer.TdtLayerTool
import com.gt.module_map.tool.MapTool
import com.gt.module_map.tool.PointTool
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.File

class MapFragment : BaseFragment<MapPresenter, MapModel>(), MapContract.View {
    private lateinit var map: ArcGISMap
    private var locationDisplay: LocationDisplay? = null//定位
    private var locationListener: LocationDisplay.LocationChangedListener? = null
    private lateinit var vectorLayer: WebTiledLayer//矢量
    private lateinit var vectorLableLayer: WebTiledLayer//矢量-标注

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

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        try {
            ArcGISRuntimeEnvironment.setLicense("runtimestandard,101,rux00000,none,5SKIXc21JlankElJ")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        map_view.isAttributionTextVisible = false
        map = ArcGISMap(SpatialReference.create(3857))
        initBaseLayers()
        addOpreationalLayer(ConstStrings.getSketchLayersPath())
        doLocation()
        map_view.map = map
        map_view.setViewpointCenterAsync(
           Point(
                11864933.73932961,
                3447878.713329921,
                map.spatialReference
            ),
            50000.0
        )
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_map
    }

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
            if (WHandTool.isOpen && WHandTool.isConnect) {
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
    private fun initBaseLayers() {
        val spatialReference = map.spatialReference
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
        map.basemap.baseLayers.add(vectorLayer.apply { name = "矢量地图" })
        map.basemap.baseLayers.add(vectorLableLayer.apply { name = "矢量标注" })

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
                        featureLayer.loadAsync()
                        featureLayer.addDoneLoadingListener {
                            featureLayer.name = file.name.substring(0, file.name.lastIndexOf("."))
                            map.operationalLayers.add(featureLayer)
                        }
                    }
                }
            }
        }
    }

}