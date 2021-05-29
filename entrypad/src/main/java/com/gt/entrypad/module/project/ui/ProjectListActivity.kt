package com.gt.entrypad.module.project.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.activity.BaseActivity
import com.gt.base.app.CheckBean
import com.gt.base.app.ConstStrings
import com.gt.base.bean.toJson
import com.gt.base.manager.UserManager
import com.gt.entrypad.app.RouterPath
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel

import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import com.gt.entrypad.module.project.mvp.model.ProjectListModel
import com.gt.entrypad.module.project.mvp.presenter.ProjectListPresenter
import com.gt.entrypad.module.project.func.view.titleView.TitleViewViewModel
import com.gt.entrypad.R
import com.gt.entrypad.app.ConstString
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.gt.entrypad.module.project.func.adapter.ProjectListAdapter
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.bui.ui.buidialog.BUIDialog
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.*
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.activity_project_list.*
import kotlinx.android.synthetic.main.fragment_sketch_create.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
@Route(path = RouterPath.PROJECT_LIST)
class ProjectListActivity : BaseActivity<ProjectListPresenter, ProjectListModel>(),
    ProjectListContract.View {
    private var data = arrayListOf<ProjectListBean>()
    private var projectAdapter = ProjectListAdapter(data)
    private var totalCheckList = arrayListOf<ProjectListBean>()
    private var currpage = 0
    private var templeteId = ""
    private var clickPosition = -1

    companion object {
        private const val ChangeTag = "sketch_list"
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, ProjectListActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    override fun onViewListener() {
        //刷新
        sr_project_layers.setOnRefreshListener {
            currpage = 0
            loadData()
        }
        //切换
        rgProjectLocal.setOnCheckedChangeListener { group, checkedId ->
            setCheckList()
        }
        spProjectType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                templeteId = spProjectType.selectedValue.toString()
                currpage = 0
                if (templeteId.isNotEmpty()) {
                    ConstStrings.drawTemplatesId = templeteId
                    loadData()
                }
            }

        }
    }

    private fun setCheckList() {
        data.clear()
//        data.addAll(totalCheckList)
        data.addAll(if (rb_project_listlocal.isChecked) {
            totalCheckList.filter {
                it.checkInfo == null
            }
        } else if (rb_project_listnet.isChecked) {
            totalCheckList.filter {
                it.checkInfo != null
            }
        } else {
            totalCheckList
        })
        projectAdapter.notifyDataSetChanged()
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_project_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        leftTv.visibility = View.GONE
        right2Tv.visibility = View.GONE
        spProjectType
            .setData(ConstStrings.mGuideBean.getTemplates())
            .showUnderineColor(false)
            .setItemHeightDp(40)
            .setItemTextSizeSp(14)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setDefaultItem(0)
            .build()
        ConstStrings.drawTempleteName = spProjectType.selectedKey.toString() + ".gpkg"
        ivBack.apply {
            this.visibility = View.VISIBLE
            setOnClickListener {
                finish()
            }
        }
//        right2Tv.apply {
//           this.visibility=View.VISIBLE
//           setData(TitleViewViewModel(getString(R.string.createProject)))
//           setActionListener(object : ICustomViewActionListener {
//               override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
//                   moduleDialog()
//               }
//
//           })
//       }

        rightTv.apply {
            setData(
                TitleViewViewModel(
                    getString(R.string.moduleDownload)
                )
            )
            this.visibility = View.VISIBLE
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    DrawTemplateDownloadActivity.startAction(this@ProjectListActivity, false)
                }

            })
        }
        btn_project_create.setOnClickListener {
            moduleDialog()
        }

        toolBarTitleTv.text = getString(R.string.registrationList)

        rv_project_layers.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = projectAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
        ZXRecyclerDeleteHelper(this, rv_project_layers)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                when (id) {
                    R.id.tv_delete -> {
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否删除该项目，这将同时删除该项目的草图数据？"
                        ) { dialog, which ->
                            ConstStrings.sktchId = data[pos].id
                            data[pos].featureLayer?.let {
                                FileUtils.deleteFilesByName(
                                    ConstStrings.getSketchLayersPath(),
                                    it.name
                                )
                                DeleteLayerFileTool.deleteFileByLayer(
                                    ConstStrings.getSketchLayersPath() + it.name + "/file/",
                                    it
                                )
                                FileUtils.deleteFiles(mContext.filesDir.path + "/${data[pos].id}/sketch/draw.jpg")
                                MapTool.postLayerChange(
                                    ChangeTag,
                                    it,
                                    MapTool.ChangeType.OperationalRemove
                                )
                                val list =
                                    mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)
                                if (!list.isNullOrEmpty()) list?.remove(ConstStrings.sktchId)
                                mSharedPrefUtil.putList(ConstStrings.SketchIdList, list)
                                totalCheckList.remove(data[pos])
                                data.removeAt(pos)
                                projectAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    R.id.tv_upload -> {
                        if (data[pos].featureLayer == null) {
                            ZXDialogUtil.showYesNoDialog(
                                mContext,
                                "提示",
                                "是否下载该条数据?"
                            ) { dialog, which ->
                                downloadProject(data[pos])
                            }
                            return@setSwipeable
                        }
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否上传该条任务数据？"
                        ) { dialog, which ->
                            clickPosition = pos
                            val files = FileUtils.getFilesByName(
                                ConstStrings.getSketchLayersPath(),
                                data[pos].featureLayer?.name ?: ""
                            )
                            files.firstOrNull { it.isDirectory }?.apply {
                                val path = ZipUtils.zip(path, false)
                                if (path != null) {
                                    mPresenter.uploadSurvey(
                                        path,
                                        data[pos].featureLayer?.name ?: "",
                                        templeteId,
                                        "",
                                        collectId = UUID.randomUUID().toString()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            .setClickable { position ->
                if (data[position].featureLayer == null) {
                    showToast("本机不存在该任务")
                    return@setClickable
                }
                ConstString.feature = data[position].featureLayer
                ConstStrings.sktchId = data[position].id
                ConstStrings.copyDrawPath = data[position].drawPath
                mSharedPrefUtil.putBool("isEdit", true)
                SketchLoadActivity.startAction(
                    this,
                    false
                )
            }
        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                loadData()
            }
        })
        super.initView(savedInstanceState)
    }

    private fun downloadProject(projectListBean: ProjectListBean) {
        getPermission(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            projectListBean.checkInfo?.let {
                mPresenter.downloadProject(it)
            }
        }
    }

    private fun loadData() {
        mPresenter.getProject(
            hashMapOf(
                "currPage" to currpage,
                "pageSize" to 15, "filters" to arrayListOf(
                    hashMapOf("col" to "user_id", "op" to "=", "val" to UserManager.user?.userId),
                    hashMapOf("col" to "template_id", "op" to "=", "val" to templeteId)
                )
            ).toJson()
        )
    }

    /**
     * 项目列表数据回调
     */
    override fun onProjectList(result: List<CheckBean>?) {
        if (currpage == 0) {
            data.clear()
            totalCheckList.clear()
        }
        var checkList = result ?: arrayListOf()
        val tempCheck = arrayListOf<CheckBean>().apply {
            addAll(checkList)
        }

        //拿到所有的模板id
        val map = mSharedPrefUtil.getMap<String, String>(ConstStrings.SketchIdList)
        map?.apply {
            entries.forEach { sketch ->
                ConstStrings.sktchId = sketch.key
                //取对应模板的文件夹
                val file = File(ConstStrings.getSketchLayersPath() + sketch.value)
                if (file.exists() && file.isDirectory) {
                    file.listFiles()?.forEach {
                        //拿到所有的gpkg
                        if (it.isFile && it.name.endsWith(".gpkg")) {
                            getFeatureLayerFromGpkg(checkList, tempCheck, it.path, sketch)
                        }
                    }
                }
            }
        }
        setCheckList()
        sr_project_layers.isRefreshing = false

//        var map = mSharedPrefUtil.getMap<String, String>(ConstStrings.SketchIdList)
//        map?.apply {
//            entries.forEach { sketch ->
//                ConstStrings.sktchId = sketch.key
//                val file = File(ConstStrings.getSketchLayersPath() + sketch.value)
//                if (file.exists() && file.isDirectory) {
//                    file.listFiles()?.forEach {
//                        if (it.isFile && it.name.endsWith(".gpkg")) {
//                            GeoPackageTool.getTablesFromGpkg(it.path) { featureTableList ->
//                                featureTableList.forEach { featureTable ->
//                                    data.add(
//                                        ProjectListBean(
//                                            id = sketch.key,
//                                            featureLayer = FeatureLayer(featureTable).apply {
//                                                name = sketch.value
//                                                featureTable.displayName = sketch.value
//                                            })
//                                    )
//                                }
//                                //循环判断本地是否跟线上的相等
//                                data.forEachIndexed { index, it ->
//                                    var bean: CheckBean? = null
//                                    checkList.forEach { check ->
//                                        if (it.featureLayer?.name == check.getFileName().replace(
//                                                ".gpkg",
//                                                ""
//                                            )
//                                        ) {
//                                            bean = check
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                                tempCheck.removeIf {
//                                                    it.collectId == check.collectId
//                                                }
//                                            } else {
//                                                tempCheck.remove(check)
//                                            }
//                                        }
//                                    }
//                                    totalCheckList.add(
//                                        ProjectListBean(
//                                            checkInfo = bean,
//                                            featureLayer = it.featureLayer
//                                        )
//                                    )
//                                }
//                                //将本地没有保存的checkBean添加进
//                                tempCheck.forEach check@{ check ->
//                                    totalCheckList.add(
//                                        ProjectListBean(
//                                            checkInfo = check,
//                                            featureLayer = null
//                                        )
//                                    )
//                                }
//                                setCheckList()
//                                sr_project_layers.isRefreshing = false
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (map.isNullOrEmpty()) {
//            totalCheckList.addAll(data)
//        }
//        setCheckList()
//        sr_project_layers.isRefreshing = false
    }

    private fun getFeatureLayerFromGpkg(
        checkList: List<CheckBean>,
        tempCheck: ArrayList<CheckBean>,
        path: String,
        sketch: MutableMap.MutableEntry<String, String>
    ) {
        //拿到所有的数据并遍历
        GeoPackageTool.getTablesFromGpkg(path) { featureTableList ->
            featureTableList.forEach { featureTable ->
                //                data.add(
//                    ProjectListBean(
//                        id = sketch.key,
//                        featureLayer = FeatureLayer(featureTable).apply {
//                            name = sketch.value
//                            featureTable.displayName = sketch.value
//                        })
//                )
                val layer = FeatureLayer(featureTable).apply {
                    name = sketch.value
                    featureTable.displayName = sketch.value
                }
                //判断跟线上的是否相等
                var bean: CheckBean? = null
                checkList.forEach { check ->
                    if (layer.name == check.getFileName().replace(
                            ".gpkg",
                            ""
                        )
                    ) {
                        bean = check
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tempCheck.removeIf {
                                it.collectId == check.collectId
                            }
                        } else {
                            tempCheck.remove(check)
                        }
                        return@forEach
                    }
                }
                totalCheckList.add(
                    ProjectListBean(
                        checkInfo = bean,
                        featureLayer = layer,
                        drawPath = mContext.filesDir.path + "/${ConstStrings.sktchId}/sketch/draw.jpg"
                    )
                )
            }
            setCheckList()
            sr_project_layers.isRefreshing = false
        }
    }


    /**
     * 获取模板列表
     */
    private fun moduleDialog() {
        val createView = LayoutInflater.from(mContext).inflate(R.layout.layout_work_create, null)
        val etName = createView.findViewById<EditText>(R.id.et_work_create_name)
        val rvTempList = createView.findViewById<RecyclerView>(R.id.rv_work_create_temp)
        val tempList = ConstStrings.mGuideBean.getTemplates()
        var selectTemp: KeyValueEntity? = null
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
                        R.id.iv_work_temp_select, if (item.value.toString() == selectTemp?.value) {
                            R.mipmap.select
                        } else {
                            R.mipmap.not_select
                        }
                    )
                }
            }.apply {
                setOnItemClickListener { adapter, view, position ->
                    selectTemp = tempList[position]
                    notifyDataSetChanged()
                    if (!ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
                        DrawTemplateDownloadActivity.startAction(this@ProjectListActivity, false)
                    }
                }
            }

        }
        BUIDialog.showCustom(mContext, "创建工程", createView, BUIDialog.BtnBuilder().withSubmitBtn(false){
            if (etName.text.toString().isEmpty()) {
                showToast("请输入工程名称")
            }else if (selectTemp == null){
                showToast("请选择工程模板")
            } else if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
                ConstStrings.drawTempleteName = selectTemp?.key + ".gpkg"
                templeteId = selectTemp?.value.toString()
                ConstStrings.drawTemplatesId = templeteId
                DrawSketchActivity.startAction(
                    this,
                    false,
                    ZXTimeUtil.getTime(
                        System.currentTimeMillis(),
                        SimpleDateFormat("yyyyMMdd_HHmmss")
                    )
                )
                //TODO
            } else {
                showToast("请下载模板")
                DrawTemplateDownloadActivity.startAction(this@ProjectListActivity, false)

            }
        }.withCancelBtn())


//        BUIDialog.showSimpleList(mContext, "请选择", arrayListOf<BUIDialog.ListBean>().apply {
//            ConstStrings.mGuideBean.getTemplates().forEach {
//                add(BUIDialog.ListBean(it.key, it.value.toString()))
//            }
//        }, {
//            ConstStrings.drawTempleteName = it.key + ".gpkg"
//            templeteId = it.value.toString()
//            ConstStrings.drawTemplatesId = templeteId
//            if (ZXFileUtil.isFileExists(ConstStrings.getDrawTemplatePath() + ConstStrings.drawTempleteName)) {
//
//                DrawSketchActivity.startAction(
//                    this@ProjectListActivity,
//                    false,
//                    ZXTimeUtil.getTime(
//                        System.currentTimeMillis(),
//                        SimpleDateFormat("yyyyMMdd_HHmmss")
//                    )
//                )
//            } else {
//                showToast("请下载模板")
//                DrawTemplateDownloadActivity.startAction(this@ProjectListActivity, false)
//
//            }
//        }, BUIDialog.BtnBuilder().withCancelBtn { })

    }

    override fun onSurveyUpload(name: String) {
        showToast("上传成功")
        if (!data.isNullOrEmpty()) {
            val bean = data[clickPosition]
            bean?.featureLayer?.let {
                FileUtils.deleteFilesByName(
                    ConstStrings.getSketchLayersPath(),
                    it.name
                )
                DeleteLayerFileTool.deleteFileByLayer(
                    ConstStrings.getSketchLayersPath() + it.name + "/file/",
                    it
                )
                FileUtils.deleteFiles(mContext.filesDir.path + "/${data[clickPosition].id}/sketch/draw.jpg")
                MapTool.postLayerChange(ChangeTag, it, MapTool.ChangeType.OperationalRemove)
                val list = mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)
                if (!list.isNullOrEmpty()) list?.remove(ConstStrings.sktchId)
                mSharedPrefUtil.putList(ConstStrings.SketchIdList, list)
                totalCheckList.remove(data[clickPosition])
                data.removeAt(clickPosition)
                projectAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCollectDownload(file: File) {
        if (file.exists()) {
            val geoPackage = GeoPackage(file.path)
            geoPackage.loadAsync()
            geoPackage.addDoneLoadingListener {
                if (geoPackage.loadStatus == LoadStatus.LOADED) {
                    val geoTables = geoPackage.geoPackageFeatureTables
                    geoTables.forEach { table ->
                        val featureLayer = FeatureLayer(table)
                        featureLayer.loadAsync()
                        featureLayer.addDoneLoadingListener {
                            featureLayer.name = file.name.substring(0, file.name.lastIndexOf("."))

                            //  MapTool.mapListener?.getMap()?.operationalLayers?.add(featureLayer)
                        }
                    }
                }
            }
            loadData()
            showToast("下载成功")
        }
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)

    }
}



