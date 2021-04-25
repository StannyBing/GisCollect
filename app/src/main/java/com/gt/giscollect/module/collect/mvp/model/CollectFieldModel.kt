package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.base.bean.NormalList
import com.gt.giscollect.api.ApiService

import com.gt.giscollect.module.collect.mvp.contract.CollectFieldContract
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class CollectFieldModel : BaseModel(), CollectFieldContract.Model {
    override fun doDictQuery(body: RequestBody): Observable<Any> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .doQueryDict(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun doDictByDictQuery(body: RequestBody): Observable<NormalList<DataResBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .doQueryDictByDict(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
}