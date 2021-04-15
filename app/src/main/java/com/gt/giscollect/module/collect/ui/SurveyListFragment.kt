package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.data.FeatureTable
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
import com.gt.base.bean.NormalList
import com.gt.base.bean.toJson
import com.gt.giscollect.app.MyApplication
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.MapTool
import com.gt.base.tool.MyUtil
import com.gt.giscollect.module.collect.func.adapter.SurveyListAdapter
import com.gt.giscollect.module.collect.mvp.contract.SurveyListContract
import com.gt.giscollect.module.collect.mvp.model.SurveyListModel
import com.gt.giscollect.module.collect.mvp.presenter.SurveyListPresenter
import com.gt.giscollect.module.system.bean.DataResBean
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_collect_list.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Create By XB
 * 功能：采集列表
 */
class SurveyListFragment : BaseFragment<SurveyListPresenter, SurveyListModel>(),
    SurveyListContract.View {
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

    private val surveyList = arrayListOf<DataResBean>()
    private val surveyAdapter = SurveyListAdapter(surveyList)

    var fragChangeListener: FragChangeListener? = null

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

        rg_collect_listtype.visibility = View.GONE
        btn_collect_create.visibility = View.GONE

        rv_collect_layers.apply {
            layoutManager = LinearLayoutManager(mContext) as RecyclerView.LayoutManager?
            adapter = surveyAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
        refresh()

        ZXRecyclerDeleteHelper(activity, rv_collect_layers)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_download)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                if (MyApplication.isOfflineMode) {
                    showToast("当前为离线模式，无法进行该操作！")
                    return@setSwipeable
                }
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_download -> {
                        if (surveyList[pos].isDownload){
                            ZXDialogUtil.showYesNoDialog(
                                mContext,
                                "提示",
                                "是否删除该条任务数据?"
                            ) { dialog, which ->
                                FileUtils.deleteFilesByName(   ConstStrings.getSurveyPath(),
                                    surveyList[pos].materialName)
                                surveyList[pos].isDownload = false
                                surveyAdapter.notifyDataSetChanged()
                                }
                        }else{
                            ZXDialogUtil.showYesNoDialog(
                                mContext,
                                "提示",
                                "是否下载该条任务数据?"
                            ) { dialog, which ->
                                mPresenter.downloadSurvey(surveyList[pos])
//                                showToast("正在建设中")
                            }
                        }
                        return@setSwipeable
                    }
                    R.id.tv_upload -> {
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否上传该条任务数据？"
                        ) { dialog, which ->
                            val files = FileUtils.getFilesByName(
                                ConstStrings.getSurveyPath(),
                                surveyList[pos].materialName
                            )
                            files.firstOrNull { it.isDirectory }?.apply {
                                val path = ZipUtils.zip(path, false)
                                if (path != null) {
                                    mPresenter.uploadSurvey(
                                        path,
                                        surveyList[pos].materialName,
                                        surveyList[pos].templateid,
                                        surveyList[pos].catalogId,
                                        collectId = UUID.randomUUID().toString()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            .setClickable { position ->
                if (!surveyList[position].isDownload) {
                    showToast("该任务暂未下载，请下载后编辑")
                } else {
                    val files = FileUtils.getFilesByName(
                        ConstStrings.getSurveyPath(),
                        surveyList[position].materialName
                    )

                    files.firstOrNull { it.isFile }?.apply {

                        if (exists()) {
                            val geoPackage = GeoPackage(path)
                            geoPackage.loadAsync()
                            geoPackage.addDoneLoadingListener {
                                if (geoPackage.loadStatus == LoadStatus.LOADED) {
                                    val geoTables = geoPackage.geoPackageFeatureTables
                                    geoTables.forEach { table ->
                                        val featureLayer = FeatureLayer(table as FeatureTable?)
                                        featureLayer.loadAsync()
                                        featureLayer.addDoneLoadingListener {

                                            fragChangeListener?.onFragGoto(
                                                SurveyMainFragment.Survey_Feature,
                                                featureLayer to arrayOf(
                                                    true,
                                                    false
                                                )
                                            )

                                            featureLayer.name =
                                                name.substring(0, name.lastIndexOf("."))

                                            MapTool.mapListener?.getMap()?.operationalLayers?.add(
                                                featureLayer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        btn_collect_create.visibility = View.GONE
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //刷新
        sr_collect_layers.setOnRefreshListener {
            refresh()
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
        mPresenter.getSurveyDataList(
            hashMapOf(
                "currPage" to 0,
                "pageSize" to 999,
                "filters" to arrayListOf(
                    hashMapOf(
                        "col" to "rn_code",
                        "op" to "like",
                        "val" to UserManager.user?.rnCode
                    ),
                    hashMapOf("col" to "data_type", "op" to "like", "val" to "task")
                )
            ).toJson()
        )
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
    }

    override fun onSurveyDownload(file: File) {
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
    override fun onSurveyUpload(name: String) {
        refresh()
        showToast("上传成功")
    }

    override fun onSurveyListResult(tempalteList: NormalList<DataResBean>) {
        //获取templateid
        this.surveyList.clear()
        tempalteList.rows.apply {
            forEach {
                it.templateid=getTemplateId()
                surveyList.add(it)
                try {
                    val fileObj = JSONArray(it.fileJson).getJSONObject(0)
                    val fileExt = fileObj.getString("fileExt")
//                    val fileName = it.materialName + "." + fileExt
                    it.isDownload =
                        ZXFileUtil.isFileExists(ConstStrings.getSurveyPath() + it.materialName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        surveyAdapter.notifyDataSetChanged()
        sr_collect_layers.isRefreshing = false
    }

    private fun getTemplateId():String{
        var templateId=""
        mSharedPrefUtil.getString("fieldShow")?.let {
            if (it.isNotEmpty()){
                val jsonToLinkedHashMap = MyUtil.jsonToLinkedHashMap(JSONObject(it))
               jsonToLinkedHashMap.entries.forEach {
                    if (it.key=="房屋调查"){
                        MyUtil.jsonToLinkedHashMap(JSONObject(it.value)).entries.forEach temp@{
                            if (it.key=="templateid"){
                                templateId = it.value
                                return@temp
                            }
                        }
                    }
                }
            }
        }
        return templateId
    }
}
