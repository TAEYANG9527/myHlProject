package com.itcalf.renhe.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.ContactList;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.eventbusbean.ContactDeleteOrAndEvent;
import com.itcalf.renhe.http.TaskManager;

import java.util.ArrayList;
import java.util.List;

import cn.renhe.heliao.idl.contact.ImportContact;
import de.greenrobot.event.EventBus;

/**
 * @author Chan
 * @description 联系人操作类
 * @date 2015-6-30
 */
public class ContactsUtil implements com.itcalf.renhe.http.Callback {

    private Context context;
    private IContactCommand contactCommand;
    private UserInfo mUserInfo;
    private ContactList clist;
    private boolean isContinue = false;
    private int index = 0;
    private SharedPreferences blackListSp;
    private SharedPreferences.Editor blackListEditor;

    //更新手机通讯录
    private AsyncQueryHandler asyncQuery;
    private SharedPreferences mobileSp;
    private SharedPreferences.Editor mobileEditor;
    private String lastReadTime;

    private static final int ID_TASK_PUSH_MOBILELIST = TaskManager.getTaskId();//将本地通讯录导入到服务端

    public ContactsUtil(Context context) {
        this.context = context;
        blackListSp = context.getSharedPreferences(Constants.BLOCKED_CONTACTS_SHAREDPREFERENCES, 0);
        blackListEditor = blackListSp.edit();
        mobileSp = context.getSharedPreferences("last_upload_mobile_time" + RenheApplication.getInstance().getUserInfo().getSid(),
                0);
        mobileEditor = mobileSp.edit();
    }

    /**
     * 提醒人脉列表去获取新的人脉
     */
    public void SyncContacts() {
        ContactDeleteOrAndEvent contactDeleteOrAndEvent = new ContactDeleteOrAndEvent(ContactDeleteOrAndEvent.CONTACT_EVENT_TYPE_ADD);
        EventBus.getDefault().post(contactDeleteOrAndEvent);//通知人脉列表获取新增的item
    }

    /**
     * @param openId
     * @param type   1:拉入黑名单；2解除黑名单
     */
    public void updateContactBlockState(int openId, int type, String name, String userFace) {
        switch (type) {
            case 1:
                blackListEditor.putBoolean(openId + "", true);
                blackListEditor.commit();
                //通知会话列表删除被拉黑的好友对话
                context.sendBroadcast(new Intent(Constants.BroadCastAction.BLOCKED_CONTACTS));
                updateConversationNotify(false, openId, name, userFace);
                break;
            case 2:
                updateConversationNotify(true, openId, name, userFace);
                blackListEditor.putBoolean(openId + "", false);
                blackListEditor.commit();
                break;
            default:
                break;
        }

    }

    /**
     * 设置会话是否免打扰，供ios客户端用
     */
    private void updateConversationNotify(final boolean isNotify, int openId, final String name, final String userFace) {
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (null != conversation) {
                    //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                    ConversationListUtil.updateChatConversationExtension(conversation, name, userFace);
                    final Conversation conversation2 = conversation;
                    conversation.updateNotification(isNotify, new Callback<Void>() {
                        @Override
                        public void onException(String arg0, String arg1) {
                        }

                        @Override
                        public void onProgress(Void arg0, int arg1) {

                        }

                        @Override
                        public void onSuccess(Void arg0) {

                        }

                    });
                }
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, name, userFace, null, Conversation.ConversationType.CHAT, Long.parseLong(openId + ""));
    }

    private boolean aboveJellyBean = false;//是否>16

    /**
     * 同步手机联系人
     */
    public void SyncMobileContacts() {
        boolean isAuth = mobileSp.getBoolean("isAuthImport", false);
        if (RenheApplication.getInstance().isUserExist() && isAuth) {
            if (null == asyncQuery) {
                asyncQuery = new MyAsyncQueryHandler(context.getContentResolver());
            }
            //判断版本android 小米4.1.2 contact_last_updated_timestamp字段不存在
            if (Build.VERSION.SDK_INT > 16) {
                aboveJellyBean = true;
                //取时间
                String lastSyncTime = mobileSp.getString("lastUpdateTime", "0");
                //上传通讯录
                Uri uri = Uri.parse("content://com.android.contacts/data/phones");
                String[] projection = {"_id", "raw_contact_id", "display_name", "data1", "contact_last_updated_timestamp", "data3",
                        "sort_key"};//查询字段
                String condition = "contact_last_updated_timestamp>" + lastSyncTime;//查询条件time
                asyncQuery.startQuery(0, null, uri, projection, condition, null,
                        "contact_last_updated_timestamp COLLATE LOCALIZED asc");
//                asyncQuery.startQuery(0, null, uri, projection, null, null, null);
            } else {
                aboveJellyBean = false;
                //上传通讯录
                Uri uri = Uri.parse("content://com.android.contacts/data/phones");
                String[] projection = {"_id", "raw_contact_id", "display_name", "data1", "data3",
                        "sort_key"};//查询字段
                asyncQuery.startQuery(0, null, uri, projection, null, null,
                        null);
            }
        }
    }

    /**
     * 上传本地手机通讯录的好友
     */
    private void pushMobileContactsList(List<ImportContact.ContactItem> contactItemList) {
        if (TaskManager.getInstance().exist(ID_TASK_PUSH_MOBILELIST)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_PUSH_MOBILELIST);
        GrpcController grpcController = new GrpcController();
        grpcController.pushMobileContactsList(ID_TASK_PUSH_MOBILELIST, contactItemList);
    }

    @Override
    public void onSuccess(int type, Object result) {
        TaskManager.getInstance().removeTask(type);
        //存下时间
        mobileEditor.putString("lastUpdateTime", lastReadTime);
        mobileEditor.commit();
        SyncContacts();
        if (null != context) {
            //发广播，通知拉取所有联系人<仅在手机导入联系人界面接收>
            context.sendBroadcast(new Intent(BroadCastAction.UPLOAD_MOBILE_CONTACTS_ACTION));
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        TaskManager.getInstance().removeTask(type);
        if (null != context) {
            context.sendBroadcast(new Intent(BroadCastAction.UPLOAD_MOBILE_CONTACTS_FAILED_ACTION));
            ToastUtil.showToast(context, R.string.import_contact_failed);
        }
    }

    @Override
    public void cacheData(int type, Object data) {
    }

    /**
     * 查找手机通讯录
     */
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        ContentResolver contentResolver;
        List<ImportContact.ContactItem> list;

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
            this.contentResolver = cr;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                /***只传id ， 名字和号码**/
                list = new ArrayList<>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    //过滤：写入的数据不上传
                    String label = aboveJellyBean ? cursor.getString(5) : cursor.getString(4);
                    if (TextUtils.isEmpty(label) || !label.equals("和聊")) {
                        ImportContact.ContactItem.Builder builder = ImportContact.ContactItem.newBuilder();
                        builder.setId(cursor.getString(1));
                        builder.setName(cursor.getString(2));
                        String[] numbers = new String[]{cursor.getString(3)};
                        for (String mobile : numbers) {
                            if (!TextUtils.isEmpty(mobile))
                                builder.addMobile(mobile);
                        }
                        ImportContact.ContactItem contactItem = builder.build();
                        list.add(contactItem);
//                        ContactUpLoadBean cv = new ContactUpLoadBean();
//                        cv.setId(cursor.getString(1));
//                        cv.setName(cursor.getString(2));
//                        cv.setMobiles(numbers);
//                        list.add(cv);
                    }
                }
                try {
                    lastReadTime = aboveJellyBean ? cursor.getString(4) : "-1";
                } catch (Exception e) {
                    e.printStackTrace();
                    lastReadTime = "0";
                }
                //合并相同的id电话号码；
                for (int i = 0; i < list.size(); i++) {
                    for (int j = list.size() - 1; j > i; j--) {
                        if (null != list.get(i) && null != list.get(j) && list.get(j).getId().equals(list.get(i).getId())) {
                            ImportContact.ContactItem.Builder builder = list.get(i).toBuilder();
                            List<String> jMobileList = list.get(j).getMobileList();
                            for (String jMobile : jMobileList) {
                                if (!TextUtils.isEmpty(jMobile))
                                    builder.addMobile(jMobile);
                            }
//                            String[] numbers = list.get(i).getMobiles();
//                            String[] numbers2 = list.get(j).getMobiles();
//                            list.get(i).setMobiles(ArrayUtils.addAll(numbers, numbers2));
                            list.remove(j);
                        }
                    }
                }
                cursor.close();
                if (null != list && list.size() > 0) {
                    pushMobileContactsList(list);
                }
            } else {
                context.sendBroadcast(new Intent(BroadCastAction.UPLOAD_MOBILE_CONTACTS_FAILED_ACTION));
            }
        }

        @Override
        public void startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
            super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
        }
    }
}
