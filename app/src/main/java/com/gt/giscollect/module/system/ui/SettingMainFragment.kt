package com.gt.giscollect.module.system.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gt.giscollect.R
import com.gt.giscollect.base.BaseFragment
import com.gt.giscollect.base.FragChangeListener
import com.gt.giscollect.module.system.mvp.contract.SettingMainContract
import com.gt.giscollect.module.system.mvp.model.SettingMainModel
import com.gt.giscollect.module.system.mvp.presenter.SettingMainPresenter
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_collect_main.*
import kotlinx.android.synthetic.main.fragment_setting_main.*

/**
 * Create By XB
 * 功能：设置
 */
class SettingMainFragment : BaseFragment<SettingMainPresenter, SettingMainModel>(), SettingMainContract.View, FragChangeListener {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SettingMainFragment {
            val fragment = SettingMainFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        const val Setting_List = "设置"
        const val Setting_Pwd = "密码修改"
        const val Template_List = "模板下载"
        const val Data_List = "数据下载"
    }

    private lateinit var settingListFragment: SettingListFragment
    private lateinit var templateDownloadFragment: TemplateDownloadFragment
    private lateinit var dataDownloadFragment: DataDownloadFragment
    private lateinit var changePwdFragment: ChangePwdFragment

    private var currentFragType = Setting_List

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_setting_main
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        settingListFragment = SettingListFragment.newInstance()
        templateDownloadFragment = TemplateDownloadFragment.newInstance()
        dataDownloadFragment = DataDownloadFragment.newInstance()
        changePwdFragment = ChangePwdFragment.newInstance()

        settingListFragment.fragChangeListener = this
        templateDownloadFragment.fragChangeListener = this
        dataDownloadFragment.fragChangeListener = this

        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(settingListFragment)
            add(templateDownloadFragment)
            add(dataDownloadFragment)
            add(changePwdFragment)
        }, R.id.fm_setting_main, 0)
        tv_setting_title_name.text = "设置"
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //返回
        iv_setting_title_back.setOnClickListener {
            onFragBack(currentFragType)
        }
    }

    override fun onFragBack(type: String, any: Any?) {
        when (type) {
            Setting_List -> {

            }
            Setting_Pwd -> {
                onFragGoto(Setting_List)
            }
            Template_List -> {
                onFragGoto(Setting_List)
            }
            Data_List -> {
                onFragGoto(Setting_List)
            }
        }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_setting_title_name.text = type
        when (type) {
            Setting_List -> {
                iv_setting_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(settingListFragment)
            }
            Setting_Pwd -> {
                iv_setting_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(changePwdFragment)
                changePwdFragment.reInit()
            }
            Template_List -> {
                iv_setting_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(templateDownloadFragment)
            }
            Data_List -> {
                iv_setting_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(dataDownloadFragment)
            }
        }
    }
}
