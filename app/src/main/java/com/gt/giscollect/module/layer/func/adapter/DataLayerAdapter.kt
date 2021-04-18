package com.gt.giscollect.module.layer.func.adapter

import android.app.ActionBar
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.esri.arcgisruntime.layers.Layer
import com.gt.giscollect.R
import com.gt.giscollect.module.layer.bean.GisLayerBean
import com.gt.giscollect.module.layer.bean.GisSpotLayerBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXMultiItemQuickAdapter
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

class DataLayerAdapter: ZXMultiItemQuickAdapter<MultiItemEntity, ZXBaseHolder>{
    constructor(data:List<MultiItemEntity>):super(data){
        addItemType(0,R.layout.item_layer_parent)
        addItemType(1,R.layout.item_layer_data)
    }

    override fun convert(helper: ZXBaseHolder, item: MultiItemEntity) {
        when(item.itemType){
            0->{
                if (item is GisLayerBean){
                    helper.setText(R.id.tv_item_child_layer_name,item.name)
                    if (item.isExpanded) {
                        helper.setBackgroundRes(R.id.layerMenuItemArrowIv,R.mipmap.arrow_open)
                    } else {
                        helper.setBackgroundRes(R.id.layerMenuItemArrowIv,R.mipmap.arrow_close)
                    }
                }
            }
            1->{
                if (item is GisSpotLayerBean){
                    var layer= item.layer
                    helper.setText(R.id.tv_layer_name, layer.name)
                    helper.setImageResource(R.id.iv_layer_checked, if (layer.isVisible) R.mipmap.select else R.mipmap.not_select)
                    helper.setEnabled(R.id.sb_layer_alpha, layer.isVisible)
                    helper.setText(R.id.tv_layer_alpha, "${(layer.opacity * 100).toInt()}%")
                    helper.getView<SeekBar>(R.id.sb_layer_alpha).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                layer.opacity = progress / 100f
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }
                    })
                    helper.addOnClickListener(R.id.iv_layer_checked)
                    helper.addOnClickListener(R.id.tv_layer_name)
                    if (layer.name in arrayOf("矢量地图", "矢量标注", "天地图影像", "影像标注")) {
                        helper.getView<TextView>(R.id.tv_delete).visibility = View.GONE
                    } else {
                        helper.getView<TextView>(R.id.tv_delete).visibility = View.VISIBLE
                    }
                }
                }
            }
        }
    }
