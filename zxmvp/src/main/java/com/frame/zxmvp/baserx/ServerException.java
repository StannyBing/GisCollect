package com.frame.zxmvp.baserx;

/**
 * des:服务器请求异常
 * Created by xsf
 * on 2016.09.10:16
 */
public class ServerException extends Exception{

    private int code;
    public ServerException(String msg) {
        super(msg);
    }

    public ServerException(String code, String msg) {
        super(msg);
        this.code = Integer.parseInt(code);
    }

    public int getCode() {
        return code;
    }

}
