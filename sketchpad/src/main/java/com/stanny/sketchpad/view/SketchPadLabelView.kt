package com.stanny.sketchpad.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.stanny.sketchpad.R
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchPadConstant


class SketchPadLabelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle){
    private var mPaint = Paint()
    private var mPoint = PointF()
    private var text = ""
    var sketchPadListener: SketchPadListener? = null
    private  var labelBitmap: Bitmap
    private  var labelCanvas : Canvas

    init {
        mPaint.apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context,R.color.black)
            strokeWidth = 1.0f
            textSize=14f
            isAntiAlias = true
        }
        labelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        labelCanvas = Canvas(labelBitmap)
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawText(text,mPoint.x,mPoint.y,mPaint)
        }
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                mPoint.x = event.x
                mPoint.y=event.y

                sketchPadListener?.drawLabel(mPoint)
            }
        }
        return true
    }

    fun drawLabel(content:String){
        text = content
        invalidate()
    }

    /**
     * 保存图形
     */
    private fun saveLabelView(){
        draw(labelCanvas)
    }
}