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
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXToastUtil
import java.io.Serializable

object WHandTool {

    private var isRegister = false
    var isOpen = true
    private val infoListenerList: ArrayList<WHandDeviceListener> = arrayListOf()
    private val handler = Handler()
    private var deviceList = arrayListOf<BluetoothDevice>()

    private var iDivice: IDevice? = null
    private var openLaser = false
    private var newWHandInfo: WHandInfo? = null

    interface WHandRegisterListener {
        fun onScanStart(context: Context) {
            ZXDialogUtil.showLoadingDialog(context, "正在查找设备...")
        }

        fun onScanError(context: Context, errorCode: Int, info: String) {
            ZXDialogUtil.showInfoDialog(
                context,
                "提示",
                "设备查找失败，请确保设备已启动，并已开启手机蓝牙\n错误信息：${errorCode},${info}"
            )
        }

        fun onDeviceLogIn(
            context: Context,
            bluetoothDevice: BluetoothDevice
        ) {
            ZXDialogUtil.showLoadingDialog(context, "正在注册设备...")
        }

        fun onDeviceStatusChange(context: Context, status: Int) {
            ZXDialogUtil.dismissLoadingDialog()
            when (status) {
                BluetoothProfile.STATE_CONNECTING -> {
//                    ZXToastUtil.showToast("QX:设备连接中")
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    ZXDialogUtil.dismissDialog()
//                    ZXToastUtil.showToast("设备连接成功")
                    ZXToastUtil.showToast("连接已设备")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
//                    ZXToastUtil.showToast("QX:设备断开中")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    ZXDialogUtil.dismissDialog()
//                    ZXToastUtil.showToast("设备已断开")
                }
            }
            isRegister = true
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
            WHandRegisterListener {}
    ) {
        ZXDialogUtil.showYesNoDialog(
            context,
            "提示",
            "连接前，请确保设备GPS指示灯（绿色）正在闪烁，否则易出现无法获取到定位的问题"
        ) { dialog, which ->
            listener.onScanStart(context)
            deviceList.clear()
            WHandManager.getInstance().stopScan()
            WHandManager.getInstance().startScan(object : ScanCallback {
                override fun onLeScan(p0: BluetoothDevice, p1: Int, p2: ByteArray) {
                    var isContains = false
                    deviceList.forEach {
                        if (it.address == p0.address) {
                            isContains = true
                            return@forEach
                        }
                    }
                    if (!isContains) {
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
    }

    fun disConnectDivice() {
        isRegister = false
        WHandManager.getInstance().device?.disconnect()
    }

    fun addDeviceInfoListener(listener: WHandDeviceListener) {
        infoListenerList.add(listener)
    }

    fun getDeviceInfoOneTime(): WHandInfo? {
        return newWHandInfo
    }

    fun isRegister() = isRegister

    private fun showDeviceList(context: Context, listener: WHandRegisterListener) {
        val nameList = arrayListOf<String>()
        deviceList.forEach {
            val info = "${it.name}:${it.address}"
            nameList.add(info)
        }
        ZXDialogUtil.dismissDialog()
        ZXDialogUtil.showListDialog(context, "设备列表", "取消", nameList) { dialog, which ->
            WHandManager.getInstance().stopScan()
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
                    isRegister = true
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