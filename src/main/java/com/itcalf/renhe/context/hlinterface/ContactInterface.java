package com.itcalf.renhe.context.hlinterface;

/**
 * Created by wangning on 2016/3/9.
 */
public interface ContactInterface {

    /**
     * 生成HlContacts
     */
    void onGenerateHlContactsListSuccess();

    /**
     * 生成HlContactMap
     */
    void onGenerateHlContactMapSuccess();

    /**
     * 获取本地联系人
     */
    void onHandleLocalDataSuccess();

    /**
     * 将联系人插入本地数据库
     */
    void onHandleDataToDbSuccess();

    /**
     * 获取常用联系人
     */
    void onGetOftenUsedContactSuccess();

}