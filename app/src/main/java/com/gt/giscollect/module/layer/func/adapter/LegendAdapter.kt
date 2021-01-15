package com.gt.giscollect.module.layer.func.adapter

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.gt.giscollect.R
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class LegendAdapter(dataList: List<Layer>) : ZXQuickAdapter<Layer, ZXBaseHolder>(R.layout.item_layer_legend, dataList) {
    override fun convert(helper: ZXBaseHolder, item: Layer?) {
        if (item != null) {
            if (item is FeatureLayer) {
                try {
                    val feature = item.featureTable.createFeature()
                    val symbol = item.renderer.getSymbol(feature)
                    val bitmapListenable = symbol.createSwatchAsync(mContext, ContextCompat.getColor(mContext, R.color.content_bg))
                    try {
                        Glide.with(mContext)
                            .load(bitmapListenable.get())
                            .into(helper.getView(R.id.iv_legend_img))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    helper.setText(R.id.tv_legend_name, item.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}