package com.itcalf.renhe.dto;

/**
 * Created by wangning on 2016/6/15.
 */
public class AdUploadPhoto {
    private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
    private String cover;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
