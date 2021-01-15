package com.gt.giscollect.module.system.bean

data class TemplateBean(
    val catalogId: String,
    val created: Long,
    val featureCode: String,
    val featureName: String,
    val fieldsJson: String,
    val fileExt: String,
    val fileJson: String,
    val fileUri: String,
    val maYear: Int,
    val rnCode: String,
    val rnName: String,
    val rowno: Int,
    val sdePath: String,
    val tableName: String,
    val templateId: String,
    val tplName: String,
    val tplState: String,
    val userId: String,
    val webmapid: String,
    val wkid: String,
    val xzqdm: String,
    val xzqmc: String,
    var isDownload: Boolean = false
) {
}