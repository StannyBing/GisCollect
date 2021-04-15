package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.gt.giscollect.api.ApiConfigModule
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.base.bean.NormalList
import com.gt.giscollect.module.system.bean.TemplateBean
import com.gt.giscollect.module.system.mvp.contract.TemplateDownloadContract
import com.zx.zxutils.util.ZXFileUtil
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File


/**
 * Create By XB
 * 功能：
 */
class TemplateDownloadPresenter : TemplateDownloadContract.Presenter() {
    override fun getTemplateList(requestBody: RequestBody) {
        if (MyApplication.isOfflineMode){
            return
        }
        mModel.templateListData(requestBody)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<TemplateBean>>(mView) {
                override fun _onNext(t: NormalList<TemplateBean>?) {
                    if (t != null) {
                        mView.onTemplateListResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

    override fun downloadTemplate(templateBean: TemplateBean) {
        var filePath = ""
        var fileName = ""
        var countLength = 0L
        try {
            val fileObj = JSONArray(templateBean.fileJson).getJSONObject(0)
//            fileName = fileObj.getString("fileName")
            fileName = templateBean.tplName + "." + fileObj.getString("fileExt")
            filePath = fileObj.getString("fileUri")
            countLength = fileObj.getLong("fileSize")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val downInfo = DownInfo("file/downloadCollectTemplate?fileName=${fileName}&fileUri=${filePath}")
        downInfo.countLength = countLength
        downInfo.baseUrl = ApiConfigModule.BASE_IP
        if (ZXFileUtil.isFileExists(ConstStrings.getCollectTemplatePath() + fileName)) {
            ZXFileUtil.deleteFiles(ConstStrings.getCollectTemplatePath() + fileName)
        }
        downInfo.savePath = ConstStrings.getCollectTemplatePath() + fileName
        downInfo.listener = object : DownloadOnNextListener<Any>() {
            override fun onNext(o: Any) {

            }

            override fun onStart() {
                mView.onDownloadProgress(0)
            }

            override fun onComplete(file: File) {
                mView.onTemplateDowmload(file)
                mView.dismissLoading()
            }

            override fun onError(message: String?) {
                mView.showToast(message)
                mView.dismissLoading()
            }

            override fun updateProgress(progress: Int) {
                mView.onDownloadProgress(progress)
            }
        }
        if (ZXFileUtil.isFileExists(ConstStrings.getCollectTemplatePath() + fileName)) {
            mView.onTemplateDowmload(File(ConstStrings.getCollectTemplatePath() + fileName))
        } else {
            HttpDownManager.getInstance().startDown(downInfo) { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Cookie", ConstStrings.Cookie)
                    .build()
                chain.proceed(request)
            }
        }
    }


}