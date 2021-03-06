package com.gt.giscollect.app

import com.gt.giscollect.base.UserManager
import com.gt.giscollect.module.collect.bean.CheckBean
import com.gt.giscollect.module.system.bean.AppFuncBean
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
    var bussinessId: String = ""//当前选中的id
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

    const val TemplateIdList = "template_id_list"
    const val DataIdList = "data_id_list"
    const val RxLayerChange = "rx_layer_change"

    fun getDatabasePath(): String {
        return "$INI_PATH/$APPNAME/DATABASE/"
    }

    fun getCachePath(): String {
        return "$LOCAL_PATH/$APPNAME/Cache/"
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

    fun getCollectTemplatePath(): String {
        return "$INI_PATH$APPNAME/CollectTemplate/"
    }

    fun getStylePath(): String {
        return "$LOCAL_PATH$APPNAME/MapStyle/"
    }

    fun getOperationalLayersPath(): String {
        return "$INI_PATH$APPNAME/OperationalLayers/" + UserManager.user?.userId + "/" + bussinessId + "/"
    }

    //地图默认中心点
    var Longitude = 106.496001
    var Latitude = 29.62016

    //定位时地图比例尺
    var LocationScale = 72142.9670553589

    //两点之间距离容差（在此距离内可视为同一点）
    var TolDistance = 1.0f

}