package com.neusoft.mpc.norepeat.entity;

public enum FilterTokenType {
    COOKIE("cookie"),
    HEADER("header");

    FilterTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    private String tokenType;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
