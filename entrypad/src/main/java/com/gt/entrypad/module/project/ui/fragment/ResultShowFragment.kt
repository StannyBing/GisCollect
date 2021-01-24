package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.gt.base.fragment.BaseFragment
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
import kotlinx.android.synthetic.main.fragment_result_show.*
import rx.functions.Action1

/**
 * create by 96212 on 2021/1/22.
 * Email 962123525@qq.com
 * desc  成果展示
 */
class ResultShowFragment : BaseFragment<ResultShowPresenter, ResultShowModel>(),ResultShowContract.View{
    private var data = arrayListOf<InputInfoBean>()
    private var resultAdapter = ResultShowAdapter(data)
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): ResultShowFragment {
            val fragment = ResultShowFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
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
                        .start(mActivity)
                }
                "宗地房屋图"->{
                    ZXPhotoPreview.builder()
                        .setPhotos(arrayListOf<String>().apply {
                            add(R.drawable.ground_picture.toString())
                        })
                        .setCurrentItem(0)
                        .start(mActivity)
                }
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_result_show
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
