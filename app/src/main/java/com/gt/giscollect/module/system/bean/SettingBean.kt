package com.gt.giscollect.module.system.bean

import androidx.annotation.DrawableRes

data class SettingBean(var name: String, @DrawableRes var icon: Int, var showMore: Boolean = false) {
}