package com.frame.zxmvp.http.download;


import com.frame.zxmvp.http.download.listener.DownloadOnNextListener;
import com.frame.zxmvp.http.download.manager.HttpService;

/**
 * apk下载请求数据基础类
 * Created by WZG on 2016/10/20.
 */

public class DownInfo {
    /*存储位置*/
    private String savePath;
    /*下载url*/
    private String url;
    /*基础url*/
    private String baseUrl;
    /*文件总长度*/
    private long countLength;
    /*下载长度*/
    private long readLength;
    /*下载唯一的HttpService*/
    private HttpService service;
    /*回调监听*/
    private DownloadOnNextListener listener;
    /*超时设置*/
    private int DEFAULT_TIMEOUT = 30;
    /*下载状态*/
    private HttpState state;
    /*文件类型*/
    private int fileType;
    /*文件名称*/
    private String fileName;
    /*文件修改时间*/
    private String fileUpdateTime;

    /*服务器文件时间*/
    private String serverFileTime;


    public DownInfo(String url, DownloadOnNextListener listener) {
        setUrl(url);
        setBaseUrl(getBasUrl(url));
        setListener(listener);
    }

    public HttpState getState() {
        return state;
    }

    public void setState(HttpState state) {
        this.state = state;
    }

    public DownInfo(String url) {
        setUrl(url);
        setBaseUrl(getBasUrl(url));
    }


    public String getServerFileTime() {
        return serverFileTime;
    }

    public void setServerFileTime(String serverFileTime) {
        this.serverFileTime = serverFileTime;
    }

    public int getConnectionTime() {
        return DEFAULT_TIMEOUT;
    }

    public void setConnectionTime(int DEFAULT_TIMEOUT) {
        this.DEFAULT_TIMEOUT = DEFAULT_TIMEOUT;
    }

    public DownloadOnNextListener getListener() {
        return listener;
    }

    public void setListener(DownloadOnNextListener listener) {
        this.listener = listener;
    }

    public HttpService getService() {
        return service;
    }

    public void setService(HttpService service) {
        this.service = service;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getCountLength() {
        return countLength;
    }

    public void setCountLength(long countLength) {
        this.countLength = countLength;
    }


    public long getReadLength() {
        return readLength;
    }

    public void setReadLength(long readLength) {
        this.readLength = readLength;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUpdateTime() {
        return fileUpdateTime;
    }

    public void setFileUpdateTime(String fileUpdateTime) {
        this.fileUpdateTime = fileUpdateTime;
    }

    /**
     * 读取baseurl
     *
     * @param url
     * @return
     */
    protected String getBasUrl(String url) {
        String head = "";
        int index = url.indexOf("://");
        if (index != -1) {
            head = url.substring(0, index + 3);
            url = url.substring(index + 3);
        }
        index = url.indexOf("/");
        if (index != -1) {
            url = url.substring(0, index + 1);
        }
        return head + url;
    }
}
