package com.gt.entrypad.module.project.bean

import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.app.CheckBean
import java.util.*

data class ProjectListBean(
     var id:String= "",
    var checkInfo: CheckBean? = null,
    var featureLayer: FeatureLayer? = null,
     var sketchPath:String=""
) {

}