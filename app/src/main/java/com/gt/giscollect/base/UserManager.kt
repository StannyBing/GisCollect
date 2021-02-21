package com.gt.giscollect.base

import com.gt.base.manager.UserBean
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.app.MyApplication

/**
 * Created by Xiangb on 2019/3/5.
 * 功能：用户管理器
 */
object UserManager {

    var user: UserBean? = null
        get() {
            if (field == null) {
                val sharedPref = MyApplication.mSharedPrefUtil
                return sharedPref.getObject("userBean")
            }
            return field
        }
        set(value) {
            val sharedPref = MyApplication.mSharedPrefUtil
            sharedPref.putObject("userBean", value)
            field = value
        }

    var userName: String = ""
        set(value) {
            val sharedPref = MyApplication.mSharedPrefUtil
            sharedPref.putString("m_username", value)
            field = value
        }
        get() {
            if (field.isEmpty()) {
                val sharedPref = MyApplication.mSharedPrefUtil
                return sharedPref.getString("m_username")
            } else {
                return field
            }
        }

    var passWord: String = ""
        set(value) {
            val sharedPref = MyApplication.mSharedPrefUtil
            sharedPref.putString("m_password", value)
            field = value
        }
        get() {
            if (field.isEmpty()) {
                val sharedPref = MyApplication.mSharedPrefUtil
                return sharedPref.getString("m_password")
            } else {
                return field
            }
        }

    fun loginOut() {
        ConstStrings.Cookie = ""
        passWord = ""
        user = null
    }

}