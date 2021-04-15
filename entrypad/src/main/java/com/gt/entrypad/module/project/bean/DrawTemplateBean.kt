package com.gt.entrypad.module.project.bean

data class DrawTemplateBean(
    val catalogId: String="",
    val created: Long=0L,
    val featureCode: String="",
    val featureName: String="",
    val fieldsJson: String="",
    val fileJson: String="",
    val fileUri: String="",
    val maYear: Int=0,
    val rnCode: String="",
    val rnName: String="",
    val rowno: Int=0,
    val sdePath: String="",
    val tableName: String="",
    val templateId: String="",
    val tplName: String="",
    val tplState: String="",
    val userId: String="",
    val webmapid: String="",
    val wkid: String="",
    val xzqdm: String="",
    val xzqmc: String="",
    var isDownload: Boolean = false
) {
}