package com.gt.entrypad.module.project.bean

import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.app.CheckBean

data class ProjectListBean(
     var id:String= "",
    var checkInfo: CheckBean? = null,
    var featureLayer: FeatureLayer? = null,
     var drawPath:String = ""
) {

    fun isEdit(): Boolean {
        if (featureLayer == null) {
            return false
        }
        if (checkInfo == null) {
            return true
        }
        if (checkInfo!!.status in arrayOf("0", "1", "2", "3", "5", "6") ) {
            return false
        }
        if (checkInfo!!.status == "4") {
            return true
        }
        return true
    }
}