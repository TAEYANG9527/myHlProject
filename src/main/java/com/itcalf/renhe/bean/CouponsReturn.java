package com.itcalf.renhe.bean;

import java.util.List;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/10/20
 */
public class CouponsReturn {
    private int state;
    private List<CouponsInfo> couponList;

    public CouponsReturn() {
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<CouponsInfo> getCouponList() {
        return couponList;
    }

    public void setCouponList(List<CouponsInfo> couponList) {
        this.couponList = couponList;
    }
}
