package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * 用户详情
 *
 * @author xp
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 5687209122688406931L;
    private Long id; // 会员ID
    private String email;// 电子邮箱
    private String name;// 用户名
    private int state;// 说明 ：1、登录成功；-1、用户名或密码错误；-2、用户名或密码为空
    private String userface;// 用户头像url
    private String title;// 职务信息
    private String company;// 公司信息
    private String sid;// 加密后的id用于请求档案等一些页面
    private String adSId;// 加密后用户id和密码的信息每次请求都要带上它

    private String pwd;// 密码
    private String logintime;
    private boolean isRemember = true;
    private String mobile;
    private String loginAccountType;

    private boolean imValid;
    private int imId;
    private String imPwd;
    private String location;

    private int prov;// int 说明：用户所在省份，若为-1，则用户未留省份信息
    private int city;//int 说明：用户所在城市，若为-1，则用户未留城市信息
    private String industry;
    private int accountType;// int 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealname;// boolean 是否是实名认证的会员

    private String errorInfo;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserface() {
        return userface;
    }

    public void setUserface(String userface) {
        this.userface = userface;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAdSId() {
        return adSId;
    }

    public void setAdSId(String adSId) {
        this.adSId = adSId;
    }

    public String getLogintime() {
        return logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public void setRemember(boolean isRemember) {
        this.isRemember = isRemember;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLoginAccountType() {
        return loginAccountType;
    }

    public void setLoginAccountType(String loginAccountType) {
        this.loginAccountType = loginAccountType;
    }

    public boolean isImValid() {
        return imValid;
    }

    public void setImValid(boolean imValid) {
        this.imValid = imValid;
    }

    public int getImId() {
        return imId;
    }

    public void setImId(int imId) {
        this.imId = imId;
    }

    public String getImPwd() {
        return imPwd;
    }

    public void setImPwd(String imPwd) {
        this.imPwd = imPwd;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getProv() {
        return prov;
    }

    public void setProv(int prov) {
        this.prov = prov;
    }

    public int getCity() {
        return city;
    }

    public void setCity(int city) {
        this.city = city;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public boolean isRealName() {
        return isRealname;
    }

    public void setRealName(boolean isRealname) {
        this.isRealname = isRealname;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String toString() {
        return "UserInfo [adSId=" + adSId + ", company=" + company + ", email=" + email + ", id=" + id + ", name=" + name
                + ", sid=" + sid + ", state=" + state + ", title=" + title + ", userface=" + userface + "]";
    }

}
