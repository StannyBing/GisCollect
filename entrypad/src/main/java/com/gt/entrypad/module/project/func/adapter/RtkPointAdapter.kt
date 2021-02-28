package com.gt.entrypad.module.project.func.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.esri.arcgisruntime.data.Field
import com.gt.base.widget.CustomerEditText
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.bean.SiteBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import java.util.logging.Handler

class RtkPointAdapter(dataList:List<RtkPointBean>) : ZXQuickAdapter<RtkPointBean, ZXBaseHolder>(R.layout.item_rtk_point_layout, dataList) {
   private var textChangeListener:(Int,String)->Unit={position,text->}

    override fun convert(helper: ZXBaseHolder, item: RtkPointBean) {
        helper.setText(R.id.itemRtkTitleTv,item.title)
        helper.setText(R.id.itemPointTv,item.result)
        helper.setText(R.id.itemDistanceEt,item.resultDistance)
        helper.addOnClickListener(R.id.rtkTv)
        helper.setTag(R.id.itemDistanceEt,helper.adapterPosition)
        helper.getView<CustomerEditText>(R.id.itemDistanceEt).addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (helper.getView<CustomerEditText>(R.id.itemDistanceEt).tag==helper.adapterPosition){
                    recyclerView?.post {
                        textChangeListener(helper.adapterPosition,s?.toString()?.trim()?:"")

                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    fun addTextChangeListener(textChangeListener:(Int,String)->Unit={position,text->}){
        this.textChangeListener = textChangeListener
    }
}