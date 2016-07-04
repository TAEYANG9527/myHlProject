package com.itcalf.renhe.utils;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;

import com.orhanobut.logger.Logger;

/**
 * description :监听通讯录表有没有变化
 * Created by Chans Renhenet
 * 2015/12/1
 */
public class ContactsContentObserver extends ContentObserver {

    public static final Uri CONTENT_DATA_URI = ContactsContract.Data.CONTENT_URI;
    //    public static final Uri CONTENT_DATA_URI = Uri.parse("content://com.android.contacts/data/phones");
    private Activity activity = null;

    public ContactsContentObserver(Activity activity, Handler handler) {
        super(handler);
        this.activity = activity;
    }

    @Override
    public void onChange(boolean selfChange) {
//        super.onChange(selfChange);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.e("监听到本地通讯录变更，开始导入新的通讯录人脉");
                new ContactsUtil(activity).SyncMobileContacts();//同步联系人
            }
        }, 60 * 1000);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }
}
