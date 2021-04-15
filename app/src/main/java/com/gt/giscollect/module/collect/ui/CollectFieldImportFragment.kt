package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.manager.UserManager
import com.gt.giscollect.R
import com.gt.base.bean.toJson
import com.gt.giscollect.module.collect.bean.FieldImportBean
import com.gt.giscollect.module.collect.func.adapter.FieldImportAdapter
import com.gt.giscollect.module.collect.mvp.contract.CollectFieldImportContract
import com.gt.giscollect.module.collect.mvp.model.CollectFieldImportModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectFieldImportPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.fragment_collect_field_import.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class CollectFieldImportFragment :
    BaseFragment<CollectFieldImportPresenter, CollectFieldImportModel>(),
    CollectFieldImportContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectFieldImportFragment {
            val fragment = CollectFieldImportFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null

    private val fieldImportList = arrayListOf<FieldImportBean>()
    private val fieldImportAdapter = FieldImportAdapter(fieldImportList)

    private var pageNum = 0

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_field_import
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_field_import_list.apply {
            layoutManager = LinearLayoutManager(mContext) as RecyclerView.LayoutManager?
            adapter = fieldImportAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }

        super.initView(savedInstanceState)
        loadData()
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //搜索
        tv_field_import_search.setOnClickListener {
            loadData()
        }
        et_field_import_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ZXSystemUtil.closeKeybord(requireActivity())
                loadData()
            }
            true
        }
        //刷新
        sr_field_import_list.setOnRefreshListener {
            loadData()
        }
        //加载更多
//        fieldImportAdapter.setEnableLoadMore(true)
//        fieldImportAdapter.setOnLoadMoreListener({
//            pageNum += pageSize
//            loadData()
//        }, rv_field_import_list)
        //点击事件
        fieldImportAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.ll_field_import_title -> {
                    val isExpand = !fieldImportList[position].isExpand
                    fieldImportList.filter { it.isExpand }
                        .forEach {
                            it.isExpand = false
                        }
                    fieldImportList[position].isExpand = isExpand
                    fieldImportAdapter.notifyDataSetChanged()
                }
                R.id.iv_field_import_check -> {
                    fragChangeListener?.onFragBack(
                        CollectMainFragment.Collect_Import,
                        fieldImportList[position]
                    )
                }
            }
        }
    }

    private fun loadData() {
//        if (et_field_import_search.text.isEmpty()) {
//            showToast("请输入关键字")
//            sr_field_import_list.isRefreshing = false
//        } else {
            mPresenter.getFieldList(
                hashMapOf(
                    "currPage" to 0,
                    "pageSize" to 9999,
                    "filters" to arrayListOf(
                        hashMapOf(
                            "col" to "姓名,身份证号",
                            "op" to "like",
                            "val" to et_field_import_search.text.toString()
                        ),
                        hashMapOf(
                            "col" to "business_id",
                            "op" to "like",
                            "val" to ConstStrings.mGuideBean.getBusinessesFirst()
                        ),
//                        hashMapOf(
//                            "col" to "user_id",
//                            "op" to "=",
//                            "val" to UserManager.user?.userId
//                        ),
                        hashMapOf(
                            "col" to "company_id",
                            "op" to "=",
                            "val" to UserManager.user?.companyId
                        )
                    )
                ).toJson()
            )
//        }
    }

    override fun onListResult(list: List<FieldImportBean>) {
        sr_field_import_list.isRefreshing = false
        fieldImportList.clear()
        fieldImportList.addAll(list)
        fieldImportAdapter.notifyDataSetChanged()
    }

    /**
     * 初始化界面
     */
    fun reInit() {

    }
}
