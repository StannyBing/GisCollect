package com.gt.entrypad.module.project.ui.fragment

import android.Manifest
import android.os.Bundle
import android.util.Log
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.Basemap
import com.gt.entrypad.R
import com.gt.entrypad.module.project.mvp.contract.GroundFigureContract
import com.gt.entrypad.module.project.mvp.model.GroundFigureModel
import com.gt.entrypad.module.project.mvp.presenter.GroundFigureresenter
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.gt.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_ground_figure.*


/**
 * create by 96212 on 2021/1/22.
 * Email 962123525@qq.com
 * desc 宗地落图
 */
class GroundFigureFragment : BaseFragment<GroundFigureresenter, GroundFigureModel>(),GroundFigureContract.View{
    private var locationDisplay: LocationDisplay? = null//定位
    private var locationListener: LocationDisplay.LocationChangedListener? = null

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): GroundFigureFragment {
            val fragment = GroundFigureFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ground_figure
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setupMap()
        doLocation()
    }

    override fun onPause() {
        mapView?.pause()
        super.onPause()
    }

    override fun onResume() {
        mapView?.resume()
        super.onResume()
    }

    override fun onDestroy() {
        mapView?.dispose()
        super.onDestroy()
    }
    private fun doLocation(){
        getPermission(arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)){
            (locationDisplay?:mapView?.locationDisplay)?.apply {
                if (isStarted){
                    stop()
                }
                if (locationListener!=null)removeLocationChangedListener(locationListener)
                addLocationChangedListener {
                    Log.e("fdfd","${it.location}")
                }
                autoPanMode=LocationDisplay.AutoPanMode.RECENTER
                startAsync()
            }
        }
    }
    private fun setupMap(){
        try {
            ArcGISRuntimeEnvironment.setLicense("runtimestandard,101,rux00000,none,5SKIXc21JlankElJ")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView?.let {
            it.isAttributionTextVisible = false
            val baseMapType = Basemap.Type.TOPOGRAPHIC
            val latitude = 34.056295
            val longitude = -117.195800
            val levelOfDetail = 13
            val map = ArcGISMap(baseMapType, latitude, longitude, levelOfDetail)
            it.map = map
        }

    }
}