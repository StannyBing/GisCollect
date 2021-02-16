package com.gt.entrypad.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.frame.zxmvp.baserx.RxManager
import com.gt.entrypad.R
import com.gt.entrypad.module.project.func.CheckBoxAdapter
import com.gt.entrypad.module.project.ui.view.checkBox.CheckBoxViewViewModel
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import rx.functions.Action1

class CustomInfoDialog @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var data = arrayListOf<CheckBoxViewViewModel>()
    private var checkAdapter = CheckBoxAdapter(data)
    init {
        initView()
    }
    private fun initView(){
        initData()
        addView( LayoutInflater.from(context).inflate(R.layout.layout_recycler_view,this,false).apply {
           recyclerView.layoutManager=LinearLayoutManager(context)
           recyclerView.adapter = checkAdapter
       })
    }

    private fun initData(){
        data.apply {
            add(CheckBoxViewViewModel("混合"))
            add(CheckBoxViewViewModel("砖混"))
            add(CheckBoxViewViewModel("砖瓦"))
            add(CheckBoxViewViewModel("砖木"))
            add(CheckBoxViewViewModel("钢湿"))
            add(CheckBoxViewViewModel("其他"))

        }
        checkAdapter.notifyDataSetChanged()
    }
    fun getData():List<CheckBoxViewViewModel>{
        return data
    }


}