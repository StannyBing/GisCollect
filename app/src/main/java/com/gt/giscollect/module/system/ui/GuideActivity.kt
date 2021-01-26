package com.gt.giscollect.module.system.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.base.activity.BaseActivity
import com.gt.entrypad.module.project.ui.activity.DrawSketchActivity
import com.gt.entrypad.module.project.ui.activity.ProjectListActivity
import com.gt.giscollect.R
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.module.main.ui.MainActivity
import com.gt.giscollect.module.system.bean.GuideBean
import com.gt.giscollect.module.system.func.adapter.GuideAdapter

import com.gt.giscollect.module.system.mvp.contract.GuideContract
import com.gt.giscollect.module.system.mvp.model.GuideModel
import com.gt.giscollect.module.system.mvp.presenter.GuidePresenter
import com.gt.giscollect.base.UserManager
import com.gt.giscollect.module.system.bean.AppFuncBean
import com.zx.zxutils.util.ZXDialogUtil
import kotlinx.android.synthetic.main.activity_guide.*
import org.json.JSONObject


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

        rv_guide_step.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = guideAdapter
        }

        mPresenter.getAppFuncs(hashMapOf("userId" to (UserManager.user?.userId ?: "")))
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

        guideAdapter.setChildCall {
            when {
              //  it.itemName.contains("竣工") -> ProjectListActivity.startAction(this, false)
                it.itemName.contains("草图") -> ProjectListActivity.startAction(this,false)
                else -> {
                    ConstStrings.appfuncList.clear()
                    ConstStrings.appfuncList.addAll(it.appFuncs)
                    ConstStrings.bussinessId = it.templateId ?: ""
                    MainActivity.startAction(this, false)
                }
            }
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
                                    templateId = it.obj.templateId
                                )
                            )
                        }
                    }
                ).apply {
                    subItems = childList
                }
            )
//            when (it.label) {
//                "用地规划阶段" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "用地规划阶段",
//                            icon = R.drawable.ydghjd,
//                            childList = arrayListOf<GuideBean>().apply {
//                                it.children.forEach {
//                                    add(GuideBean(it.label, it.label, type = GuideBean.GUIDE_ITEM))
//                                }
////                                add(GuideBean(it.label, "选址测绘", type = GuideBean.GUIDE_ITEM))
////                                add(GuideBean(it.label, "土地勘测定界", type = GuideBean.GUIDE_ITEM))
////                                add(GuideBean(it.label, "地籍调查", type = GuideBean.GUIDE_ITEM))
////                                add(GuideBean(it.label, "拔地测量", type = GuideBean.GUIDE_ITEM))
//                            }
//                        ).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//                "工程规划阶段" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "工程规划阶段",
//                            icon = R.drawable.gcghjd,
//                            childList = arrayListOf<GuideBean>().apply {
//                                add(GuideBean(it.label, "报建图测绘", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "房屋面积测算", type = GuideBean.GUIDE_ITEM))
//                            }).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//                "施工监督阶段" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "施工监督阶段",
//                            icon = R.drawable.sgjdjd,
//                            childList = arrayListOf<GuideBean>().apply {
//                                add(GuideBean(it.label, "规划放样", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "规划验线", type = GuideBean.GUIDE_ITEM))
//                            }).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//                "竣工验收阶段" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "竣工验收阶段",
//                            icon = R.drawable.jgysjd,
//                            childList = arrayListOf<GuideBean>().apply {
//                                add(GuideBean(it.label, "规划核实", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "土地核验", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "房产测量", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "消防测量", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "人防测量", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "绿化测量", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "地下管线测量", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "不动产补充测绘", type = GuideBean.GUIDE_ITEM))
//                            }).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//                "农房测绘" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "农房测绘",
//                            icon = R.drawable.nfch,
//                            childList = arrayListOf<GuideBean>().apply {
//                                add(GuideBean(it.label, "农房选址测绘", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "农房地基测绘", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "农房竣工测绘", type = GuideBean.GUIDE_ITEM))
//                            }).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//                "专题应用" -> {
//                    guideList.add(
//                        GuideBean(
//                            itemName = "专题应用",
//                            icon = R.drawable.ztyy,
//                            childList = arrayListOf<GuideBean>().apply {
//                                add(GuideBean(it.label, "供后监管", type = GuideBean.GUIDE_ITEM))
//                                add(GuideBean(it.label, "执法监察", type = GuideBean.GUIDE_ITEM))
//                            }).apply {
//                            subItems = childList
//                        }
//                    )
//                }
//            }
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

}
