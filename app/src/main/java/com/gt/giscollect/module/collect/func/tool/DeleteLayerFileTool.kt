package com.gt.giscollect.module.collect.func.tool

import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.module.main.func.tool.FileUtils

object DeleteLayerFileTool {

    fun deleteFileByFeature(parentPath: String, feature: Feature) {
        feature.attributes.keys.forEach {
            try {
                if (it in arrayOf("camera", "video", "record","CAMERA", "VIDEO", "RECORD")) {
                    val paths = feature.attributes[it].toString().split(ConstStrings.File_Split_Char)
                    paths.forEach {
                        if (it.isNotEmpty()) {
                            FileUtils.deleteFiles(parentPath + it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFileByLayer(parentPath: String, layer: FeatureLayer) {
        if (layer.featureTable.totalFeatureCount > 0) {
            layer.featureTable.queryFeaturesAsync(QueryParameters()).addDoneListener {
                val list = layer.featureTable.queryFeaturesAsync(QueryParameters()).get()
                list.forEach {
                    deleteFileByFeature(parentPath, it)
                }
            }
        }
    }

}