package com.gt.module_map.tool

import com.esri.arcgisruntime.layers.Layer
import com.gt.module_map.listener.MapListener


object MapTool {

    var mapListener: MapListener? = null

    private val layerChangeMap = linkedMapOf<String, LayerChangeListener>()

    enum class ChangeType {
        OperationalAdd,
        OperationalRemove,
        BaseAdd,
        BaseRemove
    }

    fun reset() {
        mapListener = null
        layerChangeMap.clear()
    }

    fun registerLayerChange(tag: String, changeListener: LayerChangeListener) {
        layerChangeMap[tag] = changeListener
    }

    fun postLayerChange(tag: String, layer: Layer, type: ChangeType) {
        layerChangeMap.keys.forEach {
            if (tag != it) {
                layerChangeMap[it]?.onLayerChange(layer, type)
            }
        }
    }

    interface LayerChangeListener {
        fun onLayerChange(layer: Layer, type: ChangeType)
    }
}