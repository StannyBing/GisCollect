package com.gt.giscollect.module.collect.func.adapter

import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CheckBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import org.json.JSONObject
import java.lang.Exception

class CollectAdapter(dataList: List<FeatureLayer>) :
    ZXQuickAdapter<FeatureLayer, ZXBaseHolder>(R.layout.item_collect_list, dataList) {

    var checkList = arrayListOf<CheckBean>()
    var updateList = arrayListOf<String>()

    override fun convert(helper: ZXBaseHolder, layer: FeatureLayer) {
        helper.setText(R.id.tv_collect_name, layer.name)
        helper.setText(R.id.tv_collect_info, "共有图斑：${layer.featureTable.totalFeatureCount}条")
        var checkBean: CheckBean? = null
        try {
            checkList.forEach {
                if (it.getFileName().contains(layer.name)) {
                    checkBean = it
                    return@forEach
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (updateList.contains(layer.name)) {
            helper.setGone(R.id.tv_delete, false)
            helper.setGone(R.id.tv_upload, false)
            helper.setGone(R.id.tv_collect_note, true)
            helper.setText(R.id.tv_collect_note, "已上传")
            helper.setTextColor(
                R.id.tv_collect_note,
                ContextCompat.getColor(mContext, R.color.colorAccent)
            )
        } else {
            helper.setGone(R.id.tv_delete, true)
            helper.setGone(R.id.tv_upload, true)
            helper.setGone(R.id.tv_collect_note, false)
        }

        checkBean?.apply {
            helper.setGone(R.id.tv_collect_note, true)
            helper.setText(R.id.tv_collect_note, note)
            helper.setTextColor(
                R.id.tv_collect_note,
                ContextCompat.getColor(mContext, R.color.red8e)
            )
        }
    }

}