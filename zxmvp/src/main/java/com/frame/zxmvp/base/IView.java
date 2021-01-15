package com.frame.zxmvp.base;

/**
 * des:baseview
 * Created by xsf
 * on 2016.07.11:53
 */
public interface IView {
    //    /*******内嵌加载*******/
    void showLoading(String message);


    void showLoading(String message, int progress);

    void dismissLoading();

    void handleError(int code, String message);

    void showToast(String message);

}
