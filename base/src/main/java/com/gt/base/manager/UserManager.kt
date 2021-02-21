package com.gt.base.manager

import com.zx.zxutils.util.ZXSharedPrefUtil

/**
 * Created by Xiangb on 2019/3/5.
 * 功能：用户管理器
 */
object UserManager {
    var mSharedPrefUtil = ZXSharedPrefUtil()
    var user: UserBean? = null
        get() {
            if (field == null) {
                val sharedPref = mSharedPrefUtil
                return sharedPref.getObject("userBean")
            }
            return field
        }
        set(value) {
            val sharedPref = mSharedPrefUtil
            sharedPref.putObject("userBean", value)
            field = value
        }

    var userName: String = ""
        set(value) {
            val sharedPref = mSharedPrefUtil
            sharedPref.putString("m_username", value)
            field = value
        }
        get() {
            if (field.isEmpty()) {
                val sharedPref = mSharedPrefUtil
                return sharedPref.getString("m_username")
            } else {
                return field
            }
        }

    var passWord: String = ""
        set(value) {
            val sharedPref = mSharedPrefUtil
            sharedPref.putString("m_password", value)
            field = value
        }
        get() {
            if (field.isEmpty()) {
                val sharedPref = mSharedPrefUtil
                return sharedPref.getString("m_password")
            } else {
                return field
            }
        }

    fun loginOut() {
        passWord = ""
        user = null
    }

}