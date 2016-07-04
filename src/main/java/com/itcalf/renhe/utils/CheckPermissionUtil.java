package com.itcalf.renhe.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * description :判断有没有获得手机对应的权限
 * Created by Chans Renhenet
 * 2015/9/10
 */
public class CheckPermissionUtil {

    /**
     * 判断手机通讯录权限是否开启
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isHasMailListPermission(final Context context) {
        boolean permission = (PackageManager.PERMISSION_GRANTED == context.getPackageManager()
                .checkPermission("android.permission.READ_CONTACTS", context.getPackageName()));
        if (permission) {
            return true;
        }
        return false;
    }

    /**
     * 判断写手机通讯录权限是否开启
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isHasWriteMailListPermission(final Context context) {
        boolean permission = (PackageManager.PERMISSION_GRANTED == context.getPackageManager()
                .checkPermission("android.permission.WRITE_CONTACTS", context.getPackageName()));
        if (permission) {
            return true;
        }
        return false;
    }

    /**
     * 判断手机通讯录是否被本身/第三方应用取消授权
     * return true:代表有授权，false：代表授权被取消
     */
    public static boolean isAllowedMailListPermission(final Context context) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        //如果本地联系人为空，也被判断为未授权
        if (null != cursor && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

}
