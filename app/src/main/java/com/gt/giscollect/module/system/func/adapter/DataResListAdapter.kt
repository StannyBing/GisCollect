package com.gt.giscollect.module.system.func.adapter

import com.gt.giscollect.R
import com.gt.giscollect.module.system.bean.DataResBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import org.json.JSONArray

class DataResListAdapter(dataList: List<DataResBean>) : ZXQuickAdapter<DataResBean, ZXBaseHolder>(R.layout.item_res_down_list, dataList) {
    override fun convert(helper: ZXBaseHolder, item: DataResBean) {
        helper.setText(R.id.tv_data_name, item.materialName)
        var fileSize = 0L
        var fileExt = ""
        try {
            val fileObj = JSONArray(item.fileJson).getJSONObject(0)
            fileSize = fileObj.getLong("fileSize")
            fileExt = fileObj.getString("fileExt")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fileSizeInfo = if (fileSize == 0L) {
            ""
        } else if (fileSize < 1000 * 1024) {
            " - " + String.format("%.2f", fileSize / 1000f) + "KB"
        } else {
            " - " + String.format("%.2f", fileSize / 1000f / 1024f) + "M"
        }
        helper.setText(R.id.tv_data_info, "${fileExt}-${item.maYear}${fileSizeInfo}")
        helper.setGone(R.id.iv_data_dowloand, !item.isDownload)
        helper.setGone(R.id.tv_delete, item.isDownload)
        helper.addOnClickListener(R.id.iv_data_dowloand)
    }

}