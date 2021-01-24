package com.gt.giscollect.module.system.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.giscollect.R
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.base.AppInfoManager
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.base.FragChangeListener
import com.gt.giscollect.module.collect.bean.VersionBean
import com.gt.giscollect.module.system.bean.SettingBean
import com.gt.giscollect.module.system.func.adapter.SettingListAdapter
import com.gt.giscollect.module.system.mvp.contract.SettingListContract
import com.gt.giscollect.module.system.mvp.model.SettingListModel
import com.gt.giscollect.module.system.mvp.presenter.SettingListPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.gt.giscollect.base.UserManager
import com.zx.zxutils.util.ZXAppUtil
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.fragment_setting_list.*
import org.json.JSONObject
import java.io.File

/**
 * Create By XB
 * 功能：设置-列表
 */
class SettingListFragment :BaseFragment<SettingListPresenter, SettingListModel>(),
    SettingListContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SettingListFragment {
            val fragment = SettingListFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private val settingList = arrayListOf<SettingBean>()
    private val settingAdapter = SettingListAdapter(settingList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_setting_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        settingList.add(
            SettingBean(
                UserManager.user?.userName ?: "",
                R.drawable.icon_setting_user,
                false
            )
        )
        settingList.add(
            SettingBean(
                UserManager.user?.companyName ?: "",
                R.drawable.icon_setting_dept,
                false
            )
        )
        settingList.add(SettingBean("服务器切换", R.drawable.icon_setting_server, true))
        settingList.add(SettingBean("模板下载", R.drawable.icon_setting_template, true))
        settingList.add(SettingBean("数据下载", R.drawable.icon_setting_data, true))
        settingList.add(SettingBean("检查更新（${ZXSystemUtil.getVersionName()}）", R.drawable.icon_setting_update, true))
        settingList.add(SettingBean("密码修改", R.drawable.icon_setting_changepwd, true))
        settingList.add(SettingBean("清除缓存", R.drawable.icon_setting_cache, true))
        settingList.add(SettingBean("退出登录", R.drawable.icon_setting_logout, true))

        rv_setting_list.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = settingAdapter
        }

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        settingAdapter.setOnItemClickListener { adapter, view, position ->
            if (settingList[position].name.startsWith("检查更新")) {
                mPresenter.getVersion()
                return@setOnItemClickListener
            }
            when (settingList[position].name) {
                "用户信息" -> {

                }
                "所属部门" -> {

                }
                "服务器切换" -> {
                    if (AppInfoManager.appInfo?.apiInfo == null) {
                        showToast("暂未获取到服务器信息！")
                        return@setOnItemClickListener
                    }
                    AppInfoManager.appInfo?.apiInfo?.let { apiList ->
                        if (apiList.isEmpty()) {
                            showToast("暂未获取到服务器信息！")
                            return@let
                        }
                        val items = arrayListOf<String>().apply {
                            apiList.forEach {
                                val obj = JSONObject(it)
                                this.add(
                                    if (mSharedPrefUtil.getString(
                                            "base_ip",
                                            "http://xianrui.vip:8080/"
                                        ).contains(obj.getString("ip"))
                                        || mSharedPrefUtil.getString(
                                            "base_ip",
                                            "http://xianrui.vip:8080/"
                                        ).contains(obj.getString("domain"))
                                    ) {
                                        obj.getString("itemName") + "---当前"
                                    } else {
                                        obj.getString("itemName")
                                    }
                                )
                            }
                        }
                        ZXDialogUtil.showListDialog(
                            mContext,
                            "服务器切换",
                            "取消",
                            items
                        ) { dialog, which ->
                            if (items[which].contains("---当前")) {
                                showToast("该服务器正在使用，无需修改")
                            } else {
                                val obj = JSONObject(apiList[which])
                                val name = obj.getString("itemName")
                                val url = obj.getString("ip")
                                ZXDialogUtil.showYesNoDialog(
                                    mContext,
                                    "提示",
                                    "是否切换服务器至--$name, 点击确认将切换服务器并重启应用？"
                                ) { _, _ ->
                                    mSharedPrefUtil.putString("base_ip", "http://$url/")
                                    MyApplication.instance.reinit()
                                    SplashActivity.startAction(requireActivity(), true)
                                }
                            }
                        }
                    }
                }
                "模板下载" -> {
                    fragChangeListener?.onFragGoto(SettingMainFragment.Template_List)
                }
                "数据下载" -> {
                    fragChangeListener?.onFragGoto(SettingMainFragment.Data_List)
                }
                "检查更新" -> {
                    mPresenter.getVersion()
                }
                "密码修改" -> {
                    fragChangeListener?.onFragGoto(SettingMainFragment.Setting_Pwd)
                }
                "清除缓存" -> {
                    ZXDialogUtil.showYesNoDialog(
                        requireActivity(),
                        "提示",
                        "是否清除缓存数据？"
                    ) { dialog, which ->
                        showLoading("正在清理")
                        ZXAppUtil.cleanInternalCache()
                        handler.postDelayed({
                            dismissLoading()
                            showToast("清理完成")
                        }, 500)
                    }
                }
                "退出登录" -> {
                    ZXDialogUtil.showYesNoDialog(
                        requireActivity(),
                        "提示",
                        "是否退出登录？"
                    ) { dialog, which ->
                        LoginActivity.startAction(requireActivity(), true)
                        UserManager.loginOut()
                    }
                }
            }
        }
    }

    override fun onVersionResult(versionBean: VersionBean) {
        if (ZXSystemUtil.getVersionCode() < versionBean.versionCode) {
            val content = if (versionBean.content.isNullOrEmpty()) {
                ""
            } else {
                "-" + versionBean.content
            }
            ZXDialogUtil.showYesNoDialog(
                mContext,
                "提示",
                "检测到最新版本：${versionBean.versionName}${content}，是否立即更新"
            ) { dialog, which ->
                mPresenter.downloadApk(versionBean)
            }
        } else {
            showToast("已是最新版本")
        }
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
    }

    override fun onApkDownloadResult(file: File) {
        ZXDialogUtil.dismissLoadingDialog()
        ZXAppUtil.installApp(mActivity, file.path)
    }
}
