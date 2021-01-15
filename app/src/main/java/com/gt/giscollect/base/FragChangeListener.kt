package com.gt.giscollect.base

import android.os.Bundle

interface FragChangeListener {

    fun onFragBack(type: String, any: Any? = null)

    fun onFragGoto(type: String, any: Any? = null)

}