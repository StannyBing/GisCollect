package com.frame.zxmvp.http.download.listener;

import java.io.File;

/**
 * 下载过程中的回调处理
 * Created by WZG on 2016/10/20.
 */
public abstract class HttpProgressOnNextListener<T> {
    /**
     * 成功后回调方法
     *
     * @param t
     */
    public abstract void onNext(T t);

    /**
     * 开始下载
     */
    public abstract void onStart();

    /**
     * 完成下载
     */
    public abstract void onComplete(File file);


    /**
     * 下载进度
     *
     * @param progress
     */
    public abstract void updateProgress(int progress);

    /**
     * 失败或者错误方法
     * 主动调用，更加灵活
     *
     * @param message
     */
    public void onError(String message) {

    }

    /**
     * 暂停下载
     */
    public void onPuase() {

    }

    /**
     * 停止下载销毁
     */
    public void onStop() {

    }
}
