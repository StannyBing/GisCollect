package com.gt.giscollect.app

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.StrictMode
import com.frame.zxmvp.baseapp.BaseApplication
import com.frame.zxmvp.di.component.AppComponent
import com.gt.giscollect.BuildConfig
import com.gt.giscollect.module.main.func.tool.MapTool
import com.tencent.bugly.crashreport.CrashReport
import com.zx.zxutils.ZXApp
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSharedPrefUtil
import com.zx.zxutils.util.ZXSystemUtil
import java.util.*

/**
 * Created by Xiangb on 2019/2/26.
 * 功能：
 */
open class MyApplication : BaseApplication() {
    companion object {
        lateinit var instance: MyApplication

        //        fun getInstance(): MyApplication {
//            return
//        }
        lateinit var mSharedPrefUtil: ZXSharedPrefUtil

        var isOfflineMode = false
    }

    lateinit var mContest: Context
    lateinit var component: AppComponent

    override fun onCreate() {

        super.onCreate()

        isOfflineMode = false

//        ZXApp.init(this, true)
        ZXApp.init(this, !BuildConfig.RELEASE)

        //配置Bugly
        CrashReport.initCrashReport(this, "dcbe01e839", !BuildConfig.RELEASE)

        mSharedPrefUtil = ZXSharedPrefUtil()
        instance = this
        mContest = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }

        //初始化
        ConstStrings.LOCAL_PATH = ZXSystemUtil.getSDCardPath()
        ConstStrings.INI_PATH = filesDir.path + "/"
        ZXFileUtil.deleteFiles(ConstStrings.getApkPath())
        ZXFileUtil.deleteFiles(ConstStrings.getOnlinePath())
//        component = appComponent
        MapTool.reset()

        reinit()
    }

    fun reinit() {
        initAppDelegate(this)
        component = appComponent
    }

    private val activityList = ArrayList<Activity>()

    fun recreateView() {
        activityList.forEach {
            it.recreate()
        }
    }

    // 添加Activity到容器中
    override fun addActivity(activity: Activity) {
        if (activityList.size > 0 && activityList[activityList.size - 1].javaClass == activity.javaClass) {
            return
        }
        activityList.add(activity)
    }

    // 遍历所有Activity并finish
    override fun exit() {
        for (activity in activityList) {
            activity.finish()
        }
        //		App.getInstance().destroyMap();
        System.exit(0)
    }


    //销毁某个activity实例
    override fun remove(t: Class<out Activity>) {
        for (i in activityList.indices.reversed()) {
            if (activityList[i].javaClass == t) {
                activityList[i].finish()
                return
            }
        }
    }

    // 遍历所有Activity并finish

    fun finishAll() {
        for (activity in activityList) {
            activity.finish()
        }
        //		App.getInstance().destroyMap();
    }

    override fun haveActivity(t: Class<out Activity>): Boolean {
        for (activity in activityList) {
            if (activity.javaClass == t) {
                return true
            }
        }
        return false
    }

    override fun clearActivityList() {
        for (activity in activityList) {
            activity.finish()
        }
        activityList.clear()
    }


    override fun getActivityList(): ArrayList<Activity> {
        return activityList
    }

}