package com.gt.giscollect.module.system.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.gt.giscollect.api.ApiConfigModule
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.system.bean.DataResBean
import com.gt.giscollect.module.system.mvp.contract.DataDownloadContract
import com.zx.zxutils.util.ZXFileUtil
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File


/**
 * Create By XB
 * 功能：
 */
class DataDownloadPresenter : DataDownloadContract.Presenter() {
    override fun getDataList(requestBody: RequestBody) {
        if (MyApplication.isOfflineMode){
            return
        }
        mModel.dataListData(requestBody)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<DataResBean>>(mView) {
                override fun _onNext(t: NormalList<DataResBean>?) {
                    if (t != null) {
                        mView.onDataListResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

    override fun downloadData(dataBean: DataResBean) {
        if (MyApplication.isOfflineMode){
            mView.showToast("当前为离线模式，无法进行该操作！")
            return
        }
        mModel.checkFileAuth(hashMapOf("catalogId" to dataBean.catalogId))
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<String>(mView) {
                override fun _onNext(t: String?) {
                    if (t?.equals("true") == true) {
                        doDownload(dataBean)
                    } else {
                        mView.showToast("暂无下载权限！")
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

    private fun doDownload(dataBean: DataResBean) {
        var filePath = ""
        var fileName = ""
        var countLength = 0L
        try {
            val fileObj = JSONArray(dataBean.fileJson).getJSONObject(0)
//            fileName = fileObj.getString("fileName")
            fileName = dataBean.materialName + "." + fileObj.getString("fileExt")
            filePath = fileObj.getString("fileUri")
            countLength = fileObj.getLong("fileSize")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val downInfo =
            DownInfo("file/downloadFile?fileUri=${filePath}@@${dataBean.catalogId}&fileName=${fileName}")
        downInfo.countLength = countLength
        downInfo.baseUrl = ApiConfigModule.BASE_IP
        if (ZXFileUtil.isFileExists(ConstStrings.getLocalMapPath(fileName) + fileName)) {
            ZXFileUtil.deleteFiles(ConstStrings.getLocalMapPath(fileName) + fileName)
        }

        downInfo.savePath = ConstStrings.getLocalMapPath(fileName) + fileName
        downInfo.listener = object : DownloadOnNextListener<Any>() {
            override fun onNext(o: Any) {

            }

            override fun onStart() {
                mView.onDownloadProgress(0)
            }

            override fun onComplete(file: File) {
                mView.onDataDowmload(file)
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
        if (ZXFileUtil.isFileExists(ConstStrings.getLocalMapPath(fileName) + fileName)) {
            mView.onDataDowmload(File(ConstStrings.getLocalMapPath(fileName) + fileName))
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