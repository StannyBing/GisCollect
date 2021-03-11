package com.gt.giscollect.module.collect.func.adapter

import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CollectCheckBean
import com.gt.giscollect.module.system.bean.DataResBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SurveyListAdapter(dataList: List<DataResBean>) :
    ZXQuickAdapter<DataResBean, ZXBaseHolder>(R.layout.item_survey_list, dataList) {

    override fun convert(helper: ZXBaseHolder, bean: DataResBean) {
        //设置名称和信息
        helper.setText(R.id.tv_collect_name, bean.materialName)
        helper.setText(
            R.id.tv_collect_info, if (bean.isDownload) {
                "已下载"
            } else {
                "未下载"
            }
        )
        helper.setGone(R.id.tv_download, !bean.isDownload)
        helper.setGone(R.id.tv_upload, bean.isDownload)
    }

}