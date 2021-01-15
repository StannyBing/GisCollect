package com.frame.zxmvp.base;

import android.content.Context;

import com.frame.zxmvp.baserx.RxManager;

/**
 * des:基类presenter
 * Created by xsf
 * on 2016.07.11:55
 */
public abstract class BasePresenter<T, E> {
    public RxManager mRxManage = new RxManager();
    public Context mContext;
    protected E mModel;
    protected T mView;


    public void setVM(T v, E m) {
        this.mView = v;
        this.mModel = m;
        this.onStart();
    }

    public Context getContext() {
        return mContext;
    }

    public void onStart() {
    }

    public void onDestroy() {
        mRxManage.clear();
    }


}
