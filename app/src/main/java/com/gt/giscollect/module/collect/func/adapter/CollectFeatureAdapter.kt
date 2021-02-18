package com.gt.giscollect.module.collect.func.adapter

import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Feature
import com.gt.giscollect.R
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.ZXTimeUtil
import java.text.SimpleDateFormat
import java.util.*

class CollectFeatureAdapter(dataList: List<Feature>) :
    ZXQuickAdapter<Feature, ZXBaseHolder>(R.layout.item_feature_list, dataList) {

    var editable: Boolean = true
    var showName: String = ""

    override fun convert(helper: ZXBaseHolder, item: Feature) {
        if (item is ArcGISFeature) {
            (item as ArcGISFeature).loadAsync()
            item.addDoneLoadingListener {
                setInfo(item, helper)
            }
        } else {
            setInfo(item, helper)
        }
    }

    private fun setInfo(item: Feature, helper: ZXBaseHolder) {
        item.attributes.keys.forEach {
            if (it == showName) {
                helper.setText(
                    R.id.tv_collect_feature_name, if (item.attributes[it] == null) {
                        ""
                    } else if (item.attributes[it] is GregorianCalendar) {
                        ZXTimeUtil.getTime(
                            (item.attributes[it] as GregorianCalendar).timeInMillis,
                            SimpleDateFormat("yyyy/MM/dd")
                        )
                    } else {
                        item.attributes[it].toString()
                    }
                )
                return@forEach
            }
        }
        helper.setGone(R.id.tv_delete, editable)
        helper.setGone(R.id.tv_edit, editable)
    }
}