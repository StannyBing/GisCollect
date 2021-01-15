package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.module.collect.mvp.contract.CollectCreateContract


/**
 * Create By XB
 * 功能：
 */
class CollectCreatePresenter : CollectCreateContract.Presenter() {
    override fun checkMultiName(map: HashMap<String, String>) {
        mModel.checkMultiNameData(map)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<Boolean>(mView) {
                override fun _onNext(t: Boolean?) {
                    mView.checkMultiNameResult(t ?: false)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.checkMultiNameResult(false)
                }
            })
    }


}