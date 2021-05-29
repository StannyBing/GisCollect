package com.gt.giscollect.module.collect.func.adapter

import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CollectCheckBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter

class CollectListAdapter(dataList: List<CollectCheckBean>) :
    ZXRecyclerQuickAdapter<CollectCheckBean, ZXBaseHolder>(R.layout.item_collect_list, dataList) {

    override fun quickConvert(helper: ZXBaseHolder, bean: CollectCheckBean) {
        val layer = bean.featureLayer
        val name = layer?.name ?: (bean.checkInfo?.getFileName() ?: "")
        val info = if (layer == null) {
            "本机不存在该采集任务"
        } else {
            "共有图斑：${layer.featureTable.totalFeatureCount}条"
        }

        when (bean.checkInfo?.status) {
            "4" -> {//已驳回
                helper.setGone(R.id.tv_collect_note, true)
                helper.setTextColor(
                    R.id.tv_collect_note,
                    ContextCompat.getColor(mContext, R.color.red8e)
                )
                helper.setText(R.id.tv_collect_note, bean.checkInfo?.note ?: "")
                helper.setGone(R.id.tv_delete, true)
                helper.setGone(R.id.tv_upload, true)
            }
            "1" -> {
                helper.setGone(R.id.tv_collect_note, true)
                helper.setTextColor(
                    R.id.tv_collect_note,
                    ContextCompat.getColor(mContext, R.color.colorAccent)
                )
                helper.setText(R.id.tv_collect_note, "已入库")
                helper.setGone(R.id.tv_delete, false)
                helper.setGone(R.id.tv_upload, false)
            }
            "2" -> {
                helper.setGone(R.id.tv_collect_note, true)
                helper.setTextColor(
                    R.id.tv_collect_note,
                    ContextCompat.getColor(mContext, R.color.colorAccent)
                )
                helper.setText(R.id.tv_collect_note, "审核中")
                helper.setGone(R.id.tv_delete, false)
                helper.setGone(R.id.tv_upload, false)
            }
            "3" -> {
                helper.setGone(R.id.tv_collect_note, true)
                helper.setTextColor(
                    R.id.tv_collect_note,
                    ContextCompat.getColor(mContext, R.color.colorAccent)
                )
                helper.setText(R.id.tv_collect_note, "已审核")
                helper.setGone(R.id.tv_delete, false)
                helper.setGone(R.id.tv_upload, false)
            }
            "0", "5", "6" -> {//已上传和驳回后上传
                helper.setGone(R.id.tv_collect_note, true)
                helper.setTextColor(
                    R.id.tv_collect_note,
                    ContextCompat.getColor(mContext, R.color.colorAccent)
                )
                helper.setText(R.id.tv_collect_note, "已上传")
                helper.setGone(R.id.tv_delete, false)
                helper.setGone(R.id.tv_upload, false)
            }
            else -> {//未上传
                helper.setGone(R.id.tv_collect_note, false)
                helper.setText(R.id.tv_collect_note, "")
                helper.setGone(R.id.tv_delete, true)
                helper.setGone(R.id.tv_upload, true)
            }
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
                helper.setText(R.id.tv_collect_info, "共有图斑：${layer.featureTable.totalFeatureCount}条")
            }
        }
    }

}