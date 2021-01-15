package com.frame.zxmvp.base;


import com.frame.zxmvp.baseapp.BaseApplication;
import com.frame.zxmvp.integration.IRepositoryManager;

/**
 * Created by jess on 8/5/16 12:55
 * contact with jess.yan.effort@gmail.com
 */

public class BaseModel implements IModel {

    protected static IRepositoryManager mRepositoryManager =
            BaseApplication.baseApplication.getAppComponent().repositoryManager();//用于管理网络请求层,以及数据缓存层

    public static void init() {
        mRepositoryManager = BaseApplication.baseApplication.getAppComponent().repositoryManager();//用于管理网络请求层,以及数据缓存层
    }
//    public BaseModel(IRepositoryManager repositoryManager) {
//        this.mRepositoryManager = repositoryManager;
//    }

    @Override
    public void onDestroy() {
        mRepositoryManager = null;
    }
}
