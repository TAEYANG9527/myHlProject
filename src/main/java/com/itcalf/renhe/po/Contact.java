package com.itcalf.renhe.po;

import java.io.Serializable;

public class Contact implements Serializable {

    private static final long serialVersionUID = 7392938055673357174L;

    private String mySid;//登入者的sid

    private String id;
    private String email;
    private String name;
    private String job;
    private String company;
    private String contactface;
    private int accountType;// 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealname;// 是否是实名认证的会员
    private boolean blockedContact;//是否已拉黑该会员
    private boolean beBlocked;// 是否已被该会员拉黑
    private boolean imValid;// boolean 是否有有效的IM账号
    private int imId;// int 有效的IM账号id
    private String mobile;
    private String tel;

    private int cardId;
    private String vcardContent;//vcard格式名片数据
    private String shortName;
    private int colorIndex;
    private String cover;//背景图片

    private int profileSourceType;//区分存放的类型:0,和聊好友；1.手机通讯录好友；2.名片好友；3.手机通讯录和名片中非好友的和聊会员

    private boolean isSelect;//是否选中
    // 构造方法，方便添加数据
    public Contact() {

    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getMySid() {
        return mySid;
    }

    public void setMySid(String mySid) {
        this.mySid = mySid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContactface() {
        return contactface;
    }

    public void setContactface(String contactface) {
        this.contactface = contactface;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public boolean isRealname() {
        return isRealname;
    }

    public void setRealname(boolean isRealname) {
        this.isRealname = isRealname;
    }

    public boolean isBlockedContact() {
        return blockedContact;
    }

    public void setBlockedContact(boolean blockedContact) {
        this.blockedContact = blockedContact;
    }

    public boolean isBeBlocked() {
        return beBlocked;
    }

    public void setBeBlocked(boolean beBlocked) {
        this.beBlocked = beBlocked;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getVcardContent() {
        return vcardContent;
    }

    public void setVcardContent(String vcardContent) {
        this.vcardContent = vcardContent;
    }

    public int getProfileSourceType() {
        return profileSourceType;
    }

    public void setProfileSourceType(int profileSourceType) {
        this.profileSourceType = profileSourceType;
    }

    @Override
    public String toString() {
        return "Contact [company=" + company + ", contactface=" + contactface + ", id=" + id + ", job=" + job + ", name=" + name
                + "]";
    }

}
