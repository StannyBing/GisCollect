package com.gt.entrypad.app

import com.zx.zxutils.util.ZXSharedPrefUtil

object ConstString {
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