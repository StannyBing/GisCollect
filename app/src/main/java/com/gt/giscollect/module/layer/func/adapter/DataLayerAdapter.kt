package com.gt.giscollect.module.layer.func.adapter

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.esri.arcgisruntime.layers.Layer
import com.gt.giscollect.R
import com.gt.giscollect.module.layer.bean.LayerBean
import com.gt.giscollect.module.main.func.tool.MapTool
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class DataLayerAdapter
    (dataList: List<Layer>) : ZXQuickAdapter<Layer, ZXBaseHolder>(R.layout.item_layer_data, dataList) {

    override fun convert(helper: ZXBaseHolder, item: Layer?) {
        if (item != null) {
            helper.setText(R.id.tv_layer_name, item.name)
            helper.setImageResource(R.id.iv_layer_checked, if (item.isVisible) R.mipmap.select else R.mipmap.not_select)
            helper.setEnabled(R.id.sb_layer_alpha, item.isVisible)
            helper.setText(R.id.tv_layer_alpha, "${(item.opacity * 100).toInt()}%")
            helper.getView<SeekBar>(R.id.sb_layer_alpha).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        item.opacity = progress / 100f
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            helper.addOnClickListener(R.id.iv_layer_checked)
            helper.addOnClickListener(R.id.tv_layer_name)
            if (item.name in arrayOf("矢量地图", "矢量标注", "天地图影像", "影像标注")) {
                helper.getView<TextView>(R.id.tv_delete).visibility = View.GONE
            } else {
                helper.getView<TextView>(R.id.tv_delete).visibility = View.VISIBLE
            }
        }
    }
}