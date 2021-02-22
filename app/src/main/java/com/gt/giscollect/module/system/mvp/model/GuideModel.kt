package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService

import com.gt.giscollect.module.system.mvp.contract.GuideContract
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class GuideModel : BaseModel(), GuideContract.Model {
    override fun appFuncData(map : HashMap<String, String>) : Observable<Any> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getAppFuncs(map)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}