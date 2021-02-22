package com.gt.base.listener
interface FragChangeListener {

    fun onFragBack(type: String, any: Any? = null)

    fun onFragGoto(type: String, any: Any? = null)

}