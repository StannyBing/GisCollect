package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.module.collect.bean.CheckBean
import com.gt.giscollect.module.collect.mvp.contract.CollectCheckContract
import okhttp3.RequestBody


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectCheckPresenter : CollectCheckContract.Presenter() {
    override fun getCheckList(body: RequestBody) {
        mModel.checkListData(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<List<CheckBean>>(mView){
                override fun _onNext(t: List<CheckBean>?) {
                    if (t != null) {
                        mView.onCheckListResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }


}