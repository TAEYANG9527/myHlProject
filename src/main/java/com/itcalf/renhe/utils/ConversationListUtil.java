package com.itcalf.renhe.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.auth.AuthInfo;
import com.alibaba.wukong.auth.AuthParam;
import com.alibaba.wukong.auth.AuthService;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.User;
import com.alibaba.wukong.im.UserService;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.actionprovider.PlusActionProvider;
import com.itcalf.renhe.context.common.LoginIMErrorActivity;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.relationship.AdvancedSearchIndexActivityTwo;
import com.itcalf.renhe.context.wukong.im.ActivitySearchCircle;
import com.itcalf.renhe.context.wukong.im.LoadConversationInfoTask;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.context.wukong.im.ValidateImMemberTask;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.dto.LoadConversationInfo;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.view.TipBox;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by wangning on 2016/1/8.
 */
public class ConversationListUtil {
    private static Context mContext;
    private static final int RETRY_LOGIN_IM_COUNT = 10;//登录IM重试最高次数
    private int retryLoginIm = 0;
    private ConversationCallBack conversationCallBack;
    private UserInfo userInfo;
    private static SharedPreferences sp;
    private SharedPreferences blackListSp;
    private ArrayList<ConversationItem> datas;
    private static SharedPreferences userGuideSp;
    private static TipBox tipBox;

    public ConversationListUtil(Context mContext, ArrayList<ConversationItem> datas) {
        this.mContext = mContext;
        this.userInfo = RenheApplication.getInstance().getUserInfo();
        this.sp = RenheApplication.getInstance().getUserSharedPreferences();
        this.blackListSp = mContext.getSharedPreferences(Constants.BLOCKED_CONTACTS_SHAREDPREFERENCES, 0);
        this.datas = datas;
    }

    public ConversationCallBack getConversationCallBack() {
        return conversationCallBack;
    }

    public void setConversationCallBack(ConversationCallBack conversationCallBack) {
        this.conversationCallBack = conversationCallBack;
    }

    /**
     * 登陆IM
     *
     * @author wangning
     */
    public void signIn(final AuthParam param) {
        if (!IMEngine.getIMService(AuthService.class).isLogin()) {
            retryLoginIm++;
            IMEngine.getIMService(AuthService.class).login(param, new Callback<AuthInfo>() {
                @Override
                public void onSuccess(AuthInfo authInfo) {
                    RenheApplication.getInstance().currentOpenId = authInfo.getOpenId();
                    RenheApplication.getInstance().currentNickName = authInfo.getNickname();
                    getConversationList();
                }

                @Override
                public void onException(String code, String reason) {
                    //失败，重试，最多重试10次
                    if (retryLoginIm <= RETRY_LOGIN_IM_COUNT) {
                        signIn(param);
                    } else {
                        loginIMError(R.string.account_exception);
//                        ToastUtil.showToast(mContext, R.string.login_im_fail_tip);
                        Logger.w("登录失败，请重新登录" + code + " " + reason);
                    }
                }

                @Override
                public void onProgress(AuthInfo authInfo, int i) {
                }
            });
        } else {
            getConversationList();
        }
    }

    /**
     * 容错机制，服务端注册IM未成功的时候调用该方法，由客户端再主动发起注册
     */
    public void clientRegisterIM() {
        new ValidateImMemberTask(mContext) {
            public void doPre() {
            }

            public void doPost(final com.itcalf.renhe.dto.RegisterImMemberOperation result) {
                if (result != null && result.getState() == 1) {
                    signIn(RenheIMUtil.buildAuthParam(Long.parseLong(result.getImId() + ""),
                            userInfo.getName(), result.getImPwd()));
                    //将用户的imOpenId、ImValid写入到用户表
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UserInfoUtil.chengeUserImValidInfo(true);
                            UserInfoUtil.chengeUserImOpenIdInfo(result.getImId());
                        }
                    }).start();
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId());
    }

    public static void loginIMError(int errorRes) {
        if (RenheApplication.getInstance().getLogin() == 0) {
            new LogoutUtil(mContext, null).closeLogin(true);
        }
        if (RenheApplication.getInstance().getLogin() != 0) {
            Intent intent2 = new Intent(mContext, LoginIMErrorActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent2.putExtra("tiker", mContext.getString(errorRes));
            mContext.startActivity(intent2);
        }
        StatisticsUtil.statisticsCustomClickEvent(mContext.getString(R.string.android_create_conversation_fail), 0, "", null);
    }

    /**
     * 获取会话列表
     */
    public void getConversationList() {
        IMEngine.getIMService(ConversationService.class).listConversations(new Callback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations != null && !conversations.isEmpty()) {
                    ArrayList<ConversationItem> conversationItems = new ArrayList<>();
                    for (Conversation conversation : conversations) {
                        if (conversation.status() == Conversation.ConversationStatus.NORMAL) {
                            ConversationItem conversationItem = new ConversationItem();
                            conversationItem.setType(ConversationItem.IM_CONVERSATION_TYPE);
                            if (conversation.type() == Conversation.ConversationType.CHAT) {
                                Map<String, String> chatConverInfoMap = conversation.extension();
                                if (null != chatConverInfoMap) {
                                    String name = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME);
                                    String userface = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE);
                                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(userface)) {
                                        conversationItem.setNickname(name);
                                        conversationItem.setIconUrl(userface);
                                    } else {
                                        getUserInfoById(conversation, conversation.getPeerId());
                                    }
                                } else {
                                    getUserInfoById(conversation, conversation.getPeerId());
                                }
                            } else {
                                conversationItem.setNickname(conversation.title());
                                conversationItem.setIconUrl(conversation.icon());
                            }
                            conversationItem.setConversation(conversation);
                            conversationItems.add(conversationItem);

                            if (sp.getBoolean(conversationItem.getConversation().conversationId() + "msg_void_bother", false)) {
                                continue;
                            }
                        }
                    }

                    conversationCallBack.loadConversationListSuccess(conversationItems);
                    deleteBlackContact(null);
                } else {
                    conversationCallBack.loadConversationListFail();
                }
            }

            @Override
            public void onException(String code, String reason) {
                ToastUtil.showToast(mContext, R.string.load_conversation_list_fail_tip);
                Logger.w("加载会话列表失败.code:" + code + " reason:" + reason);
                conversationCallBack.loadConversationListFail();
            }

            @Override
            public void onProgress(List<Conversation> conversations, int i) {
            }
        }, Integer.MAX_VALUE, Conversation.ConversationType.CHAT | Conversation.ConversationType.GROUP);
    }

    /**
     * 初始化其他的对话（IM之外的行业头条、和聊助手）
     */
    public void initOtherConversationItems() {
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        /**
         * 和聊助手
         */
        ConversationItem heliaoHelperItem = new ConversationItem();
        heliaoHelperItem.setIconUrl("drawable://" + R.drawable.sysmsg);
        heliaoHelperItem.setNickname(mContext.getString(R.string.conversation_heliao_helper));
        heliaoHelperItem.setType(ConversationItem.SYSTEM_MSG_TYPE);
        ConversationItem.ConversationListOtherItem conversationListOtherHelperItem = new ConversationItem.ConversationListOtherItem();
        conversationListOtherHelperItem.setCreateTime(SharedPreferencesUtil.getLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_CREATE_TIME, 0, true));
        conversationListOtherHelperItem.setLastMessage(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_LAST_MSG, null, true));
        conversationListOtherHelperItem.setUnreadCount(SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_UNREAD_COUNT, 0, true));
        heliaoHelperItem.setConversationListOtherItem(conversationListOtherHelperItem);
        /**
         * 行业头条
         */
        ConversationItem toutiaoItem = new ConversationItem();
        toutiaoItem.setIconUrl("drawable://" + R.drawable.toutiao);
        toutiaoItem.setNickname(mContext.getString(R.string.conversation_toutiao));
        toutiaoItem.setType(ConversationItem.TOU_TIAO_TYPE);
        ConversationItem.ConversationListOtherItem conversationListOtherToutiaoItem = new ConversationItem.ConversationListOtherItem();
        conversationListOtherToutiaoItem.setCreateTime(SharedPreferencesUtil.getLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_CREATE_TIME, 0, true));
        conversationListOtherToutiaoItem.setLastMessage(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_LAST_MSG, null, true));
        conversationListOtherToutiaoItem.setUnreadCount(SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_UNREAD_COUNT, 0, true));
        toutiaoItem.setConversationListOtherItem(conversationListOtherToutiaoItem);

        if (!TextUtils.isEmpty(conversationListOtherHelperItem.getLastMessage()) || conversationListOtherHelperItem.getUnreadCount() > 0) {
            conversationItems.add(heliaoHelperItem);
        }
        if (!TextUtils.isEmpty(conversationListOtherToutiaoItem.getLastMessage()) || conversationListOtherToutiaoItem.getUnreadCount() > 0) {
            conversationItems.add(toutiaoItem);
        }
        conversationCallBack.loadConversationListSuccess(conversationItems);
    }

    /**
     * 容错机制，为了兼容老版本，单聊的conversation里没有对方姓名、头像的扩展字段时，根据他的opeid去获取头像姓名
     *
     * @param openId
     */
    public void getUserInfoById(final Conversation conversation, long openId) {
        IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {

            @Override
            public void onException(String arg0, String arg1) {

            }

            @Override
            public void onProgress(User arg0, int arg1) {

            }

            @Override
            public void onSuccess(User arg0) {
                String senderName = arg0.nickname();
                String userFace = arg0.avatar();
                updateChatConversationExtension(conversation, senderName, userFace);
            }

        }, openId);
    }

    /**
     * 针对单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
     *
     * @param conversation
     * @param otherName
     * @param otherFace
     */
    public static void updateChatConversationExtension(Conversation conversation, String otherName, String otherFace) {
        if (conversation.type() == Conversation.ConversationType.CHAT) {
            Map<String, String> chatConversationExtension = conversation.extension();
            if (null == chatConversationExtension)
                chatConversationExtension = new HashMap<>();
            chatConversationExtension.put(conversation.getPeerId() +
                    Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME, otherName);
            chatConversationExtension.put(conversation.getPeerId() +
                    Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE, otherFace);
            chatConversationExtension.put(RenheApplication.getInstance().currentOpenId +
                    Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME, RenheApplication.getInstance().getUserInfo().getName());
            chatConversationExtension.put(RenheApplication.getInstance().currentOpenId +
                    Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE, RenheApplication.getInstance().getUserInfo().getUserface());
            conversation.updateExtension(chatConversationExtension);
        }
    }

    /**
     * 针对群聊message，给message添加扩展字段，存放message发送者的头像和姓名，方便列表页获取
     *
     * @param message
     * @param senderName
     * @param senderFace
     */
    public static void updateGroupMessageExtension(Message message, String senderName, String senderFace) {
//        if (message.conversation().type() != Conversation.ConversationType.GROUP)
//            return;
//        Map<String, String> groupMessageExtension = new HashMap<>();
//        groupMessageExtension.put(Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME + ";"
//                + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE, senderName + ";" + senderFace);
//        message.updateExtension(groupMessageExtension);
    }

    /**
     * 删除被拉黑的好友对话
     */
    public void deleteBlackContact(String openId) {
        if (null != datas) {
            for (ConversationItem conversationItem : datas) {
                Conversation conversation = conversationItem.getConversation();
                if (conversation != null && conversation.type() == Conversation.ConversationType.CHAT) {
                    if (!TextUtils.isEmpty(openId)) {
                        String conversationOpenId = conversation.getPeerId() + "";
                        if (conversationOpenId.equals(openId)) {
                            conversation.remove();
                            break;
                        }
                    } else {
                        if (blackListSp.getBoolean(conversation.getPeerId() + "", false)) {
                            conversation.remove();
                        }
                    }
                }
            }
        }

    }

    /**
     * 计算列表所有item的未读数之和
     */
    public void calculateUnreadCount() {
        int totalCount = 0;
        for (int i = 0; i < datas.size(); i++) {
            ConversationItem conversationItem = datas.get(i);
            if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                if (sp.getBoolean(conversationItem.getConversation().conversationId() + "msg_void_bother", false)) {
                    continue;
                }
                totalCount += conversationItem.getConversation().unreadMessageCount();
            } else {
                totalCount += conversationItem.getConversationListOtherItem().getUnreadCount();
            }
        }
        Intent tabIntent = new Intent(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
        tabIntent.putExtra(TabMainFragmentActivity.TAB_FLAG, 1);
        tabIntent.putExtra(TabMainFragmentActivity.TAB_ICON_CONVERSATION_UNREAD_NUM, totalCount);
        mContext.sendBroadcast(tabIntent);
    }

    /**
     * 处理点击行业头条
     *
     * @param conversationItem
     */
    public void handleTouTiaoAction(ConversationItem conversationItem) {
        ConversationItem.ConversationListOtherItem toutiaoItem = conversationItem.getConversationListOtherItem();
        toutiaoItem.setUnreadCount(0);
        SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_UNREAD_COUNT, 0, true);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Integer.MAX_VALUE - 7);
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        conversationItems.add(conversationItem);
        conversationCallBack.onConversationsUpdated(conversationItems);
    }

    /**
     * 处理点击和聊助手
     *
     * @param conversationItem
     */
    public void handleHeLiaoHelperAction(ConversationItem conversationItem) {
        ConversationItem.ConversationListOtherItem helperItem = conversationItem.getConversationListOtherItem();
        helperItem.setUnreadCount(0);
        SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_UNREAD_COUNT, 0, true);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Integer.MAX_VALUE - 9);
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        conversationItems.add(conversationItem);
        conversationCallBack.onConversationsUpdated(conversationItems);
    }

    /**
     * 长按会话列表 弹出对话框
     *
     * @param conversationItem
     */
    public void createDeleteDialog(final ConversationItem conversationItem) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(mContext);
        materialDialog.showSelectList(R.array.conversation_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                            conversationItem.getConversation().remove();
                        }
//                        else {
//                            ArrayList<ConversationItem> conversationItems = new ArrayList<>();
//                            conversationItems.add(conversationItem);
//                            conversationCallBack.onConversationsRemoved(conversationItems);
//                        }
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }

            }
        });
        materialDialog.show();
    }

    /**
     * 从服务器获取最新的行业头条、和聊助手消息
     */
    public void loadOtherConversationInfo() {
        long lastLoadTime = SharedPreferencesUtil.getLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_LASTLOAD_TIME, 0, true);
        new LoadConversationInfoTask(mContext) {
            public void doPre() {
            }

            public void doPost(com.itcalf.renhe.dto.LoadConversationInfo result) {
                if (null != result && result.getState() == 1) {
                    SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_LASTLOAD_TIME,
                            result.getLastLoadTime(), true);
                    ArrayList<ConversationItem> conversationItems = new ArrayList<>();
                    LoadConversationInfo.ConversationInfo conversationInfo = result.getConversationInfo();
                    if (conversationInfo.getSystemMessageUnReadCount() > 0 || !TextUtils.isEmpty(conversationInfo.getSystemMessageContent())) {
                        ConversationItem helperItem = getConversationItem(ConversationItem.SYSTEM_MSG_TYPE);
                        ConversationItem.ConversationListOtherItem sysMsgItem;
                        if (null != helperItem) {
                            sysMsgItem = helperItem.getConversationListOtherItem();
                            if (null == sysMsgItem) {
                                sysMsgItem = new ConversationItem.ConversationListOtherItem();
                            }
                        } else {
                            helperItem = new ConversationItem();
                            sysMsgItem = new ConversationItem.ConversationListOtherItem();
                        }
                        helperItem.setIconUrl("drawable://" + R.drawable.sysmsg);
                        helperItem.setNickname(mContext.getString(R.string.conversation_heliao_helper));
                        helperItem.setType(ConversationItem.SYSTEM_MSG_TYPE);
                        sysMsgItem.setUnreadCount(sysMsgItem.getUnreadCount() + conversationInfo.getSystemMessageUnReadCount());
                        sysMsgItem.setLastMessage(conversationInfo.getSystemMessageContent());
                        sysMsgItem.setCreateTime(conversationInfo.getSystemMessageUpdatedDate());
                        helperItem.setConversationListOtherItem(sysMsgItem);

                        SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_UNREAD_COUNT,
                                sysMsgItem.getUnreadCount(), true);
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_LAST_MSG,
                                conversationInfo.getSystemMessageContent(), true);
                        SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_HELIAO_HELPER_CREATE_TIME,
                                conversationInfo.getSystemMessageUpdatedDate(), true);
                        conversationItems.add(helperItem);
                    }
                    if (conversationInfo.getToutiaoUnReadCount() > 0 || !TextUtils.isEmpty(conversationInfo.getToutiaoContent())) {
                        ConversationItem toutiaoItem = getConversationItem(ConversationItem.TOU_TIAO_TYPE);
                        ConversationItem.ConversationListOtherItem toutiaoOtherItem;
                        if (null != toutiaoItem) {
                            toutiaoOtherItem = toutiaoItem.getConversationListOtherItem();
                            if (null == toutiaoOtherItem) {
                                toutiaoOtherItem = new ConversationItem.ConversationListOtherItem();
                            }
                        } else {
                            toutiaoItem = new ConversationItem();
                            toutiaoOtherItem = new ConversationItem.ConversationListOtherItem();
                        }
                        toutiaoItem.setIconUrl("drawable://" + R.drawable.toutiao);
                        toutiaoItem.setNickname(mContext.getString(R.string.conversation_toutiao));
                        toutiaoItem.setType(ConversationItem.TOU_TIAO_TYPE);
                        toutiaoOtherItem.setUnreadCount(toutiaoOtherItem.getUnreadCount() + conversationInfo.getToutiaoUnReadCount());
                        toutiaoOtherItem.setLastMessage(conversationInfo.getToutiaoContent());
                        toutiaoOtherItem.setCreateTime(conversationInfo.getToutiaoUpdatedDate());
                        toutiaoItem.setConversationListOtherItem(toutiaoOtherItem);

                        SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_UNREAD_COUNT,
                                toutiaoOtherItem.getUnreadCount(), true);
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_LAST_MSG,
                                conversationInfo.getToutiaoContent(), true);
                        SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.IM_CONVERSATION_TOUTIAO_CREATE_TIME,
                                conversationInfo.getToutiaoUpdatedDate(), true);
                        conversationItems.add(toutiaoItem);
                    }
                    if (!conversationItems.isEmpty())
                        conversationCallBack.onConversationsUpdated(conversationItems);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), lastLoadTime + "");
    }

    /**
     * 根据type获取相应的item（和聊助手、行业头条）
     *
     * @param type
     */
    private ConversationItem getConversationItem(int type) {
        for (ConversationItem conversationItem : datas) {
            if (conversationItem.getType() == type) {
                return conversationItem;
            }
        }
        return null;
    }

    /**
     * 检查一个conversation是否处于消息免打扰的状态
     *
     * @param conversationId
     * @return
     */
    public static boolean checkIfConversationUnBother(String conversationId) {
        if (sp.getBoolean(conversationId + "msg_void_bother", false)) {
            return true;
        }
        return false;
    }

    public static PopupWindow createMenuPopupWindow(final Activity mActivity, final PopupWindow pop, ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (RenheApplication.getInstance().getUserInfo() != null
                                && RenheApplication.getInstance().getUserInfo().getImId() > 0) {
                            mActivity.startActivity(new Intent(mContext, ActivitySearchCircle.class));
                            mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            MobclickAgent.onEvent(mContext, "join_circle");
                        } else {
                            ToastUtil.showToast(mContext, "即时通讯创建失败,请退出重新登录");
                        }
                        StatisticsUtil.statisticsCustomClickEvent(mActivity.getString(R.string.android_btn_menu2_join_circle_click), 0, "", null);
                        break;
                    case 1:
//                        RenheIMUtil.showProgressDialog(mContext, R.string.loading);
                        if (NetworkUtil.hasNetworkConnection(mContext) != -1) {
                            PlusActionProvider.checkCircleCreationPrivilege(mActivity);//检查是否有权限创建圈子
                        } else
                            ToastUtil.showToast(mContext, "网络异常");
//                        RenheIMUtil.dismissProgressDialog();//如果网络异常加载提示消失
                        MobclickAgent.onEvent(mContext, "create_circle");
                        StatisticsUtil.statisticsCustomClickEvent(mActivity.getString(R.string.android_btn_menu2_add_circle_click), 0, "", null);
                        break;
                    case 2:
                        MobclickAgent.onEvent(mContext, "add_new_friend");
                        mContext.startActivity(new Intent(mContext, AdvancedSearchIndexActivityTwo.class));
                        ((Activity) mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }

    /**
     * 刚注册进来的用户，有个popWindows引导框
     */
    public static void handleGuideNewFisher(final Context context) {
        userGuideSp = context.getSharedPreferences("regiser_guide_setting_info", 0);
        boolean isRegister = userGuideSp.getBoolean("regiser_conversation" + RenheApplication.getInstance().getUserInfo().getSid(), false);
        Handler mHandler = new Handler();
        if (isRegister) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tipBox = new TipBox(context, 1, new TipBox.OnItemClickListener() {
                        @Override
                        public void onItemClick() {
                            SharedPreferences.Editor editor2 = userGuideSp.edit();
                            editor2.putBoolean("regiser_conversation" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                            editor2.commit();
                        }
                    });
                    if (null != context && context instanceof AppCompatActivity) {
                        tipBox.showAtLocation(tipBox.getContentView(), Gravity.RIGHT | Gravity.TOP, 0,
                                ((AppCompatActivity) context).getSupportActionBar().getHeight() + RenmaiQuanUtils.getStatusBarHeight(context) - 15);
                    }
                }
            }, 50);
        }


    }

    /**
     * 刚注册进来的用户，有个popWindows引导框
     */
    public static void handelGuideNewFisherWindow(Context context, boolean isVisibleToUser) {
        if (null == userGuideSp) {
            if (null != context) {
                userGuideSp = context.getSharedPreferences("regiser_guide_setting_info", 0);
            }
        }
        if (null != userGuideSp) {
            if (isVisibleToUser) {
                if (tipBox == null) {
                    tipBox = new TipBox(context, 1, new TipBox.OnItemClickListener() {
                        @Override
                        public void onItemClick() {
                            SharedPreferences.Editor editor2 = userGuideSp.edit();
                            editor2.putBoolean("regiser_conversation" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                            editor2.commit();
                        }
                    });
                }
                boolean isRegister = userGuideSp.getBoolean("regiser_conversation" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                if (isRegister) {
                    tipBox.showAtLocation(tipBox.getContentView(), Gravity.RIGHT | Gravity.TOP, 0,
                            ((AppCompatActivity) context).getSupportActionBar().getHeight() + RenmaiQuanUtils.getStatusBarHeight(context) - 15);
                }
            } else {
                if (tipBox != null && tipBox.isShowing()) {
                    tipBox.dismiss();
                }
            }
        }
    }

    /**
     * 刚注册进来的用户，有个popWindows引导框
     */
    public static void resetGuideNewFisherWindow() {
        if (tipBox != null) {
            tipBox.dismiss();
            tipBox = null;
        }
    }

    public interface ConversationCallBack {
        void onLoginSuccess();

        void loadConversationListSuccess(ArrayList<ConversationItem> conversationItems);

        void loadConversationListFail();

        void onConversationsAdd(ArrayList<ConversationItem> conversationItems);

        void onConversationsRemoved(ArrayList<ConversationItem> conversationItems);

        void onConversationsUpdated(ArrayList<ConversationItem> conversationItems);
    }
}
