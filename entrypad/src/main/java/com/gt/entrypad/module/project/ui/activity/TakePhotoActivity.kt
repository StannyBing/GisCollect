package com.gt.entrypad.module.project.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.camera.module.CameraVedioActivity
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.bean.IDCardInfoBean
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.func.PhotoAdapter
import com.gt.entrypad.module.project.mvp.contract.TakePhotoContract
import com.gt.entrypad.module.project.mvp.model.TakePhotoModel
import com.gt.entrypad.module.project.mvp.presenter.TakePhotoPresenter
import com.gt.entrypad.module.project.ui.view.BottomSheetOptionsDialog
import com.gt.entrypad.module.project.ui.view.editText.EditTextViewViewModel
import com.gt.entrypad.module.project.ui.view.photoView.PhotoViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXSystemUtil
import com.zx.zxutils.views.PhotoPicker.PhotoPickUtils
import com.zx.zxutils.views.PhotoPicker.PhotoPicker
import com.zx.zxutils.views.PhotoPicker.ZXPhotoPreview
import kotlinx.android.synthetic.main.activity_info_input.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import rx.functions.Action1
import java.util.*
import kotlin.collections.ArrayList

@Route(path =RouterPath.TAKE_PHOTO)
class TakePhotoActivity : BaseActivity<TakePhotoPresenter, TakePhotoModel>(),TakePhotoContract.View{
   private var photoList= arrayListOf<PhotoViewViewModel>()
    private var photoAdapter = PhotoAdapter(photoList)
    private var bottomSheetOptionsDialog:BottomSheetOptionsDialog?=null


    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, TakePhotoActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(5, OrientationHelper.VERTICAL)
            adapter = photoAdapter
        }
        photoList.add(PhotoViewViewModel().apply {
            resId=R.drawable.tianjia
        })
        toolBarTitleTv.text = getString(R.string.takePhoto)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@TakePhotoActivity)
                }

            })
        }
        rightTv.apply {
            visibility=View.VISIBLE
            setData(TitleViewViewModel(getString(R.string.nextStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    //信息上传
                    ZXDialogUtil.showYesNoDialog(mContext,"提示","确认上传?",DialogInterface.OnClickListener { dialog, which ->
                        uploadInfo()
                    })
                }

            })
        }
        finishTv.apply {
            setData(TitleViewViewModel(getString(R.string.finish)))
            visibility = View.VISIBLE
        }
    }
    override fun onViewListener() {
        mRxManager.on("bottom", Action1<String> {
            bottomSheetOptionsDialog?.dismiss()
            when(it){
                getString(R.string.photo)->{
                    //相册
                    PhotoPickUtils.startPick(this, false, 1, arrayListOf(), UUID.randomUUID().toString(), false, false)
                }
                getString(R.string.camera)->{
                    //拍照
                    getPermission(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        CameraVedioActivity.startAction(this, false, 1,PhotoPicker.REQUEST_CODE, ZXSystemUtil.getSDCardPath()+"/house")
                    }
                }
                getString(R.string.sketch)->{
                    //拍摄草图
                    getPermission(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        CameraVedioActivity.startAction(this, false, 1,PhotoPicker.REQUEST_CODE, ZXSystemUtil.getSDCardPath()+"/house")
                    }
                }
            }
        })
        //预览
        mRxManager.on("preview", Action1<String>{
            ZXPhotoPreview.builder()
                .setPhotos(arrayListOf<String>().apply {
                    add(it)
                })
                .setCurrentItem(0)
                .start(this)
        })
        mRxManager.on("show", Action1<Int> {
            BottomSheetOptionsDialog(mContext, arrayListOf<TitleViewViewModel>().apply {
                add(TitleViewViewModel(getString(R.string.photo)))
                add(TitleViewViewModel(getString(R.string.camera)))
                add(TitleViewViewModel(getString(R.string.sketch)))
            }).apply {
                bottomSheetOptionsDialog = this
                show()
            }
        })
        mRxManager.on("delete", Action1 <PhotoViewViewModel>{
            photoList.remove(it)
            photoAdapter.notifyDataSetChanged()
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_take_photo
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== PhotoPicker.REQUEST_CODE&&resultCode== Activity.RESULT_OK){
            val s = data?.getStringExtra("path") ?: ""
            if (s.isEmpty()){
                //相册选取
               (data?.extras?.get("SELECTED_PHOTOS") as ArrayList<String>)?.forEach {
                    photoList.add(0,PhotoViewViewModel(it))
                }
            }else{
                //拍照
                photoList.add(0,PhotoViewViewModel(s))
            }
            photoAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 上传填写信息
     */
    private fun uploadInfo(){
        val infoData = mSharedPrefUtil.getString("infoData")
        var fileData = arrayListOf<String>()
        var inputInfoData:List<InputInfoBean>?=null
        if (infoData.isNotEmpty()){
            inputInfoData = Gson().fromJson<List<InputInfoBean>>(infoData,object : TypeToken<List<InputInfoBean>>(){}.type)
        }
        //获取文件信息
        photoList.forEach {
            fileData.add(it.url)
        }
        mPresenter.uploadInfo(inputInfoData,fileData)
    }

    /**
     * 上传回调接口
     */
    override fun uploadResult(uploadResult: String?) {
        Log.e("fdfdf","${uploadResult?:"是viu"}")
    }
}