package com.itcalf.renhe.dto;

import java.io.Serializable;

public class TextSize implements Serializable {
    private static final long serialVersionUID = 1L;
    private int state; // 说明：1 请求成功；
    private int renMaiQuanContentSize;// 人脉圈内容字数限制
    private int renMaiQuanCommentSize;// 人脉圈评论字数限制
    private int circleTitleSize;// 圈子名称字数限制
    private int circleDescriptionSize;// 圈子描述字数限制
    /**
     * 上传日志的时间间隔 s
     **/
    private int uploadLogTimeInterval;
    private String searchPlaceholder;// 搜索框默认显示文字

    private int scanImgIndex;// int 使用哪种图片：1 代表 扫一扫的图标 2 代表相机拍照的图标
    /**
     * 会员升级版本控制
     * former 原先的版本；standard 水平版本；vertical 垂直版本
     */
    private String member;

    /**
     * 会员升级 顺序排列;asc 升级;desc 降序
     */
    private String order;

    private static final TextSize textSize = new TextSize();

    private TextSize() {

    }

    public static TextSize getInstance() {
        return textSize;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state)
                .append("renMaiQuanContentSize", renMaiQuanContentSize).append("renMaiQuanCommentSize", renMaiQuanCommentSize)
                .append("circleTitleSize", circleTitleSize).append("circleDescriptionSize", circleDescriptionSize).toString();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getRenMaiQuanContentSize() {
        return renMaiQuanContentSize;
    }

    public void setRenMaiQuanContentSize(int renMaiQuanContentSize) {
        this.renMaiQuanContentSize = renMaiQuanContentSize;
    }

    public int getRenMaiQuanCommentSize() {
        return renMaiQuanCommentSize;
    }

    public void setRenMaiQuanCommentSize(int renMaiQuanCommentSize) {
        this.renMaiQuanCommentSize = renMaiQuanCommentSize;
    }

    public int getCircleTitleSize() {
        return circleTitleSize;
    }

    public void setCircleTitleSize(int circleTitleSize) {
        this.circleTitleSize = circleTitleSize;
    }

    public int getCircleDescriptionSize() {
        return circleDescriptionSize;
    }

    public void setCircleDescriptionSize(int circleDescriptionSize) {
        this.circleDescriptionSize = circleDescriptionSize;
    }

    public int getUploadLogTimeInterval() {
        return uploadLogTimeInterval;
    }

    public void setUploadLogTimeInterval(int uploadLogTimeInterval) {
        this.uploadLogTimeInterval = uploadLogTimeInterval;
    }

    public String getSearchPlaceholder() {
        return searchPlaceholder;
    }

    public void setSearchPlaceholder(String searchPlaceholder) {
        this.searchPlaceholder = searchPlaceholder;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public int getScanImgIndex() {
        return scanImgIndex;
    }

    public void setScanImgIndex(int scanImgIndex) {
        this.scanImgIndex = scanImgIndex;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
