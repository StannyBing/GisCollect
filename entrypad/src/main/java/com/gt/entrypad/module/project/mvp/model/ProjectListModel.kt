package com.gt.entrypad.module.project.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.entrypad.api.ApiService
import com.gt.entrypad.module.project.bean.HouseTableBean

import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ProjectListModel : BaseModel(), ProjectListContract.Model{

    override fun uploadInfo(body: RequestBody): Observable<HouseTableBean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadInfo(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
}