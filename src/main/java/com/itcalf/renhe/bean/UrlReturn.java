package com.itcalf.renhe.bean;

/**
 * description :和财富url返回结果集
 * Created by Chans Renhenet
 * 2015/10/20
 */
public class UrlReturn {
    private int state;
    private String url;
    private String describe;

    public UrlReturn() {
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
