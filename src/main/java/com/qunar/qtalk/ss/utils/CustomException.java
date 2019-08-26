package com.qunar.qtalk.ss.utils;

/**
 * @author yunhai.hu
 * @date 2017/7/26
 */
public class CustomException extends Exception {
    private int errcode;

    public CustomException() {
        super();
    }

    public CustomException(int errcode, String message) {
        super(message);
        this.errcode = errcode;
    }

    public CustomException(String message) {
        super(message);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
}
