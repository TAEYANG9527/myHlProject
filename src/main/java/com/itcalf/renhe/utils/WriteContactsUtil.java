package com.itcalf.renhe.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.itcalf.renhe.bean.HlMemberSaveToMail;
import com.itcalf.renhe.po.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * description :往通讯录里写入联系人
 * Created by Chans Renhenet
 * 2015/12/16
 */
public class WriteContactsUtil {

    private Context context;

    public WriteContactsUtil(Context context) {
        this.context = context;
    }

    /*
    * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获得系统返回的rawContactId
    * 这时后面插入data表的数据，只有执行空值插入，才能使插入的联系人在通讯录里面可见
    */
    public void signalInsert() {
        ContentValues values = new ContentValues();
        /*
         * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获得系统返回的rawContactId
         */
        Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        System.out.println("--insert Id--" + rawContactId);

        //往data表里写入姓名数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE); //内容类型
        values.put(StructuredName.GIVEN_NAME, "Alisa");
        context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

        //往data表里写入电话数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER, "13800006666");
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

        //往data表里写入Email的数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        values.put(Email.DATA, "66666@itcast.cn");
        values.put(Email.TYPE, Email.TYPE_WORK);

        context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
    }

    /**
     * 单条写入
     *
     * @param contact
     * @return -1:没有权限，0：写入失败，1写入成功
     */
    public int SignalToAdd(Contact contact) {
        if (null == contact)
            return 0;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withYieldAllowed(true).build());
        //名字
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getName())
                .withYieldAllowed(true).build());
        //手机主号码
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, contact.getMobile())
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .withValue(Phone.LABEL, "和聊")
                .withYieldAllowed(true).build());
        //邮箱，如果有写入
        if (!TextUtils.isEmpty(contact.getEmail())) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                    .withValue(Email.DATA, contact.getEmail())
                    .withValue(Email.TYPE, Email.TYPE_WORK)
                    .withYieldAllowed(true).build());
        }
        //公司
        if (!TextUtils.isEmpty(contact.getCompany())) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                    .withValue(Organization.DATA, contact.getCompany())
                    .withValue(Organization.TYPE, Organization.TYPE_WORK)
                    .withYieldAllowed(true).build());
        }
        if (ops != null) {
            try {
                ContentProviderResult[] results = context.getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, ops);
                return 1;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    /*
     * 批量添加，处于用一个事务中
     */
    public int BatchToAdd(List<HlMemberSaveToMail> contacts) {
        if (null == contacts || contacts.size() <= 0)
            return 0;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex;
        for (HlMemberSaveToMail contact : contacts) {
            rawContactInsertIndex = ops.size();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build());
            //名字
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getHlContactRenheMember().getName())
                    .withYieldAllowed(true).build());
            //手机主号码
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, contact.getHlContactRenheMember().getMobile())
                    .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                    .withValue(Phone.LABEL, "和聊")
                    .withYieldAllowed(true).build());
            //邮箱，如果有写入
//            if (!TextUtils.isEmpty(contact.getHlContactRenheMember().gete) {
//                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
//                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
//                        .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
//                        .withValue(Email.DATA, contact.getEmail())
//                        .withValue(Email.TYPE, Email.TYPE_WORK)
//                        .withYieldAllowed(true).build());
//            }
            //公司
            if (!TextUtils.isEmpty(contact.getHlContactRenheMember().getCompany())) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                        .withValue(Organization.DATA, contact.getHlContactRenheMember().getCompany())
                        .withValue(Organization.TYPE, Organization.TYPE_WORK)
                        .withYieldAllowed(true).build());
            }
        }
        if (ops != null) {
            try {
                ContentProviderResult[] results = context.getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, ops);
                return 1;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

}
