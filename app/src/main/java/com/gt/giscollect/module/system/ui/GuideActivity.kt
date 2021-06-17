package com.gt.giscollect.module.system.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.gt.base.activity.BaseActivity
import com.gt.entrypad.module.project.ui.ProjectListActivity
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.base.app.AppInfoManager
import com.gt.base.manager.UserManager
import com.gt.giscollect.module.main.ui.MainActivity
import com.gt.base.app.AppFuncBean
import com.gt.base.bean.GuideBean
import com.gt.base.bean.GisServiceBean
import com.gt.giscollect.module.system.func.adapter.GuideAdapter
import com.gt.giscollect.module.system.mvp.contract.GuideContract
import com.gt.giscollect.module.system.mvp.model.GuideModel
import com.gt.giscollect.module.system.mvp.presenter.GuidePresenter
import com.gt.module_map.tool.FileUtils
import com.stanny.module_rtk.tool.WHandService
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import kotlinx.android.synthetic.main.activity_guide.*
import org.json.JSONObject
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class GuideActivity : BaseActivity<GuidePresenter, GuideModel>(), GuideContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, GuideActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    private val guideList = arrayListOf<GuideBean>()
    private val guideAdapter = GuideAdapter(guideList)

    private val appfuncList = arrayListOf<AppFuncBean>()

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_guide
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        tv_guide_name.text = UserManager.user?.userName ?: "用户"
        tv_guide_appinfo.text = "欢迎使用${AppInfoManager.appInfo?.appInfo?.name ?: "清河CIM移动端数据采集系统"}"

        rv_guide_step.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = guideAdapter
        }

        startService(Intent(this, WHandService::class.java))

        //TODO 图层文件转移
//        showToast("正在进行源文件拷贝")
        getPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            if (ZXFileUtil.isFileExists(ConstStrings.getOperationalLayersPath(true))) {
                val status = FileUtils.copyFolder(
                    ConstStrings.getOperationalLayersPath(true),
                    ConstStrings.getOperationalLayersPath()
                )
                if (status) {
//                    showToast("采集图层拷贝完成")
                    ZXFileUtil.deleteFiles(ConstStrings.getOperationalLayersPath(true))
                } else {
//                    showToast("采集图层拷贝失败")
                }
            }

            if (ZXFileUtil.isFileExists(ConstStrings.getSurveyLayersPath(true))) {
                val status = FileUtils.copyFolder(
                    ConstStrings.getSurveyLayersPath(true),
                    ConstStrings.getOperationalLayersPath()
                )
                if (status) {
//                    showToast("调查图层拷贝完成")
                    ZXFileUtil.deleteFiles(ConstStrings.getSurveyLayersPath(true))
                } else {
//                    showToast("调查图层拷贝失败")
                }
            }
        }

        mPresenter.getAppFuncs(hashMapOf("userId" to (UserManager.user?.userId ?: "")))
        mPresenter.doSurveyType()
        mPresenter.doGisService(hashMapOf("userId" to (UserManager.user?.userId ?: "")))
    }

    private fun getBussinessId(name: String): String {
        UserManager.user?.businesses?.let {
            val json = JSONObject(it.toString())
//            val json = it
            if (json.has(name)) {
                return json.getString(name)
            }
        }
        return "empty"
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        ll_guide_logout.setOnClickListener {
            ZXDialogUtil.showYesNoDialog(this, "提示", "是否退出登录？") { dialog, which ->
                LoginActivity.startAction(this, true)
                UserManager.loginOut()
            }
        }

        guideAdapter.setChildCall { it ->
            ConstStrings.appfuncList.clear()
            ConstStrings.appfuncList.addAll(it.appFuncs)
            ConstStrings.mGuideBean = it
            it.param
            if (!ZXFileUtil.isFileExists(ConstStrings.getSketchLayersFirstPath())) {
                File(ConstStrings.getSketchLayersFirstPath()).mkdirs()
            }
            if (!ZXFileUtil.isFileExists(ConstStrings.getSketchLayersSecondPath())) {
                File(ConstStrings.getSketchLayersSecondPath()).mkdirs()
            }

            //只加载当前用户的
            File(ConstStrings.getOperationalLayersPath()).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            File(ConstStrings.getOperationalLayersPath(isOld = true)).apply {
                if (isDirectory && listFiles().isNotEmpty()) {
                    listFiles().filter {
                        it.isDirectory && it.name.length < 10
                    }.forEach {
                        FileUtils.copyFolder(it.path, ConstStrings.getOperationalLayersPath() + it.name +"/")
                        ZXFileUtil.deleteFiles(it)
                    }
                }
            }

            if (it.getAppType() == "draw") {
                ProjectListActivity.startAction(this, false)
            }
//            else if(it.itemName.contains("农房")){
//                SceneMapActivity.startAction(this, false)
//            }
            else {
                MainActivity.startAction(this, false)
            }
//            when {
//              //  it.itemName.contains("竣工") -> ProjectListActivity.startAction(this, false)
//                it.itemName.contains("草图") -> ProjectListActivity.startAction(this,false)
//                else -> {
//                    ConstStrings.appfuncList.clear()
//                    ConstStrings.appfuncList.addAll(it.appFuncs)
//                    ConstStrings.bussinessId = it.templateId ?: ""
//                    MainActivity.startAction(this, false)
//                }
//            }
//            when (it.itemName) {
//                "农房选址测绘", "农房地基测绘", "农房竣工测绘" -> {
//                    ConstEntryStrings.bussinessId = getBussinessId(it.itemName)
//                    MainActivity.startAction(this, false)
//                }
//                else -> {
//                    showToast("正在建设中")
//                }
//            }
        }
    }

    /**
     * app功能
     */
    override fun appFuncResult(appFuncs: List<AppFuncBean>) {
        initList(appFuncs)
    }

    private fun initList(appFuncs: List<AppFuncBean>) {
        appfuncList.clear()
        appfuncList.addAll(appFuncs)
        guideList.clear()
        appFuncs.forEach {
            guideList.add(
                GuideBean(
                    itemName = it.label,
                    icon = R.drawable.ydghjd,
                    childList = arrayListOf<GuideBean>().apply {
                        //TODO:
                        val childeren = it.children.toMutableList().apply {
                            //                           add(AppFuncBean(id = "m103",label = "竣工验收",obj = AppFuncBean.TemplateInfo(),children = arrayListOf()))
                        }
                        childeren.forEach {
                            add(
                                GuideBean(
                                    it.label,
                                    it.label,
                                    type = GuideBean.GUIDE_ITEM,
                                    appFuncs = it.children,
                                    templateId = it.obj.templateId,
                                    param = it.obj.param
                                )
                            )
                        }
                    }
                ).apply {
                    subItems = childList
                }
            )
        }
        guideAdapter.notifyDataSetChanged()
    }

    var backMills = 0L
    override fun onBackPressed() {
        if (backMills == 0L || System.currentTimeMillis() - backMills > 2000) {
            showToast("再次点击，退出应用")
            backMills = System.currentTimeMillis()
        } else {
//            MyApplication.instance.finishAll()
            super.onBackPressed()
        }
    }

    /**
     * 调查类型接口回调
     */
    override fun surveyTypeResult(typeResult: String?) {
        typeResult?.let {
            mSharedPrefUtil.putString("fieldShow", it)
        }
    }

    /**
     * 用户在线服务数据回调
     */
    override fun gisServiceResult(serviceResult: List<GisServiceBean>?) {
        var jsonMap1 = LinkedHashMap<String, GisServiceBean.OnlineBean>()
        serviceResult?.let {
            val list = it.sortedBy { it.sseq }.apply {
                forEach {
                    it.children = it.children?.sortedBy { it.sseq }
                }
            }
            //取出所有在线服务
            list.forEach {
                it.children?.forEach {
                    jsonMap1[it.sname] = GisServiceBean.OnlineBean(it.surl, it.visible, it.type)
                }
            }
//            jsonMap["onlineService"]=jsonMap1
            try {
                val obj = JSONObject(Gson().toJson(jsonMap1))
                val onlineService = arrayListOf<String>()
                obj.keys().forEach {
                    onlineService.add(obj.getJSONObject(it).apply {
                        put("itemName", it)
                    }.toString())
                }
                AppInfoManager.appInfo?.onlineService = onlineService
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            AppInfoManager.setData(Gson().toJson(jsonMap))
            AppInfoManager.gisService = list
        }
        if (serviceResult.isNullOrEmpty()) AppInfoManager.gisService = arrayListOf()
    }
}
