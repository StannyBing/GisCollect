package com.gt.entrypad.module.project.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.core.app.ActivityCompat
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.model.DrawSketchModel
import com.gt.entrypad.module.project.mvp.presenter.DrawSketchPresenter
import com.gt.entrypad.module.project.ui.fragment.LoadMainFragment
import com.gt.entrypad.module.project.ui.view.photoView.PhotoViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXFragmentUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.activity_draw_sketch.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File

class DrawSketchActivity : BaseActivity<DrawSketchPresenter, DrawSketchModel>(),DrawSketchContract.View{
    private var loadMainFragment:LoadMainFragment?=null
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean,data:ArrayList<PhotoViewViewModel>,infoData:ArrayList<String>) {
            val intent = Intent(activity, DrawSketchActivity::class.java)
            intent.putExtra("photoData",data)
            intent.putExtra("infoData",infoData)
           activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolBarTitleTv.text = getString(R.string.registrationPlatform)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@DrawSketchActivity)
                }

            })
        }
        rightTv.apply {
            visibility= View.VISIBLE
            setData(TitleViewViewModel(getString(R.string.load)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
/*                    val arrayList = arrayListOf<String>().apply {
                        add("房屋图.docx")
                        add("宗地图.docx")
                        add("渝北现场查勘表.docx")
                    }
                    //信息上传
                    ZXDialogUtil.showListDialog(mContext,"生成图形","确定", arrayList,DialogInterface.OnClickListener { dialog, which ->
                        when(arrayList[which]){
                            "房屋图.docx"->{

                            }
                            "宗地图.docx"->{


                            }
                            "渝北现场查勘表.docx"->{
                              *//*  CopyAssetsToSd.copy(mContext,"渝北现场查勘表app.docx",ZXSystemUtil.getSDCardPath()+"/chankan","chankan.docx")
                                ZXFileUtil.openFile(mContext,File("${ZXSystemUtil.getSDCardPath()+"/chankan/chankan.docx"}"))*//*
                                uploadInfo("渝北现场查勘表.docx")
                            }
                        }
                    },DialogInterface.OnClickListener { dialog, which ->

                    })*/
                   sketchPadView.saveGraphicInfo()
                    //获取所有楼层
                    val string = mSharedPrefUtil.getString("graphicList")?.let {
                       if (it.isNotEmpty()){
                           iv_data_show.performClick()
                           loadMainFragment?.let {

                           }?:ZXFragmentUtil.addFragment(supportFragmentManager, LoadMainFragment.newInstance().apply {
                               loadMainFragment=this
                           },R.id.fm_data)
                       }
                    }?:showToast("请绘制草图")
                }

            })
        }
        finishTv.apply {
            setData(TitleViewViewModel(getString(R.string.finish)))
            visibility = View.GONE
        }
    }
    override fun onViewListener() {
        //收起菜单
        iv_data_hide.setOnClickListener {
            rl_main_data.animation =
                TranslateAnimation(0f, ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f)
                    .apply {
                        duration = 500
                        start()
                    }
            rl_main_data.visibility = View.GONE
            iv_data_show.visibility = View.VISIBLE
        }
        //打开菜单
        iv_data_show.setOnClickListener {
            if (rl_main_data.visibility != View.VISIBLE) {
                rl_main_data.animation =
                    TranslateAnimation(ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f, 0f)
                        .apply {
                            duration = 500
                            start()
                        }
                rl_main_data.visibility = View.VISIBLE
            }
            iv_data_show.visibility = View.GONE
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_draw_sketch
    }
    /**
     * 上传填写信息
     */
    private fun uploadInfo(tplName:String){
        getPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ){
            var fileData = arrayListOf<String>()
            val photoList = intent?.getSerializableExtra("photoData") as ArrayList<PhotoViewViewModel>
            var infoData = if (intent.hasExtra("infoData")) intent.getSerializableExtra("infoData") as ArrayList<String> else arrayListOf()
            //获取文件信息
            photoList.forEach {
                fileData.add(it.url)
            }
            //获取草图
            val sketch = mContext.filesDir.path + "/sketch/draw.jpg"
            if (ZXFileUtil.isFileExists(sketch)) fileData.add(sketch)
            mPresenter.uploadInfo(infoData,fileData,tplName)
        }
    }

    /**
     * 上传回调接口
     */
    override fun uploadResult(uploadResult: HouseTableBean?) {
       uploadResult?.let {
           mPresenter.downloadFile("房屋勘查表.docx","/office/word/downloadReport?fileName=${it.fileName}&filePath=${it.localUri}")
       }
    }

    /**
     * 下载文件成功回调
     */
    override fun onFileDownloadResult(file: File) {
        ResultShowActivity.startAction(this,false,file.absolutePath)
    }
}