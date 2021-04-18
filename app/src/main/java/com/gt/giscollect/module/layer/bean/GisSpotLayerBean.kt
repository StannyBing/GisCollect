package com.gt.giscollect.module.layer.bean

import com.esri.arcgisruntime.layers.Layer
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

open  class GisSpotLayerBean(var layer:Layer):MultiItemEntity{
    override fun getItemType(): Int {
        return 1
    }

}