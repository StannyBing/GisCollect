package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.base.activity.BaseActivity
import com.gt.base.app.ConstStrings
import com.gt.base.app.TempIdsBean
import com.gt.base.bean.NormalList
import com.gt.base.bean.toJson
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.func.adapter.DrawTemplateListAdapter
import com.gt.entrypad.module.project.mvp.contract.DrawTemplateContract
import com.gt.entrypad.module.project.mvp.model.DrawTemplateModel
import com.gt.entrypad.module.project.mvp.presenter.DrawTemplatePresenter
import com.gt.entrypad.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.activity_draw_templete.*
import org.json.JSONArray
import java.io.File

class DrawTemplateDownloadActivity :BaseActivity<DrawTemplatePresenter,DrawTemplateModel>(),DrawTemplateContract.View{
    private val templateList = arrayListOf<DrawTemplateBean>()
    private val tempalteAdapter = DrawTemplateListAdapter(templateList)
    private var page=0
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, DrawTemplateDownloadActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rv_tempalte_list.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = tempalteAdapter
        }
        drawTemplateList()
    }

    private fun  drawTemplateList(){
        ConstStrings.mGuideBean.getTemplates().forEach {
            templateList.add(DrawTemplateBean(tplName = it.key,templateId = it.value.toString()))
            downLoad(it.value.toString())
        }
    }

    override fun onViewListener() {
        iv_setting_title_back.setOnClickListener {
            finish()
        }
        ZXRecyclerDeleteHelper(this, rv_tempalte_list)
            .setSwipeOptionViews(R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                //滑动菜单点击事件
                if (id == R.id.tv_delete) {
                    ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否删除该文件？") { dialog, which ->
                        var fileName = ""
                        try {
                            val fileObj = JSONArray(templateList[pos].fileJson).getJSONObject(0)
                            fileName = templateList[pos].tplName + "." + fileObj.getString("fileExt")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + fileName)) {
                            ZXFileUtil.deleteFiles(ConstStrings.getDrawTemplatePath() + fileName)
                        }
                        refreshList()
                    }
                }
            }

        tempalteAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.iv_data_dowloand) {
                if (!templateList[position].isDownload) {
                    mPresenter.downloadTemplate(templateList[position])
                } else {

                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_draw_templete
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
    }

    override fun onTemplateDowmload(file: File) {
        if (file.exists()) {
            refreshList()
            showToast("下载成功")
        }
    }

    override fun onTemplateListResult(tempalteList: NormalList<DrawTemplateBean>) {
        if (page==0)this.templateList.clear()
        page++
        this.templateList.addAll(tempalteList.rows)
        tempalteAdapter.notifyDataSetChanged()
        refreshList()
    }
    /**
     * 更新列表
     */
    private fun refreshList() {
        templateList.forEach {
            try {
                val fileObj = JSONArray(it.fileJson).getJSONObject(0)
                val fileExt = fileObj.getString("fileExt")
                val fileName = it.tplName + "." + fileExt
                it.isDownload = ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + fileName)
                if (it.isDownload) {
                    val templateIds = arrayListOf<TempIdsBean>()
                    if (mSharedPrefUtil.contains(ConstStrings.DrawTemplateIdList) && !mSharedPrefUtil.getList<TempIdsBean>(
                            ConstStrings.DrawTemplateIdList)
                            .isNullOrEmpty()
                    ) {
                        templateIds.addAll(mSharedPrefUtil.getList(ConstStrings.DrawTemplateIdList) ?: arrayListOf())
                    }
                    var isHasTemplate = false
                    templateIds.forEach {
                        if (it.name == fileName) {
                            isHasTemplate = true
                            return@forEach
                        }
                    }
                    if (!isHasTemplate) templateIds.add(
                        TempIdsBean(
                            fileName,
                            it.templateId,
                            it.catalogId
                        )
                    )
                    mSharedPrefUtil.putList(ConstStrings.DrawTemplateIdList, templateIds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        tempalteAdapter.notifyDataSetChanged()
    }



    private fun downLoad(templateId:String){
        mPresenter.getTemplateList(
            hashMapOf(
                "currPage" to 0, "total" to 0, "pageSize" to 999, "filters" to arrayListOf(
                    hashMapOf("col" to "template_id", "op" to "=", "val" to templateId)
                )
            ).toJson()
        )
    }
}