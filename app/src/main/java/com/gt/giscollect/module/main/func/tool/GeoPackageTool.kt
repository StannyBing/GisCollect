package com.gt.giscollect.module.main.func.tool

import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.symbology.Renderer
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.gt.giscollect.app.ConstStrings
import com.zx.zxutils.util.ZXLogUtil
import java.io.File

object GeoPackageTool {

    fun getTablesFromGpkg(path: String, layerCall: (List<FeatureTable>) -> Unit) {
        val geoPackage = getGpkg(path)
        geoPackage?.loadAsync()
        geoPackage?.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                val geoTables = geoPackage.geoPackageFeatureTables
                layerCall(geoTables)
            }
        }
    }

    fun getFeatureFromGpkg(name: String, featureCall: (FeatureLayer) -> Unit) {
        getFeatureFromGpkgWithNull(name) {
            it?.let {
                featureCall(it)
            }
        }
    }

    fun getFeatureFromGpkgWithNull(name: String, featureCall: (FeatureLayer?) -> Unit) {
        val geoPackage = getGpkg(ConstStrings.getInnerLocalMapPath())
        if (geoPackage == null){
            featureCall(null)
            return
        }
        geoPackage.loadAsync()
        geoPackage.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                val gepTables = geoPackage.geoPackageFeatureTables
                gepTables.forEach {
                    if (it != null) {
                        val featureLayer = FeatureLayer(it)
//                        UniqueValueRenderer.fromJson()
//                        featureLayer.renderer = Renderer.fromJson()
                        if (name == featureLayer.name) {
                            featureLayer.loadAsync()
                            featureLayer.addDoneLoadingListener {
                                featureCall(featureLayer)
//                                return@forEach
                            }
                        }
                    }
                }
            }
        }
    }

    val shipList = arrayListOf(
        QueryParameters.SpatialRelationship.CROSSES,
        QueryParameters.SpatialRelationship.CONTAINS,
        QueryParameters.SpatialRelationship.ENVELOPE_INTERSECTS,
        QueryParameters.SpatialRelationship.EQUALS,
        QueryParameters.SpatialRelationship.INDEX_INTERSECTS,
        QueryParameters.SpatialRelationship.INTERSECTS,
        QueryParameters.SpatialRelationship.OVERLAPS,
        QueryParameters.SpatialRelationship.RELATE,
        QueryParameters.SpatialRelationship.UNKNOWN,
        QueryParameters.SpatialRelationship.WITHIN
    )

    var shipIndex = 0

    fun getIdentifyFromGpkg(point: Point, featureCall: (List<Feature>) -> Unit) {
        val geoPackage = getGpkg(ConstStrings.getInnerLocalMapPath())
        val features = arrayListOf<Feature>()
        geoPackage?.loadAsync()
        geoPackage?.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                if (geoPackage.geoPackageFeatureTables.isEmpty()) {
                    featureCall(features)
                }
                geoPackage.geoPackageFeatureTables.forEachIndexed { index, it ->
                    if (it != null) {
                        MapTool.mapListener?.getMap()?.basemap?.baseLayers?.forEach { layer ->
                            if (layer is ArcGISVectorTiledLayer && layer.name == it.tableName && layer.isVisible) {
                                val listenable =
                                    FeatureLayer(it).featureTable.queryFeaturesAsync(QueryParameters().apply {
                                        geometry = point
                                        spatialRelationship =
                                            QueryParameters.SpatialRelationship.INTERSECTS
                                    })
//                                if (listenable.get().toList().isNotEmpty()) {
//                                    features.add(listenable.get().first())
//                                }
                                features.addAll(listenable.get())
                            }
                        }
                    }
                }
                featureCall(features)
            }
        }
    }

    private fun getGpkg(path: String = ConstStrings.getLocalMapPath()): GeoPackage? {
        val file = File(path)
        if (file.exists() && file.isDirectory && file.listFiles().isNotEmpty()) {
            file.listFiles().forEach {
                if (it.isFile && it.name.endsWith("gpkg")) {
                    return GeoPackage(it.path)
                }
            }
        } else if (file.exists() && file.isFile && file.name.endsWith(".gpkg")) {
            return GeoPackage((path))
        }
        return null
    }

}