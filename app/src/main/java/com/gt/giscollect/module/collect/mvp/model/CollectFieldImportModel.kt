package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.base.bean.NormalList

import com.gt.giscollect.module.collect.mvp.contract.CollectFieldImportContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectFieldImportModel : BaseModel(), CollectFieldImportContract.Model {
    override fun listData(body: RequestBody): Observable<NormalList<Any>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getFieldList(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}