package com.stanny.sketchpad.bean

data class SketchPadSettingBean(
    var name: String,
    var type: SettingType,
    var switchStatus: Boolean = true,
    var editText: String = "",
    var selectItems: ArrayList<String> = arrayListOf()
) {

    enum class SettingType {
        Switch,
        Edit,
        Select
    }

}