package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.module.system.mvp.contract.ChangePwdContract


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ChangePwdPresenter : ChangePwdContract.Presenter() {
    override fun changePwd(map: Map<String, String>) {
        mModel.changePwdData(map)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<String>(mView) {
                override fun _onNext(t: String?) {
                    mView.onChangePwdResult()
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }


}