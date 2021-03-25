package com.gt.entrypad.tool;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.zx.zxutils.util.ZXFileUtil;
import com.zx.zxutils.util.ZXToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * create by 96212 on 2021/1/27.
 * Email 962123525@qq.com
 * desc
 */
public class CopyAssetsToSd {
    public static void copy(Context myContext, String ASSETS_NAME, String savePath, String saveName) {
        String filename = savePath + "/" + saveName;
        File dir = new File(savePath);
        File file = ZXFileUtil.createNewFile(filename);
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = myContext.getResources().getAssets().open(ASSETS_NAME);
            fos = new FileOutputStream(filename);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
