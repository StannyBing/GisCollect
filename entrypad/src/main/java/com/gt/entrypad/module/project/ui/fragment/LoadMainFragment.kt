package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.gt.base.bean.RtkInfoBean
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.stanny.module_rtk.ui.RtkDeviceFragment
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_load_main.iv_collect_title_back
import kotlinx.android.synthetic.main.fragment_load_main.tv_collect_title_name
import java.io.Serializable

class LoadMainFragment :BaseFragment<SketchMainPresenter,SketchMainModel>(),SketchMainContract.View,FragChangeListener{
    private lateinit var sitePointFragment: SitePointFragment
    private lateinit var rtkPointFragment: RTKPointFragment
    private lateinit var rtkDeviceFragment: RtkDeviceFragment

    private var currentFragType = Site_Point

    companion object {
        /**
         * 启动器
         */
        fun newInstance(vararg params: Serializable): LoadMainFragment {
            val fragment = LoadMainFragment()
            val bundle = Bundle()
            params.forEachIndexed { index, serializable ->
                bundle.putSerializable("p$index",serializable)
            }
            fragment.arguments = bundle
            return fragment
        }
        const val Site_Point ="界址列表"
        const val RTK_Point = "RTK打点"
        const val RTK_Set = "定位设置"

    }
    override fun initView(savedInstanceState: Bundle?) {
        sitePointFragment = SitePointFragment.newInstance()
        rtkPointFragment = RTKPointFragment.newInstance()
        rtkDeviceFragment = RtkDeviceFragment.newInstance()
        rtkDeviceFragment.fragChangeListener = this

        sitePointFragment.fragChangeListener = this
        rtkPointFragment.fragChangeListener = this
        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(sitePointFragment)
            add(rtkPointFragment)
            add(rtkDeviceFragment)

        }, R.id.fm_load_main, 0)

        tv_collect_title_name.text = currentFragType
        super.initView(savedInstanceState)
    }
    override fun onViewListener() {
        //返回
        iv_collect_title_back.setOnClickListener {
            onFragBack(currentFragType)
        }

        rootView.setOnClickListener(null)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_load_main
    }

    override fun onFragBack(type: String, any: Any?) {
       when(type){
           RTK_Point->{
               onFragGoto(Site_Point,any)
           }
           RTK_Set -> {
               onFragGoto(RTK_Point)
           }
       }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_collect_title_name.text = type
        when(currentFragType){
            Site_Point->{
                iv_collect_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(sitePointFragment)
                if (any is ArrayList<*>){
                    val list = any as ArrayList<RtkPointBean>
                    sitePointFragment.refreshData(list)
                }else if (any is RtkPointBean){
                    sitePointFragment.refreshData(any)
                }
            }
            RTK_Point->{
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(rtkPointFragment)
                rtkPointFragment.showData(any)
            }
            RTK_Set -> {
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(rtkDeviceFragment)
                rtkDeviceFragment.reInitWhandInfo()
            }
        }
    }
}