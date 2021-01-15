package com.gt.giscollect.module.collect.bean

import com.esri.arcgisruntime.layers.FeatureLayer

data class CollectCheckBean(
    var checkInfo: CheckBean? = null,
    var featureLayer: FeatureLayer? = null
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