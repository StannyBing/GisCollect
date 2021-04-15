package com.gt.giscollect.module.system.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.base.app.AppInfoManager
import com.gt.base.manager.UserBean
import com.gt.base.bean.toJson
import com.gt.giscollect.module.system.func.tool.NetworkUtils

import com.gt.giscollect.module.system.mvp.contract.SplashContract
import com.gt.giscollect.module.system.mvp.model.SplashModel
import com.gt.giscollect.module.system.mvp.presenter.SplashPresenter
import com.gt.base.manager.UserManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXUniqueIdUtil
import com.zx.zxutils.views.ZXStatusBarCompat


/**
 * Create By XB
 * 功能：欢迎页
 */
class SplashActivity : BaseActivity<SplashPresenter, SplashModel>(), SplashContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, SplashActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZXStatusBarCompat.setStatusBarDarkMode(this)
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        ConstStrings.Cookie = ""
        if (!NetworkUtils.isNetworkConnected(mContext) && UserManager.userName.isNotEmpty() && UserManager.passWord.isNotEmpty()) {//无网络情况，并且已自动登录
            ZXDialogUtil.showYesNoDialog(
                mContext,
                "提示",
                "当前未连接到网络，是否使用离线模式登入？",
                "离线进入",
                "返回登录", { _, _ ->
                    GuideActivity.startAction(this, true)
                    MyApplication.isOfflineMode = true
                }, { _, _ ->
                    onLoginResult(null)
                }).setOnCancelListener {
                onLoginResult(null)
            }
            return
        }
        mPresenter.initData(
            if (UserManager.userName.isNotEmpty() && UserManager.passWord.isNotEmpty()) {
                hashMapOf(
                    "userId" to UserManager.userName,
                    "passwd" to UserManager.passWord,
                    "appId" to ZXUniqueIdUtil.getUniqueId()
                ).toJson()
            } else {
                null
            }
        )
        dialog?.setOnCancelListener {
            onLoginResult(null)
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

    }

    override fun onAppConfigResult(appInfo: String) {
        AppInfoManager.setData(appInfo)
    }

    override fun onLoginResult(bean: UserBean?) {
        if (bean != null) {
            UserManager.user = bean
            GuideActivity.startAction(this, true)
            MyApplication.isOfflineMode = false
        } else {
            UserManager.loginOut()
            LoginActivity.startAction(this, true)
        }
    }

}
