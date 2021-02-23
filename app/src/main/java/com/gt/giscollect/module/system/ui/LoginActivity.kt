package com.gt.giscollect.module.system.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.base.app.AppInfoManager
import com.gt.base.manager.UserBean
import com.gt.giscollect.base.toJson

import com.gt.giscollect.module.system.mvp.contract.LoginContract
import com.gt.giscollect.module.system.mvp.model.LoginModel
import com.gt.giscollect.module.system.mvp.presenter.LoginPresenter
import com.gt.base.manager.UserManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXUniqueIdUtil
import com.zx.zxutils.views.ZXStatusBarCompat
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.File


/**
 * Create By XB
 * 功能：登录页
 */
class LoginActivity : BaseActivity<LoginPresenter, LoginModel>(), LoginContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZXStatusBarCompat.setStatusBarDarkMode(this)
        tv_login_name.text = AppInfoManager.appInfo?.appInfo?.name ?: "清河CIM移动端数据采集系统"
//        tv_login_enname.text = AppInfoManager.appInfo?.appInfo?.enName ?: "Data Collect System"

        et_login_username.setText(UserManager.userName)
        tv_login_server.paint.flags = Paint.UNDERLINE_TEXT_FLAG
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        getPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            if (!ZXFileUtil.isFileExists(ConstStrings.getLocalPath())) {
                File(ConstStrings.getLocalPath()).mkdirs()
            }
//            if (!ZXFileUtil.isFileExists(ConstEntryStrings.getStylePath())) {
//                File(ConstEntryStrings.getStylePath()).mkdirs()
//            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getLocalMapPath())) {
                File(ConstStrings.getLocalMapPath()).mkdirs()
            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getInnerLocalMapPath())) {
                File(ConstStrings.getInnerLocalMapPath()).mkdirs()
            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getOperationalLayersPath())) {
                File(ConstStrings.getOperationalLayersPath()).mkdirs()
            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getCollectTemplatePath())) {
                File(ConstStrings.getCollectTemplatePath()).mkdirs()
            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getSketchLayersPath())) {
                File(ConstStrings.getSketchLayersPath()).mkdirs()
            }
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        btn_login_do.setOnClickListener {
            //            MainActivity.startAction(this, true)
            if (et_login_username.text.toString().isEmpty()) {
                showToast("请输入用户名")
            } else if (et_login_password.text.toString().isEmpty()) {
                showToast("请输入密码")
            } else {
                mPresenter.doLogin(
                    hashMapOf(
                        "userId" to et_login_username.text.toString(),
                        "passwd" to et_login_password.text.toString(),
                        "appId" to ZXUniqueIdUtil.getUniqueId()
                    ).toJson()
                )
            }
        }
        tv_login_server.setOnClickListener {
            if (AppInfoManager.appInfo?.apiInfo == null) {
                showToast("暂未获取到服务器信息！")
                return@setOnClickListener
            }
            AppInfoManager.appInfo?.apiInfo?.let { apiList ->
                if (apiList.isEmpty()) {
                    showToast("暂未获取到服务器信息！")
                    return@let
                }
                val items = arrayListOf<String>().apply {
                    apiList.forEach {
                        val obj = JSONObject(it)
                        this.add(
                            if (mSharedPrefUtil.getString(
                                    "base_ip",
                                    "http://xianrui.vip:8080/"
                                ).contains(obj.getString("ip"))
                                || mSharedPrefUtil.getString(
                                    "base_ip",
                                    "http://xianrui.vip:8080/"
                                ).contains(obj.getString("domain"))
                            ) {
                                obj.getString("itemName") + "---当前"
                            } else {
                                obj.getString("itemName")
                            }
                        )
                    }
                }
                ZXDialogUtil.showListDialog(mContext, "服务器切换", "取消", items) { dialog, which ->
                    if (items[which].contains("---当前")) {
                        showToast("该服务器正在使用，无需修改")
                    } else {
                        val obj = JSONObject(apiList[which])
                        val name = obj.getString("itemName")
                        val url = obj.getString("ip")
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否切换服务器至--$name, 点击确认将切换服务器并重启应用？"
                        ) { _, _ ->
                            mSharedPrefUtil.putString("base_ip", "http://$url/")
                            MyApplication.instance.reinit()
                            SplashActivity.startAction(this, true)
                        }
                    }
                }
            }
        }
    }

    override fun onLoginResult(bean: UserBean?) {
        if (bean != null) {
            UserManager.userName = et_login_username.text.toString()
            UserManager.passWord = et_login_password.text.toString()
            UserManager.user = bean
            GuideActivity.startAction(this, true)
            MyApplication.isOfflineMode = false
        }
    }

}
