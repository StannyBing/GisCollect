package com.gt.entrypad.module.project.ui.change.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.gt.base.activity.BaseActivity
import com.gt.base.app.CheckBean
import com.gt.base.app.ConstStrings
import com.gt.base.bean.toJson
import com.gt.base.manager.UserManager
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.gt.entrypad.module.project.func.adapter.ProjectListAdapter
import com.gt.entrypad.module.project.func.view.titleView.TitleViewViewModel
import com.gt.entrypad.module.project.ui.DrawSketchActivity
import com.gt.entrypad.module.project.ui.DrawTemplateDownloadActivity
import com.gt.entrypad.module.project.ui.ProjectListActivity

import com.gt.entrypad.module.project.ui.change.mvp.contract.WorkListContract
import com.gt.entrypad.module.project.ui.change.mvp.model.WorkListModel
import com.gt.entrypad.module.project.ui.change.mvp.presenter.WorkListPresenter
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.MapTool
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXTimeUtil
import kotlinx.android.synthetic.main.activity_work_list.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File
import java.text.SimpleDateFormat


/**
 * Create By admin On 2017/7/11
 * 功能：工程列表
 */
@Route(path = RouterPath.PROJECT_LIST)
class WorkListActivity : BaseActivity<WorkListPresenter, WorkListModel>(), WorkListContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, WorkListActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }

    }


    private var dataList = arrayListOf<ProjectListBean>()
    private var projectAdapter = ProjectListAdapter(dataList)
    private var totalCheckList = arrayListOf<ProjectListBean>()
    private var submitPos = -1

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_work_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        leftTv.visibility = View.GONE
        right2Tv.visibility = View.GONE
        sp_work_type
            .setData(ConstStrings.mGuideBean.getTemplates())
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(14)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setDefaultItem(0)
            .build()

        ivBack.visibility = View.VISIBLE

        rightTv.setData(TitleViewViewModel(getString(R.string.moduleDownload)))
        rightTv.visibility = View.VISIBLE
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //返回
        ivBack.setOnClickListener {
            finish()
        }
        //下载模板
        rightTv.setActionListener(object : ICustomViewActionListener {
            override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                DrawTemplateDownloadActivity.startAction(this@WorkListActivity, false)
            }
        })
        //刷新
        sr_work_list.setOnRefreshListener {
            loadData()
        }
        //切换状态
        rg_work_status.setOnCheckedChangeListener { _, _ ->
            setCheckList()
        }
        //切换类别
        sp_work_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (sp_work_type.selectedValue.toString().isNotEmpty()) {
                    loadData()
                }
            }
        }
        //创建
        btn_work_create.setOnClickListener {
            moduleDialog()
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        mPresenter.getProject(
            hashMapOf(
                "currPage" to 0,
                "pageSize" to 15, "filters" to arrayListOf(
                    hashMapOf("col" to "user_id", "op" to "=", "val" to UserManager.user?.userId),
                    hashMapOf(
                        "col" to "template_id",
                        "op" to "=",
                        "val" to sp_work_type.selectedValue.toString()
                    )
                )
            ).toJson()
        )
    }

    /**
     * 设置页面数据
     */
    private fun setCheckList() {
        dataList.clear()
        dataList.addAll(if (rb_work_local.isChecked) {
            totalCheckList.filter {
                it.checkInfo == null
            }
        } else if (rb_work_net.isChecked) {
            totalCheckList.filter {
                it.checkInfo != null
            }
        } else {
            totalCheckList
        })
        projectAdapter.notifyDataSetChanged()
    }

    /**
     * 获取模板列表
     */
    private fun moduleDialog() {
        val createView = LayoutInflater.from(mContext).inflate(R.layout.layout_work_create, null)
        val etName = createView.findViewById<EditText>(R.id.et_work_create_name)
        val rvTempList = createView.findViewById<RecyclerView>(R.id.rv_work_create_temp)
        val tempList = ConstStrings.mGuideBean.getTemplates()
        var selectTemp = ""
        rvTempList.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = object : ZXQuickAdapter<KeyValueEntity, ZXBaseHolder>(
                R.layout.item_work_create_list,
                tempList
            ) {
                override fun convert(helper: ZXBaseHolder, item: KeyValueEntity) {
                    helper.setText(
                        R.id.tv_work_create_tempName,
                        item.key + if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
                            ""
                        } else {
                            "(未下载)"
                        }
                    )
                    helper.setBackgroundRes(
                        R.id.iv_work_temp_select, if (item.value.toString() == selectTemp) {
                            R.mipmap.select
                        } else {
                            R.mipmap.not_select
                        }
                    )
                }
            }.apply {
                setOnItemClickListener { adapter, view, position ->
                    selectTemp = tempList[position].value.toString()
                    notifyDataSetChanged()
                    if (!ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
                        DrawTemplateDownloadActivity.startAction(this@WorkListActivity, false)
                    }
                }
            }

        }
        BUIDialog.showCustom(mContext, "创建工程", createView, BUIDialog.BtnBuilder().withSubmitBtn {
            if (etName.text.toString().isEmpty()) {
                showToast("请输入工程名称")
            } else if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
//                DrawSketchActivity.startAction(
//                    this@WorkListActivity,
//                    false,
//                    ZXTimeUtil.getTime(
//                        System.currentTimeMillis(),
//                        SimpleDateFormat("yyyyMMdd_HHmmss")
//                    )
//                )
                //TODO
            } else {
                showToast("请下载模板")
                DrawTemplateDownloadActivity.startAction(this@WorkListActivity, false)

            }
        }.withCancelBtn())
    }

    override fun onSurveyUpload(name: String) {
        showToast("上传成功")
        if (!dataList.isNullOrEmpty()) {
            val bean = dataList[submitPos]
            bean.featureLayer?.let {
                FileUtils.deleteFilesByName(
                    ConstStrings.getSketchLayersPath(),
                    it.name
                )
                DeleteLayerFileTool.deleteFileByLayer(
                    ConstStrings.getSketchLayersPath() + it.name + "/file/",
                    it
                )
                FileUtils.deleteFiles(mContext.filesDir.path + "/${dataList[submitPos].id}/sketch/draw.jpg")
                val list = mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)
                if (!list.isNullOrEmpty()) list.remove(ConstStrings.sktchId)
                mSharedPrefUtil.putList(ConstStrings.SketchIdList, list)
                totalCheckList.remove(dataList[submitPos])
                dataList.removeAt(submitPos)
                projectAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onProjectList(result: List<CheckBean>?) {

    }

    override fun onDownloadProgress(progress: Int) {

    }

    override fun onCollectDownload(file: File) {

    }

}
