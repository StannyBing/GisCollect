package com.gt.entrypad.module.project.ui

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.module.project.mvp.contract.ResultShowContract
import com.gt.entrypad.module.project.mvp.model.ResultShowModel
import com.gt.entrypad.module.project.mvp.presenter.ResultShowPresenter
import com.gt.entrypad.module.project.func.view.titleView.TitleViewViewModel
import kotlinx.android.synthetic.main.layout_tool_bar.*

//import com.tencent.smtt.sdk.TbsReaderView


/**
 * create by 96212 on 2021/1/22.
 * Email 962123525@qq.com
 * desc  成果展示
 */
class ResultShowActivity : BaseActivity<ResultShowPresenter, ResultShowModel>(),ResultShowContract.View{
    private var filePath =""
//    private  var tbsReaderView:TbsReaderView?=null
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean,path:String) {
            val intent = Intent(activity, ResultShowActivity::class.java)
            intent.putExtra("filePath",path)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_result_show
    }

    override fun initView(savedInstanceState: Bundle?) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.initView(savedInstanceState)
        filePath =  if (intent?.hasExtra("filePath")==true) intent.getStringExtra("filePath") else ""
        toolBarTitleTv.text = getString(R.string.resultShow)
        leftTv.apply {
            setData(
                TitleViewViewModel(
                    getString(R.string.lastStep)
                )
            )
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@ResultShowActivity)
                }

            })
        }
        rightTv.apply {
            visibility = View.GONE
            setData(
                TitleViewViewModel(
                    getString(R.string.save)
                )
            )
        }
        right2Tv.apply {
            visibility=View.VISIBLE
            setData(
                TitleViewViewModel(
                    getString(R.string.print)
                )
            )
            setActionListener(object :ICustomViewActionListener{
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    
                }

            })
        }
//        initReaderView()
    }


//    private fun initReaderView(){
//        tbsReaderView = TbsReaderView(this,
//            TbsReaderView.ReaderCallback { p0, p1, p2 -> })
//        tbsReaderFl.addView(tbsReaderView,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT))
//        val preOpen = tbsReaderView?.preOpen("docx", false)
//        if (preOpen==true){
//            tbsReaderView?.openFile(Bundle().apply {
//                putString("filePath",filePath)
//                putString("tempPath",Environment.getExternalStorageDirectory().path)
//            })
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        tbsReaderView?.onStop()
//    }
}