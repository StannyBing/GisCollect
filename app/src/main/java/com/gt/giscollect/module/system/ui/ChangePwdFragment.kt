package com.gt.giscollect.module.system.ui

import android.os.Bundle
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.base.FragChangeListener
import com.gt.giscollect.module.system.mvp.contract.ChangePwdContract
import com.gt.giscollect.module.system.mvp.model.ChangePwdModel
import com.gt.giscollect.module.system.mvp.presenter.ChangePwdPresenter
import com.gt.base.manager.UserManager
import com.zx.zxutils.util.ZXDialogUtil
import kotlinx.android.synthetic.main.fragment_change_pwd.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ChangePwdFragment : BaseFragment<ChangePwdPresenter, ChangePwdModel>(),
    ChangePwdContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): ChangePwdFragment {
            val fragment = ChangePwdFragment()
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
        return R.layout.fragment_change_pwd
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        btn_change_pwd.setOnClickListener {
            if (et_changepwd_old.text.toString().isEmpty() || et_changepwd_new1.text.toString().isEmpty() || et_changepwd_new2.text.toString().isEmpty()) {
                showToast("请完成输入！")
            } else if (et_changepwd_old.text.toString() != UserManager.passWord) {
                showToast("原密码输入错误，请重试")
            } else if (et_changepwd_new1.text.toString() != et_changepwd_new2.text.toString()) {
                showToast("两次新密码输入不一致，请重试")
            } else if (et_changepwd_new1.text.toString().length < 6) {
                showToast("密码长度最低不能少于6位")
            } else {
                ZXDialogUtil.showYesNoDialog(
                    mContext,
                    "提示",
                    "是否确认修改密码，修改完成后，需重新登录？"
                ) { dialog, which ->
                    mPresenter.changePwd(hashMapOf("pwd" to et_changepwd_new1.text.toString()))
                }
            }
        }
    }

    fun reInit() {
        et_changepwd_old.setText("")
        et_changepwd_new1.setText("")
        et_changepwd_new2.setText("")
    }

    override fun onChangePwdResult() {
        showToast("密码修改成功，请重新登录")
        UserManager.loginOut()
        LoginActivity.startAction(requireActivity(), true)
    }
}
