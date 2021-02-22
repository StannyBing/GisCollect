package com.gt.giscollect.module.system.ui

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.base.*
import com.gt.giscollect.module.main.func.tool.LayerTool
import com.gt.module_map.tool.MapTool
import com.gt.giscollect.module.system.bean.DataResBean
import com.gt.giscollect.module.system.func.adapter.DataResListAdapter
import com.gt.giscollect.module.system.mvp.contract.DataDownloadContract
import com.gt.giscollect.module.system.mvp.model.DataDownloadModel
import com.gt.giscollect.module.system.mvp.presenter.DataDownloadPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.gt.base.manager.UserManager
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_data_download.*
import org.json.JSONArray
import java.io.File

/**
 * Create By XB
 * 功能：
 */
class DataDownloadFragment : BaseFragment<DataDownloadPresenter, DataDownloadModel>(), DataDownloadContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): DataDownloadFragment {
            val fragment = DataDownloadFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private val dataList = arrayListOf<DataResBean>()
    private val dataAdapter = DataResListAdapter(dataList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_data_download
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_data_list.apply {
            layoutManager = ZXInScrollRecylerManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = dataAdapter
        }
        loadData()
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        ZXRecyclerDeleteHelper(requireActivity() as Activity?, rv_data_list)
            .setSwipeOptionViews(R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->//滑动菜单点击事件
                if (id == R.id.tv_delete) {
                    ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否删除该文件？") { dialog, which ->
                        var fileName = ""
                        try {
                            val fileObj = JSONArray(dataList[pos].fileJson).getJSONObject(0)
                            fileName = dataList[pos].materialName + "." + fileObj.getString("fileExt")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (ZXFileUtil.isFileExists(ConstStrings.getLocalMapPath(fileName) + fileName)) {
                            ZXFileUtil.deleteFiles(ConstStrings.getLocalMapPath(fileName) + fileName)
                        }
                        refreshList()
                    }
                }
            }

        dataAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.iv_data_dowloand) {
                if (!dataList[position].isDownload) {
                    mPresenter.downloadData(dataList[position])
                } else {
//                    ZXDialogUtil.showYesNoDialog(mContext, "提示", "该文件已下载，是否删除本地文件，重新获取？") { dialog, which ->
//                        mPresenter.downloadData(dataList[position])
//                    }
                }
            }
        }

        et_dowmlaod_data_name.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ZXSystemUtil.closeKeybord(requireActivity())
                loadData()
            }
            true
        }
    }

    private fun loadData() {
        mPresenter.getDataList(
            hashMapOf(
                "currPage" to 0, "total" to 0, "pageSize" to 999, "filters" to arrayListOf(
                    hashMapOf("col" to "rn_code", "op" to "like", "val" to UserManager.user?.rnCode),
                    hashMapOf("col" to "data_type", "op" to "like", "val" to "app")
                )
            ).toJson()
        )

    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
    }

    override fun onDataListResult(tempalteList: NormalList<DataResBean>) {
        this.dataList.clear()
        this.dataList.addAll(tempalteList.rows)
        dataAdapter.notifyDataSetChanged()
        refreshList()
    }

    override fun onDataDowmload(file: File) {
        if (file.exists()) {
            MapTool.mapListener?.getMap()?.let {
                LayerTool.addLocalMapLayer(it, file)
            }
            refreshList()
            showToast("下载成功")
        }
    }

    private fun refreshList() {
        dataList.forEach {
            try {
                val fileObj = JSONArray(it.fileJson).getJSONObject(0)
                val fileExt = fileObj.getString("fileExt")
                val fileName = it.materialName + "." + fileExt
                it.isDownload = ZXFileUtil.isFileExists(ConstStrings.getLocalMapPath(fileExt) + fileName)
                if (it.isDownload) {
                    val dataIds = arrayListOf<DataResBean>()
                    if (mSharedPrefUtil.contains(ConstStrings.DataIdList) && mSharedPrefUtil.getList<DataResBean>(
                            ConstStrings.DataIdList)
                            .isNotEmpty()
                    ) {
                        dataIds.addAll(mSharedPrefUtil.getList(ConstStrings.DataIdList))
                    }
                    var isHasData = false
                    dataIds.forEach {
                        if (it.materialName == fileName) {
                            isHasData = true
                            return@forEach
                        }
                    }
                    if (!isHasData) dataIds.add(DataResBean(fileName, it.materialPid, it.catalogId))
                    mSharedPrefUtil.putList(ConstStrings.DataIdList, dataIds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dataAdapter.notifyDataSetChanged()
    }
}
