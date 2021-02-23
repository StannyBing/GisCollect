package com.gt.entrypad.module.project.func.adapter

import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter

class ProjectListAdapter(dataList: List<ProjectListBean>) : ZXRecyclerQuickAdapter<ProjectListBean, ZXBaseHolder>(R.layout.item_project_list, dataList) {

    override fun quickConvert(helper: ZXBaseHolder, bean: ProjectListBean) {
        val layer = bean.featureLayer
        val name = layer?.name ?: (bean.checkInfo?.getFileName() ?: "")
        val info = if (layer == null) {
            ""
        } else {
            "共有图层：${layer.featureTable.totalFeatureCount}条"
        }
        //但如果该任务本地数据都没有，直接展示下载按钮
        helper.setText(R.id.tv_upload, "上传")
        if (layer == null) {
            helper.setText(R.id.tv_upload, "下载")
            helper.setGone(R.id.tv_delete, false)
            //TODO 暂时删除了下载功能
            helper.setGone(R.id.tv_upload, true)
        }
        if (layer?.featureTable is ServiceFeatureTable) {
            helper.setGone(R.id.tv_upload, false)
            helper.setGone(R.id.tv_delete, false)
        }
        //设置名称和信息
        helper.setText(R.id.tv_collect_name, name)
        helper.setText(R.id.tv_collect_info, info)

        layer?.featureTable?.loadAsync()
        layer?.featureTable?.addDoneLoadingListener {
            val queryGet = layer.featureTable.queryFeaturesAsync(QueryParameters().apply {
                whereClause = "1=1"
                isReturnGeometry = false
            })
            queryGet.addDoneListener {
                helper.setText(R.id.tv_collect_info, "共有图层：${layer.featureTable.totalFeatureCount}条")
            }
        }
    }

}