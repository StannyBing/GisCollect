package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_load_main.*
import kotlinx.android.synthetic.main.fragment_load_main.iv_collect_title_back
import kotlinx.android.synthetic.main.fragment_load_main.tv_collect_title_name
import kotlinx.android.synthetic.main.fragment_sketch_main.*
import java.io.Serializable

class LoadMainFragment :BaseFragment<SketchMainPresenter,SketchMainModel>(),SketchMainContract.View,FragChangeListener{
    private lateinit var sitePointFragment: SitePointFragment
    private lateinit var rtkPointFragment: RTKPointFragment
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
    }
    override fun initView(savedInstanceState: Bundle?) {
        sitePointFragment = SitePointFragment.newInstance()
        rtkPointFragment = RTKPointFragment.newInstance()
        sitePointFragment.fragChangeListener = this
        rtkPointFragment.fragChangeListener = this
        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(sitePointFragment)
            add(rtkPointFragment)
        }, R.id.fm_load_main, 0)

        tv_collect_title_name.text = currentFragType
        super.initView(savedInstanceState)
    }
    override fun onViewListener() {
        //返回
        iv_collect_title_back.setOnClickListener {
            onFragBack(currentFragType)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_load_main
    }

    override fun onFragBack(type: String, any: Any?) {
       when(type){
           RTK_Point->{
               onFragGoto(Site_Point,any)
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
                }
            }
            RTK_Point->{
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(rtkPointFragment)
                rtkPointFragment.showData(any)
            }
        }
    }
}