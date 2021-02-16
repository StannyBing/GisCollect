package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.bean.IDCardInfoBean
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.func.InfoInputAdapter
import com.gt.entrypad.module.project.mvp.contract.InfoInputContract
import com.gt.entrypad.module.project.mvp.model.InfoInputModel
import com.gt.entrypad.module.project.mvp.presenter.InfoInputPresenter
import com.gt.entrypad.module.project.ui.view.editText.EditTextViewViewModel
import com.gt.entrypad.module.project.ui.view.infoDialogView.InfoDialogViewViewModel
import com.gt.entrypad.module.project.ui.view.spinnerView.SpinnerViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.entity.KeyValueEntity
import kotlinx.android.synthetic.main.activity_info_input.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import rx.functions.Action1

@Route(path = RouterPath.INFO_INPUT)
class InfoInputActivity : BaseActivity<InfoInputPresenter, InfoInputModel>(),InfoInputContract.View{
    private var dataList = arrayListOf<InputInfoBean>()
    private var infoAdapter = InfoInputAdapter(dataList)
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, InfoInputActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        recyclerView.apply {
            layoutManager= GridLayoutManager(mContext, 2).apply {
                spanSizeLookup = object :GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        when(dataList[position].itemType){
                            1->{
                                return  2
                            }
                            else ->{
                                1
                            }
                        }
                        return 1
                    }

                }
            }
            adapter = infoAdapter
        }
        toolBarTitleTv.text = getString(R.string.registrationInfo)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@InfoInputActivity)
                }

            })
        }
        rightTv.apply {
            visibility = View.VISIBLE
            setData(TitleViewViewModel(getString(R.string.nextStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    var infoList = arrayListOf<String>()
                    dataList.forEach {
                        if (it.itemType==2){
                            infoList.add((it.data as EditTextViewViewModel).inputContent)
                        }
                    }
                    TakePhotoActivity.startAction(this@InfoInputActivity,false,infoList)
                }

            })
        }
        initData()

    }


    override fun onViewListener() {
        mRxManager.on("notify", Action1 <InfoDialogViewViewModel>{
           kotlin.run data@{
               dataList.forEachIndexed { index, inputInfoBean ->
                   if (inputInfoBean.data==it){
                       infoAdapter.notifyItemChanged(index,"notify")
                       return@data
                   }
               }
           }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_info_input
    }

    private fun initData(){
        //先获取身份信息
        val cardInfo = mSharedPrefUtil.getString("cardInfo")
        var name = ""
        var sex = ""
        var card =""
        var address =""
        var cardInfoData:List<IDCardInfoBean>?=null
        if (cardInfo.isNotEmpty()){
           cardInfoData = Gson().fromJson<List<IDCardInfoBean>>(cardInfo,object :TypeToken<List<IDCardInfoBean>>(){}.type)
        }
        if (!cardInfoData.isNullOrEmpty()&&cardInfoData.size==5){
            name =  cardInfoData?.get(0)?.data?.worlds?:""
            sex =  cardInfoData?.get(1)?.data?.worlds?:""
            card =  cardInfoData?.get(5)?.data?.worlds?:""
            address =  cardInfoData?.get(4)?.data?.worlds?:""
        }


        dataList.apply {
            add(InputInfoBean(1,TitleViewViewModel("基础登记信息").apply {
                resId = R.style.titleText
            }))

            add(InputInfoBean(2,EditTextViewViewModel("姓名","请输入姓名",inputContent = name)))
            add(InputInfoBean(2,EditTextViewViewModel("性别","请输入性别",inputContent = sex)))
            add(InputInfoBean(2,EditTextViewViewModel("身份证号码","请输入身份证号码",inputContent = card)))
            add(InputInfoBean(2,EditTextViewViewModel("联系电话","请输入联系电话")))
            add(InputInfoBean(2,EditTextViewViewModel("乡镇街道","请输入乡镇街道")))
            add(InputInfoBean(2,EditTextViewViewModel("村名","请输入村名")))
            add(InputInfoBean(2,EditTextViewViewModel("村小组","请输入村小组")))
            add(InputInfoBean(2,EditTextViewViewModel("家庭住址","请输入家庭住址",inputContent = address)))
            add(InputInfoBean(2,EditTextViewViewModel("申请理由","请输入申请理由")))

            add(InputInfoBean(1,TitleViewViewModel("房屋基本信息").apply {
                resId=R.style.titleText
            }))
            add(InputInfoBean(2,EditTextViewViewModel("宅基地面积","请输入宅基地面积(㎡)")))
            add(InputInfoBean(2,EditTextViewViewModel("房基占地面积","请输入宅基地面积(㎡)")))
            add(InputInfoBean(2,EditTextViewViewModel("拟批地址","请输入拟批地址")))
            add(InputInfoBean(2,EditTextViewViewModel("占建设用地","请输入占建设用地")))
            add(InputInfoBean(2,EditTextViewViewModel("占未利用地","请输入占建设未利用地")))
            add(InputInfoBean(2,EditTextViewViewModel("占农用地","请输入占农用地")))
            add(InputInfoBean(2,EditTextViewViewModel("住宅建筑面积","请输入住宅建筑面积(㎡)")))
            add(InputInfoBean(2,EditTextViewViewModel("建筑层数","请输入建筑层数")))
            add(InputInfoBean(2,EditTextViewViewModel("建筑高度","请输入建筑高度")))
            add(InputInfoBean(2,EditTextViewViewModel("性质","请输入性质")))
            add(InputInfoBean(2,EditTextViewViewModel("占地面积","请输入占地面积(㎡)")))
            add(InputInfoBean(2,EditTextViewViewModel("东至","请输入东至")))
            add(InputInfoBean(2,EditTextViewViewModel("南至","请输入南至")))
            add(InputInfoBean(2,EditTextViewViewModel("西至","请输入西至")))
            add(InputInfoBean(2,EditTextViewViewModel("北至","请输入北至")))
            add(InputInfoBean(2,EditTextViewViewModel("选址日期","请输入选址日期")))
            add(InputInfoBean(2,EditTextViewViewModel("备注","请输入")))


        }
    }

    fun getData():ArrayList<InputInfoBean>{
        return dataList
    }
}