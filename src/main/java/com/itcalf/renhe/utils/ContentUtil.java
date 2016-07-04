package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.room.WebViewActivityForReport;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.task.AddBlackListTask;
import com.itcalf.renhe.task.AsyncTaskCallBack;
import com.itcalf.renhe.task.RemoveBlackListTask;
import com.umeng.analytics.MobclickAgent;

import java.util.Collection;
import java.util.concurrent.Executors;

import cn.renhe.heliao.idl.collection.MyCollection;

/**
 * 复制文本
 * Title: ContentUtil.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-3 上午10:32:21 <br>
 *
 * @author wangning
 */
public class ContentUtil implements Callback {
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 文本复制功能
     *
     * @param content
     * @param context
     */
    public static void copy(String content, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public void createCopyDialog(final Context context, final String content) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(R.array.im_choice_item1).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        ContentUtil.copy(content, context);
                        ContentUtil.showToast(context, "内容已经复制到剪贴板");
                        break;
                    default:
                        break;
                }

            }
        });
        materialDialog.show();
    }

    /**
     * 人脉圈长按文字、图片弹出对话框，复制、收藏、举报
     *
     * @param context
     * @param content
     * @param type    长按的类型 1：长按文字，有复制、收藏、举报   2：长按图片 有收藏、举报 3: 收藏、举报、加入黑名单 4:有收藏、举报(该类型针对长按“人脉圈转发他人的人脉圈的转发区域”的情况，要收藏的话是收藏转发的那条人脉圈)
     */
    public void createRenMaiQuanDialog(final Context context, final int type, final String content,
                                       final MessageBoards.NewNoticeList newNoticeList) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        if (type == 1) {
            materialDialog.showSelectList(R.array.rmq_choice_item1).itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    switch (which) {
                        case 0:
                            ContentUtil.copy(content, context);
                            ContentUtil.showToast(context, "内容已经复制到剪贴板");
                            break;
                        case 1:
                            addCollectRenMaiQuan(newNoticeList.getContentInfo().getObjectId(), newNoticeList.getSenderInfo().getSid(),
                                    newNoticeList.getType());
                            break;
                        case 2:
                            if (null != newNoticeList)
                                reportRenMaiQuan(context, newNoticeList.getSenderInfo().getSid(),
                                        newNoticeList.getContentInfo().getId() + "", newNoticeList.getContentInfo().getObjectId());
                            break;
                        default:
                            break;
                    }

                }
            });
        } else if (type == 2 || type == 4) {
            materialDialog.showSelectList(R.array.rmq_choice_item2).itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    switch (which) {
                        case 0:
                            String objectId = "";
                            if (type == 2) {
                                objectId = newNoticeList.getContentInfo().getObjectId();
                            } else {
                                if (null != newNoticeList.getContentInfo().getForwardMessageBoardInfo())
                                    objectId = newNoticeList.getContentInfo().getForwardMessageBoardInfo().getObjectId();
                            }
                            addCollectRenMaiQuan(objectId, newNoticeList.getSenderInfo().getSid(),
                                    newNoticeList.getType());
                            break;
                        case 1:
                            if (null != newNoticeList)
                                reportRenMaiQuan(context, newNoticeList.getSenderInfo().getSid(),
                                        newNoticeList.getContentInfo().getId() + "", newNoticeList.getContentInfo().getObjectId());
                            break;
                        default:
                            break;
                    }

                }
            });
        } else {
            materialDialog.showSelectList(R.array.rmq_choice_item3).itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    switch (which) {
                        case 0:
                            addCollectRenMaiQuan(newNoticeList.getContentInfo().getObjectId(), newNoticeList.getSenderInfo().getSid(),
                                    newNoticeList.getType());
                            break;
                        case 1:
                            if (null != newNoticeList)
                                reportRenMaiQuan(context, newNoticeList.getSenderInfo().getSid(),
                                        newNoticeList.getContentInfo().getId() + "", newNoticeList.getContentInfo().getObjectId());
                            break;
                        case 2:
                            if (null != newNoticeList)
                                addToBlackList(context, newNoticeList);
                            break;
                        default:
                            break;
                    }

                }
            });
        }
        materialDialog.show();
    }

    /**
     * 收藏人脉圈
     */
    private void addCollectRenMaiQuan(String objectId, String senderSid, int messageType) {
        int taskId = TaskManager.getTaskId();
        TaskManager.getInstance().addTask(this, taskId);
        GrpcController grpcController = new GrpcController();
        grpcController.addCollect(taskId, MyCollection.CollectResquest.CollectionType.RENMAIQUAN,
                objectId, senderSid, messageType);
    }

    /**
     * 举报人脉圈
     *
     * @param context
     * @param senderSid
     * @param msgId
     * @param msgObjectId
     */
    private void reportRenMaiQuan(Context context, String senderSid, String msgId, String msgObjectId) {
        MobclickAgent.onEvent(context, "renmai_report");
        Intent intent = new Intent(context, WebViewActivityForReport.class);
        intent.putExtra("sid", senderSid);
        intent.putExtra("type", 1);
        intent.putExtra("entityId", msgId);
        intent.putExtra("entityObjectId", msgObjectId);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void addToBlackList(final Context context, final MessageBoards.NewNoticeList newNoticeList) {
        AddBlackListTask addBlackListTask = new AddBlackListTask(context, new AsyncTaskCallBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(Object result) {
                if (null != result && result instanceof MessageBoardOperation) {
                    MessageBoardOperation messageBoardOperation = (MessageBoardOperation) result;
                    if (messageBoardOperation.getState() == 1) {
                        ToastUtil.showToast(context, "成功加入黑名单");
                        updateContactData(context, newNoticeList, true, 1);
                    } else {
                        ToastUtil.showToast(context, "发生未知错误");
                    }
                } else {
                    ToastUtil.showToast(context, "发生未知错误");
                }
            }
        });
        addBlackListTask.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), newNoticeList.getSenderInfo().getSid());
        Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_BOLOCK_ITEMS);
        intent.putExtra("senderSid", newNoticeList.getSenderInfo().getSid());
        context.sendBroadcast(intent);
        new RenmaiQuanUtils(context).deleteRenmaiQuanItemsBySid(newNoticeList.getSenderInfo().getSid());
    }

    private void removeFromBlackList(final Context context, final MessageBoards.NewNoticeList newNoticeList) {
        RemoveBlackListTask removeBlackListTask = new RemoveBlackListTask(context, new AsyncTaskCallBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(Object result) {
                if (null != result && result instanceof MessageBoardOperation) {
                    MessageBoardOperation messageBoardOperation = (MessageBoardOperation) result;
                    if (messageBoardOperation.getState() == 1) {
                        ToastUtil.showToast(context, "成功移除黑名单");
                        Intent intent = new Intent(Constants.BroadCastAction.REMOVE_BLACK_LIST);
                        intent.putExtra("sid", newNoticeList.getSenderInfo().getSid());
                        context.sendBroadcast(intent);
                        updateContactData(context, newNoticeList, false, 2);
                    } else {
                        ToastUtil.showToast(context, "发生未知错误");
                    }
                } else {
                    ToastUtil.showToast(context, "发生未知错误");
                }
            }
        });
        removeBlackListTask.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), newNoticeList.getSenderInfo().getSid());
    }

    public void updateContactData(final Context context, final MessageBoards.NewNoticeList newNoticeList, final boolean isBlocked, final int type) {
        new Thread() {
            @Override
            public void run() {
                //修改联系人信息
                IContactCommand contactCommand = RenheApplication.getInstance().getContactCommand();
                try {
                    contactCommand.updataContactBlock(newNoticeList.getSenderInfo().getSid(), isBlocked);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent brocastIntent = new Intent(Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION);
                context.sendBroadcast(brocastIntent);
                Contact contact = null;
                try {
                    contact = contactCommand.getMyContactBySid(RenheApplication.getInstance().getUserInfo().getSid(),
                            newNoticeList.getSenderInfo().getSid());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != contact) {
                    new ContactsUtil(context).updateContactBlockState(contact.getImId(), type,
                            newNoticeList.getSenderInfo().getName(), newNoticeList.getSenderInfo().getUserface());
                }
            }
        }.start();
    }

    /**
     * 对话列表中，表情查看
     *
     * @param span
     * @param teString 表情出现的句子
     * @param high     传入表情显示为原表情大小的比例（表情原比例大小/high）
     * @param width
     * @return
     */
    public static SpannableString getNoAtSpannedString(Context context, SpannableString span, String teString, int high, int width) {
        String[] zh = context.getResources().getStringArray(R.array.face_zh);
        String[] en = context.getResources().getStringArray(R.array.face_en);
        if (null == span) {
            span = new SpannableString(teString);
        }
//        for (int i = 0; i < zh.length; i++) {
//            if (count(teString, zh[i]) != null) {
//                int[] a = count(teString, zh[i]);
//                if (a != null && a.length > 0) {
//                    for (int f : a) {
//                        int id = context.getResources().getIdentifier(en[i], "drawable", context.getPackageName()); // name:图片的名，defType：资源类型（drawable，string。。。），defPackage:工程的包名
//                        Drawable drawable = context.getResources().getDrawable(id);
//                        BitmapDrawable bd = (BitmapDrawable) drawable;
//                        if (high > 0 && width > 0)
//                            bd.setBounds(0, 0, bd.getIntrinsicWidth() / high, bd.getIntrinsicHeight() / width);
//                        span.setSpan(new ImageSpan(bd), f, f + zh[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    }
//                }
//            }
//        }
        return span;
    }

    /**
     * 对话列表中，表情查看(默认为原表情大小除以2)
     *
     * @param span
     * @param teString 表情出现的句子
     * @return
     */
    public static SpannableString getNoAtSpannedString(Context context, SpannableString span, String teString) {
        return getNoAtSpannedString(context, span, teString, 2, 2);
    }

    protected static int[] count(String text, String sub) {
        int count = 0, start = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            start += sub.length();
            count++;
        }
        if (count == 0) {
            return null;
        }
        int a[] = new int[count];
        int count2 = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            a[count2] = start;
            start += sub.length();
            count2++;
        }
        return a;
    }

    @Override
    public void onSuccess(int type, Object result) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
        if (result instanceof MyCollection.CollectResponse) {
            ToastUtil.showToast(RenheApplication.getInstance(), R.string.collect_success);
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
    }

    @Override
    public void cacheData(int type, Object data) {

    }
}
