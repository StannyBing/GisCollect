package com.gt.entrypad.module.project.func.httpsvideoview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Xiangb on 2019/3/29.
 * 功能：
 */
public class HttpsVideoView extends VideoView {
    public HttpsVideoView(Context context) {
        super(context);
    }

    public HttpsVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HttpsVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLUtils.createSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new SSLUtils.TrustAllHostnameVerifier());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
