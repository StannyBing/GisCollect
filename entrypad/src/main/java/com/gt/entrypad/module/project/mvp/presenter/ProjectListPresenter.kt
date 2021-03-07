package com.gt.entrypad.module.project.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.ConstStrings
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ProjectListPresenter : ProjectListContract.Presenter() {

    override fun uploadInfo(info: List<String>?, files: List<String>,tplName:String) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        if (!info.isNullOrEmpty()&&info.size==26){
            builder.addFormDataPart("txt2",info[0])
            builder.addFormDataPart("xb",info[1])
            builder.addFormDataPart("sfzh",info[2])
            builder.addFormDataPart("txt8",info[3])
            builder.addFormDataPart("xzjd",info[4])
            builder.addFormDataPart("cm",info[5])
            builder.addFormDataPart("cxz",info[6])
            builder.addFormDataPart("jdzz",info[7])
            builder.addFormDataPart("num3",info[10])
            builder.addFormDataPart("num11",info[15])
            builder.addFormDataPart("num9",info[16])
            builder.addFormDataPart("east",info[20] )
            builder.addFormDataPart("south",info[21] )
            builder.addFormDataPart("west",info[22] )
            builder.addFormDataPart("north",info[23] )
            builder.addFormDataPart("txt17",info[20] )
            builder.addFormDataPart("txt19",info[21] )
            builder.addFormDataPart("txt18",info[22] )
            builder.addFormDataPart("txt20",info[24] )
            builder.addFormDataPart("txt21",info[25] )
            builder.addFormDataPart("tplName",tplName)
        }


        files.forEach {
            if (it.isNotEmpty()&&!(it.contains("http")||it.contains("https"))){
                builder.addFormDataPart(
                    "file",
                    it,
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(it))
                )
            }
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
        mModel.uploadInfo(uploadRequestBody)
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