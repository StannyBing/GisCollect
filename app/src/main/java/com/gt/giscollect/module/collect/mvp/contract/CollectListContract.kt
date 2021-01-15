package com.gt.giscollect.module.collect.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.frame.zxmvp.baserx.RxSubscriber
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.collect.bean.CheckBean
import com.gt.giscollect.module.collect.bean.UploadFileBean
import okhttp3.RequestBody
import rx.Observable
import java.io.File

/**
 * Create By XB
 * 功能：
 */
interface CollectListContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun onCollectUpload(name: String)

        fun onCheckListResult(checkList: List<CheckBean>)

        fun onDownloadProgress(progress: Int)

        fun onCollectDownload(file: File)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun uploadData(body: RequestBody): Observable<String>

        fun updateInfoData(body: RequestBody): Observable<Any>

        fun checkListData(body: RequestBody): Observable<NormalList<CheckBean>>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun uploadCollect(
            file: String,
            name: String,
            templateId: String = "5e96bc86-2b8a-47e1-abae-fd3168f9ee44",
            catalogId: String = "6f38bfad-ab3a-4bde-b485-8efcdd29bb9a",
            collectId: String
        )

        abstract fun getCheckList(body: RequestBody)

        abstract fun downloadCollect(bean: CheckBean)
    }
}

