package com.neusoft.mpc.norepeat.entity;

public enum ResultCode {
    SUCCESS(0, "成功"),
    ERROR(-1, "失败");

    private int code;
    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
