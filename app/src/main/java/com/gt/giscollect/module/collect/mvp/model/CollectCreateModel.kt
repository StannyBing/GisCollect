package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService

import com.gt.giscollect.module.collect.mvp.contract.CollectCreateContract
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class CollectCreateModel : BaseModel(), CollectCreateContract.Model {
    override fun checkMultiNameData(map: HashMap<String, String>): Observable<Boolean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .checkMultiName(map)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}