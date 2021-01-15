package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.module.collect.bean.CheckBean

import com.gt.giscollect.module.collect.mvp.contract.CollectCheckContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectCheckModel : BaseModel(), CollectCheckContract.Model {
    override fun checkListData(body: RequestBody): Observable<List<CheckBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getCheckList0(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}