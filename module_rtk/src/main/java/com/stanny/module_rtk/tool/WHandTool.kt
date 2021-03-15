package com.stanny.module_rtk.tool

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import com.woncan.whand.WHandInfo
import com.woncan.whand.WHandManager
import com.woncan.whand.device.IDevice
import com.woncan.whand.listener.OnConnectListener
import com.woncan.whand.scan.ScanCallback
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXSharedPrefUtil
import com.zx.zxutils.util.ZXToastUtil
import java.io.Serializable

object WHandTool {

    var isConnect = false//是否连接
    var isOpen = true//是否允许接入
    var isAutoConnect = false//是否自动连接
    private val infoListenerList: ArrayList<WHandDeviceListener> = arrayListOf()
    private val handler = Handler()
    private var deviceList = arrayListOf<BluetoothDevice>()

    private var iDivice: IDevice? = null
    private var openLaser = false
    private var newWHandInfo: WHandInfo? = null

    //自动连接IP
    var autoConnectDeviceAddress = ""
        set(value) {
            ZXSharedPrefUtil().putString("autoConnectDeviceAddress", value)
            field = value
        }
        get() {
            if (field.isEmpty()) {
                field = ZXSharedPrefUtil().getString("autoConnectDeviceAddress", "")
            }
            return field
        }

    interface WHandRegisterListener {
        fun onScanStart(context: Context) {
            if (!isAutoConnect) ZXDialogUtil.showLoadingDialog(context, "正在查找设备...")
        }

        fun onScanError(context: Context, errorCode: Int, info: String) {
            if (!isAutoConnect) ZXDialogUtil.showInfoDialog(
                context,
                "提示",
                "设备查找失败，请确保设备已启动，并已开启手机蓝牙\n错误信息：${errorCode},${info}"
            )
        }

        fun onDeviceLogIn(
            context: Context,
            bluetoothDevice: BluetoothDevice
        ) {
            if (!isAutoConnect) ZXDialogUtil.showLoadingDialog(context, "正在注册设备...")
        }

        fun onDeviceStatusChange(context: Context, status: Int) {
            if (!isAutoConnect) ZXDialogUtil.dismissLoadingDialog()
            isConnect = true
            when (status) {
                BluetoothProfile.STATE_CONNECTING -> {
//                    ZXToastUtil.showToast("QX:设备连接中")
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    if (!isAutoConnect) ZXDialogUtil.dismissDialog()
//                    ZXToastUtil.showToast("设备连接成功")
//                    ZXToastUtil.showToast("连接已设备")
//                    isConnect = true
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
//                    ZXToastUtil.showToast("QX:设备断开中")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (!isAutoConnect) ZXDialogUtil.dismissDialog()
//                    ZXToastUtil.showToast("设备已断开")
//                    isConnect = false
                }
            }
//            isConnect = true
        }

        fun onDeviceAccess() {
            ZXToastUtil.showToast("设备连接成功")
        }

        fun initWHandAccount(): WHandAccount {
            return WHandAccount(
                "183.230.183.10",
                2102,
                "test123",
                "123",
                "RTKRTCM32"
            )
        }
    }

    interface WHandDeviceListener {
        fun onDeviceInfoCallBack(info: WHandInfo?)
    }

    data class WHandAccount(
        var ip: String,
        var port: Int,
        var account: String,
        var password: String,
        var mountpoint: String
    ) : Serializable

    /**
     * 设备注册
     */
    fun registerWHand(
        context: Context,
        listener: WHandRegisterListener = object :
            WHandRegisterListener {},
        isAuto: Boolean = false
    ) {
        isAutoConnect = isAuto
        if (isAutoConnect) {
            startScanf(listener, context)
        } else {
            ZXDialogUtil.showYesNoDialog(
                context,
                "提示",
                "连接前，请确保本设备GPS及蓝牙已正常开启，否则易出现无法获取到定位的问题"
            ) { dialog, which ->
                startScanf(listener, context)
            }
        }
    }

    private fun startScanf(
        listener: WHandRegisterListener,
        context: Context
    ) {
        listener.onScanStart(context)
        deviceList.clear()
        WHandManager.getInstance().startScan(object : ScanCallback {
            override fun onLeScan(p0: BluetoothDevice, p1: Int, p2: ByteArray) {
                var isContains = false
                deviceList.forEach {
                    if (it.address == p0.address) {
                        isContains = true
                        return@forEach
                    }
                }
                if (isAutoConnect) {
                    if (p0.address == autoConnectDeviceAddress) {
                        getDeviceInfo(
                            context,
                            listener,
                            p0
                        )
                        WHandManager.getInstance().stopScan()
                    }
                } else if (!isContains) {
                    deviceList.add(p0)
                    showDeviceList(
                        context,
                        listener
                    )
                }
            }

            override fun onError(errorCode: Int, message: String) {
                listener.onScanError(context, errorCode, message)
            }
        })
    }

    fun disConnectDivice() {
        isConnect = false
        WHandManager.getInstance().device?.setOnConnectListener(null)
        WHandManager.getInstance().device?.setOnConnectionStateChangeListener(null)
        try {
            infoListenerList.forEach {
                it.onDeviceInfoCallBack(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        WHandManager.getInstance().device?.disconnect()
        iDivice = null
    }

    fun addDeviceInfoListener(listener: WHandDeviceListener) {
        infoListenerList.add(listener)
    }

    fun getDeviceInfoOneTime(): WHandInfo? {
        return newWHandInfo
    }

    private fun showDeviceList(context: Context, listener: WHandRegisterListener) {
        val nameList = arrayListOf<String>()
        deviceList.forEach {
            val info = "${it.name}:${it.address}"
            nameList.add(info)
        }
        ZXDialogUtil.dismissDialog()
        ZXDialogUtil.showListDialog(context, "设备列表", "取消", nameList) { dialog, which ->
            WHandManager.getInstance().stopScan()
            autoConnectDeviceAddress = deviceList[which].address
            getDeviceInfo(
                context,
                listener,
                deviceList[which]
            )
        }.setOnCancelListener {
            WHandManager.getInstance().stopScan()
        }
    }

    private fun getDeviceInfo(
        context: Context,
        listener: WHandRegisterListener,
        bluetoothDevice: BluetoothDevice
    ) {
        listener.onDeviceLogIn(context, bluetoothDevice)
        iDivice = WHandManager.getInstance().connect(context, bluetoothDevice)
        val whandAccount = listener.initWHandAccount()
        iDivice?.setNtripConfig(
            whandAccount.ip,
            whandAccount.port,
            whandAccount.mountpoint,
            whandAccount.account,
            whandAccount.password
        )
        iDivice?.setOnConnectionStateChangeListener { status, newState ->
            handler.post {
                iDivice?.showLaser(openLaser)
                listener.onDeviceStatusChange(context, status)
            }
        }
        iDivice?.setOnConnectListener(object : OnConnectListener {
            override fun onDeviceChanged(p0: WHandInfo?) {
                handler.post {
                    if (iDivice != null) {
                        isConnect = true
                    }
                    newWHandInfo = p0
                    try {
                        infoListenerList.forEach {
                            it.onDeviceInfoCallBack(p0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onNameChanged(p0: String?) {
//                ZXToastUtil.showToast(p0)
            }

            override fun onAccountChanged(p0: String?) {
//                ZXToastUtil.showToast(p0)
            }

            override fun onError(p0: Exception?) {
//                ZXToastUtil.showToast(p0?.message?.toString())
            }
        })
    }

    fun openLaser(open: Boolean) {
        iDivice?.showLaser(open)
        openLaser = open
    }

    fun getRtkInfo(): String {
        val info = newWHandInfo
        val rtkInfoBuilder = StringBuilder()
        rtkInfoBuilder.append("GPS收星颗数：${info?.gpsNum}\n")
        rtkInfoBuilder.append(
            "解算精度：${when (info?.rtkType) {
                -1 -> "未收到"
                1 -> "单点定位"
                2 -> "码差分定位"
                4 -> "固定定位"
                5 -> "浮点定位"
                else -> "未收到"
            }
            }\n"
        )
        rtkInfoBuilder.append(
            "定位精度：水平:${(info?.accuracyFlat ?: 0) / 1000.0}米,高程:${(info?.accuracyAlt
                ?: 0) / 1000.0}米\n"
        )
        rtkInfoBuilder.append("经纬度：${info?.longitude},${info?.latitude}\n")
        rtkInfoBuilder.append("加速度：${info?.accelerationX},${info?.accelerationY},${info?.accelerationZ}\n")
        rtkInfoBuilder.append("角速度：${info?.spinX},${info?.spinY},${info?.spinZ}")
        return rtkInfoBuilder.toString()
    }

}