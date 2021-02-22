package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.giscollect.base.toJson
import com.gt.base.app.CheckBean
import com.gt.giscollect.module.collect.func.adapter.CollectCheckAdapter
import com.gt.giscollect.module.collect.mvp.contract.CollectCheckContract
import com.gt.giscollect.module.collect.mvp.model.CollectCheckModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectCheckPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.gt.base.manager.UserManager
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_collect_check.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectCheckFragment : BaseFragment<CollectCheckPresenter, CollectCheckModel>(),
    CollectCheckContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectCheckFragment {
            val fragment = CollectCheckFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private val checkList = arrayListOf<CheckBean>()
    private val checkAdapter = CollectCheckAdapter(checkList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_check
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rv_collect_check.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = checkAdapter
        }

        ZXRecyclerDeleteHelper(activity, rv_collect_check)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->

            }
            .setClickable { position ->

            }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

    }

    override fun onCheckListResult(checkList: List<CheckBean>) {
        this.checkList.clear()
        this.checkList.addAll(checkList)
        checkAdapter.notifyDataSetChanged()
    }

    fun reInit() {
        mPresenter.getCheckList(hashMapOf("collector" to UserManager.user?.userName).toJson())
    }
}
