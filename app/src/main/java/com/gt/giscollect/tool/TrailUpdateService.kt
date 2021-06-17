package com.gt.giscollect.tool

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.base.bean.toJson
import com.gt.base.manager.UserManager
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.app.MyApplication
import com.gt.module_map.tool.MapTool
import com.gt.module_map.tool.PointTool
import com.stanny.module_rtk.tool.WHandTool
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class TrailUpdateService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //一秒后开始，每隔10秒执行一次
        Observable.interval(0, 10, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                updateTrail()
            }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateTrail() {
        try {

//            val mLocationManager =
//                MyApplication.instance.mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val location =
//                if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                    mLocationManager.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER,
//                        1000,
//                        0f,
//                        object : LocationListener {
//                            override fun onLocationChanged(location: Location?) {
//                            }
//
//                            override fun onStatusChanged(
//                                provider: String?,
//                                status: Int,
//                                extras: Bundle?
//                            ) {
//                            }
//
//                            override fun onProviderEnabled(provider: String?) {
//                            }
//
//                            override fun onProviderDisabled(provider: String?) {
//                            }
//
//                        }
//                    )
//                    mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//                } else {
//                    mLocationManager.requestLocationUpdates(
//                        LocationManager.GPS_PROVIDER,
//                        1000,
//                        0f,
//                        object : LocationListener {
//                            override fun onLocationChanged(location: Location?) {
//                            }
//
//                            override fun onStatusChanged(
//                                provider: String?,
//                                status: Int,
//                                extras: Bundle?
//                            ) {
//                            }
//
//                            override fun onProviderEnabled(provider: String?) {
//                            }
//
//                            override fun onProviderDisabled(provider: String?) {
//                            }
//
//                        }
//                    )
//                    mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                }

            val info = WHandTool.getDeviceInfoOneTime()
            val location = if (info == null) {
                MapTool.mapListener?.getMapView()?.locationDisplay?.mapLocation?.let {
                    PointTool.change4326To3857(it, 4326)
                }
            } else {
//            PointTool.change4326To3857(
                Point(
                    info.longitude,
                    info.latitude,
                    SpatialReference.create(4326)
                )
//            )
            }
            if (location != null) {
                MyApplication.instance.component.repositoryManager()
                    .obtainRetrofitService(ApiService::class.java)
                    .updateTrail(
                        hashMapOf(
                            "x" to location?.x,
                            "y" to location?.y,
                            "userId" to UserManager.user?.userId
                        ).toJson()
                    )
                    .compose(RxSchedulers.io_main())
                    .subscribe()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
