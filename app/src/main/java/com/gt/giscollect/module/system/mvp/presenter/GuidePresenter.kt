package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.giscollect.module.system.bean.AppFuncBean
import com.gt.giscollect.module.system.mvp.contract.GuideContract
import org.json.JSONObject
import java.lang.Exception


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class GuidePresenter : GuideContract.Presenter() {
    override fun getAppFuncs(map: HashMap<String, String>) {
        mModel.appFuncData(map)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<Any>(mView) {
                override fun _onNext(t: Any?) {
                    if (t != null) {
                        try {
                            mView.appFuncResult(Gson().fromJson<List<AppFuncBean>>(JSONObject(Gson().toJson(t)).getJSONArray("treeMenus").toString(), object : TypeToken<List<AppFuncBean>>() {}.type))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
//                        mView.appFuncResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }
            })
    }


}