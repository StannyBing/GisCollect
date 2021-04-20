package com.gt.base.bean

import androidx.annotation.DrawableRes
import com.gt.base.app.AppFuncBean
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.entity.AbstractExpandableItem
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity
import org.json.JSONObject

data class GuideBean(
    var parentName: String = "",
    var itemName: String = "",
    var id: String = "",
    @DrawableRes var icon: Int = 0,
    var type: Int = GUIDE_STEP,
    var childList: ArrayList<GuideBean> = arrayListOf(),
    var appFuncs: List<AppFuncBean> = arrayListOf(),
    var templateId: String? = "",
    var param: String? = ""
) : AbstractExpandableItem<GuideBean>(), MultiItemEntity {

    companion object {
        const val GUIDE_STEP = 0
        const val GUIDE_ITEM = 1
    }

    override fun getItemType(): Int {
        return type
    }

    override fun getLevel(): Int {
        return type
    }

    fun getTemplates(): List<KeyValueEntity> {
        val dataList = arrayListOf<KeyValueEntity>()
        try {
            val arr = JSONObject(param).optJSONArray("templates")
            for (i in 0..arr.length()) {
                dataList.add(
                    KeyValueEntity(
                        arr.optJSONObject(i).optString("name"),
                        arr.optJSONObject(i).optString("id")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataList
    }

    fun getTemplatesFirst(): String {
        return getTemplates().firstOrNull().let {
            it?.value?.toString() ?: ""
        }
    }

    fun getTemplatesSecond(): String {
        return getTemplates().lastOrNull().let {
            it?.value?.toString() ?: ""
        }
    }

    fun getBusinesses(): List<KeyValueEntity> {
        val dataList = arrayListOf<KeyValueEntity>()
        try {
            val arr = JSONObject(param).optJSONArray("businesses")
            for (i in 0..arr.length()) {
                dataList.add(
                    KeyValueEntity(
                        arr.optJSONObject(i).optString("name"),
                        arr.optJSONObject(i).optString("id")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataList
    }

    fun getBusinessesFirst(): String {
        return getBusinesses().firstOrNull().let {
            it?.value?.toString() ?: ""
        }
    }
}
