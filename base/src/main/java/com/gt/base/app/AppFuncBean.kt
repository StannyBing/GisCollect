package com.gt.base.app

data class AppFuncBean(var id: String="", var label: String="", var icon: String="", var url: String="", var obj: TemplateInfo, var children: List<AppFuncBean>) {

    data class TemplateInfo(var templateId : String?="")

}