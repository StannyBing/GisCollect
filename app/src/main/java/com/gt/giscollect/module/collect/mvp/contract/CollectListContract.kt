package com.gt.giscollect.module.collect.mvp.contract

import com.esri.arcgisruntime.layers.FeatureLayer
import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.base.bean.NormalList
import com.gt.base.app.CheckBean
import com.gt.giscollect.module.system.bean.DataResBean
import com.gt.giscollect.module.system.bean.TemplateBean
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
        fun onTemplateListResult(path : String, layer: FeatureLayer, pos: Int, tempalteList : NormalList<TemplateBean>)

        fun onCollectUpload(name: String)

        fun onCheckListResult(total:Int,checkList: List<CheckBean>){}

        fun onSurveyListResult(tempalteList: NormalList<DataResBean>){}

        fun onDownloadProgress(progress: Int)

        fun onCollectDownload(file: File)

        fun onDeleteHistoryData(historyCheckList:List<CheckBean>)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun uploadData(body: RequestBody): Observable<String>

        fun updateInfoData(body: RequestBody): Observable<Any>

        fun checkListData(body: RequestBody): Observable<NormalList<CheckBean>>

        fun dataListData(requestBody: RequestBody): Observable<NormalList<DataResBean>>

        fun templateListData(requestBody: RequestBody) : Observable<NormalList<TemplateBean>>

        fun historyCheckListData(body: RequestBody):Observable<NormalList<CheckBean>>
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

        abstract fun getSurveyDataList(requestBody: RequestBody)

        abstract fun getTemplateList(path : String, layer: FeatureLayer, pos: Int, requestBody: RequestBody)

        abstract fun getHistoryCheckList(body: RequestBody)
    }
}

