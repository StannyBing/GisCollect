package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.gt.giscollect.api.ApiConfigModule
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.module.collect.mvp.contract.FilePreviewContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class FilePreviewPresenter : FilePreviewContract.Presenter() {
    override fun downloadFile(name: String, downUrl: String) {
        val downInfo = DownInfo(downUrl)
//        if (!File(ConstStrings.getCachePath() + "Download/").exists()){
//
//        }
        val savePath = ConstStrings.getLocalPath() + "ReportFile/" + name
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
            HttpDownManager.getInstance().startDown(downInfo)
        }
    }

}