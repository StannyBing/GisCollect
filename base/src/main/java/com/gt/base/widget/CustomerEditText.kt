package com.gt.base.widget

import androidx.appcompat.widget.AppCompatEditText
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet


/**
Create By Pxb on 2019/7/1
 */
class CustomerEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var closeListener: () -> Unit = {}

    init {
        onTouchListener()
        textChangeListener()
    }

    private fun onTouchListener() {
        setOnTouchListener { view, motionEvent ->
            //0 1 2 3 分别对应左 上 右 下
            val drawable = this.compoundDrawables[2] ?: return@setOnTouchListener false
            //drawleft 是 小于 ,drawright 是 大于
            if (motionEvent.getX() > this.width - this.height) {
                this.setText("")
                closeListener()
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }
    }

    private fun textChangeListener() {
        val drawableLeft = this.compoundDrawables[0]
        val drawableRight = this.compoundDrawables[2]
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, drawableRight, null)

                } else {
                    setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

    }

    /**
     * 清除事件回调
     */
    fun setOnCloseListener(closeListener: () -> Unit = {}) {
        this.closeListener = closeListener
    }
}
