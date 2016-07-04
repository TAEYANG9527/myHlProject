package com.itcalf.renhe.bean;

/**
 * Created by wangning on 2016/6/29.
 */
public class NameAuthRes {
    public int state; //1.请求成功；-1 权限不足；-2 发生未知错误；-10 该会员已经通过实名认证
    public String bizSId; //支付认证时返回的订单号
}
