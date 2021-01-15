package com.gt.giscollect.base

import android.os.Bundle
import android.os.Handler
import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.RxBaseFragment
import com.zx.zxutils.util.ZXSharedPrefUtil
import com.zx.zxutils.util.ZXToastUtil

/**
 * Created by Xiangb
 * 功能：
 */
abstract class BaseFragment<T : BasePresenter<*, *>, E : BaseModel> : RxBaseFragment<T, E>() {
    var pageSize = 10
    private lateinit var permissionArray: Array<String>
    private lateinit var permessionBack: () -> Unit
    var mSharedPrefUtil = ZXSharedPrefUtil()
    var handler = Handler()

    override fun showLoading(message: String) {
        (activity as BaseActivity<*, *>).showLoading(message)
        //        ZXDialogUtil.showLoadingDialog(getActivity(), message);
    }

    override fun dismissLoading() {
        (activity as BaseActivity<*, *>).dismissLoading()
        //        ZXDialogUtil.dismissLoadingDialog();
    }

    override fun showLoading(message: String, progress: Int) {
        (activity as BaseActivity<*, *>).showLoading(message, progress)
    }

    override fun showToast(message: String) {
        ZXToastUtil.showToast(message)
    }

    override fun handleError(code: Int, message: String) {
        (activity as BaseActivity<*, *>).handleError(code, message)
    }

    override fun initView(savedInstanceState: Bundle?) {
        onViewListener()
    }

    abstract fun onViewListener()

    fun getPermission(permissionArray: Array<String>, permessionBack: () -> Unit) {
        if (activity != null) {
            (activity as BaseActivity<*, *>).getPermission(permissionArray, permessionBack)
        }
    }

}