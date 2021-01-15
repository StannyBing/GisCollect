package com.frame.zxmvp.baserx;

import com.frame.zxmvp.base.IView;
import com.frame.zxmvp.base.RxBaseActivity;
import com.frame.zxmvp.base.RxBaseFragment;
import com.frame.zxmvp.basebean.BaseRespose;
import com.trello.rxlifecycle.LifecycleTransformer;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * des:对服务器返回数据成功和失败处理
 * Created by xsf
 * on 2016.09.9:59
 */

/**************使用例子******************/
/*_apiService.login(mobile, verifyCode)
        .compose(RxSchedulersHelper.io_main())
        .compose(RxResultHelper.handleResult())
        .//省略*/

public class RxHelper {
    /**
     * 对服务器返回数据进行预处理
     *
     * @param <T>
     * @return
     */
    public static <T> Observable.Transformer<BaseRespose<T>, T> handleResult() {
        return tObservable -> tObservable.flatMap(new Func1<BaseRespose<T>, Observable<T>>() {
            @Override
            public Observable<T> call(BaseRespose<T> result) {

                if (result.success()) {
                    return createData(result.res);
                } else {
                    return Observable.error(new ServerException(result.code == null ? "100" : result.code, result.msg));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());

    }

    /**
     * 创建成功的数据
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> Observable<T> createData(final T data) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {

                try {
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

    }


//    public static <T> Observable.Transformer<T, T> applySchedulers(final IView view) {
//        return new Observable.Transformer<T, T>() {
//            @Override
//            public Observable<T> call(Observable<T> observable) {
//                return observable.subscribeOn(Schedulers.io())
//                        .doOnSubscribe(new Action0() {
//                            @Override
//                            public void call() {//显示进度条
//                                view.showLoading("请稍后...");
//                            }
//                        })
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doAfterTerminate(new Action0() {
//                            @Override
//                            public void call() {
//                                view.stopLoading();//隐藏进度条
//                            }
//                        }).compose(RxHelper.<T>bindToLifecycle(view));
//            }
//        };
//    }


    public static <T> LifecycleTransformer<T> bindToLifecycle(IView view) {
        if (view instanceof RxBaseActivity) {
            return ((RxBaseActivity) view).bindToLifecycle();
        } else if (view instanceof RxBaseFragment) {
            return ((RxBaseFragment) view).bindToLifecycle();
        } else {
            throw new IllegalArgumentException("view isn't activity or fragment");
        }

    }
}
