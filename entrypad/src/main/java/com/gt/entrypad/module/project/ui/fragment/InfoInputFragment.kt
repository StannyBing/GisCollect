package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.gt.base.fragment.BaseFragment
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
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
import kotlinx.android.synthetic.main.fragment_info_input.*
import rx.functions.Action1

@Route(path = RouterPath.INFO_INPUT)
class InfoInputFragment : BaseFragment<InfoInputPresenter, InfoInputModel>(),InfoInputContract.View{
    private var dataList = arrayListOf<InputInfoBean>()
    private var infoAdapter = InfoInputAdapter(dataList)
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): InfoInputFragment {
            val fragment = InfoInputFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
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
        return R.layout.fragment_info_input
    }

    private fun initData(){
        dataList.apply {
            add(InputInfoBean(1,TitleViewViewModel("基础登记信息").apply {
                resId = R.style.titleText
            }))
            add(InputInfoBean(2,EditTextViewViewModel(title = "权利人姓名",isRequired = true,requiredContent = "(必填)",hint = "请输入姓名")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "权利人身份证号",hint = "请输入身份证号码")))
            add(InputInfoBean(1,TitleViewViewModel("房屋基本信息").apply {
                resId=R.style.titleText
            }))
            add(InputInfoBean(2,EditTextViewViewModel(title = "宗地代码",hint = "请输入宗地代码")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "栋号",hint = "请输入栋号")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "户号",hint = "请输入户号")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "房屋坐落",isRequired = true,requiredContent = "(必填，注意修改)")))
            add(InputInfoBean(3,SpinnerViewViewModel(title = "登记类型",entries = arrayListOf<KeyValueEntity>().apply {
                add(KeyValueEntity("请选择登记类型","0"))
                add(KeyValueEntity("首次登记","1"))
            })))
            add(InputInfoBean(4,InfoDialogViewViewModel(title = "房屋结构",hint = "请选择房屋结构")))
            add(InputInfoBean(3,SpinnerViewViewModel(title = "层数",entries = arrayListOf<KeyValueEntity>().apply {
                add(KeyValueEntity("请选择层数","0"))
                add(KeyValueEntity("1","1"))
                add(KeyValueEntity("2","2"))
                add(KeyValueEntity("3","3"))
                add(KeyValueEntity("4","4"))
                add(KeyValueEntity("5","5"))
            })))
            add(InputInfoBean(3,SpinnerViewViewModel(title = "所在名义层",entries = arrayListOf<KeyValueEntity>().apply {
                add(KeyValueEntity("请选择层数","0"))
                add(KeyValueEntity("第1层","1"))
                add(KeyValueEntity("第2层","2"))
                add(KeyValueEntity("第3层","3"))
                add(KeyValueEntity("第4层","4"))
                add(KeyValueEntity("第5层","5"))
            })))
            add(InputInfoBean(2,EditTextViewViewModel(title = "批准土地面积",hint = "请输入批准使用面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "批准建筑面积",hint = "请输入批准建筑面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "专有建筑面积",hint = "请输入专有建筑面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "分摊面积",hint = "请输入分摊面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "原土地使用面积",hint = "请输入原土地使用面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "乡村房屋所有权证号",hint = "请输入乡村房屋所有权证号")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "实际土地使用面积",hint = "请输入实际土地使用面积(m²)")))
            add(InputInfoBean(2,EditTextViewViewModel(title = "集体土地使用证号",hint = "请输入集体土地使用证号")))

        }
    }

    fun getData():ArrayList<InputInfoBean>{
        return dataList
    }
}