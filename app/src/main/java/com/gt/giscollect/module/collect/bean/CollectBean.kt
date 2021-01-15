package com.gt.giscollect.module.collect.bean

import java.io.Serializable

data class CollectBean(
    var id: String,
    var name: String,
    var createTime: String,
    var status: Int = 0,//0采集中  1已上传
    var spotList: ArrayList<SpotInfo> = arrayListOf(),
    var filedList: ArrayList<FiledInfo> = arrayListOf(),
    var fileList: ArrayList<FileInfo> = arrayListOf()
) : Serializable {

    companion object {
        private const val serialVersionUID = 20000L
    }


    data class SpotInfo(
        var name: String = "",
        var createTime: String = "",
        var geojson: String = "",
        var isAddBtn: Boolean = false,
        var isChecked: Boolean = false
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 20001L
        }
    }

    data class FiledInfo(
        var type: Int = 1,//1字符型 2整形 3浮点型
        var name: String = "",
        var isDefault: Boolean = false,
        var isAddBtn: Boolean = false
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 20002L
        }
    }

    data class FileInfo(
        var name: String = "",
        var path: String = "",
        var pathImage: String = "",
        var createTime: String = "",
        var type: Int = 0,//0图片  1视频   2录音
        var isAddBtn: Boolean = false
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 20003L
        }
    }

}
