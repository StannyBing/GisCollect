package com.gt.giscollect.module.layer.bean

import com.esri.arcgisruntime.layers.Layer
import com.zx.zxutils.other.QuickAdapter.entity.AbstractExpandableItem
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity
import java.io.Serializable

data class GisLayerBean(var name:String,var sseq:Int=0):Serializable,MultiItemEntity,AbstractExpandableItem<GisSpotLayerBean>(){
    override fun getItemType(): Int {
        return 0
    }

    override fun getLevel(): Int {
        return 1
    }

}