package com.gt.giscollect.module.collect.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.manager.UserManager
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.base.*
import com.gt.base.app.CheckBean
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.giscollect.module.collect.mvp.contract.CollectListContract
import com.gt.giscollect.module.collect.mvp.model.CollectListModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectListPresenter
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.MapTool
import com.gt.base.app.TempIdsBean
import com.gt.giscollect.module.collect.bean.CollectCheckBean
import com.gt.giscollect.module.collect.func.adapter.SurveyListAdapter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_collect_list.*
import java.io.File

/**
 * Create By XB
 * 功能：采集列表
 */
class SurveyListFragment : BaseFragment<CollectListPresenter, CollectListModel>(),
    CollectListContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SurveyListFragment {
            val fragment = SurveyListFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "survey_list"
    }

    private val totalCheckList = arrayListOf<CollectCheckBean>()
    private val surveyList = arrayListOf<CollectCheckBean>()
    private val surveyAdapter = SurveyListAdapter(surveyList)

    var fragChangeListener: FragChangeListener? = null

    private var editSurveyPosition = 0//当前编辑的采集任务的列表

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {

        rv_collect_layers.apply {
            layoutManager = ZXInScrollRecylerManager(mContext) as RecyclerView.LayoutManager?
            adapter = surveyAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
        refresh()

        ZXRecyclerDeleteHelper(activity, rv_collect_layers)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                val layer = surveyList[pos].featureLayer
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_upload -> {
                        if (MyApplication.isOfflineMode) {
                            showToast("当前为离线模式，无法进行该操作！")
                            return@setSwipeable
                        }
                        if (layer == null) {
                            ZXDialogUtil.showYesNoDialog(
                                mContext,
                                "提示",
                                "是否下载该条采集数据?"
                            ) { dialog, which ->
                                downloadCollect(surveyList[pos])
//                                showToast("正在建设中")
                            }
                            return@setSwipeable
                        }
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否上传该条采集数据？"
                        ) { dialog, which ->
                            val file = FileUtils.getFileByName(
                                ConstStrings.getOperationalLayersPath(),
                                layer.name
                            )
                            if (file?.exists() == true) {
                                val path = ZipUtils.zip(file.path, false)
                                if (path != null) {
                                    var mTempId = surveyList[pos].checkInfo?.templateId ?: ""
                                    var mCataId = surveyList[pos].checkInfo?.catalogId ?: ""
                                    if (mTempId.isEmpty() || mCataId.isEmpty()) {
                                        val templateIds =
                                            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
                                        templateIds?.forEach temp@{ temp ->
                                            if (ConstStrings.bussinessId.contains(temp.templateId)) {
                                                temp.layerNames.forEach {
                                                    if (it == layer.name) {
                                                        mTempId = temp.templateId
                                                        mCataId = temp.catalogId
                                                        return@temp
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (mTempId.isEmpty()) {
                                        mPresenter.uploadCollect(
                                            path,
                                            layer.name,
                                            collectId = surveyList[pos].checkInfo?.collectId ?: ""
                                        )
                                    } else {
                                        mPresenter.uploadCollect(
                                            path,
                                            layer.name,
                                            mTempId,
                                            mCataId,
                                            collectId = surveyList[pos].checkInfo?.collectId ?: ""
                                        )
                                    }
                                }
                            } else {
                                showToast("文件不存在")
                            }
                        }
                    }
                    R.id.tv_delete -> {
                        if (layer == null) return@setSwipeable
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否删除该图层，这将同时删除该图层的相关采集数据？"
                        ) { dialog, which ->
                            FileUtils.deleteFilesByName(
                                ConstStrings.getOperationalLayersPath(),
                                layer.name
                            )
                            DeleteLayerFileTool.deleteFileByLayer(
                                ConstStrings.getOperationalLayersPath() + layer.name + "/file/",
                                layer
                            )
                            MapTool.postLayerChange(
                                ChangeTag,
                                layer,
                                MapTool.ChangeType.OperationalRemove
                            )
                            surveyList.removeAt(pos)
                            surveyAdapter.notifyItemRemoved(pos)
                            surveyAdapter.notifyItemRangeChanged(pos, 5)
                        }
                    }
                }
            }
            .setClickable { position ->
                if (surveyList[position].featureLayer == null) {
                    showToast("本机不存在该采集任务")
                } else {
                    editSurveyPosition = position
                    fragChangeListener?.onFragGoto(
                        SurveyMainFragment.Survey_Feature,
                        surveyList[position].featureLayer to arrayOf(
                            surveyList[position].isEdit(),
                            surveyList[position].checkInfo == null
                        )
                    )
                }
            }

        btn_collect_create.visibility = View.GONE
        super.initView(savedInstanceState)
    }

    private fun downloadCollect(collectCheckBean: CollectCheckBean) {
        collectCheckBean.checkInfo?.let {
            mPresenter.downloadCollect(it)
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //刷新
        sr_collect_layers.setOnRefreshListener {
            refresh()
        }

        //切换
        rg_collect_listtype.setOnCheckedChangeListener { group, checkedId ->
            setCheckList()
        }

        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                refresh()
            }
        })
    }

    /**
     * 刷新列表
     */
    fun refresh() {
        mPresenter.getCheckList(
            hashMapOf(
                "currPage" to 0,
                "pageSize" to 999,
                "filters" to arrayListOf(
                    hashMapOf("col" to "user_id", "op" to "=", "val" to UserManager.user?.userId),
                    hashMapOf("col" to "template_id", "op" to "=", "val" to ConstStrings.bussinessId)
                )
            ).toJson()
        )
    }

    /**
     * 审核反馈
     */
    override fun onCheckListResult(checkList: List<CheckBean>) {
        ConstStrings.checkList.clear()
        ConstStrings.checkList.addAll(checkList)
        totalCheckList.clear()

        val tempCheck = arrayListOf<CheckBean>().apply {
            addAll(checkList)
        }
        //先添加本地的所有图层
        MapTool.mapListener?.getMap()?.operationalLayers?.forEach layer@{ layer ->
            if (layer is FeatureLayer) {
                var bean: CheckBean? = null
                //将所有能与线上对应的layer设置checkBean
                checkList.forEach check@{ check ->
                    if (check.getFileName().replace(".gpkg", "") == layer.name) {
                        bean = check
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tempCheck.removeIf {
                                it.collectId == check.collectId
                            }
                        } else {
                            tempCheck.remove(check)
                        }
                        return@check
                    }
                }
                totalCheckList.add(CollectCheckBean(bean, layer))
            }
        }
        //将本地没有保存的checkBean添加进
        tempCheck.forEach check@{ check ->
            totalCheckList.add(CollectCheckBean(check, null))
        }

        setCheckList()

        sr_collect_layers.isRefreshing = false
    }

    private fun setCheckList() {
        surveyList.clear()
        surveyList.addAll(if (rb_collect_listlocal.isChecked) {
            totalCheckList.filter {
                it.checkInfo == null
            }
        } else if (rb_collect_listnet.isChecked) {
            totalCheckList.filter {
                it.checkInfo != null
            }
        } else {
            totalCheckList
        })
        surveyAdapter.notifyDataSetChanged()
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
    }

    override fun onCollectDownload(file: File) {
        if (file.exists()) {
            val geoPackage = GeoPackage(file.path)
            geoPackage.loadAsync()
            geoPackage.addDoneLoadingListener {
                if (geoPackage.loadStatus == LoadStatus.LOADED) {
                    val geoTables = geoPackage.geoPackageFeatureTables
                    geoTables.forEach { table ->
                        val featureLayer = FeatureLayer(table)
                        featureLayer.loadAsync()
                        featureLayer.addDoneLoadingListener {
                            featureLayer.name = file.name.substring(0, file.name.lastIndexOf("."))

                            MapTool.mapListener?.getMap()?.operationalLayers?.add(featureLayer)
                        }
                    }
                }
            }

            refresh()
            showToast("下载成功")
        }
    }

    /**
     * 上传成功
     */
    override fun onCollectUpload(name: String) {
        refresh()
        showToast("上传成功")
    }
}
