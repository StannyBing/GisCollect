package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.func.ResultShowAdapter
import com.gt.entrypad.module.project.mvp.contract.ResultShowContract
import com.gt.entrypad.module.project.mvp.model.ResultShowModel
import com.gt.entrypad.module.project.mvp.presenter.ResultShowPresenter
import com.gt.entrypad.module.project.ui.view.editText.EditTextViewViewModel
import com.gt.entrypad.module.project.ui.view.infoDialogView.InfoDialogViewViewModel
import com.gt.entrypad.module.project.ui.view.resultShowView.ResultShowViewViewModel
import com.gt.entrypad.module.project.ui.view.spinnerView.SpinnerViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.views.PhotoPicker.ZXPhotoPreview
import kotlinx.android.synthetic.main.activity_result_show.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import rx.functions.Action1

/**
 * create by 96212 on 2021/1/22.
 * Email 962123525@qq.com
 * desc  成果展示
 */
class ResultShowActivity : BaseActivity<ResultShowPresenter, ResultShowModel>(),ResultShowContract.View{
    private var data = arrayListOf<InputInfoBean>()
    private var resultAdapter = ResultShowAdapter(data)
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, ResultShowActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun onViewListener() {
        mRxManager.on("finish", Action1<ArrayList<InputInfoBean>>{
            it?.let {
                initData(it)
            }
        })
        mRxManager.on("attachment", Action1<String> {
            when(it){
                "现场勘察表"->{
                    ZXPhotoPreview.builder()
                        .setPhotos(arrayListOf<String>().apply {
                            add(R.drawable.suvery_picture.toString())
                            add(R.drawable.property_picture.toString())
                        })
                        .setCurrentItem(0)
                        .start(this)
                }
                "宗地房屋图"->{
                    ZXPhotoPreview.builder()
                        .setPhotos(arrayListOf<String>().apply {
                            add(R.drawable.ground_picture.toString())
                        })
                        .setCurrentItem(0)
                        .start(this)
                }
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_result_show
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        recyclerView?.apply {
            layoutManager= GridLayoutManager(mContext, 6).apply {
                spanSizeLookup = object :GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        when(data[position].itemType){
                            1,4->{
                                return  6
                            }
                            2->{
                                return 3
                            }
                            else ->{
                                2
                            }
                        }
                        return 1
                    }

                }
            }
            adapter = resultAdapter
        }
        toolBarTitleTv.text = getString(R.string.resultShow)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@ResultShowActivity)
                }

            })
        }
        rightTv.apply {
            visibility = View.GONE
        }
    }


    private fun initData(dataList:ArrayList<InputInfoBean>){
        data.clear()
        data.add(InputInfoBean(1,TitleViewViewModel("基础属性数据").apply {
            resId = R.style.titleText
        }))
        dataList.forEach {
            val tempData = it.data
        if (it.itemType!=1){
            when(tempData){
                is EditTextViewViewModel->{
                    data.add(InputInfoBean(2,ResultShowViewViewModel(tempData.title,tempData.inputContent)))
                }
                is SpinnerViewViewModel->{
                    data.add(InputInfoBean(2,ResultShowViewViewModel(tempData.title,tempData.inputContent)))
                }
                is InfoDialogViewViewModel->{
                    data.add(InputInfoBean(2,ResultShowViewViewModel(tempData.title,tempData.inputContent)))
                }
            }
        }
        }
        data.add(InputInfoBean(1,TitleViewViewModel("业务附件").apply {
            resId = R.style.titleText
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("现场勘察表").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("坐标文件").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("现场图片").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("宗地房屋图").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("申请表").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(3,TitleViewViewModel("权益调查表").apply {
            resId = R.style.titleTextShape
        }))
        data.add(InputInfoBean(4,TitleViewViewModel("权利人签名").apply {
            resId = R.style.titleText
        }))
        resultAdapter.notifyDataSetChanged()
    }
}
