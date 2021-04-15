package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.google.gson.Gson
import com.gt.base.bean.NormalList
import com.gt.giscollect.module.collect.bean.FieldImportBean
import com.gt.giscollect.module.collect.mvp.contract.CollectFieldImportContract
import okhttp3.RequestBody
import org.json.JSONObject
import java.lang.Exception


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectFieldImportPresenter : CollectFieldImportContract.Presenter() {
    override fun getFieldList(body: RequestBody) {
        mModel.listData(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<Any>>(mView) {
                override fun _onNext(t: NormalList<Any>?) {
                    val list = arrayListOf<FieldImportBean>()
                    t?.rows?.forEach {
                        try {
                            val pairs = arrayListOf<Pair<String, String>>()
                            val obj = JSONObject(Gson().toJson(it))
                            obj.keys().forEach { key ->
                                pairs.add(key to obj.optString(key))
                            }
                            list.add(FieldImportBean(pairs))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    mView.onListResult(list)
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }
            })
    }


}