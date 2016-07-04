package com.itcalf.renhe.dto;

/**
 * @author YZQ
 */
public class AccountUpgrade {

    /**
     * 1 成功 -3 升级类型不正确
     */
    private int state;
    /**
     * 业务类型
     */
    private int bizType;
    /**
     * 加密的业务ID
     */
    private String bizSId;
    /**
     * 支付名称
     */
    private String bizSubject;
    /**
     * 支付金额
     */
    private double payFee;
    /**
     * 购买总金额
     **/
    private double totalFee;
    /**
     * 优惠券金额
     **/
    private double couponFee;
    /**
     * VIP抵扣金额
     **/
    private double originalFee;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBizType() {
        return bizType;
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
    }

    public String getBizSId() {
        return bizSId;
    }

    public void setBizSId(String bizSId) {
        this.bizSId = bizSId;
    }

    public String getBizSubject() {
        return bizSubject;
    }

    public void setBizSubject(String bizSubject) {
        this.bizSubject = bizSubject;
    }

    public double getPayFee() {
        return payFee;
    }

    public void setPayFee(double payFee) {
        this.payFee = payFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getCouponFee() {
        return couponFee;
    }

    public void setCouponFee(double couponFee) {
        this.couponFee = couponFee;
    }

    public double getOriginalFee() {
        return originalFee;
    }

    public void setOriginalFee(double originalFee) {
        this.originalFee = originalFee;
    }
}
