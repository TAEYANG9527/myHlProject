package com.itcalf.renhe.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.MyPortal;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.utils.ContactsUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Title: MyJPushReceiver.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2013<br>
 * Create DateTime: 2014-04-06 下午3:40:25<br>
 *
 * @author wangning
 */

/**
 * Created by wangning on 2015/8/24.
 */
public class PushNotification {
    private static SharedPreferences msp;
    private static SharedPreferences.Editor mEditor;
    private static final int INNERMSG_PUSH = 1; // 站内信推送
    private static final int MESSAGENUM_PUSH = 2; // 新消息数目推送
    private static final int VIPCHECKPASS_PUSH = 3; // 会员审核通过推送
    private static final int NOTICE_MESSAGENUM_PUSH = 4; // 新增的动态提醒，更新档案、工作经历等

    private static final int NOTICE_NEW_FRIEND_PUSH = 5; // 新的朋友
    private static final int NOTICE_NEW_INNERMSG_PUSH = 6; // 站内信(新人脉刷新人脉界面)
    private static final int NOTICE_SYSTEMMSG_PUSH = 7; // 新增的动态提醒，更新档案、工作经历等
    private static final int NOTICE_TOUTIAO_PUSH = 8; // 新增的动态提醒，更新档案、工作经历等
    private static final int UNLOGIN_PUSH_MESSAGE = 9; // 新增的动态提醒，更新档案、工作经历等
    private static final int NOTICE_VCARDREC_PUSH = 10; //名片识别成功推送
    private static final int NOTICE_REALNAMEAUTH_PUSH = 11; //实名认证审核结果推送
    private static final int NOTICE_NEW_FRIEND_ADDED = 13; //好友同意邀请，刷新人脉

    public static void showPushNotification(final Context context, CPushMessage message) {
        msp = context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        mEditor = msp.edit();
        String customContent;
        String customTitle = message.getTitle();
        // 获取自定义key-value
        final String jsonContent = message.getContent();
        RenheApplication application = RenheApplication.getInstance();
        if (com.itcalf.renhe.Constants.DEBUG_MODE) {
            Log.w("showXinGNotification", "get custom title:" + customTitle);
            Log.w("showXinGNotification", "get custom content:" + jsonContent);
        }
        if (jsonContent != null && jsonContent.length() != 0) {
            Intent newIntent;
            if (application.getLogin() != 1) {
                newIntent = new Intent(context, MyPortal.class);
            } else {
                newIntent = new Intent(context, TabMainFragmentActivity.class);
            }
            try {
                JSONObject jsonObject = new JSONObject(jsonContent);
                customContent = jsonObject.getString("content");
                int type = jsonObject.getInt("bizType");
                String extension = null;
                JSONObject extensionJsonObject = null;
                try {
                    extension = jsonObject.getString("extension");
                    extensionJsonObject = new JSONObject(extension);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (type == MESSAGENUM_PUSH || type == NOTICE_MESSAGENUM_PUSH) {
                    int messageNum = 0;
                    try {
                        if (null != extensionJsonObject)
                            messageNum = extensionJsonObject.getInt("count");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent2 = new Intent(Constants.BroadCastAction.ICON_ACTION);
                    intent2.putExtra("notice_num", messageNum);
                    context.sendBroadcast(intent2);
                    mEditor.putInt("unreadmsg_num", messageNum);
                    mEditor.commit();
                    /**********************add by 12.10************************/
                    String senderUserface = null;//需要修改
                    try {
                        if (null != extensionJsonObject)
                            senderUserface = extensionJsonObject.getString("notifyImg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RenheApplication.getInstance().getUserEditor().putString(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_USERFACE, senderUserface);
                    RenheApplication.getInstance().getUserEditor().putInt(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_COUNT, messageNum);
                    RenheApplication.getInstance().getUserEditor().commit();
                    //发送通知
                    Intent unReadintent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_UNREAD_NOTICE);
                    context.sendBroadcast(unReadintent);

                    Intent intent4 = new Intent(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
                    intent4.putExtra(TabMainFragmentActivity.TAB_FLAG, 3);
                    intent4.putExtra(TabMainFragmentActivity.TAB_ICON_RENMAIQUAN_UNREAD_NUM, messageNum);
                    context.sendBroadcast(intent4);

                    int noticeType = 0;
                    try {
                        if (null != extensionJsonObject)
                            noticeType = extensionJsonObject.getInt("noticeType");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String objectId = "";
                    try {
                        if (null != extensionJsonObject)
                            objectId = extensionJsonObject.getString("objectId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("sid", RenheApplication.getInstance().getUserInfo().getSid());
                    bundle.putInt("type", noticeType);
                    bundle.putString("objectId", objectId);
                    bundle.putBoolean("isFromNoticeList", true);
                    bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
                    newIntent.putExtras(bundle);
                    //通知栏推送
                    if (msp.getBoolean("msgnotify", true))
                        showNotify(context, customTitle, customContent, type, null, newIntent);
                } else if (type == VIPCHECKPASS_PUSH) {
                    if (RenheApplication.getInstance().getmUserInfo() != null && msp.getBoolean("msgnotify", true)) {
                        showVipNotify(context, customContent, customTitle, type);
                    }
                } else if (type == NOTICE_SYSTEMMSG_PUSH || type == NOTICE_TOUTIAO_PUSH) {
                    if (msp.getBoolean("msgnotify", true)) {
                        if (type == NOTICE_SYSTEMMSG_PUSH) {
                            showNotify(context, Constants.ConversationStaticItem.CONVERSATION_ITEM_HELPER, customContent, type,
                                    BitmapFactory.decodeResource(context.getResources(), R.drawable.sysmsg), newIntent);
                        } else if (type == NOTICE_TOUTIAO_PUSH) {
                            showNotify(context, "行业头条", customContent, type,
                                    BitmapFactory.decodeResource(context.getResources(), R.drawable.toutiao), newIntent);
                        }
                    }
                    context.sendBroadcast(new Intent(Constants.BroadCastAction.LOAD_CONVERSATION_INFO_ACTION));
                } else if (type == NOTICE_NEW_FRIEND_ADDED || type == NOTICE_NEW_INNERMSG_PUSH) {
                    //刷新联系人数据
                    new ContactsUtil(context).SyncContacts();
                } else if (type == UNLOGIN_PUSH_MESSAGE) {
                    if (msp.getBoolean("msgnotify", true)) {
                        showVipNotify(context, customTitle, customContent, UNLOGIN_PUSH_MESSAGE);
                    }
                } else if (type == NOTICE_VCARDREC_PUSH) {


                } else if (type == NOTICE_REALNAMEAUTH_PUSH) {
                    Intent realnameIntent = new Intent(Constants.BroadCastAction.ACTION_NAMEAUTHRES);
                    try {
                        if (null != extensionJsonObject)
                            realnameIntent.putExtra("realNameRes", extensionJsonObject.optBoolean("passState", false) ? 1 : -1);
                        else
                            realnameIntent.putExtra("realNameRes", -1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.sendBroadcast(realnameIntent);
                    if (msp.getBoolean("msgnotify", true)) {
                        showRealNameAuthNotify(context, customTitle, customContent);
                    }
                } else if (type == NOTICE_NEW_FRIEND_PUSH) {
                    if (msp.getBoolean("msgnotify", true)) {
                        showNotify(context, "新的好友", customContent, type,
                                BitmapFactory.decodeResource(context.getResources(), R.drawable.newfriend), newIntent);
                    }
                    // 通讯录tab数字角标、通讯录列表"新的好友"角标的显示
                    Intent intent = new Intent(Constants.BroadCastAction.NEW_FRIEND_ACTION);
                    context.sendBroadcast(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    static void showNotify(Context context, String title, String content, int type, Bitmap imageBitmap, Intent newIntent) {
        int nId = Integer.MAX_VALUE - 10;
        if (type == NOTICE_NEW_FRIEND_PUSH) {
            nId = Integer.MAX_VALUE - 10;
        } else if (type == NOTICE_SYSTEMMSG_PUSH) {
            nId = Integer.MAX_VALUE - 9;
        } else if (type == NOTICE_NEW_INNERMSG_PUSH) {
            nId = Integer.MAX_VALUE - 8;
        } else if (type == NOTICE_TOUTIAO_PUSH) {
            nId = Integer.MAX_VALUE - 7;
        } else if (type == MESSAGENUM_PUSH) {
            nId = Integer.MAX_VALUE - 2;
        }
        RenheApplication application = RenheApplication.getInstance();
        if (null == newIntent) {
            if (application.getLogin() != 1) {
                newIntent = new Intent(context, MyPortal.class);
            } else {
                newIntent = new Intent(context, TabMainFragmentActivity.class);
            }
        }
        newIntent.putExtra(Constants.XING_NOTIFY_ACTION, true);
        newIntent.putExtra(Constants.XING_NOTIFY_TYPE, type);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentReceiver = PendingIntent.getActivity(context, nId, newIntent, PendingIntent.FLAG_ONE_SHOT);


        if (Build.VERSION.SDK_INT < 21) {
            Notification.Builder mNotificationBuilder = new Notification.Builder(application).setTicker(content).setContentTitle(title)
                    .setContentText(content).setContentIntent(contentReceiver).setSmallIcon(R.drawable.logo_48x48)
                    .setWhen(System.currentTimeMillis());
            if (imageBitmap == null) {
                mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_96x96));
            } else {
                mNotificationBuilder.setLargeIcon(imageBitmap);
            }

            if (null != mNotificationBuilder) {
                Notification n = mNotificationBuilder.build();
                n.flags |= Notification.FLAG_AUTO_CANCEL;
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("sound", true)) {
                    n.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bibi);
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("led", true)) {
                    n.defaults |= Notification.DEFAULT_LIGHTS;
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("warnshake", true)) {
                    n.defaults |= Notification.DEFAULT_VIBRATE;
                }
                //			n.setLatestEventInfo(context, title, content, contentReceiver);
                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(nId, n);// 通知一下才会生效
            }
        } else {
            NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(application);
            mNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.logo_48x48)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(contentReceiver)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true);
            if (imageBitmap == null) {
                mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_96x96));
            } else {
                mNotificationBuilder.setLargeIcon(imageBitmap);
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("sound", true)) {
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bibi));
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("led", true)) {
                mNotificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("warnshake", true)) {
                mNotificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
            mNotificationManager.notify(nId, mNotificationBuilder.build());
        }


    }

    static void showVipNotify(Context context, String title, String content, int type) {
        RenheApplication application = RenheApplication.getInstance();
        Notification.Builder mNotificationBuilder = null;
        if (type == VIPCHECKPASS_PUSH || type == UNLOGIN_PUSH_MESSAGE) {//3——会员通过审核通过推送
            Intent i;
            if (application.getLogin() != 1) {
                i = new Intent(context, MyPortal.class);
            } else {
                //				newIntent = new Intent(context, MainFragment.class);
                i = new Intent(context, TabMainFragmentActivity.class);
            }
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentReceiver = PendingIntent.getActivity(context, 0, i, 0);
            mNotificationBuilder = new Notification.Builder(context).setTicker(title).setContentTitle(title)
                    .setContentText(content).setContentIntent(contentReceiver)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                    .setSmallIcon(R.drawable.logo_48x48).setWhen(System.currentTimeMillis());
            if (null != mNotificationBuilder) {
                Notification n = mNotificationBuilder.getNotification();
                n.flags |= Notification.FLAG_AUTO_CANCEL;
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("sound", true)) {
                    n.defaults |= Notification.DEFAULT_SOUND;
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("led", true)) {
                    n.defaults |= Notification.DEFAULT_LIGHTS;
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("warnshake", true)) {
                    n.defaults |= Notification.DEFAULT_VIBRATE;
                }
                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1000, n);// 通知一下才会生效
            }
        }
    }

    static void showRealNameAuthNotify(Context context, String title, String content) {
        Notification.Builder mNotificationBuilder = null;
        int nId = Integer.MAX_VALUE - 5;
        RenheApplication application = RenheApplication.getInstance();
        Intent newIntent;
        if (application.getLogin() != 1) {
            newIntent = new Intent(context, MyPortal.class);
        } else {
            newIntent = new Intent(context, TabMainFragmentActivity.class);
        }
        newIntent.putExtra("fromMyJpushNotify_toMy", true);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentReceiver = PendingIntent.getActivity(context, nId, newIntent, PendingIntent.FLAG_ONE_SHOT);

        mNotificationBuilder = new Notification.Builder(application).setTicker(content).setContentTitle(title)
                .setContentText(content).setContentIntent(contentReceiver).setSmallIcon(R.drawable.logo_48x48)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_96x96))
                .setWhen(System.currentTimeMillis());

        if (null != mNotificationBuilder) {
            Notification n = mNotificationBuilder.getNotification();
            n.flags |= Notification.FLAG_AUTO_CANCEL;
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("sound", true)) {
                //					n.defaults |= Notification.DEFAULT_SOUND;
                n.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bibi);
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("led", true)) {
                n.defaults |= Notification.DEFAULT_LIGHTS;
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("warnshake", true)) {
                n.defaults |= Notification.DEFAULT_VIBRATE;
            }

            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(nId, n);// 通知一下才会生效
        }
    }

    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        private Context context;
        private String imageUrl;
        private String content;
        private String title;

        public AnimateFirstDisplayListener(Context context, String content, String title, String url) {
            this.context = context;
            this.imageUrl = url;
            this.content = content;
            this.title = title;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            //			Bitmap userImage = getUserPic(context, imageUrl);
            if (msp.getBoolean("msgnotify", true)) {
                showNotify(context, content, title, INNERMSG_PUSH, loadedImage, null);
            }
        }
    }

    private Bitmap getUserPic(Context context, String userFaceUrl) {
        //		String fileName = getWebPath(this.userFaceUrl);
        ImageLoader imageLoader = ImageLoader.getInstance();
        String fileName = null; //使用Universal ImageLoader后的缓存目录
        if (null != userFaceUrl) {
            fileName = imageLoader.getDiscCache().get(userFaceUrl).getPath();
        }
        if (null != fileName && null != CacheManager.getExternalCacheDir(context)) {
            //			File file = new File(ExternalStorageUtil.getCacheAvatarPath(this, getRenheApplication().getUserInfo().getEmail())
            //					+ fileName);
            File file = new File(fileName);
            if (null != file && file.isFile()) {
                //				Bitmap mbitmap = BitmapFactory.decodeFile(ExternalStorageUtil.getCacheAvatarPath(this, getRenheApplication()
                //						.getUserInfo().getEmail())
                //						+ fileName);
                Bitmap mbitmap = BitmapFactory.decodeFile(fileName);
                return mbitmap;
            } else {
                //放人和网logo
                Bitmap mlogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
                return mlogo;
            }
            //目前分享好友也是放人和网应用图标，如果后期想改回好友头像，将上面代码解除注释
            //			Bitmap mlogo1 = BitmapFactory.decodeResource(getResources(), R.drawable.icon_134);
            //			return mlogo1;
        } else {
            //放人和网logo
            Bitmap mlogo1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
            return mlogo1;
        }
    }
}
