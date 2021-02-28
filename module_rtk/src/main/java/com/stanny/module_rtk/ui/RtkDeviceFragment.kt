package com.stanny.module_rtk.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.stanny.module_rtk.R
import com.stanny.module_rtk.mvp.contract.RtkDeviceContract
import com.stanny.module_rtk.mvp.model.RtkDeviceModel
import com.stanny.module_rtk.mvp.presenter.RtkDevicePresenter
import com.stanny.module_rtk.tool.WHandTool
import com.zx.zxutils.util.ZXDialogUtil
import kotlinx.android.synthetic.main.fragment_rtk_device.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class RtkDeviceFragment : BaseFragment<RtkDevicePresenter, RtkDeviceModel>(),
    RtkDeviceContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): RtkDeviceFragment {
            val fragment = RtkDeviceFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_rtk_device
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    private fun initWHand() {
        WHandTool.registerWHand(mContext, object : WHandTool.WHandRegisterListener {
            override fun initWHandAccount(): WHandTool.WHandAccount {
                val whandAccount = WHandTool.WHandAccount(
                    et_rtk_ip.text.toString(),
                    et_rtk_port.text.toString().toInt(),
                    et_rtk_account.text.toString(),
                    et_rtk_password.text.toString(),
                    et_rtk_point.text.toString()
                )
                mSharedPrefUtil.putObject("whandAccount", whandAccount)
                return whandAccount
            }

            override fun onDeviceStatusChange(context: Context, status: Int) {
                super.onDeviceStatusChange(context, status)
                if (WHandTool.isRegister()) {
                    reInit()
                }
            }

        })
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        btn_rtk_connect.setOnClickListener {
            if (et_rtk_ip.text.isEmpty()) {
                showToast("请输入IP地址")
            } else if (et_rtk_port.text.isEmpty()) {
                showToast("请输入端口号")
            } else if (et_rtk_account.text.isEmpty()) {
                showToast("请输入账户")
            } else if (et_rtk_password.text.isEmpty()) {
                showToast("请输入密码")
            } else if (et_rtk_point.text.isEmpty()) {
                showToast("请输入挂载点")
            } else {
                initWHand()
            }
        }
        btn_rtk_cancel.setOnClickListener {
            ZXDialogUtil.showYesNoDialog(
                context,
                "提示",
                "是否断开当前设备连接？"
            ) { dialog, which ->
                WHandTool.disConnectDivice()
                reInit()
            }
        }
        sw_rtk_open.setOnCheckedChangeListener { buttonView, isChecked ->
            WHandTool.isOpen = isChecked
            reInit()
        }
        sw_rtk_openLaser.setOnCheckedChangeListener { buttonView, isChecked ->
            WHandTool.openLaser(isChecked)
        }
    }

    fun reInit() {
        val whandAccount = mSharedPrefUtil.getObject<WHandTool.WHandAccount>("whandAccount")
        et_rtk_ip.setText(whandAccount?.ip ?: "183.230.183.10")
        et_rtk_port.setText((whandAccount?.port ?: "2102").toString())
        et_rtk_account.setText(whandAccount?.account ?: "")
        et_rtk_password.setText(whandAccount?.password ?: "")
        et_rtk_point.setText(whandAccount?.mountpoint ?: "RTKRTCM32")

        if (WHandTool.isRegister()) {
            btn_rtk_connect.visibility = View.GONE
            btn_rtk_cancel.visibility = View.VISIBLE
        } else {
            btn_rtk_cancel.visibility = View.GONE
            btn_rtk_connect.visibility = View.VISIBLE
        }

        sw_rtk_open.isChecked = WHandTool.isOpen
        if (WHandTool.isOpen) {
            sv_rtk_info.visibility = View.VISIBLE
            ll_rtk_btn.visibility = View.VISIBLE
        } else {
            sv_rtk_info.visibility = View.GONE
            ll_rtk_btn.visibility = View.GONE
        }
    }
}
