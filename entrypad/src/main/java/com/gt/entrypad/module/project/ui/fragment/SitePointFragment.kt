package com.gt.entrypad.module.project.ui.fragment

import android.content.DialogInterface
import android.graphics.PointF
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.geometry.Point
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.bean.SiteBean
import com.gt.entrypad.module.project.func.adapter.SitePointAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.gt.entrypad.module.project.ui.activity.SketchLoadActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import kotlinx.android.synthetic.main.fragment_site_point.*
import com.google.gson.GsonBuilder
import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper.bindToLifecycle
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.view.View
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReference
import com.gt.base.app.ConstStrings
import com.gt.module_map.tool.GeometrySizeTool
import com.gt.module_map.tool.PointTool
import com.trello.rxlifecycle.RxLifecycle.bindUntilEvent
import com.zx.zxutils.util.ZXTimeUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import java.text.SimpleDateFormat


class SitePointFragment : BaseFragment<SketchMainPresenter, SketchMainModel>(), SketchMainContract.View {
    private var siteData = arrayListOf<SiteBean>()
    private var siteAdapter = SitePointAdapter(siteData)
    var fragChangeListener: FragChangeListener? = null

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SitePointFragment {
            val fragment = SitePointFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewListener() {
        tvSitAdd.setOnClickListener {
            if (siteData.size<2){
                showSiteDialog()
            }else{
                showToast("最多选择两个界址点")
            }
        }
        btnGraphic.setOnClickListener {
           run site@{
               siteData.forEach {
                   if (it.status=="未编辑"){
                       showToast("界址点坐标不能为空")
                       return@setOnClickListener
                   }
               }
           }
            val gsonBuilder = GsonBuilder()
            gsonBuilder.serializeSpecialFloatingPointValues()
            val gson = gsonBuilder.create()
            mSharedPrefUtil.putString("siteList",gson.toJson(siteData))
            SketchLoadActivity.startAction(mActivity,false)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        sitRv.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = siteAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
        ZXRecyclerDeleteHelper(mActivity, sitRv)
            .setSwipeOptionViews(R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                when(id){
                    R.id.tv_delete->{
                        ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否删除该界址点？"){ dialog, which ->
                            siteData.removeAt(pos)
                            siteAdapter.notifyDataSetChanged()
                            setBtnGraphic(false)
                        }
                    }
                }
            }
            .setClickable {
                fragChangeListener?.onFragGoto(LoadMainFragment.RTK_Point,siteData[it])
            }
        setBtnGraphic(false)
    }

    /**
     * 设置图形按钮和添加按钮状态
     */
    private fun setBtnGraphic(isEnable:Boolean){
        btnGraphic.apply {
            this.isEnabled = isEnabled
            this.isClickable=isEnable
            setBackgroundResource(if (isEnable) R.drawable.shape_btn_bg else R.drawable.shape_btn_normal_bg)
        }
        tvSitAdd.visibility = if (isEnable) View.GONE else View.VISIBLE
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_site_point
    }

    private fun showSiteDialog(){
        var data = arrayListOf<String>()
        val siteHashmap = LinkedHashMap<String, SiteBean>()
        mSharedPrefUtil.getString("graphicList")?.let {
            val points =   Gson().fromJson<List<PointF>>(it,object : TypeToken<List<PointF>>(){}.type)
            if (!points.isNullOrEmpty()){
                points.forEachIndexed { index, pointF ->
                    val key = "界址点J$index"
                    //保存界址对象
                    siteHashmap[key]=SiteBean(title = key,point = pointF)
                    data.add(key)
                }
                ZXDialogUtil.showListDialog(mContext,"请选择","确定",data,
                    DialogInterface.OnClickListener { dialog, which ->
                        siteHashmap[data[which]]?.let {siteBean->
                            //是否相同
                            siteData.forEach {
                                if (it.title==siteBean.title&&it.point==siteBean.point){
                                    showToast("界址点不能为同一个点")
                                    return@let
                                }
                            }
                            siteData.add(siteBean)
                            siteAdapter.notifyDataSetChanged()
                            //设置按钮是否可以点击
                            if (siteData.size==2){
                                setBtnGraphic(true)
                            }else{
                                setBtnGraphic(false)
                            }
                           // fragChangeListener?.onFragGoto(LoadMainFragment.RTK_Point,siteBean)
                        }
                    },
                    DialogInterface.OnClickListener { dialog, which ->

                    },false)
            }
        }

    }
    fun refreshData(any: Any?){
       any?.let {
           if (it is ArrayList<*>){
               val list = it as ArrayList<RtkPointBean>
               if (list.isNotEmpty()&&siteData.isNotEmpty()){
                   siteData.forEach {
                       if (it.id==list[0].parentId){
                           it.rtkList?.clear()
                           it.rtkList?.addAll(list)
                           return@forEach
                       }
                   }
               }
           }else{
               val rtkPointBean = it as RtkPointBean
               run siteData@ {
                   siteData?.forEach {
                       if (it.id==rtkPointBean.parentId){
                           it.rtkList?.clear()
                           it.rtkList?.add(rtkPointBean)
                           Log.e("fdfdf","${it.rtkList?.size?:0}")
                           return@siteData
                       }
                   }
               }
           }
       }
        siteAdapter.notifyDataSetChanged()
    }

}