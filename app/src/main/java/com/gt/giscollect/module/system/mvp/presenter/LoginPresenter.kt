package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.base.manager.UserBean
import com.gt.giscollect.module.system.mvp.contract.LoginContract
import okhttp3.RequestBody


/**
 * Create By XB
 * 功能：
 */
class LoginPresenter : LoginContract.Presenter() {
    override fun doLogin(body: RequestBody) {
        mModel.loginData(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<UserBean>(mView) {
                override fun _onNext(t: UserBean?) {
                    mView.onLoginResult(t)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

}