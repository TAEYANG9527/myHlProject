package com.itcalf.renhe.bean;

import cn.ocrsdk.uploadSdk.OcrCard;

/**
 * Created by wangning on 2015/11/26.
 */
public class OcrColorCard {
    private int avatarbgIndex;
    private OcrCard ocrCard;
    private OcrLocalCard ocrLocalCard;
    public int getAvatarbgIndex() {
        return avatarbgIndex;
    }

    public void setAvatarbgIndex(int avatarbgIndex) {
        this.avatarbgIndex = avatarbgIndex;
    }

    public OcrCard getOcrCard() {
        return ocrCard;
    }

    public void setOcrCard(OcrCard ocrCard) {
        this.ocrCard = ocrCard;
    }

    public OcrLocalCard getOcrLocalCard() {
        return ocrLocalCard;
    }

    public void setOcrLocalCard(OcrLocalCard ocrLocalCard) {
        this.ocrLocalCard = ocrLocalCard;
    }
}
