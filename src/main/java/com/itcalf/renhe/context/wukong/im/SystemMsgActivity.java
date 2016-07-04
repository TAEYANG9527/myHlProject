package com.itcalf.renhe.context.wukong.im;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Member;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.MyPortal;
import com.itcalf.renhe.context.archives.EditMyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.contacts.SearchForContactsActivity;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.luckymoney.MyWalletDetailActivity;
import com.itcalf.renhe.context.more.AccountLimitActivity;
import com.itcalf.renhe.context.more.InvitationCodeActivity;
import com.itcalf.renhe.context.relationship.NearbyActivity;
import com.itcalf.renhe.context.room.AddMessageBoardActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.dto.ConversationSystemMsgOperation;
import com.itcalf.renhe.dto.ConversationSystemMsgOperation.SystemMessage;
import com.itcalf.renhe.dto.IMInnerMsgChatListOperation;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.CheckUpdateUtil;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.KeyboardLayout;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.view.XListView;
import com.itcalf.renhe.view.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 和聊助手页面
 */
public class SystemMsgActivity extends BaseActivity implements IXListViewListener, OnClickListener {
    private XListView mListView;
    private MessageListAdapter mListAdapter;
    private static final String TAG = SystemMsgActivity.class.getSimpleName();
    private static long MINUTE = 60L * 1000L;

    private int page = 1;
    private List<SystemMessage> sysMessagesList = new ArrayList<SystemMessage>();
    /**
     * 监听事件
     */
    private KeyboardLayout rootRl;
    private FadeUitl fadeUitl;
    //	private View headerView;
    private boolean isFirstRequest = true;
    private boolean hasLoadFull = false;
    /**
     * 文本、图片发送按钮
     */
    private Button mSendBtn;
    /**
     * 输入框
     */
    private EditText mEditTextContent;
    private Dialog mAlertDialog;
    private ConversationItem.ConversationListOtherItem currentSystemMsgConversation;
    private static final int PAGE_COUNT = 15;
    private static final String REQUEST_URL1 = "<a href=\"http://www.renhe.cn/member/account_base.html\" target=\"_blank\">点击这里</a>";
    private static final String REQUEST_URL2 = "<a href=\"http://www.renhe.cn/member/\" target=\"_blank\">点击这里</a>";
    private static final String REQUEST_URL3 = "<a href=\"http://www.renhe.cn/member/setface.html\">点击这里修改</a>";
    private static final String CLICK_HERE = "点击这里修改";
    private boolean isUpdateUserface = false;
    private UserInfo userInfo;
    private String userface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置界面属性
        setMyContentView(R.layout.im_activity_chatting);
        // 启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        handleIntent(); //处理Conversation
    }

    @Override
    public void findView() {
        rootRl = (KeyboardLayout) findViewById(R.id.rootRl);
        findViewById(R.id.btn_send_img).setVisibility(View.GONE);
        findViewById(R.id.ivPopUp).setVisibility(View.GONE);
        findViewById(R.id.btn_send_img).setVisibility(View.GONE);
        findViewById(R.id.image_face).setVisibility(View.GONE);
        mSendBtn = (Button) findViewById(R.id.btn_send);
        mSendBtn.setVisibility(View.VISIBLE);
        mSendBtn.setOnClickListener(this);
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
        mListView = (XListView) findViewById(R.id.listview);
        mListAdapter = new MessageListAdapter();
        mListView.setAdapter(mListAdapter);

    }

    private void handleIntent() {
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        if (null != getIntent().getSerializableExtra("currentSystemMsgConversation")) {
            currentSystemMsgConversation = (ConversationItem.ConversationListOtherItem) getIntent()
                    .getSerializableExtra("currentSystemMsgConversation");
        }
        userInfo = RenheApplication.getInstance().getUserInfo();
        userface = userInfo.getUserface();
        setConversationTitle(); //会话标题
        listSystemMessages(page, "");
    }

    private void refreshData() {
        mListView.setPullRefreshEnable(true);
        mListView.showHeaderView((int) getResources().getDimension(R.dimen.xlistview_refresh_need_height));
        listSystemMessages(page, "more");
    }

    @Override
    public void initListener() {

        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(this);
        /*****检测软键盘是否弹起，消失*****/
        rootRl.setOnkbdStateListener(new KeyboardLayout.onKybdsChangeListener() {
            @Override
            public void onKeyBoardStateChange(int state, int keyboardHeight) {
                switch (state) {
                    case KeyboardLayout.KEYBOARD_STATE_HIDE:
                        break;
                    case KeyboardLayout.KEYBOARD_STATE_SHOW:
                        //滚动到底部
                        mListView.clearFocus();
                        mListView.post(new Runnable() {
                            @Override
                            public void run() {
                                mListView.setSelection(mListView.getBottom());
                            }
                        });
                        break;
                }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)){}
                        if (mListView.getFirstVisiblePosition() == 0) {
                            if (hasLoadFull)
                                return;
                            //滑动到顶部自动加载
                            refreshData();
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void listSystemMessages(int page, final String requestType) {

        new GetSystemMsgListTask(this) {
            public void doPre() {
                if (requestType.equals("more") && !hasLoadFull) {
                }
            }

            public void doPost(com.itcalf.renhe.dto.ConversationSystemMsgOperation result) {
                if (requestType.equals("more") && !hasLoadFull) {
                }
                if (null != result && result.getState() == 1) {
                    SystemMessage[] messagesArray = result.getMessageList();
                    if (null != messagesArray && messagesArray.length > 0) {
                        if (messagesArray.length < PAGE_COUNT) {
                            hasLoadFull = true;
                            mListView.setPullRefreshEnable(false);
                        }
                        incrPageNum();
                        for (SystemMessage systemMessage : messagesArray) {
                            systemMessage.setStatus(SystemMessage.SYSTEM_MSG_SENDSUCCESS);
                            sysMessagesList.add(systemMessage);
                        }
                        mListAdapter.setItems(sysMessagesList);
                        //滚动到底部
                        mListView.setSelection(messagesArray.length - 1);
                    } else {
                        hasLoadFull = true;
                    }
                }
                mListView.stopRefresh();
                mListView.stopLoadMore();
                fadeUitl.removeFade(rootRl);
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), page + "", PAGE_COUNT + "");

    }

    private void send(SystemMessage message) {
        message.setStatus(IMInnerMsgChatListOperation.UserInnerMsgChat.INNER_MSG_SENDING);
        new SendMsgTask(message).executeOnExecutor(Executors.newCachedThreadPool(), userInfo.getSid(), userInfo.getAdSId(),
                message.getSubject());
    }

    class SendMsgTask extends AsyncTask<String, Void, MessageBoardOperation> {

        SystemMessage message;

        public SendMsgTask(SystemMessage message) {
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("content", params[2]);
            reqParams.put("deviceType", "0");

            try {
                return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.HELIAO_FEEDBACK, reqParams,
                        MessageBoardOperation.class, SystemMsgActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (result != null && result.getState() == 1) {
                message.setStatus(SystemMessage.SYSTEM_MSG_SENDSUCCESS);
                mListAdapter.updateItem(message);
                if (null != currentSystemMsgConversation) {
                    currentSystemMsgConversation.setLastMessage(message.getSubject());
                    currentSystemMsgConversation.setCreateTime(message.getCreatedDate());
                }
            } else if (result.getState() == -4) {
                message.setStatus(SystemMessage.SYSTEM_MSG_OFFLINE);
                mListAdapter.updateItem(message);
                ToastUtil.showToast(SystemMsgActivity.this, "内容不能少于7个字！");
            } else {
                message.setStatus(SystemMessage.SYSTEM_MSG_OFFLINE);
                mListAdapter.updateItem(message);
                ToastUtil.showNetworkError(SystemMsgActivity.this);
            }
        }
    }

    private void incrPageNum() {
        page++;
    }

    private void setConversationTitle() {
        setTextValue(1, Constants.ConversationStaticItem.CONVERSATION_ITEM_HELPER);//和聊助手
    }

    @Override
    public void onBackPressed() {
        if (RenheApplication.getInstance().getLogin() == 0) {
            startHlActivity(new Intent(this, MyPortal.class));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send: //发送文本消息
                String contString = mEditTextContent.getText().toString();
                if (contString.trim().length() > 0 && contString.trim().length() <= 5000) {
                    SystemMessage message = new SystemMessage();
                    message.setSubject(contString);
                    message.setCreatedDate(System.currentTimeMillis());
                    message.setBizType(6);
                    mListAdapter.updateItem(message);
                    mListView.setSelection(mListView.getBottom());
                    mEditTextContent.setText("");
                    send(message);
                } else {
                    mEditTextContent.setText("");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 聊天界面消息列表适配器
     */
    private class MessageListAdapter extends ListAdapter<SystemMessage> {
        public MessageListAdapter() {
            super(SystemMsgActivity.this, R.layout.sysmsg_chatlist_itemlayout_left, R.layout.sysmsg_chatlist_itemlayout_right, R.layout.sysmsg_chatlist_wallet_itemlayout_left);
        }

        /**
         * item布局文件类型
         *
         * @param position
         * @return 返回值对应于构造函数中布局文件的顺序
         */
        @Override
        public int getItemViewType(int position) {
            SystemMessage systemMessage = getItem(position);
            if (systemMessage.getBizType() == 6) {
                return 1;//自己发送的消息，显示sysmsg_chatlist_itemlayout_right布局
            } else if (systemMessage.getBizType() == 12) {//余额相关
                return 2;//和聊助手发送的消息，显示sysmsg_chatlist_wallet_itemlayout_left布局
            } else {
                return 0;//和聊助手发送的消息，显示sysmsg_chatlist_itemlayout_left布局
            }
        }

        /**
         * 有2种item布局文件，自己发消息在右边，来消息在左边
         *
         * @return 布局文件种数
         */
        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public ViewHolder onCreateViewHolder(View convertView) {
            MessageViewHolder viewHolder = (MessageViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new MessageViewHolder();
                viewHolder.ivUserIcon = (ImageView) convertView.findViewById(R.id.iv_userhead);
                viewHolder.tvSendTime = ((TextView) convertView.findViewById(R.id.tv_sendtime));
                viewHolder.tvNickName = ((TextView) convertView.findViewById(R.id.tv_username));
                viewHolder.tvContent = ((TextView) convertView.findViewById(R.id.tv_chatcontent));
                viewHolder.ivSendFail = ((TextView) convertView.findViewById(R.id.tv_sendFail));
                viewHolder.imContent = ((ImageView) convertView.findViewById(R.id.iv_imgcontent));
                viewHolder.audioContent = ((ImageView) convertView.findViewById(R.id.iv_audiocontent));
                viewHolder.pbSendStatus = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                viewHolder.audioLengthTv = (TextView) convertView.findViewById(R.id.audio_length_tv);
                viewHolder.flContent = (RelativeLayout) convertView.findViewById(R.id.fl_content);
                viewHolder.audioUnReadIv = (ImageView) convertView.findViewById(R.id.read_circle_view);
                viewHolder.seperateView = convertView.findViewById(R.id.seperate_view);
                viewHolder.linkLayout = (RelativeLayout) convertView.findViewById(R.id.link_rl);
                viewHolder.linkTitleTv = (TextView) convertView.findViewById(R.id.link_title_tv);
                viewHolder.ivChatContent = (ImageView) convertView.findViewById(R.id.iv_chatcontent);

                //以下是余额相关view
                viewHolder.walletTimeTv = (TextView) convertView.findViewById(R.id.wallet_tv_time);
                viewHolder.walletCashmoneyTipTv = (TextView) convertView.findViewById(R.id.wallet_cashmoney_tip_tv);
                viewHolder.walletCashmoneyTv = (TextView) convertView.findViewById(R.id.wallet_cashmoney_tv);
                viewHolder.walletSeperateLineView = convertView.findViewById(R.id.wallet_seperate_view);
                viewHolder.walletCashmoneyLl1 = (LinearLayout) convertView.findViewById(R.id.wallet_cashmoney_ll1);
                viewHolder.walletCashmoneyLl2 = (LinearLayout) convertView.findViewById(R.id.wallet_cashmoney_ll2);
                viewHolder.walletCashmoneyLl3 = (LinearLayout) convertView.findViewById(R.id.wallet_cashmoney_ll3);
                viewHolder.walletCashmoneyLl4 = (LinearLayout) convertView.findViewById(R.id.wallet_cashmoney_ll4);
                viewHolder.walletCashmoneyLl5 = (LinearLayout) convertView.findViewById(R.id.wallet_cashmoney_ll5);
                viewHolder.walletCashmoneyLabel1 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_label1);
                viewHolder.walletCashmoneyContent1 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_content1);
                viewHolder.walletCashmoneyLabel2 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_label2);
                viewHolder.walletCashmoneyContent2 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_content2);
                viewHolder.walletCashmoneyLabel3 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_label3);
                viewHolder.walletCashmoneyContent3 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_content3);
                viewHolder.walletCashmoneyLabel4 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_label4);
                viewHolder.walletCashmoneyContent4 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_content4);
                viewHolder.walletCashmoneyLabel5 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_label5);
                viewHolder.walletCashmoneyContent5 = (TextView) convertView.findViewById(R.id.wallet_cashmoney_content5);
                convertView.setTag(viewHolder);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final SystemMessage message, final int position) {
            MessageViewHolder messageViewHolder = (MessageViewHolder) viewHolder;

            if (position >= 1) {
                SystemMessage preMessage = getItem(position - 1);
                if (message.getCreatedDate() - preMessage.getCreatedDate() > 5 * MINUTE) {//新的消息据上条相差5分钟，才显示时间
                    messageViewHolder.tvSendTime.setVisibility(View.VISIBLE);
                    messageViewHolder.tvSendTime
                            .setText(DateUtil.newFormatByDay(SystemMsgActivity.this, new Date(message.getCreatedDate())));
                } else {
                    messageViewHolder.tvSendTime.setVisibility(View.GONE);
                }
            } else {
                messageViewHolder.tvSendTime.setVisibility(View.VISIBLE);
                messageViewHolder.tvSendTime
                        .setText(DateUtil.newFormatByDay(SystemMsgActivity.this, new Date(message.getCreatedDate())));
            }

            messageViewHolder.tvContent.setVisibility(View.VISIBLE);

            if (message.getBizType() != 12) {
                messageViewHolder.imContent.setVisibility(View.GONE);
                messageViewHolder.audioContent.setVisibility(View.GONE);
                messageViewHolder.audioLengthTv.setVisibility(View.GONE);

                switch (getItemViewType(position)) {
                    case 0:
                        //和聊助手头像
                        messageViewHolder.ivUserIcon.setImageResource(R.drawable.sysmsg);
                        messageViewHolder.ivUserIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        messageViewHolder.tvContent.setText(Html.fromHtml(message.getSubject()));
                        if (!TextUtils.isEmpty(message.getSubject())) {
                            String conString = message.getSubject();
                            SpannableStringBuilder sb = new SpannableStringBuilder(conString);
                            final ForegroundColorSpan cs = new ForegroundColorSpan(Color.rgb(158, 158, 158));

                            if (conString.contains(REQUEST_URL1)) {
                                conString = conString.replaceAll(REQUEST_URL1, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL2, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL3, CLICK_HERE);
                                sb = new SpannableStringBuilder(conString);
                                int i = conString.indexOf(CLICK_HERE);
                                sb.setSpan(cs, i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                sb.setSpan(new UrlClick(), i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                if (i > 0) {
                                    int j = conString.indexOf(CLICK_HERE, i + CLICK_HERE.length());
                                    if (j > 0) {
                                        sb.setSpan(cs, j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        sb.setSpan(new UrlClick(), j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    }
                                }
                                messageViewHolder.tvContent.setText(sb);
                            } else if (conString.contains(REQUEST_URL2)) {
                                conString = conString.replaceAll(REQUEST_URL1, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL2, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL3, CLICK_HERE);
                                sb = new SpannableStringBuilder(conString);
                                int i = conString.indexOf(CLICK_HERE);

                                sb.setSpan(cs, i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                sb.setSpan(new UrlClick(), i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                                if (i > 0) {
                                    int j = conString.indexOf(CLICK_HERE, i + CLICK_HERE.length());
                                    if (j > 0) {
                                        sb.setSpan(cs, j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        sb.setSpan(new UrlClick(), j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                                    }
                                }
                                messageViewHolder.tvContent.setText(sb);
                            } else if (conString.contains(REQUEST_URL3)) {
                                isUpdateUserface = true;
                                conString = conString.replaceAll(REQUEST_URL1, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL2, CLICK_HERE);
                                conString = conString.replaceAll(REQUEST_URL3, CLICK_HERE);
                                sb = new SpannableStringBuilder(conString);
                                int i = conString.indexOf(CLICK_HERE);
                                sb.setSpan(cs, i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                sb.setSpan(new UrlClick(), i, i + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                if (i > 0) {
                                    int j = conString.indexOf(CLICK_HERE, i + CLICK_HERE.length());
                                    if (j > 0) {
                                        sb.setSpan(cs, j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        sb.setSpan(new UrlClick(), j, j + CLICK_HERE.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                                    }

                                }
                                messageViewHolder.tvContent.setText(sb);
                            } else if (conString.contains("href")) {
                                messageViewHolder.tvContent.setText(getClickableHtml(conString));
                            }
                        }
                        messageViewHolder.tvContent.setMovementMethod(LinkMovementMethod.getInstance());

                        //如果需要加载一张图片
                        if (message.isShowImage()) {
                            messageViewHolder.ivChatContent.setVisibility(View.VISIBLE);
                            //用户头像
                            ImageLoader imageLoader = ImageLoader.getInstance();
                            try {
                                imageLoader.displayImage(message.getImageUrl(), messageViewHolder.ivChatContent, CacheManager.imageOptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            messageViewHolder.ivChatContent.setVisibility(View.GONE);
                        }
                        if (message.getBizType() == 0) {
                            messageViewHolder.seperateView.setVisibility(View.GONE);
                            messageViewHolder.linkLayout.setVisibility(View.GONE);
                        } else if (message.getBizType() == 1) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }
                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    Intent intent = new Intent(SystemMsgActivity.this, MyHomeArchivesActivity.class);
                                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                            RenheApplication.getInstance().getUserInfo().getSid());
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                            messageViewHolder.tvContent.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    Intent intent = new Intent(SystemMsgActivity.this, MyHomeArchivesActivity.class);
                                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                            RenheApplication.getInstance().getUserInfo().getSid());
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        } else if (message.getBizType() == 2) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    ConversationService service = IMEngine.getIMService(ConversationService.class);
                                    service.getConversation(new Callback<Conversation>() {

                                        @Override
                                        public void onSuccess(final Conversation arg0) {
                                            if (arg0 != null) {
                                                IMEngine.getIMService(ConversationService.class)
                                                        .listMembers(new Callback<List<Member>>() {
                                                            @Override
                                                            public void onException(String exception, String arg1) {
                                                                ToastUtil.showToast(SystemMsgActivity.this,
                                                                        getResources().getString(R.string.cicle_get_error_be_kikout));
                                                            }

                                                            public void onProgress(List<Member> arg0, int arg1) {
                                                            }

                                                            public void onSuccess(List<Member> menbers) {
                                                                for (Member menber : menbers) {
                                                                    if (null != menber && menber.user()
                                                                            .openId() == RenheApplication.getInstance().currentOpenId) {
                                                                        Intent intent = new Intent(SystemMsgActivity.this, ChatMainActivity.class);
                                                                        intent.putExtra("conversation", arg0);
                                                                        startActivity(intent);
                                                                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                                                        finish();
                                                                        return;
                                                                    }
                                                                }
                                                                ToastUtil.showToast(SystemMsgActivity.this,
                                                                        getResources().getString(R.string.cicle_get_error_be_kikout));
                                                            }

                                                        }, arg0.conversationId(), 0, Integer.MAX_VALUE);

                                            } else {
                                                ToastUtil.showToast(SystemMsgActivity.this,
                                                        getResources().getString(R.string.cicle_get_error_not_exist));
                                            }
                                        }

                                        @Override
                                        public void onProgress(Conversation arg0, int arg1) {

                                        }

                                        @Override
                                        public void onException(String arg0, String arg1) {

                                        }
                                    }, message.getExtdata().getImConversationId());
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //实名认证
                        else if (message.getBizType() == 3) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, TabMainFragmentActivity.class);
                                    intent.putExtra("fromSystemMsg", true);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    finish();
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //注册时，通知输邀请码
                        else if (message.getBizType() == 4) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, InvitationCodeActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    finish();
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //邀请码验证成功，查看限额
                        else if (message.getBizType() == 5) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, AccountLimitActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //会员开通/升级
                        else if (message.getBizType() == 7) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("立刻加入");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }
                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(SystemMsgActivity.this, UpgradeActivity.class));
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    statisticsMap.put("type", "2");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //8 引导发人脉圈
                        else if (message.getBizType() == 8) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("发人脉圈");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, AddMessageBoardActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //9 引导完善资料
                        else if (message.getBizType() == 9) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("完善资料");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, EditMyHomeArchivesActivity.class);
                                    intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_DATA, userInfo.getSid());
                                    intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO, EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //10 引导找人脉
                        else if (message.getBizType() == 10) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("找人脉");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, SearchForContactsActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //11 引导使用附近人脉功能
                        else if (message.getBizType() == 11) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("附近人脉");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SystemMsgActivity.this, NearbyActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                    statisticsMap.put("bizType", message.getBizType() + "");
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                }
                            });
                        }
                        //13 打开一个网页
                        else if (message.getBizType() == 13) {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(message.getLinkTitle())) {
                                messageViewHolder.linkTitleTv.setText("查看详情");
                            } else {
                                messageViewHolder.linkTitleTv.setText(message.getLinkTitle());
                            }

                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (null != message.getExtdata() && !TextUtils.isEmpty(message.getExtdata().getUrl())) {
                                        Intent intent = new Intent(SystemMsgActivity.this, WebViewActWithTitle.class);
                                        intent.putExtra("url", message.getExtdata().getUrl());
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                        Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                                        statisticsMap.put("bizType", message.getBizType() + "");
                                        StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu2_hlzsdetail_click), 0, "", statisticsMap);
                                    }
                                }
                            });
                        } else {
                            messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                            messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                            messageViewHolder.linkTitleTv.setText(getString(R.string.app_version_is_old));
                            messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new CheckUpdateUtil(SystemMsgActivity.this).checkUpdate(true);
                                }
                            });
                        }
                        break;
                    case 1:
                        //用户头像
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        try {
                            imageLoader.displayImage(userface, messageViewHolder.ivUserIcon);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        messageViewHolder.tvContent.setText(message.getSubject());
                        //消息发送状态
                        if (message.getStatus() == SystemMessage.SYSTEM_MSG_OFFLINE) {
                            messageViewHolder.audioLengthTv.setVisibility(View.GONE);
                            messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
                            messageViewHolder.ivSendFail.setVisibility(View.VISIBLE);
                            messageViewHolder.pbSendStatus.setVisibility(View.GONE);
                            messageViewHolder.ivSendFail.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    RenheIMUtil.showAlertDialog(SystemMsgActivity.this, "是否重新发送", new RenheIMUtil.DialogCallback() {
                                        @Override
                                        public void onPositive() {
                                            send(message);
                                        }

                                        @Override
                                        public void onCancle() {

                                        }

                                    });
                                }
                            });
                        } else if (message.getStatus() == SystemMessage.SYSTEM_MSG_SENDING) {
                            messageViewHolder.audioLengthTv.setVisibility(View.GONE);
                            messageViewHolder.ivSendFail.setVisibility(View.GONE);
                            messageViewHolder.pbSendStatus.setVisibility(View.VISIBLE);
                        } else {
                            messageViewHolder.ivSendFail.setVisibility(View.GONE);
                            messageViewHolder.pbSendStatus.setVisibility(View.GONE);
                            messageViewHolder.audioLengthTv.setVisibility(View.GONE);
                            messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
                        }

                        messageViewHolder.audioLengthTv.setVisibility(View.GONE);
                        messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
                        messageViewHolder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {

                            @Override
                            public boolean onLongClick(View arg0) {
                                createCopyDialog(SystemMsgActivity.this, message.getSubject());
                                return true;
                            }
                        });
                        messageViewHolder.ivUserIcon.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(SystemMsgActivity.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, userInfo.getSid());
                                startActivity(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        });
                        messageViewHolder.seperateView.setVisibility(View.GONE);
                        messageViewHolder.linkLayout.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }

            } else {
                if (message.getBizType() == 12) {
                    messageViewHolder.tvContent.setText(message.getSubject());
                    ConversationSystemMsgOperation.Extdata extdata = message.getExtdata();
                    if (null != extdata) {
                        messageViewHolder.walletTimeTv.setVisibility(View.VISIBLE);
                        messageViewHolder.walletCashmoneyTipTv.setVisibility(View.VISIBLE);
                        messageViewHolder.walletCashmoneyTv.setVisibility(View.VISIBLE);
                        messageViewHolder.walletTimeTv.setText(extdata.getCreate_date());
                        messageViewHolder.walletCashmoneyTipTv.setText(extdata.getAmount_name());
                        messageViewHolder.walletCashmoneyTv.setText("￥" + extdata.getAmount());
                        ConversationSystemMsgOperation.Detail[] details = extdata.getDetail();
                        if (null != details && details.length > 0) {
                            messageViewHolder.walletSeperateLineView.setVisibility(View.VISIBLE);
                            switch (details.length) {
                                case 1:
                                    messageViewHolder.walletCashmoneyLl1.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl2.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl3.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl4.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLabel1.setText(details[0].getLabel());
                                    messageViewHolder.walletCashmoneyContent1.setText(details[0].getContent());
                                    break;
                                case 2:
                                    messageViewHolder.walletCashmoneyLl1.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl2.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl3.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl4.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLabel1.setText(details[0].getLabel());
                                    messageViewHolder.walletCashmoneyContent1.setText(details[0].getContent());
                                    messageViewHolder.walletCashmoneyLabel2.setText(details[1].getLabel());
                                    messageViewHolder.walletCashmoneyContent2.setText(details[1].getContent());
                                    break;
                                case 3:
                                    messageViewHolder.walletCashmoneyLl1.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl2.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl3.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl4.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyLabel1.setText(details[0].getLabel());
                                    messageViewHolder.walletCashmoneyContent1.setText(details[0].getContent());
                                    messageViewHolder.walletCashmoneyLabel2.setText(details[1].getLabel());
                                    messageViewHolder.walletCashmoneyContent2.setText(details[1].getContent());
                                    messageViewHolder.walletCashmoneyLabel3.setText(details[2].getLabel());
                                    messageViewHolder.walletCashmoneyContent3.setText(details[2].getContent());
                                    break;
                                case 4:
                                    messageViewHolder.walletCashmoneyLl1.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl2.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl3.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl4.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                                    messageViewHolder.walletCashmoneyContent1.setText(details[0].getContent());
                                    messageViewHolder.walletCashmoneyLabel2.setText(details[1].getLabel());
                                    messageViewHolder.walletCashmoneyContent2.setText(details[1].getContent());
                                    messageViewHolder.walletCashmoneyLabel3.setText(details[2].getLabel());
                                    messageViewHolder.walletCashmoneyContent3.setText(details[2].getContent());
                                    messageViewHolder.walletCashmoneyLabel4.setText(details[3].getLabel());
                                    messageViewHolder.walletCashmoneyContent4.setText(details[3].getContent());
                                    break;
                                case 5:
                                    messageViewHolder.walletCashmoneyLl1.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl2.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl3.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl4.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyLl5.setVisibility(View.VISIBLE);
                                    messageViewHolder.walletCashmoneyContent1.setText(details[0].getContent());
                                    messageViewHolder.walletCashmoneyLabel2.setText(details[1].getLabel());
                                    messageViewHolder.walletCashmoneyContent2.setText(details[1].getContent());
                                    messageViewHolder.walletCashmoneyLabel3.setText(details[2].getLabel());
                                    messageViewHolder.walletCashmoneyContent3.setText(details[2].getContent());
                                    messageViewHolder.walletCashmoneyLabel4.setText(details[3].getLabel());
                                    messageViewHolder.walletCashmoneyContent4.setText(details[3].getContent());
                                    messageViewHolder.walletCashmoneyLabel5.setText(details[4].getLabel());
                                    messageViewHolder.walletCashmoneyContent5.setText(details[4].getContent());
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            messageViewHolder.walletSeperateLineView.setVisibility(View.GONE);
                            messageViewHolder.walletCashmoneyLl1.setVisibility(View.GONE);
                            messageViewHolder.walletCashmoneyLl2.setVisibility(View.GONE);
                            messageViewHolder.walletCashmoneyLl3.setVisibility(View.GONE);
                            messageViewHolder.walletCashmoneyLl4.setVisibility(View.GONE);
                            messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                        }
                    } else {
                        messageViewHolder.walletTimeTv.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyTipTv.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyTv.setVisibility(View.GONE);
                        messageViewHolder.walletSeperateLineView.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyLl1.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyLl2.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyLl3.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyLl4.setVisibility(View.GONE);
                        messageViewHolder.walletCashmoneyLl5.setVisibility(View.GONE);
                    }
                    messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(SystemMsgActivity.this, MyWalletDetailActivity.class));
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    });
                } else {
                    messageViewHolder.seperateView.setVisibility(View.VISIBLE);
                    messageViewHolder.linkLayout.setVisibility(View.VISIBLE);
                    messageViewHolder.linkTitleTv.setText(getString(R.string.app_version_is_old));
                    messageViewHolder.flContent.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new CheckUpdateUtil(SystemMsgActivity.this).checkUpdate(true);
                        }
                    });
                }
            }

        }

        class MessageViewHolder extends ViewHolder {
            /**
             * 发送者头像
             */
            ImageView ivUserIcon;
            /**
             * 消息发送时间
             */
            TextView tvSendTime;
            /**
             * 发送者昵称
             */
            TextView tvNickName;
            /**
             * 文本消息内容
             */
            TextView tvContent;
            /**
             * 消息发送失败提醒
             */
            TextView ivSendFail;
            /**
             * 图像消息内容
             */
            ImageView imContent;
            /**
             * 语音消息内容
             */
            ImageView audioContent;
            /**
             * 发送进度条
             */
            ProgressBar pbSendStatus;
            TextView audioLengthTv;
            RelativeLayout flContent;
            ImageView audioUnReadIv;
            View seperateView;
            RelativeLayout linkLayout;
            TextView linkTitleTv;
            ImageView ivChatContent;

            //以下是余额相关view
            TextView walletTimeTv;
            TextView walletCashmoneyTipTv;
            TextView walletCashmoneyTv;
            View walletSeperateLineView;
            LinearLayout walletCashmoneyLl1;
            LinearLayout walletCashmoneyLl2;
            LinearLayout walletCashmoneyLl3;
            LinearLayout walletCashmoneyLl4;
            LinearLayout walletCashmoneyLl5;
            TextView walletCashmoneyLabel1;
            TextView walletCashmoneyContent1;
            TextView walletCashmoneyLabel2;
            TextView walletCashmoneyContent2;
            TextView walletCashmoneyLabel3;
            TextView walletCashmoneyContent3;
            TextView walletCashmoneyLabel4;
            TextView walletCashmoneyContent4;
            TextView walletCashmoneyLabel5;
            TextView walletCashmoneyContent5;
        }

        @Override
        public void sort() {
            Collections.sort(mList, new Comparator<SystemMessage>() {
                @Override
                public int compare(SystemMessage lhs, SystemMessage rhs) {
                    return lhs.getCreatedDate() > rhs.getCreatedDate() ? 1 : -1;
                }
            });
        }
    }

    class UrlClick extends ClickableSpan {

        @Override
        public void onClick(View arg0) {
            MobclickAgent.onEvent(SystemMsgActivity.this, "inner_reedit_avartar");
            Intent intent = new Intent(SystemMsgActivity.this, MyHomeArchivesActivity.class);
            intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, RenheApplication.getInstance().getUserInfo().getSid());
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }

    }

    @Override
    public void onRefresh() {
        listSystemMessages(page, "more");
    }

    @Override
    public void onLoadMore() {

    }

    private void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder,
                                  final URLSpan urlSpan) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);
        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SystemMsgActivity.this, WebViewActWithTitle.class);
                i.putExtra("url", urlSpan.getURL());
                startHlActivity(i);
            }
        };
        clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
    }

    private CharSequence getClickableHtml(String html) {
        Spanned spannedHtml = Html.fromHtml(html);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            setLinkClickable(clickableHtmlBuilder, span);
        }
        return clickableHtmlBuilder;
    }

    public void createCopyDialog(Context context, final String content) {
        int arrayId = R.array.im_choice_items;
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(arrayId).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        MobclickAgent.onEvent(SystemMsgActivity.this, "Innermsg_copy");
                        if (null != mAlertDialog) {
                            mAlertDialog.dismiss();
                        }
                        ContentUtil.copy(content, SystemMsgActivity.this);
                        ToastUtil.showToast(SystemMsgActivity.this, R.string.already_copy_to_plate);
                        break;
                    case 1:
                        MobclickAgent.onEvent(SystemMsgActivity.this, "Innermsg_forward");
                        if (null != mAlertDialog) {
                            mAlertDialog.dismiss();
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("toForwardContent", content);
                        bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD);
                        Intent intent = new Intent(SystemMsgActivity.this, ToShareWithRecentContactsActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                    default:
                        break;
                }

            }
        });
        materialDialog.show();
    }
}