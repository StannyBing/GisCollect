package com.gt.entrypad.module.project.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.entrypad.api.ApiService
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.MapContract
import com.gt.entrypad.module.project.mvp.contract.SketchFiledContract
import okhttp3.RequestBody
import rx.Observable


/**
 * Create By XB
 * 功能：
 */
class SketchFiledModel : BaseModel(), SketchFiledContract.Model {

    override fun zddWorld(body: RequestBody): Observable<HouseTableBean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .zddWorld(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
    override fun fwdWorld(body: RequestBody): Observable<HouseTableBean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .fwdWorld(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
}