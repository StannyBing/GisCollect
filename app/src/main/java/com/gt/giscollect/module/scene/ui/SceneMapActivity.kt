package com.gt.giscollect.module.scene.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Camera
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R
import com.gt.giscollect.module.main.func.maplayer.TdtLayerTool

import com.gt.giscollect.module.scene.mvp.contract.SceneMapContract
import com.gt.giscollect.module.scene.mvp.model.SceneMapModel
import com.gt.giscollect.module.scene.mvp.presenter.SceneMapPresenter
import com.gt.module_map.tool.maplayer.GoogleLayer
import kotlinx.android.synthetic.main.activity_scene_map.*


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class SceneMapActivity : BaseActivity<SceneMapPresenter, SceneMapModel>(), SceneMapContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, SceneMapActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_scene_map
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        try {
            ArcGISRuntimeEnvironment.setLicense("runtimestandard,101,rux00000,none,5SKIXc21JlankElJ")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val arcgisScene = ArcGISScene()
        sceneView.spatialReference
        arcgisScene.basemap = Basemap.createImageryWithLabels()
//        arcgisScene.basemap = Basemap(ArcGISTiledLayer("https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer"))
//        arcgisScene.basemap = Basemap(ArcGISTiledLayer("https://tiles.arcgis.com/tiles/0p6i4J6xhQas4Unf/arcgis/rest/services/Building_Basement/SceneServer"))
//        arcgisScene.baseSurface.elevationSources.add(ArcGISTiledElevationSource("http://xr002.gis.cn:6080/arcgis/rest/services/Hosted/%E9%92%93%E9%B1%BC%E5%98%B4%E5%80%BE%E6%96%9C/SceneServer"))
        sceneView.isAttributionTextVisible = false

//        arcgisScene.operationalLayers.add(ArcGISSceneLayer("http://xr002.gis.cn:6080/arcgis/rest/services/Hosted/%E9%92%93%E9%B1%BC%E5%98%B4%E5%80%BE%E6%96%9C/SceneServer"))
        val layer =
            ArcGISSceneLayer("http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer")
//            ArcGISSceneLayer("http://192.168.0.235:6080/arcgis/rest/services/Hosted/%E7%92%A7%E5%B1%B1%E6%A8%A1%E5%9E%8B%E6%A5%BC%E5%AE%874326/SceneServer")
//            ArcGISSceneLayer("http://192.168.0.235:6080/arcgis/rest/services/Hosted/%E9%92%93%E9%B1%BC%E5%98%B4%E5%80%BE%E6%96%9C/SceneServer")
        arcgisScene.operationalLayers.add(layer)

        arcgisScene.operationalLayers.add(
            TdtLayerTool.getTdtLayer(
                TdtLayerTool.ServiceType.VEC_C,
                TdtLayerTool.LayerName.IMAGE_ANNOTATION_CHINESE,
                TdtLayerTool.TiledFormat.TILES,
                arcgisScene.spatialReference
            )
        )
//        imageLayer = TdtLayerTool.getTdtLayer(
//            TdtLayerTool.ServiceType.IMG_W,
//            TdtLayerTool.LayerName.IMAGE,
//            TdtLayerTool.TiledFormat.TILES,
//            spatialReference
//        )
//        imageLabelLayer = TdtLayerTool.getTdtLayer(
//            TdtLayerTool.ServiceType.CIA_W,
//            TdtLayerTool.LayerName.IMAGE_ANNOTATION_CHINESE,
//            TdtLayerTool.TiledFormat.TILES,
//            spatialReference
//        )

//        googleMap =
//            GoogleLayer.getInstance(GoogleLayer.MapType.IMAGE, requireActivity(), spatialReference)

//        map.basemap.baseLayers.add(vectorLayer.apply { name = "矢量地图" })
//        map.basemap.baseLayers.add(vectorLableLayer.apply { name = "矢量标注" })
//        map.basemap.baseLayers.add(googleMap.apply { name = "谷歌影像" })
//        map.basemap.baseLayers.add(imageLayer.apply { name = "天地图影像" })
//        map.basemap.baseLayers.add(imageLabelLayer.apply { name = "影像标注" })

        sceneView.scene = arcgisScene

        layer.addDoneLoadingListener {
            sceneView.setViewpoint(Viewpoint(layer.fullExtent.center, 1000.0))
        }

//        sceneView.setViewpointAsync(
//            Viewpoint(
//                11864933.73932961,
//                3447878.713329921,
//                9000.0
//            ),
//            2f
//        )
//        sceneView.setViewpointAsync(
//            Viewpoint(
//                30.78903630942007,
//                108.60563257382893,
//                9000.0
//            ),
//            2f
//        )

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

    }

    override fun onDestroy() {
        super.onDestroy()
        sceneView?.dispose()
    }

}
