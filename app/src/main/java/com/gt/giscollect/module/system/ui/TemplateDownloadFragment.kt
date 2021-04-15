package com.gt.giscollect.module.system.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.base.app.TempIdsBean
import com.gt.base.bean.NormalList
import com.gt.base.bean.toJson
import com.gt.giscollect.module.system.bean.TemplateBean
import com.gt.giscollect.module.system.func.adapter.TempalteListAdapter
import com.gt.giscollect.module.system.mvp.contract.TemplateDownloadContract
import com.gt.giscollect.module.system.mvp.model.TemplateDownloadModel
import com.gt.giscollect.module.system.mvp.presenter.TemplateDownloadPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_template_download.*
import org.json.JSONArray
import java.io.File

/**
 * Create By XB
 * 功能：
 */
class TemplateDownloadFragment : BaseFragment<TemplateDownloadPresenter, TemplateDownloadModel>(), TemplateDownloadContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): TemplateDownloadFragment {
            val fragment = TemplateDownloadFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private val templateList = arrayListOf<TemplateBean>()
    private val tempalteAdapter = TempalteListAdapter(templateList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_template_download
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_tempalte_list.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = tempalteAdapter
        }
        mPresenter.getTemplateList(
            hashMapOf(
                "currPage" to 0, "total" to 0, "pageSize" to 999, "filters" to arrayListOf(
                    hashMapOf("col" to "template_id", "op" to "=", "val" to ConstStrings.mGuideBean.getTemplatesFirst())
                )
            ).toJson()
        )
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

        ZXRecyclerDeleteHelper(requireActivity(), rv_tempalte_list)
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
                        if (ZXFileUtil.isFileExists(ConstStrings.getCollectTemplatePath() + fileName)) {
                            ZXFileUtil.deleteFiles(ConstStrings.getCollectTemplatePath() + fileName)
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
//                    ZXDialogUtil.showYesNoDialog(mContext, "提示", "该模板已下载，是否删除本地文件，重新获取？") { dialog, which ->
//                        mPresenter.downloadTemplate(templateList[position])
//                    }
                }
            }
        }
    }

    override fun onTemplateListResult(tempalteList: NormalList<TemplateBean>) {
        this.templateList.clear()
        this.templateList.addAll(tempalteList.rows)
        tempalteAdapter.notifyDataSetChanged()
        refreshList()
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

    /**
     * 更新列表
     */
    private fun refreshList() {
        templateList.forEach {
            try {
                val fileObj = JSONArray(it.fileJson).getJSONObject(0)
                val fileExt = fileObj.getString("fileExt")
                val fileName = it.tplName + "." + fileExt
                it.isDownload = ZXFileUtil.isFileExists(ConstStrings.getCollectTemplatePath() + fileName)
                if (it.isDownload) {
                    val templateIds = arrayListOf<TempIdsBean>()
                    if (mSharedPrefUtil.contains(ConstStrings.TemplateIdList) && !mSharedPrefUtil.getList<TempIdsBean>(
                            ConstStrings.TemplateIdList)
                            .isNullOrEmpty()
                    ) {
                        templateIds.addAll(mSharedPrefUtil.getList(ConstStrings.TemplateIdList) ?: arrayListOf())
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
                    mSharedPrefUtil.putList(ConstStrings.TemplateIdList, templateIds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        tempalteAdapter.notifyDataSetChanged()
    }
}
