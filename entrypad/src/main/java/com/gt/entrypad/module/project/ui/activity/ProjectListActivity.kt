package com.gt.entrypad.module.project.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.activity.BaseActivity
import com.gt.base.app.ConstStrings
import com.gt.base.bean.NormalList
import com.gt.base.bean.toJson
import com.gt.entrypad.app.RouterPath
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel

import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import com.gt.entrypad.module.project.mvp.model.ProjectListModel
import com.gt.entrypad.module.project.mvp.presenter.ProjectListPresenter
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.views.ZXStatusBarCompat
import com.gt.entrypad.R
import com.gt.entrypad.app.ConstString
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.gt.entrypad.module.project.func.adapter.ProjectListAdapter
import com.gt.base.tool.CopyAssetsToSd
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.util.*
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.activity_project_list.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
@Route(path = RouterPath.PROJECT_LIST)
class ProjectListActivity : BaseActivity<ProjectListPresenter, ProjectListModel>(), ProjectListContract.View {
    private var data = arrayListOf<ProjectListBean>()
    private var projectAdapter = ProjectListAdapter(data)
    companion object {
        private const val ChangeTag = "sketch_list"
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, ProjectListActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun onViewListener() {

    }

	/**
     * layout配置
     */
	override fun getLayoutId(): Int {
        return R.layout.activity_project_list
    }

	/**
     * 初始化
     */
	override fun initView(savedInstanceState: Bundle?) {
        ZXStatusBarCompat.translucent(this)
        ZXStatusBarCompat.setStatusBarLightMode(this)
        leftTv.apply {
           setData(TitleViewViewModel(getString(R.string.createProject)))
           setActionListener(object : ICustomViewActionListener {
               override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                   moduleDialog()
               }

           })
       }

        rightTv.apply {
            setData(TitleViewViewModel(getString(R.string.moduleDownload)))
            this.visibility=View.VISIBLE
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    DrawTemplateDownloadActivity.startAction(this@ProjectListActivity,false)
                }

            })
        }

        toolBarTitleTv.text = getString(R.string.registrationList)

        rvProject.apply {
            layoutManager=LinearLayoutManager(mContext)
            adapter = projectAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }

        ZXRecyclerDeleteHelper(this, rvProject)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                when(id){
                    R.id.tv_delete->{
                        ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否删除该项目，这将同时删除该项目的草图数据？"){ dialog, which ->
                            ConstStrings.sktchId = data[pos].id
                            data[pos].featureLayer?.let {
                                FileUtils.deleteFilesByName(
                                    ConstStrings.getSketchLayersPath(),
                                    it.name
                                )
                                DeleteLayerFileTool.deleteFileByLayer(
                                    ConstStrings.getSketchLayersPath() + it.name + "/file/",
                                    it
                                )
                                FileUtils.deleteFiles(mContext.filesDir.path+"/${ data[pos].id}/sketch/draw.jpg")
                                MapTool.postLayerChange(ChangeTag,it,MapTool.ChangeType.OperationalRemove)
                                val list = mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)
                                list?.remove(ConstStrings.sktchId)
                                mSharedPrefUtil.putList(ConstStrings.SketchIdList,list)
                                data.removeAt(pos)
                                projectAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    R.id.tv_upload->{
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否上传该条任务数据？"
                        ) { dialog, which ->
                         val files = FileUtils.getFilesByName(
                                ConstStrings.getSketchLayersPath(),
                             data[pos].featureLayer?.name?:""
                            )
                            files.firstOrNull { it.isDirectory }?.apply {
                                val path = ZipUtils.zip(path, false)
                                if (path != null) {
                                    mPresenter.uploadSurvey(
                                        path,
                                        data[pos].featureLayer?.name?:"",
                                        "",
                                        "",
                                        collectId = UUID.randomUUID().toString()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            .setClickable { position ->
                ConstString.feature = data[position].featureLayer
                ConstStrings.sktchId = data[position].id
                mSharedPrefUtil.putBool("isEdit",true)
               SketchLoadActivity.startAction(this,false)
            }
        refresh()
        MapTool.registerLayerChange(ChangeTag,object :MapTool.LayerChangeListener{
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                refresh()
            }
        })
        mPresenter.getProject()
    }

    private fun refresh(){
        data.clear()
        mSharedPrefUtil.getMap<String,String>(ConstStrings.SketchIdList)?.apply {
            entries?.forEach {sketch->
                ConstStrings.sktchId=sketch.key
                val file = File(ConstStrings.getSketchLayersPath()+sketch.value)
                if (file.exists() && file.isDirectory) {
                    file.listFiles()?.forEach {
                        if (it.isFile && it.name.endsWith(".gpkg")) {
                            GeoPackageTool.getTablesFromGpkg(it.path){featureTableList->
                                featureTableList.forEach {featureTable->
                                    data.add(ProjectListBean(id=sketch.key,featureLayer = FeatureLayer(featureTable).apply {
                                        name = sketch.value
                                        featureTable.displayName = sketch.value
                                    }))
                                }
                                projectAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 项目列表数据回调
     */
    override fun onProjectList(result: String?) {

    }


    /**
     * 获取模板列表
     */
    private fun moduleDialog(){
       BUIDialog.showSimpleList(mContext,"请选择", arrayListOf<BUIDialog.ListBean>().apply {
           ConstStrings.mGuideBean.getTemplates().forEach {
                add(BUIDialog.ListBean(it.key,it.value.toString()))
           }
       },{
           ConstStrings.drawTempleteName = it.key+".gpkg"
           if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath()+ConstStrings.drawTempleteName)){
                DrawSketchActivity.startAction(this@ProjectListActivity,false,ZXTimeUtil.getTime(System.currentTimeMillis(), SimpleDateFormat("yyyyMMdd_HHmmss")))
            }else{
                showToast("请下载模板")
            }
       },BUIDialog.BtnBuilder().withCancelBtn {  }.withSubmitBtn {  })

    }

    override fun onSurveyUpload(name: String) {
        showToast("上传成功")

    }
}



