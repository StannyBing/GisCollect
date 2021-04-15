package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.base.bean.NormalList
import com.gt.giscollect.module.collect.bean.VersionBean

import com.gt.giscollect.module.system.mvp.contract.SettingListContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class SettingListModel : BaseModel(), SettingListContract.Model {
    override fun versionData(body: RequestBody): Observable<NormalList<VersionBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getVersion(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}