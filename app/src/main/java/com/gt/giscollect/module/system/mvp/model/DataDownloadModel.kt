package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.system.bean.DataResBean

import com.gt.giscollect.module.system.mvp.contract.DataDownloadContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class DataDownloadModel : BaseModel(), DataDownloadContract.Model {
    override fun dataListData(requestBody: RequestBody): Observable<NormalList<DataResBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getDataResList(requestBody)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun checkFileAuth(map : HashMap<String, String>): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .checkFileAuth(map)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

}