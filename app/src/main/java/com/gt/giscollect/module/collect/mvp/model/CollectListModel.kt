package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.collect.bean.CheckBean
import com.gt.giscollect.module.collect.bean.UploadFileBean

import com.gt.giscollect.module.collect.mvp.contract.CollectListContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class CollectListModel : BaseModel(), CollectListContract.Model {
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