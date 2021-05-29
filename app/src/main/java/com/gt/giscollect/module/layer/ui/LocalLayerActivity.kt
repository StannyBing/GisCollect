package com.gt.giscollect.module.layer.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R

import com.gt.giscollect.module.layer.mvp.contract.LocalLayerContract
import com.gt.giscollect.module.layer.mvp.model.LocalLayerModel
import com.gt.giscollect.module.layer.mvp.presenter.LocalLayerPresenter


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class LocalLayerActivity : BaseActivity<LocalLayerPresenter, LocalLayerModel>(),
    LocalLayerContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, LocalLayerActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_local_layer
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

    }

}
