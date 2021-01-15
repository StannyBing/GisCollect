package com.gt.giscollect.module.collect.func.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CollectBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class CollectSpotAdapter(dataList: List<CollectBean.SpotInfo>) :
    ZXQuickAdapter<CollectBean.SpotInfo, ZXBaseHolder>(R.layout.item_collect_create_spot, dataList) {

    override fun convert(helper: ZXBaseHolder, item: CollectBean.SpotInfo?) {
        if (item != null) {
            helper.setText(R.id.tv_collect_create_spot_name, item.name)
            helper.setText(R.id.tv_collect_create_spot_time, item.createTime)
            if (item.isAddBtn) {
                helper.getView<LinearLayout>(R.id.ll_collect_create_add).visibility = View.VISIBLE
                helper.getView<TextView>(R.id.tv_edit).visibility = View.GONE
                helper.getView<TextView>(R.id.tv_delete).visibility = View.GONE
            } else {
                helper.getView<LinearLayout>(R.id.ll_collect_create_add).visibility = View.GONE
                helper.getView<TextView>(R.id.tv_edit).visibility = View.VISIBLE
                helper.getView<TextView>(R.id.tv_delete).visibility = View.VISIBLE
            }
            helper.setBackgroundRes(
                R.id.iv_collect_create_spot_checked, if (item.isChecked) {
                    R.mipmap.select
                } else {
                    R.mipmap.not_select
                }
            )
        }
    }
}