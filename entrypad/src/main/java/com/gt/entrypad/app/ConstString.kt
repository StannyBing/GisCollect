package com.gt.entrypad.app

import com.gt.base.manager.UserManager
import com.zx.zxutils.util.ZXSharedPrefUtil

object ConstString {
    val File_Split_Char = ","
    fun getSketchLayersPath(): String {
        return "GisCollect/SketchlLayers/" + UserManager.user?.userId + "/" + "sketch/"
    }
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
}