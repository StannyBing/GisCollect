package com.gt.giscollect.module.system.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody
import rx.Observable
import java.io.File

/**
 * Create By XB
 * 功能：
 */
interface DataDownloadContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun onDataListResult(tempalteList: NormalList<DataResBean>)

        fun onDownloadProgress(progress: Int)

        fun onDataDowmload(file: File)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun checkFileAuth(map: HashMap<String, String>): Observable<String>

        fun dataListData(requestBody: RequestBody): Observable<NormalList<DataResBean>>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun getDataList(requestBody: RequestBody)

        abstract fun downloadData(dataBean: DataResBean)
    }
}

