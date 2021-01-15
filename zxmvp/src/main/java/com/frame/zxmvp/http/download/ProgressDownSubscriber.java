package com.frame.zxmvp.http.download;


import com.frame.zxmvp.http.download.listener.DownloadOnNextListener;
import com.frame.zxmvp.http.download.manager.HttpDownManager;
import com.frame.zxmvp.http.unzip.HttpProgressListener;

import java.io.File;
import java.lang.ref.WeakReference;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by WZG on 2016/7/16.
 */
public class ProgressDownSubscriber<T> extends Subscriber<T> implements HttpProgressListener {
    //弱引用结果回调
    private WeakReference<DownloadOnNextListener> mSubscriberOnNextListener;
    /*下载数据*/
    private DownInfo downInfo;


    public ProgressDownSubscriber(DownInfo downInfo) {
        this.mSubscriberOnNextListener = new WeakReference<>(downInfo.getListener());
        this.downInfo = downInfo;
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onStart() {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onStart();
        }
        downInfo.setState(HttpState.START);
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onCompleted() {
        if (mSubscriberOnNextListener.get() != null) {
            File file = new File(downInfo.getSavePath());
            if (file.exists()) {
                mSubscriberOnNextListener.get().onComplete(file);
            } else {
                mSubscriberOnNextListener.get().onError("文件下载失败，请重试！");
            }
        }
        downInfo.setState(HttpState.FINISH);
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        /*停止下载*/
        HttpDownManager.getInstance().stopDown(downInfo);
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onError(e.getMessage());
        }
        downInfo.setState(HttpState.ERROR);
    }

    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     *
     * @param t 创建Subscriber时的泛型类型
     */
    @Override
    public void onNext(T t) {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onNext(t);
        }
    }

    @Override
    public void update(long read, long count, boolean done) {
        if (count != -1) {
            if (downInfo.getCountLength() > count) {
                read = downInfo.getCountLength() - count + read;
            } else {
                downInfo.setCountLength(count);
            }
        }
        downInfo.setReadLength(read);
        if (mSubscriberOnNextListener.get() != null) {
            /*接受进度消息，造成UI阻塞，如果不需要显示进度可去掉实现逻辑，减少压力*/
            rx.Observable.just(read).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            /*如果暂停或者停止状态延迟，不需要继续发送回调，影响显示*/
                            if (downInfo.getCountLength() == 0 || downInfo.getState() == HttpState.PAUSE || downInfo.getState() == HttpState.STOP)
                                return;
                            downInfo.setState(HttpState.DOWN);
                            long progress = (long) aLong * 100 / (long) downInfo.getCountLength();
                            mSubscriberOnNextListener.get().updateProgress((int) progress);
                        }
                    });
        }
    }

}