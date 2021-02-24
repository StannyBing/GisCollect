package com.gt.entrypad.module.project.ui.activity

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
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.gt.entrypad.module.project.func.adapter.ProjectListAdapter
import com.gt.entrypad.tool.CopyAssetsToSd
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import com.zx.zxutils.util.ZXToastUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import com.zx.zxutils.views.SwipeRecylerView.ZXSRListener
import kotlinx.android.synthetic.main.activity_project_list.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File

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
                   DrawSketchActivity.startAction(this@ProjectListActivity,false, arrayListOf(), arrayListOf())
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
        }

        ZXRecyclerDeleteHelper(this, rvProject)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                when(id){
                    R.id.tv_delete->{
                        ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否删除该项目，这将同时删除该项目的草图数据？"){ dialog, which ->
                            data[pos].featureLayer?.let {
                                FileUtils.deleteFilesByName(
                                    ConstStrings.getSketchLayersPath(),
                                    it.name
                                )
                                DeleteLayerFileTool.deleteFileByLayer(
                                    ConstStrings.getSketchLayersPath() + it.name + "/file/",
                                    it
                                )
                                data.removeAt(pos)
                                projectAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    R.id.tv_upload->{

                    }
                }
            }
            .setClickable { position ->
                ConstString.feature = data[position].featureLayer
                mSharedPrefUtil.putBool("isEdit",true)
               SketchLoadActivity.startAction(this,false)
            }
        refresh()
    }
    /**
     * 模板下载
     */
    private fun downloadModule(){
        if (!ZXFileUtil.isFileExists("${ConstStrings.getSketchTemplatePath()}jungong.gpkg")){
            showLoading("模板下载中...")
            CopyAssetsToSd.copy(mContext,"jungong.gpkg", ConstStrings.getSketchTemplatePath(),"jungong.gpkg")
            dismissLoading()
        }
    }

    private fun refresh(){
        mSharedPrefUtil.getList<String>("sketchId")?.apply {
            forEach {
                ConstStrings.sktchId=it
                val file = File(ConstStrings.getSketchLayersPath())
                if (file.exists() && file.isDirectory) {
                    file.listFiles()?.forEach {
                        if (it.isFile && it.name.endsWith(".gpkg")) {
                            GeoPackageTool.getTablesFromGpkg(it.path){featureTableList->
                                featureTableList.forEach {featureTable->
                                    data.add(ProjectListBean(featureLayer = FeatureLayer(featureTable)))
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



