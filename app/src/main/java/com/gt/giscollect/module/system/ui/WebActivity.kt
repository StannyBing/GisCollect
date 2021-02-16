package com.gt.giscollect.module.system.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R

import com.gt.giscollect.module.system.mvp.contract.WebContract
import com.gt.giscollect.module.system.mvp.model.WebModel
import com.gt.giscollect.module.system.mvp.presenter.WebPresenter


/**
 * Create By XB
 * 功能：
 */
class WebActivity : BaseActivity<WebPresenter, WebModel>(), WebContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, WebActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_web
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
