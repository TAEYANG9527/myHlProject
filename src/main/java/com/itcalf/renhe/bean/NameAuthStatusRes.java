package com.itcalf.renhe.bean;

/**
 * Created by wangning on 2016/6/29.
 */
public class NameAuthStatusRes {
    public int state; //1.请求成功；-1 权限不足；-2 发生未知错误；-10 该会员已经通过实名认证
    public int realNameStatus; // -1 未认证  0认证中  1已认证
}