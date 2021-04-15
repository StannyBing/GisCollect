package com.gt.entrypad.module.project.func.adapter


import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import org.json.JSONArray

class DrawTemplateListAdapter(dataList: List<DrawTemplateBean>) : ZXQuickAdapter<DrawTemplateBean, ZXBaseHolder>(
    R.layout.item_draw_template_layout, dataList) {
    override fun convert(helper: ZXBaseHolder, item: DrawTemplateBean) {
        helper.setText(R.id.tv_data_name, item.tplName)
        if (!item.fileJson.isNullOrEmpty()){
            var fileSize = 0L
            try {
                val fileObj = JSONArray(item.fileJson).getJSONObject(0)
                fileSize = fileObj.getLong("fileSize")
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
            helper.setText(R.id.tv_data_info, "${item.rnName} - ${item.maYear}${fileSizeInfo}")
        }
        helper.setGone(R.id.iv_data_dowloand, !item.isDownload)
        helper.setGone(R.id.tv_delete, item.isDownload)
        helper.addOnClickListener(R.id.iv_data_dowloand)
    }

}