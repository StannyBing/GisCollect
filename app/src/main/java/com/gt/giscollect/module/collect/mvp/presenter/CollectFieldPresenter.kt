package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.base.bean.NormalList
import com.gt.giscollect.module.collect.mvp.contract.CollectFieldContract
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody


/**
 * Create By XB
 * 功能：
 */
class CollectFieldPresenter : CollectFieldContract.Presenter() {
    override fun doDictQuery(body: RequestBody) {
        mModel.doDictQuery(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<Any>(mView) {
                override fun _onNext(t: Any?) {
                    mView.dictQueryResult(t)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }
            })
    }

    override fun doDictByDictQuery(body: RequestBody) {
        mModel.doDictByDictQuery(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<DataResBean>>(mView) {
                override fun _onNext(t: NormalList<DataResBean>?) {
                    mView.dictQueryByQictResult(t)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.dictQueryByQictResult(null)
                    mView.handleError(code, message)
                }
            })
    }
}