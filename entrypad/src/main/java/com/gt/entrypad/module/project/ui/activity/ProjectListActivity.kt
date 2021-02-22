package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.gt.base.activity.BaseActivity
import com.gt.base.app.ConstStrings
import com.gt.entrypad.app.RouterPath
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel

import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import com.gt.entrypad.module.project.mvp.model.ProjectListModel
import com.gt.entrypad.module.project.mvp.presenter.ProjectListPresenter
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.views.ZXStatusBarCompat
import com.gt.entrypad.R
import com.gt.entrypad.tool.CopyAssetsToSd
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
@Route(path = RouterPath.PROJECT_LIST)
class ProjectListActivity : BaseActivity<ProjectListPresenter, ProjectListModel>(), ProjectListContract.View {
    companion object {
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
}



