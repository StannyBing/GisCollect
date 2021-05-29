package com.gt.entrypad.module.project.func.view.signView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SignView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0) :View(context, attrs, defStyleAttr) {
    /**
     * 画笔
     */
    private var paint = Paint()
    private var path =Path()
    private lateinit var cacheCanvas : Canvas
    private var t:Int = 0
    /**
     * 签名画布
     */
    private lateinit var signBitmap: Bitmap
    //画笔颜色
    private var paintColor : Int = Color.BLACK
    //画笔宽度
    private var paintWidth  = 15f
    private var lastXAlixs : Float = 0.0f
    private var lastYAlixs : Float = 0.0f
    private var xAlixs : Float = 0.0f
    private var yAlixs : Float = 0.0f
    /**
     * 背景色（指最终签名结果文件的背景颜色,这里我设置为白色）
     *  你也可以设置为透明的
     */

    //是否已经签名
    private var isSigned : Boolean = false
    init {
        init(context)
    }

    fun init(context: Context){
        paint.color = paintColor//设置签名颜色
        paint.style = Paint.Style.STROKE  //设置填充样式
        paint.isAntiAlias = true  //抗锯齿功能
        paint.strokeWidth = paintWidth//设置画笔宽度
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //创建跟view一样大的bitmap，用来保存签名
        signBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        cacheCanvas = Canvas(signBitmap)
        isSigned = false
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //画此次笔画之前的签名
        canvas.drawBitmap(signBitmap, 0f, 0f, paint)
        // 通过画布绘制多点形成的图形
        canvas.drawPath(path,paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //记录每次 X ， Y轴的坐标
        parent.parent.requestDisallowInterceptTouchEvent(true)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastXAlixs = event.x
                lastYAlixs = event.y
                path?.reset()
                path?.moveTo(lastXAlixs, lastYAlixs)
            }

            MotionEvent.ACTION_MOVE -> {
                xAlixs = event.x
                yAlixs = event.y
                path?.lineTo(xAlixs, yAlixs)
                isSigned = true
            }

            MotionEvent.ACTION_UP -> {
                //将路径画到bitmap中，即一次笔画完成才去更新bitmap，而手势轨迹是实时显示在画板上的。
                cacheCanvas.drawPath(path, paint)
                path?.reset()
            }
        }

        // 更新绘制
        invalidate()
        return true
    }
    /**
     * 清除画板
     */
    public fun clear(){
        isSigned = false
        path?.reset()
        paint?.color = paintColor
        cacheCanvas.drawColor(paintColor, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    /**
     * 保存画板
     *
     * @param path       保存到路径
     */
    @SuppressLint("WrongThread")
    @Throws(IOException::class)
    fun save(path: String) {

        val bitmap = signBitmap
        //  如果图片过大的话，需要压缩图片，不过在我测试手机上最大才50多kb

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val buffer = bos.toByteArray()
        if (buffer != null) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }

            val outputStream = FileOutputStream(file)
            outputStream.write(buffer)
            outputStream.close()
        }
    }


    /**
     * 是否有签名
     *
     * @return isSigned
     */
    public fun getHasSigned() : Boolean{
        return isSigned
    }

}