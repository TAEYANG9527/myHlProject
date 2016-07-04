package com.itcalf.renhe.task;

/**
 * Created by wangning on 2016/2/26.
 */
public interface AsyncTaskCallBack {

    void onPre();

    void doPost(Object result);

}