package com.gt.giscollect.module.main.func.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.esri.arcgisruntime.mapping.view.MapView
import com.gt.giscollect.R
import kotlinx.android.synthetic.main.fragment_map.view.*

class MapCompassView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    var mAngle = 0f
    var mPaint: Paint? = null
    var mBitmap: Bitmap? = null
    var mMatrix: Matrix? = null

    var mMapView: MapView? = null

    fun initMap(mapView: MapView?) {
        this.mMapView = mapView
        if (mMapView != null) {
            mMapView!!.addMapRotationChangedListener { setRotationAngle(mMapView!!.mapRotation) }
        }
    }

    init {
        mPaint = Paint()
        mMatrix = Matrix()
        this.visibility = GONE //默认不显示
        alpha = 0.9f
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_map_compass)

        setOnClickListener {
            mMapView?.setViewpointRotationAsync(0.0)
            setRotationAngle(0.0)
            postDelayed({
                this.visibility = GONE
            }, 500)
        }
    }

    /** Updates the angle, in degrees, at which the compass is draw within this view.  */
    private fun setRotationAngle(angle: Double) {
        // Save the new rotation angle.
        mAngle = angle.toFloat()
        if (mAngle > 0) {
            this.visibility = VISIBLE //只要一旋转就显示出来
        }

        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        mMatrix!!.reset()
        mMatrix!!.postRotate(-mAngle, mBitmap!!.height / 2.toFloat(), mBitmap!!.width / 2.toFloat())
        canvas?.drawBitmap(mBitmap!!, mMatrix!!, mPaint)
        super.onDraw(canvas)
    }
}