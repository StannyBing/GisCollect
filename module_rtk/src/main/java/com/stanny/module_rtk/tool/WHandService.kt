package com.stanny.module_rtk.tool

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXSharedPrefUtil
import com.zx.zxutils.util.ZXToastUtil
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class WHandService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //一秒后开始，每隔3秒执行一次
        Observable.interval(1, 3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (WHandTool.autoConnectDeviceAddress.isEmpty() || WHandTool.isConnect) {
                    return@subscribe
                }
                WHandTool.registerWHand(this, object : WHandTool.WHandRegisterListener {
                    override fun initWHandAccount(): WHandTool.WHandAccount {
                        ZXToastUtil.showToast("正在自动连接设备")
                        val whandAccount =
                            ZXSharedPrefUtil().getObject<WHandTool.WHandAccount>("whandAccount")
                        WHandTool.WHandAccount(
                            whandAccount.ip,
                            whandAccount.port,
                            whandAccount.account,
                            whandAccount.password,
                            whandAccount.mountpoint
                        )
                        return whandAccount
                    }

                    override fun onDeviceStatusChange(context: Context, status: Int) {
                        super.onDeviceStatusChange(context, status)
                    }

                }, true)
            }
        return super.onStartCommand(intent, flags, startId)
    }
}