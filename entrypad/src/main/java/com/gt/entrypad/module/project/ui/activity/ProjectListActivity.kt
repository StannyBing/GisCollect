package com.gt.entrypad.module.project.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.activity.BaseActivity
import com.gt.base.app.ConstStrings
import com.gt.base.app.TempIdsBean
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
import com.gt.entrypad.module.project.ui.view.photoView.PhotoViewViewModel
import com.gt.entrypad.tool.CopyAssetsToSd
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.util.*
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import com.zx.zxutils.views.SwipeRecylerView.ZXSRListener
import kotlinx.android.synthetic.main.activity_project_list.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.typeOf

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
                   DrawSketchActivity.startAction(this@ProjectListActivity,false,ZXTimeUtil.getTime(System.currentTimeMillis(), SimpleDateFormat("yyyyMMdd_HHmmss")))
               }

           })
       }
        toolBarTitleTv.text = getString(R.string.registrationList)
       getPermission(arrayOf()){
            downloadModule()
       }
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
                        Log.e("sketch",data[pos].sketchPath)
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
    }
    /**
     * 模板下载
     */
    private fun downloadModule(){
        if (!ZXFileUtil.isFileExists("${ConstStrings.getSketchTemplatePath()}竣工验收.gpkg")){
            showLoading("模板下载中...")
            CopyAssetsToSd.copy(mContext,"jungong.gpkg", ConstStrings.getSketchTemplatePath(),"竣工验收.gpkg")
            dismissLoading()
        }
    }

    private fun refresh(){
        data.clear()
        mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)?.apply {
            forEach {projectId->
            ConstStrings.sktchId = projectId
                var filePath = mContext.filesDir.path+"/sketch/draw.jpg"
                var toPath = mContext.filesDir.path+"/$projectId/sketch/draw.jpg"
                toPath = if (ZXFileUtil.isFileExists(toPath)){
                     ZXFileUtil.copyFile(filePath, toPath)?.path?:""
                } else toPath
                if (!projectId.isNullOrEmpty()){   val file = File(ConstStrings.getSketchLayersPath())
                 if (file.exists() && file.isDirectory) {
                     file.listFiles()?.forEach {
                         if (it.isFile && it.name.endsWith(".gpkg")) {
                             GeoPackageTool.getTablesFromGpkg(it.path){featureTableList->
                                 featureTableList.forEach {featureTable->
                                     data.add(ProjectListBean(id=projectId,featureLayer = FeatureLayer(featureTable).apply {
                                         name = projectId
                                         featureTable.displayName = projectId
                                     },sketchPath =toPath))
                                 }
                                 projectAdapter.notifyDataSetChanged()
                             }
                         }
                     }
                 }
             }
            }
        }
    }

    /**
     * 下载成功接口回调
     */
    override fun onFileDownloadResult(file: File) {
        ResultShowActivity.startAction(this,false,file.absolutePath)
    }

    /**
     * 上传接口回调
     */
    override fun uploadResult(uploadResult: HouseTableBean?) {
        uploadResult?.let {
            mPresenter.downloadFile("房屋勘查表.docx","/office/word/downloadReport?fileName=${it.fileName}&filePath=${it.localUri}")
        }
    }
    /**
     * 上传填写信息
     */
    private fun uploadInfo(tplName:String){
        getPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ){
            var fileData = arrayListOf<String>()
            val photoList = intent?.getSerializableExtra("photoData") as ArrayList<PhotoViewViewModel>
            var infoData = if (intent.hasExtra("infoData")) intent.getSerializableExtra("infoData") as ArrayList<String> else arrayListOf()
            //获取文件信息
            photoList.forEach {
                fileData.add(it.url)
            }
            //获取草图
            val sketch = mContext.filesDir.path + "/sketch/draw.jpg"
            if (ZXFileUtil.isFileExists(sketch)) fileData.add(sketch)
            mPresenter.uploadInfo(infoData,fileData,tplName)
        }
    }
}



