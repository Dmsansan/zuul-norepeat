package com.neusoft.mpc.norepeat.entity;

import java.io.Serializable;

public class ApiResult implements Serializable {
    private int code;
    private String message;
    private Object data;

    public ApiResult() {

    }

    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ApiResult success(ResultCode resultCode, Object data) {
        ApiResult result = new ApiResult();
        result.setResultCode(resultCode);
        result.setData(data);
        return result;
    }

    public static ApiResult failure(ResultCode resultCode, Object data) {
        ApiResult result = new ApiResult();
        result.setResultCode(resultCode);
        result.setData(data);
        return result;
    }

    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.code();
        this.message = resultCode.message();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
