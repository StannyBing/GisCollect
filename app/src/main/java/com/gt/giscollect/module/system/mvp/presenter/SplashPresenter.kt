package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.base.UserBean
import com.gt.giscollect.module.system.func.tool.NetworkUtils
import com.gt.giscollect.module.system.mvp.contract.SplashContract
import okhttp3.RequestBody


/**
 * Create By XB
 * 功能：
 */
class SplashPresenter : SplashContract.Presenter() {
    override fun initData(body: RequestBody?) {
        mModel.appConfigData()
            .compose(RxHelper.bindToLifecycle(mView))
            .flatMap {
                mView.onAppConfigResult(it)
                if (body == null) {
                    mView.onLoginResult(null)
                    null
                } else {
                    mModel.loginData(body)
                }
            }
            .subscribe(object : RxSubscriber<UserBean>(mView) {
                override fun _onNext(t: UserBean?) {
                    mView.onLoginResult(t)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.onLoginResult(null)
                    mView.handleError(code, message)
                }

            })
    }


}