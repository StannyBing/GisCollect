package com.gt.entrypad.module.project.bean

import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.app.CheckBean

data class ProjectListBean(
    var checkInfo: CheckBean? = null,
    var featureLayer: FeatureLayer? = null,
    var path:String=""
) {

}