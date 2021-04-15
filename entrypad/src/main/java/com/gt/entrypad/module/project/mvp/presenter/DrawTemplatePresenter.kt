package com.gt.entrypad.module.project.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.ConstStrings
import com.gt.base.bean.NormalList
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.contract.DrawTemplateContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class DrawTemplatePresenter : DrawTemplateContract.Presenter() {

    override fun getTemplateList(requestBody: RequestBody) {
        mModel.templateListData(requestBody)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<DrawTemplateBean>>(mView) {
                override fun _onNext(t: NormalList<DrawTemplateBean>?) {
                    if (t != null) {
                        mView.onTemplateListResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

    override fun downloadTemplate(templateBean: DrawTemplateBean) {
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
        if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + fileName)) {
            ZXFileUtil.deleteFiles(ConstStrings.getDrawTemplatePath() + fileName)
        }
        downInfo.savePath = ConstStrings.getDrawTemplatePath() + fileName
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
        if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + fileName)) {
            mView.onTemplateDowmload(File(ConstStrings.getDrawTemplatePath() + fileName))
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