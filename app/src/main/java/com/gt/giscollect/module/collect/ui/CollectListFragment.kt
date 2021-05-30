package com.gt.giscollect.module.collect.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.manager.UserManager
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.base.app.CheckBean
import com.gt.giscollect.module.collect.func.adapter.CollectListAdapter
import com.gt.module_map.tool.DeleteLayerFileTool
import com.gt.giscollect.module.collect.mvp.contract.CollectListContract
import com.gt.giscollect.module.collect.mvp.model.CollectListModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectListPresenter
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.MapTool
import com.gt.base.app.TempIdsBean
import com.gt.base.bean.NormalList
import com.gt.base.bean.toJson
import com.gt.giscollect.module.collect.bean.CollectCheckBean
import com.gt.giscollect.module.system.bean.TemplateBean
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_collect_list.*
import java.io.File

/**
 * Create By XB
 * 功能：采集列表
 */
class CollectListFragment : BaseFragment<CollectListPresenter, CollectListModel>(),
    CollectListContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectListFragment {
            val fragment = CollectListFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "collect_list"
    }

    private val totalCheckList = arrayListOf<CollectCheckBean>()
    private var localCheckList = arrayListOf<CollectCheckBean>()
    private val collectList = arrayListOf<CollectCheckBean>()
    private val collectAdapter = CollectListAdapter(collectList)

    var fragChangeListener: FragChangeListener? = null

    private var editCollectPosition = 0//当前编辑的采集任务的列表

    private var pageNum =1

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {

        rv_collect_layers.apply {
            layoutManager = LinearLayoutManager(mContext) as RecyclerView.LayoutManager?
            adapter = collectAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
       //本地数据
        localData()
        ZXRecyclerDeleteHelper(activity, rv_collect_layers)
            .setSwipeOptionViews(R.id.tv_upload, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                val layer = collectList[pos].featureLayer
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_upload -> {
                        if (MyApplication.isOfflineMode) {
                            showToast("当前为离线模式，无法进行该操作！")
                            return@setSwipeable
                        }
                        if (layer == null) {
                            ZXDialogUtil.showYesNoDialog(
                                mContext,
                                "提示",
                                "是否下载该条采集数据?"
                            ) { dialog, which ->
                                downloadCollect(collectList[pos])
//                                showToast("正在建设中")
                            }
                            return@setSwipeable
                        }
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否上传该条采集数据？"
                        ) { dialog, which ->
                            editCollectPosition = pos
                            val file = FileUtils.getFileByName(
                                ConstStrings.getOperationalLayersPath(),
                                layer.name
                            )
                            if (file?.exists() == true) {
                                val path = ZipUtils.zip(file.path, false)
                                if (path != null) {
                                    var mTempId = collectList[pos].checkInfo?.templateId ?: ""
                                    var mCataId = collectList[pos].checkInfo?.catalogId ?: ""
                                    if (mTempId.isEmpty() || mCataId.isEmpty()) {
                                        val templateIds =
                                            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
                                        templateIds?.forEach temp@{ temp ->
                                            if (ConstStrings.mGuideBean.getTemlatesList().contains(
                                                    temp.templateId
                                                )
                                            ) {
                                                temp.layerNames.forEach {
                                                    if (it == layer.name) {
                                                        mTempId = temp.templateId
                                                        mCataId = temp.catalogId
                                                        return@temp
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (mTempId.isEmpty()) {
                                        mPresenter.getTemplateList(
                                            path, layer, pos,
                                            hashMapOf(
                                                "currPage" to 0,
                                                "total" to 0,
                                                "pageSize" to 999,
                                                "filters" to arrayListOf(
                                                    hashMapOf(
                                                        "col" to "template_id",
                                                        "op" to "in",
                                                        "val" to ConstStrings.mGuideBean.getTemlatesList()
                                                    )
                                                )
                                            ).toJson()
                                        )
                                    } else {
                                        mPresenter.uploadCollect(
                                            path,
                                            layer.name,
                                            mTempId,
                                            mCataId,
                                            collectId = collectList[pos].checkInfo?.collectId ?: ""
                                        )
                                    }
                                }
                            } else {
                                showToast("文件不存在")
                            }
                        }
                    }
                    R.id.tv_delete -> {
                        if (layer == null) return@setSwipeable
                        ZXDialogUtil.showYesNoDialog(
                            mContext,
                            "提示",
                            "是否删除该图层，这将同时删除该图层的相关采集数据？"
                        ) { dialog, which ->
                            refreshCache(pos)
                            localCheckList.removeAt(pos)
                            MapTool.mapListener?.getMap()?.operationalLayers?.remove(layer)
                            DeleteLayerFileTool.deleteFileByLayer(
                                ConstStrings.getOperationalLayersPath() + layer.name + "/file/",
                                layer
                            )
                            FileUtils.deleteFilesByName(
                                ConstStrings.getOperationalLayersPath(),
                                layer.name
                            )
                        }
                    }
                }
            }
            .setClickable { position ->
                if (collectList[position].featureLayer == null) {
                    showToast("本机不存在该采集任务")
                } else {
                    editCollectPosition = position
                    fragChangeListener?.onFragGoto(
                        CollectMainFragment.Collect_Feature,
                        collectList[position].featureLayer to arrayOf(
                            collectList[position].isEdit(),
                            collectList[position].checkInfo == null
                        )
                    )
                }
            }
        super.initView(savedInstanceState)
    }

    /**
     * 刷新缓存
     */
    private fun refreshCache(pos: Int){
        var name = collectList[pos].featureLayer?.name
        collectList.removeAt(pos)
        collectAdapter.notifyItemRemoved(pos)
        collectAdapter.notifyItemRangeChanged(pos, 5)

        /*
         MapTool.mapListener?.getMap()?.tables?.remove(layer.featureTable)*/

        val list =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
                ?: arrayListOf()
        list.forEach list@{
            var tempLayersName = it.layerNames.filter {
                it != name
            }
            it.layerNames.clear()
            it.layerNames.addAll(tempLayersName)
        }
        mSharedPrefUtil.putList(
            ConstStrings.TemplateIdList,
            list
        )
    }
    private fun renameLayer(currentLayer: FeatureLayer, beforeName: String?, afterName: String) {
        val files = FileUtils.getFilesByName(
            ConstStrings.getOperationalLayersPath(),
            currentLayer.name
        )
        files.forEach {

            ZXFileUtil.rename(
                it, if (it.isFile) {
                    afterName + ".gpkg"
                } else {
                    afterName
                }
            )
        }
        val filesAfter = FileUtils.getFilesByName(
            ConstStrings.getOperationalLayersPath(),
            afterName
        )
        var gpkgFile = filesAfter.first {
            it.isFile
        }

        //添加模板id
        val templateIds =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
        templateIds.forEach outFor@{ temp ->
            var hasTemp = false
            temp.layerNames.forEach inFor@{
                if (it == beforeName) {
                    hasTemp = true
                    return@inFor
                }
            }
            if (hasTemp) {
                temp.layerNames.remove(beforeName)
                temp.layerNames.add(afterName)
            }
        }
        mSharedPrefUtil.putList(ConstStrings.TemplateIdList, templateIds)

        val geoPackage = GeoPackage(gpkgFile?.path)
        geoPackage.loadAsync()
        geoPackage.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                val geoTables = geoPackage.geoPackageFeatureTables
                geoTables.forEach { table ->
                    val featureLayer = FeatureLayer(table)
                    featureLayer.loadAsync()
                    featureLayer.addDoneLoadingListener {
                        featureLayer.name =
                            gpkgFile?.name?.substring(
                                0,
                                gpkgFile?.name?.lastIndexOf(".") ?: 0
                            )
                        MapTool.mapListener?.getMap()?.operationalLayers?.add(featureLayer)
                    }
                }
            }
        }


        currentLayer?.name = afterName
        MapTool.mapListener?.getMap()?.operationalLayers?.remove(currentLayer)
        showToast("修改成功！")
        fragChangeListener?.onFragBack(CollectMainFragment.Collect_Feature)
    }

    private fun downloadCollect(collectCheckBean: CollectCheckBean) {
        getPermission(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            collectCheckBean.checkInfo?.let {
                mPresenter.downloadCollect(it)
            }
        }
    }

    /**
     * 获取本地数据
     */
    private fun localData(){
        collectList.clear()
        localCheckList.clear()
        //获取跟当前模块相关的TemplateIdBean
        var tempBeans = arrayListOf<TempIdsBean>()
        ConstStrings.mGuideBean?.getTemlatesList()?.forEach { id->
          tempBeans.addAll(mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList).filter {
              it.templateId ==id
          } )
        }
        //进行文件对比
        MapTool.mapListener?.getMap()?.operationalLayers?.filter {
            //只添加采集
            FileUtils.getFilesByName(
                ConstStrings.getOperationalLayersPath(),
                it.name
            ).isNotEmpty()
        }?.forEach { layer->
            if (layer is FeatureLayer){
                if (tempBeans.filter {
                        it.layerNames.contains(layer.name)
                    }.isNotEmpty()){
                    //有数据
                    collectList.add(CollectCheckBean(null,layer))
                }
            }
        }
        localCheckList.addAll(collectList)
        collectAdapter.notifyDataSetChanged()
        sr_collect_layers.isRefreshing=false
    }
    /**
     * View事件设置
     */
    override fun onViewListener() {
        //刷新
        sr_collect_layers.setOnRefreshListener {
           if (rb_collect_listnet.isChecked){
               pageNum = 1
               refresh()
           }else{
               localData()
           }
        }
        //审核
        btn_collect_check.setOnClickListener {
            fragChangeListener?.onFragGoto(CollectMainFragment.Collect_Check)
        }

        //创建
        btn_collect_create.setOnClickListener {
            fragChangeListener?.onFragGoto(CollectMainFragment.Collect_Create)
        }

        //切换
        rg_collect_listtype.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId==R.id.rb_collect_listnet){
                searchEt.visibility=View.VISIBLE
                pageNum=1
                refresh()
                loadMoreGone(false)
            }else{
                collectList.clear()
                collectList.addAll(localCheckList)
                loadMoreGone(true)
                collectAdapter.notifyDataSetChanged()
                searchEt.visibility=View.GONE
            }
        }

        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                localData()
            }
        })

        searchEt.setOnEditorActionListener { v, actionId, event ->
            if (actionId==EditorInfo.IME_ACTION_SEARCH){
                pageNum =1
                refresh(searchEt.text.toString().trim())
            }
            return@setOnEditorActionListener false
        }
        loadMoreGone(true)
    }

    private fun loadMoreGone(gone:Boolean){
        if (gone){
            collectAdapter.loadMoreEnd()
        }else{
            collectAdapter.setOnLoadMoreListener(object :ZXQuickAdapter.RequestLoadMoreListener{
                override fun onLoadMoreRequested() {
                    //加载更多
                    pageNum++
                    refresh()
                }
            })
        }
    }

    /**
     * 刷新列表
     */
    fun refresh(keyword:String="") {
        if (rb_collect_listnet.isChecked){
            mPresenter.getCheckList(
                hashMapOf(
                    "currPage" to pageNum,
                    "pageSize" to pageSize,
                    "filters" to arrayListOf(
                        hashMapOf("col" to "user_id", "op" to "=", "val" to UserManager.user?.userId),
                        hashMapOf("col" to "template_id", "op" to "in", "val" to ConstStrings.mGuideBean.getTemlatesList()),
                        hashMapOf("col" to "layer_name", "op" to "like", "val" to keyword)
                    )
                ).toJson()
            )
        }else{
            localData()
        }
    }

    /**
     * 审核反馈
     */
    override fun onCheckListResult(total:Int,checkList: List<CheckBean>) {
        ConstStrings.checkList.clear()
        ConstStrings.checkList.addAll(checkList)
        if (pageNum==1) totalCheckList.clear()
        val tempCheck = arrayListOf<CheckBean>().apply {
            addAll(checkList)
        }
        //先添加本地的所有图层
        MapTool.mapListener?.getMap()?.operationalLayers?.filter {
            //只添加采集
            FileUtils.getFilesByName(
                ConstStrings.getOperationalLayersPath(),
                it.name
            ).isNotEmpty()
        }?.forEach layer@{ layer ->
            if (layer is FeatureLayer) {
                var bean: CheckBean? = null
                //将所有能与线上对应的layer设置checkBean
                kotlin.run check@{
                    checkList.forEach{ check ->
                        if (check.getFileName().replace(".gpkg", "") == layer.name) {
                            bean = check
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                tempCheck.removeIf {
                                    it.collectId == check.collectId
                                }
                            } else {
                                tempCheck.remove(check)
                            }
                            return@check
                        }
                    }
                }
                totalCheckList.add(CollectCheckBean(bean, layer))
            }
        }
        //将本地没有保存的checkBean添加进
        tempCheck.forEach check@{ check ->
            totalCheckList.add(CollectCheckBean(check, null))
        }
        if (pageNum*pageSize<total){
            collectAdapter.loadMoreComplete()
        }else{
            collectAdapter.loadMoreEnd()
        }

        setCheckList()
        sr_collect_layers.isRefreshing = false
    }

    private fun setCheckList() {
        collectList.clear()
        collectList.addAll(/*if (rb_collect_listlocal.isChecked) {
            totalCheckList.filter {
                it.checkInfo == null
            }
        } else*/ if (rb_collect_listnet.isChecked) {
            totalCheckList.filter {
                it.checkInfo != null
            }
        } else {
            totalCheckList
        })

        collectAdapter.notifyDataSetChanged()
    }

    override fun onDownloadProgress(progress: Int) {
        ZXDialogUtil.showLoadingDialog(mContext, "下载中...", if (progress >= 100) 99 else progress)
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

                            MapTool.mapListener?.getMap()?.operationalLayers?.add(featureLayer)
                        }
                    }
                }
            }

            refresh()
            showToast("下载成功")
        }
    }

    /**
     * 上传成功
     */
    override fun onCollectUpload(name: String) {
        refreshCache(editCollectPosition)
        localData()
        showToast("上传成功")
    }

    override fun onTemplateListResult(
        path: String,
        layer: FeatureLayer,
        pos: Int,
        tempalteList: NormalList<TemplateBean>
    ) {
        if (tempalteList.rows.size == 1) {
            mPresenter.uploadCollect(
                path,
                layer.name,
                tempalteList.rows.first().templateId,
                tempalteList.rows.first().catalogId,
                collectId = collectList[pos].checkInfo?.collectId ?: ""
            )
            return
        }
        tempalteList.rows.firstOrNull {
            it.tplName.contains(layer.featureTable.tableName)
        }.let {
            if (it == null) {
                mPresenter.uploadCollect(
                    path,
                    layer.name,
                    ConstStrings.mGuideBean.getTemplatesFirst(),
                    collectId = collectList[pos].checkInfo?.collectId ?: ""
                )
            } else {
                mPresenter.uploadCollect(
                    path,
                    layer.name,
                    it.templateId,
                    it.catalogId,
                    collectId = collectList[pos].checkInfo?.collectId ?: ""
                )
            }
        }
    }
}
