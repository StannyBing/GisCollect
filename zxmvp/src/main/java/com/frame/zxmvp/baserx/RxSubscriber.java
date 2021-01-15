package com.frame.zxmvp.baserx;

import android.util.Log;

import com.frame.zxmvp.base.IView;
import com.google.gson.JsonSyntaxException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;
import rx.Subscriber;


/**
 * des:订阅封装
 * Created by xsf
 * on 2016.09.10:16
 */

public abstract class RxSubscriber<T> extends Subscriber<T> {
    private String msg;
    private IView iView;

    public RxSubscriber() {

    }

    public RxSubscriber(IView iView) {
        this(iView, "正在加载中");
    }

    public RxSubscriber(IView iView, String msg) {
        this.iView = iView;
        this.msg = msg;
    }

    @Override
    public void onCompleted() {
        if (iView != null) {
            iView.dismissLoading();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (iView != null) {
            try {
                if (msg != null) {
                    iView.showLoading(msg);
                } else {
                    iView.showLoading("正在加载中...");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onNext(T t) {

        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        try {
            if (iView != null) {
                iView.dismissLoading();
            }

            e.printStackTrace();
            String message = e.getMessage();
            int code = 0;
            if (e instanceof UnknownHostException || e instanceof ConnectException) {
                code = 50000;
                message = "网络连接失败，请连接网络！";
            } else if (e instanceof SocketTimeoutException) {
                code = 50001;
                message = "网络请求超时！";
            } else if (e instanceof HttpException) {
                code = 50002;
                message = "服务器错误！";
            } else if (e instanceof JsonSyntaxException) {
                code = 50004;
                message = "结果未正确封装，请稍后重试";
            } else if (e instanceof ServerException) {
                message = e.getMessage();
                code = ((ServerException) e).getCode();
            }


            //        String message = "";
            //        //网络
            //        if (!NetWorkUtils.isNetConnected(BaseApplication.getAppContext())) {
            //            message = "网络未连接，请检查后再试";
            //        }
            //        //服务器
            //        else if (e instanceof ServerException) {
            //            message = e.getMessage();
            //        } else if (e instanceof JsonSyntaxException) {
            //            Log.e("error", "未对结果进行正确封装，请检查");
            //            message = "结果未正确封装，请稍后重试";
            //        }
            //        //其它
            //        else {
            //            message = "请求出错，请稍后重试";
            //        }
            //        Log.e("错误:", e.getMessage() + ":\n" + e.getStackTrace()[0]);

            if (message.length() > 20) {
                message = "请求出错，请重新再试";
                Log.e("errot", message);
            }
            _onError(code, message);
        } catch (Exception ea) {
            ea.printStackTrace();
        }
    }

    protected abstract void _onNext(T t);

    protected abstract void _onError(int code, String message);

}
