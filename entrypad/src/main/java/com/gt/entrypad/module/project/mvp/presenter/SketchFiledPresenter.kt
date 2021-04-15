package com.gt.entrypad.module.project.mvp.presenter

import android.util.Log
import com.esri.arcgisruntime.data.Field
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.ConstStrings
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.SketchFiledContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import com.zx.zxutils.util.ZXTimeUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Create By XB
 * 功能：
 */
class SketchFiledPresenter : SketchFiledContract.Presenter() {
    override fun zddWorld(info: List<Pair<Field, Any?>>?, files: String) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        info?.forEach {
            if (it.second is String){
                builder.addFormDataPart(it.first.name,it.second.toString())
            }else if (it.second is GregorianCalendar){
                var time = ZXTimeUtil.getTime((it.second as GregorianCalendar).timeInMillis, SimpleDateFormat("yyyy/MM/dd"))?:""
                builder.addFormDataPart(it.first.name,time)
            }
        }
        if (files.isNotEmpty()&&!(files.contains("http")||files.contains("https"))){
            builder.addFormDataPart(
                "file",
                files,
                RequestBody.create(MediaType.parse("multipart/form-data"), File(files))
            )
        }
        val uploadRequestBody =
            UploadRequestBody(
                builder.build(),
                UploadRequestBody.UploadListener { progress, done ->
                    mView.showLoading(
                        "上传中...",
                        progress
                    )
                })
        if (ConstStrings.drawTempleteName.contains("宗地")){
            mModel.zddWorld(uploadRequestBody)
        }else{
            mModel.fwdWorld(uploadRequestBody)
        }
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<HouseTableBean>(mView) {
                override fun _onNext(t: HouseTableBean?) {
                    mView.uploadResult(t)
                    mView.dismissLoading()
                    mView.showToast("上传成功")
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                    mView.dismissLoading()
                }
            })
    }

    override fun downloadFile(name: String, downUrl: String) {
        val downInfo = DownInfo(downUrl)
        val savePath = ZXSystemUtil.getSDCardPath() + "GisCollect/houseEntry/" + name
        downInfo.baseUrl = ApiConfigModule.BASE_IP
        downInfo.savePath = savePath
        downInfo.listener = object : DownloadOnNextListener<Any>() {
            override fun onNext(o: Any) {
                mView.showToast(o.toString())
                ZXDialogUtil.dismissLoadingDialog()
            }

            override fun onStart() {
                ZXDialogUtil.showLoadingDialog(mContext, "正在下载中，请稍后...", 0)

            }

            override fun onComplete(file: File) {
                mView.onFileDownloadResult(file)
                ZXDialogUtil.dismissLoadingDialog()

            }

            override fun onError(message: String?) {
                mView.showToast(message)
                mView.dismissLoading()

            }

            override fun updateProgress(progress: Int) {
                ZXDialogUtil.showLoadingDialog(mContext, "正在下载中，请稍后...", progress)
            }
        }
        if (ZXFileUtil.isFileExists(savePath)) {
            mView.onFileDownloadResult(File(savePath))
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