package com.gt.base.activity

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.RxBaseActivity
import com.gt.base.R
import com.zx.zxutils.util.*
import com.zx.zxutils.views.ZXStatusBarCompat


/**
 * Created by Xiangb
 * 功能：
 */
abstract class BaseActivity<T : BasePresenter<*, *>, E : BaseModel> : RxBaseActivity<T, E>() {
    val mSharedPrefUtil = ZXSharedPrefUtil()
    val handler = Handler()

    private var loadMills = 0L

    private var permessionBack: () -> Unit = {}
    private var permissionArray: Array<String>? = null

    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZXStatusBarCompat.translucent(this)
        ZXStatusBarCompat.setStatusBarLightMode(this)
//        ZXCrashUtil.init(BuildConfig.RELEASE) { t, e ->
//            showToast("出现未知问题，请稍后再试")
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            // 始终允许窗口延伸到屏幕短边上的刘海区域
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
    }

    /**
     * 判断当前activity是否在栈顶，避免重复处理
     */
    private fun isTopActivity(): Boolean {
        var isTop = false
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cn = am.getRunningTasks(1)[0].topActivity
        if (cn?.className?.contains(this.localClassName) == true) {
            isTop = true
        }
        return isTop
    }

    override fun showToast(message: String?) {
        ZXToastUtil.showToast(message?:"")
    }

    override fun showLoading(message: String) {
//        BUIDialog.showLoading(this, message)
        dialog = ZXDialogUtil.showLoadingDialog(this, message)
    }

    override fun dismissLoading() {
//        BUIDialog.dismissLoading()
        ZXDialogUtil.dismissLoadingDialog()
    }


    override fun showLoading(message: String, progress: Int) {
        handler.post {
            ZXDialogUtil.dismissLoadingDialog()
//            BUIDialog.showLoading(this, message, progress)
        }
    }

    override fun handleError(code: Int, message: String) {
        if (code == 50004) {
            showToast("获取结果失败，请稍后再试")
        } else {
            showToast(message)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        onViewListener()
    }

    abstract fun onViewListener()

    fun getPermission(permissionArray: Array<String>, permessionBack: () -> Unit) {
        this.permessionBack = permessionBack
        this.permissionArray = permissionArray
        if (permissionArray.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (!ZXLocationUtil.isLocationEnabled()) {
                ZXLocationUtil.openGPS(this)
                return
            }
        }
        if (!ZXPermissionUtil.checkPermissionsByArray(permissionArray)) {
            ZXPermissionUtil.requestPermissionsByArray(this)
        } else {
            this.permessionBack()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionArray == null) {
            return
        }
        if (ZXPermissionUtil.checkPermissionsByArray(permissionArray)) {
            permessionBack()
        } else {
            showToast("未获取到系统权限，请先在设置中开启相应权限！")
        }
    }

}