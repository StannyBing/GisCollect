package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.base.bean.NormalList
import com.gt.base.app.CheckBean

import com.gt.giscollect.module.collect.mvp.contract.CollectListContract
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class CollectListModel : BaseModel(), CollectListContract.Model {

    override fun dataListData(requestBody: RequestBody): Observable<NormalList<DataResBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getDataResList(requestBody)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun uploadData(body: RequestBody): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadCollect(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun updateInfoData(body: RequestBody): Observable<Any> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .updateCollectInfo(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun checkListData(body: RequestBody): Observable<NormalList<CheckBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getCheckList(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}