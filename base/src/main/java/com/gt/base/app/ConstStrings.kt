package com.gt.base.app

import com.gt.base.bean.GuideBean
import com.gt.base.manager.UserManager
import com.zx.zxutils.util.ZXSharedPrefUtil


/**
 * Created by Xiangb on 2019/2/26.
 * 功能：
 */
object ConstStrings {
    var ISRELEASE = false
    var code = ""
    var e = ""
    var usename = ""
    var AppCode = ""
    var INI_PATH = ""
    var DEVICE_TYPE = "android_pad"
    val APPNAME = "GisCollect"
    val File_Split_Char = ","
    val RESPONSE_SUCCESS = "1" // 请求成功
    val COLLECT_SP_NAME = "collect_sp_name"
    val COLLECT_UPDATE_LIST = "updateList"
    val arcgisKey = "5SKIXc21JlankElJ"
    var LOCAL_PATH: String? = null
    var mTemplatesId: String = ""//当前选中的id
    var mGuideBean: GuideBean = GuideBean()//当前列表进入信息
    var sktchId: String = ""//当前选中的id
    var drawTempleteName = "" //草图模板名字
    var drawTemplatesId: String = ""//当前选中的id
    var copyDrawPath = "" //复制草图地址
    val checkList = arrayListOf<CheckBean>()
    val appfuncList = arrayListOf<AppFuncBean>()

    var Cookie = ""
        get() {
            if (field.isEmpty()) {
                return ZXSharedPrefUtil().getString("cookie")
            } else {
                return field
            }
        }
        set(value) {
            field = value
            ZXSharedPrefUtil().putString("cookie", value)
        }
    const val DrawTemplateIdList = "draw_template_id_list"
    const val TemplateIdList = "template_id_list"
    const val DataIdList = "data_id_list"
    const val RxLayerChange = "rx_layer_change"
    const val SketchIdList = "sketch_id_list"

    fun getDatabasePath(): String {
        return "$INI_PATH/$APPNAME/DATABASE/"
    }

    fun getCachePath(): String {
        return "$LOCAL_PATH/$APPNAME/Cache/"
    }

    fun getProjectCachePath(): String {
        return "$LOCAL_PATH/$APPNAME/Project/Cache"
    }

    fun getZipPath(): String {
        return "$INI_PATH/$APPNAME/.zip/"
    }

    fun getOnlinePath(): String {
        return "$LOCAL_PATH$APPNAME/ONLINE/"
    }

    fun getCrashPath(): String {
        return "$LOCAL_PATH$APPNAME/CRASH/"
    }

    fun getApkPath(): String {
        return "$LOCAL_PATH$APPNAME/APK/"
    }

    fun getMainPath(): String {
        return "$INI_PATH/$APPNAME/"
    }

    fun getLocalPath(): String {
        return "$LOCAL_PATH$APPNAME/"
    }

    fun getLocalMapPath(): String {
        return "$LOCAL_PATH$APPNAME/LocalMap/"
    }

    fun getInnerLocalMapPath(): String {
        return "$INI_PATH$APPNAME/LocalMap/"
    }

    fun getLocalMapPath(fileExt: String): String {
        if (fileExt.contains("gpkg") || fileExt.contains("vtpk") || fileExt.contains("tpk")) {
            return getInnerLocalMapPath()
        } else {
            return getLocalMapPath()
        }
    }

    fun getSurveyPath(): String {
        return "$LOCAL_PATH$APPNAME/Survey/"
    }

    fun getCollectTemplatePath(): String {
        return "$INI_PATH$APPNAME/CollectTemplate/"
    }

    fun getDrawTemplatePath(): String {
        return "$INI_PATH$APPNAME/DrawTemplate/"
    }

    fun getStylePath(): String {
        return "$LOCAL_PATH$APPNAME/MapStyle/"
    }

    fun getOperationalLayersPath(isInner: Boolean = false): String {
        if (isInner) {
            return "$INI_PATH$APPNAME/OperationalLayers/" + UserManager.user?.userId + "/" + mGuideBean.getTemplatesFirst() + "/"
        }
        return "$LOCAL_PATH$APPNAME/OperationalLayers/" + UserManager.user?.userId + "/" + mGuideBean.getTemplatesFirst() + "/"
    }

    fun getSurveyLayersPath(isInner: Boolean = false): String {
        if (isInner) {
            return "$INI_PATH$APPNAME/SurveyLayers/" + UserManager.user?.userId + "/"
        }
        return "$LOCAL_PATH$APPNAME/SurveyLayers/" + UserManager.user?.userId + "/"
    }

    fun getSketchLayersPath(): String {
        return "$INI_PATH$APPNAME/SketchLayers/" + UserManager.user?.userId + "/$drawTemplatesId/" + sktchId
    }

    fun getSketchLayersFirstPath():String{
        return "$INI_PATH$APPNAME/SketchLayers/" + UserManager.user?.userId + "/${mGuideBean.getTemplatesFirst()}/" + sktchId
    }

    fun getSketchLayersSecondPath():String{
        return "$INI_PATH$APPNAME/SketchLayers/" + UserManager.user?.userId + "/${mGuideBean.getTemplatesSecond()}/" + sktchId
    }

    fun getSketchTemplatePath(): String {
        return "$LOCAL_PATH$APPNAME/jungong/" + UserManager.user?.userId
    }

    fun getSurveyTemplatePath(): String {
        return "$LOCAL_PATH$APPNAME/survey/" + UserManager.user?.userId + "/"
    }

    //踏勘文件搜索
    fun getSurveySearchPath(): String {
        return "$LOCAL_PATH$APPNAME/survey/file/"
    }

    //地图默认中心点
    var Longitude = 106.496001
    var Latitude = 29.62016

    //定位时地图比例尺
    var LocationScale = 72142.9670553589

    //两点之间距离容差（在此距离内可视为同一点）
    var TolDistance = 1.0f

    fun clear() {
        sktchId = ""
    }
}