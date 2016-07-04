package com.itcalf.renhe.context.fragmentMain;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.wukong.AuthConstants;
import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.CommonContactList;
import com.itcalf.renhe.bean.OcrLocalCard;
import com.itcalf.renhe.bean.TabHasNewBean;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.contacts.NewFriendsAct;
import com.itcalf.renhe.context.contacts.OcrCardsListActivity;
import com.itcalf.renhe.context.contacts.SearchForContactsActivity;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.room.AddMessageBoardActivity;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.context.wukong.im.ChatMainActivity;
import com.itcalf.renhe.context.wukong.im.ConversationListFragment;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.context.wukong.im.SystemMsgActivity;
import com.itcalf.renhe.context.wukong.im.TouTiaoActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.eventbusbean.FinishOcrActivityEvent;
import com.itcalf.renhe.eventbusbean.RefreshNewFriendListEvent;
import com.itcalf.renhe.eventbusbean.TopContactEvent;
import com.itcalf.renhe.eventbusbean.TopImEvent;
import com.itcalf.renhe.eventbusbean.TopRmqEvent;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.service.CreateCircleServise;
import com.itcalf.renhe.service.RenheService;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.CheckUpdateUtil;
import com.itcalf.renhe.utils.ContactsContentObserver;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.DownloadServiceTool;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LogoutUtil;
import com.itcalf.renhe.utils.OcrLocalCardUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.SystemBarTintManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.TransferUrl2Drawable;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import cn.ocrsdk.uploadSdk.OcrActivityCamera;
import cn.ocrsdk.uploadSdk.OcrBackAuth;
import cn.ocrsdk.uploadSdk.OcrBackUpload;
import cn.ocrsdk.uploadSdk.OcrErrorCode;
import cn.ocrsdk.uploadSdk.OcrServer;
import cn.renhe.heliao.idl.member.MyModuleNotice;
import cn.renhe.heliao.idl.notice.HeliaoNotice;
import de.greenrobot.event.EventBus;

public class TabMainFragmentActivity extends AppCompatActivity implements OnPageChangeListener, OnClickListener, Callback {

    private ViewPager mViewPager;
    private List<Fragment> mTabs = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;
    private List<TextView> mTabIndicator = new ArrayList<>();
    private TextView one, two, three, four;
    private TextView newDialogue, newContacts, newRoom, newMynumb;
    private ImageView search_newicon, room_newicon;
    //    /**
//     * 人脉圈更新的最大时间
//     */
//    private long maxDate = 0;
    // 接收通知更新界面
    public final static String NEWMSG_ICON_ACTION = "newmsg_notice_icon_num";
    private boolean logoutFlag = false;
    // 初始化所有切换图片
    private Drawable tab_icon_im, tab_icon_im_on, tab_icon_search, tab_icon_search_on, tab_icon_feed, tab_icon_feed_on,
            tab_icon_me, tab_icon_me_on;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;
    // IM
    // IM广播接收器
    private LocalBroadcastManager mLocalBroadcastManager;
    /**
     * 根据不同的会话、消息事件类型做相应的业务处理
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                msp = context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
                mEditor = msp.edit();
                String action = intent.getAction();
                if (AuthConstants.Event.EVENT_AUTH_KICKOUT.equals(action)) {
                    // //放到application监听
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public static final String TAB_ICON_UNREAD_RECEIVER_ACTION = "tab_icon_unread_receiver_action";
    public static final String TAB_FLAG = "tab_flag";
    private TabUnreadIconReceiver tabUnreadIconReceiver;
    public static final String TAB_ICON_CONVERSATION_UNREAD_NUM = "tab_icon_conversation_unread_num";
    public static final String TAB_ICON_SEARCH_UNREAD_NUM = "tab_icon_search_unread_num";
    public static final String TAB_ICON_RENMAIQUAN_UNREAD_NUM = "tab_icon_renmaiquan_unread_num";
    public static final String TAB_ICON_ME_UNREAD_NUM = "tab_icon_me_unread_num";
    public static final String TAB_ICON_NUM_TOO_LARGE = "...";
    public static final float TAB_ICON_NUM_TOO_LARGE_TEXTSIZE = 10f;
    public static final float TAB_ICON_NUM_NORMAL_TEXTSIZE = 12f;
    private RelativeLayout rootRl;

    private SharedPreferences conversationMsp;
    private SharedPreferences.Editor conversationEditor;
    public static final String KIK_OUT_ACTION = "account_kit_out";

    private View view;
    private static TextView titleTv;
    private static RelativeLayout diytitle;

    private long firstClick;
    private long lastClick;
    // 计算点击的次数
    private int count;
    public static final String ACTIONBAR_DOUBLE_TAP_ACTION = "actionbar_double_tap_action";
    public static final String CANCLE_ACTIONBAR_DOUBLE_TAP_ACTION = "cancle_actionbar_double_tap_action";
    public static final String CONVERSATION_ACTIONBAR_DOUBLE_TAP_ACTION = "conversation_actionbar_double_tap_action";
    public static final String CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION = "contacts_actionbar_double_tap_action";
    private GuideDoubleTapReceiver guideDoubleTapReceiver;
    public static final String SHOW_KIKOUT_DIALOG_ACTION = "renhe_show_kikout_dialog_action";
    public static final String COMPLETE_RECEIVER_ACTION = "com.itcalf.renhe.context.fragmentMain";//完善资料程度频道
    private ImageButton scanImgBtn;
    private LinearLayout searchEdt;
    private TextSize textSize;
    private int scanBtn = 1;
    //是否需要完善资料
    private String sid, adSid;
    private OcrServer maiKeXunServer;//脉可寻 上传名片server
    private boolean isMaiKeXunInit;//账户是否初始化（key，secret）
    private int uploadTimes = 0;//脉可寻重新上传名片的重复次数
    //    private ArrayList<String> upLoadFails = new ArrayList<>();//脉可寻上传失败的名片
    private ImageView data_complete;
    private boolean isNeedToOcrCardList = false;//用来标示是否是拍完名片返回到主界面，如果是true，跳转到名片列表页
    private int mkxAuthCount = 0;//脉可寻认证次数，最多十次，否则认为失败
    private int scanImgIndex = 1;//左上角拍名片图标的type，1是二维码扫描图标，2是相机图标，用来友盟统计，哪种图标效果好
    private ContactsContentObserver content;//注册监听联系人表变化的ContentObserver
    private Toolbar toolbar;
    private GrpcController grpcController;//grpc调用
    private int ID_TASK_MYMODULE_NOTICE = TaskManager.getTaskId();//我的页面，完善资料是否需要显示小红点
    private int ID_TASK_HELIAO_NOTICE = TaskManager.getTaskId();//和聊应用内所有的消息提醒

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.tab_activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.renhe_actionbar_bcg);
        }
        rootRl = (RelativeLayout) findViewById(R.id.rel);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        titleTv = (TextView) findViewById(R.id.title_text);
        diytitle = (RelativeLayout) findViewById(R.id.diytitle);
        scanImgBtn = (ImageButton) findViewById(R.id.scanning_imgbtn);
        searchEdt = (LinearLayout) findViewById(R.id.searchEdt);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        diytitle.setVisibility(View.VISIBLE);
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        newDialogue = (TextView) findViewById(R.id.newdig_numb);
        newContacts = (TextView) findViewById(R.id.search_numb);
        newRoom = (TextView) findViewById(R.id.room_numb);
        newMynumb = (TextView) findViewById(R.id.me_numb);
        //人脉上小红点
        search_newicon = (ImageView) findViewById(R.id.search_newicon);
        room_newicon = (ImageView) findViewById(R.id.room_newicon);
        //我上小红点
        data_complete = (ImageView) findViewById(R.id.data_complete);
        initTabIndicator();//初始化人脉圈，和聊，人脉，我 书签
        initIcons();//初始化书签图标
        initData();//初始化数据
        initListener();
        startService(new Intent(this, RenheService.class));//开启服务，实现定位功能
        startService(new Intent(this, CreateCircleServise.class));// 开启服务 创建圈子
        sid = ((RenheApplication) TabMainFragmentActivity.this.getApplicationContext()).getUserInfo().getSid();
        adSid = ((RenheApplication) TabMainFragmentActivity.this.getApplicationContext()).getUserInfo().getAdSId();
        //将cookie保存到webview中，实现 sid，adSid保存到cookie中
        syncCookie(new String[]{sid, adSid});
        //获取通用配置
        new GetConfigTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid);
        //获取常用联系人
        new getCommonContactListTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid);
        //初始化下载类，目前供IM发送文件使用
        DownloadServiceTool dt = new DownloadServiceTool(this);//download服务
        RenheApplication.getInstance().setDownloadService(dt);
        //记录下次进入不出现引导视频
        SharedPreferences msp = getSharedPreferences("first_guide_setting_info", 0);
        SharedPreferences.Editor mEditor = msp.edit();
        mEditor.putBoolean("ifFirst", false);
        mEditor.apply();
        //注册监听联系人表变化的ContentObserver
        content = new ContactsContentObserver(TabMainFragmentActivity.this, new Handler());
        this.getContentResolver().registerContentObserver(ContactsContentObserver.CONTENT_DATA_URI, true, content);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //创建异步任务访问网络,获取Tab下面的红点或数字
        new getTabNewInfoTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId());
        getMyModuleCompleteArchiveNotice();
        getHeLiaoNotice();
        if (isNeedToOcrCardList) {
            isNeedToOcrCardList = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(TabMainFragmentActivity.this, OcrCardsListActivity.class);
                    intent.putExtra("isFromTakeCard", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }, 800);
        }
        initTabView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (getIntent().getBooleanExtra("fromNotify", false)) {//来源IM notification
            one.setSelected(true);
            mViewPager.setCurrentItem(1);// 默认页面
//            sendBroadcast(new Intent(ChatActivity.FINISH_CHAT_ACTION));
            Conversation mConversation = null;
            try {
                mConversation = (Conversation) getIntent().getSerializableExtra("notifyconversation");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != mConversation) {
                Intent mintent = new Intent(this, ChatMainActivity.class);
                mintent.putExtra("conversation", mConversation);
                startActivity(mintent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        } else if (getIntent().getBooleanExtra(Constants.XING_NOTIFY_ACTION, false)) {//来源信鸽notification
            handleXinGeNotify(getIntent());
        } else if (getIntent().getBooleanExtra("fromMyJpushNotify_toMy", false)) {
            four.setSelected(true);
            mViewPager.setCurrentItem(3);// 默认页面
        } else if (getIntent().getBooleanExtra("fromSystemMsg", false)) {
            four.setSelected(true);
            mViewPager.setCurrentItem(3);// 默认页面
        } else if (getIntent().getBooleanExtra("fromAccountLimit", false)) {
//            four.setSelected(true);
//            mViewPager.setCurrentItem(3);// 默认页面
            scanImgBtn.performClick();
        }

        String browserData = getIntent().getStringExtra("browserData");
        if (Constants.DEBUG_MODE) {
            Log.e("heliaoapp", browserData + "");
        }
        if (!TextUtils.isEmpty(browserData)) {
            handleBrowseData(browserData);
        }
        super.onNewIntent(intent);
    }

    private void initTabIndicator() {
        one = (TextView) findViewById(R.id.id_indicator_one);//和聊
        two = (TextView) findViewById(R.id.id_indicator_two);//人脉
        three = (TextView) findViewById(R.id.id_indicator_three);//人脉圈
        four = (TextView) findViewById(R.id.id_indicator_four);//我
        //将各个书签添加到list集合中
        mTabIndicator.add(three);
        mTabIndicator.add(one);
        mTabIndicator.add(two);
        mTabIndicator.add(four);
        //给各个书签注册监听器
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        three.setSelected(true);
    }

    private void initIcons() {
        tab_icon_im = getResources().getDrawable(R.drawable.tab_icon_im);
        tab_icon_im.setBounds(0, 0, tab_icon_im.getMinimumWidth(), tab_icon_im.getMinimumHeight());

        tab_icon_im_on = getResources().getDrawable(R.drawable.tab_icon_im_on);
        tab_icon_im_on.setBounds(0, 0, tab_icon_im_on.getMinimumWidth(), tab_icon_im_on.getMinimumHeight());

        tab_icon_search = getResources().getDrawable(R.drawable.tab_icon_search);
        tab_icon_search.setBounds(0, 0, tab_icon_search.getMinimumWidth(), tab_icon_search.getMinimumHeight());
        tab_icon_search_on = getResources().getDrawable(R.drawable.tab_icon_search_on);
        tab_icon_search_on.setBounds(0, 0, tab_icon_search_on.getMinimumWidth(), tab_icon_search_on.getMinimumHeight());

        tab_icon_feed = getResources().getDrawable(R.drawable.tab_icon_feed);
        tab_icon_feed.setBounds(0, 0, tab_icon_feed.getMinimumWidth(), tab_icon_feed.getMinimumHeight());
        tab_icon_feed_on = getResources().getDrawable(R.drawable.tab_icon_feed_on);
        tab_icon_feed_on.setBounds(0, 0, tab_icon_feed_on.getMinimumWidth(), tab_icon_feed_on.getMinimumHeight());

        tab_icon_me = getResources().getDrawable(R.drawable.tab_icon_me);
        tab_icon_me.setBounds(0, 0, tab_icon_me.getMinimumWidth(), tab_icon_me.getMinimumHeight());
        tab_icon_me_on = getResources().getDrawable(R.drawable.tab_icon_me_on);
        tab_icon_me_on.setBounds(0, 0, tab_icon_me_on.getMinimumWidth(), tab_icon_me_on.getMinimumHeight());
    }

    void initData() {
        RenheApplication.getInstance().setLogOut(0);
        RenheApplication.getInstance().addActivity(this);
        ConversationListFragment tab1 = new ConversationListFragment();//和聊Fragment页面
        NewContactFragmentVersion tab2 = new NewContactFragmentVersion();//人脉Fragment页面
        RenMaiQuanFragment tab3 = new RenMaiQuanFragment();//人脉圈Fragment页面
        MyFragment tab4 = new MyFragment();//我Fragment页面

        mTabs.add(tab3);
        mTabs.add(tab1);
        mTabs.add(tab2);
        mTabs.add(tab4);

        FragmentManager fm = getSupportFragmentManager();//获取碎片管理器
        mAdapter = new MyFragmentPagerAdapter(fm, mTabs);//创建适配器,加载Fragment
        mViewPager.setAdapter(mAdapter);//viewPager绑定适配器
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(4);// 预先加载的页面个数
        mViewPager.setCurrentItem(0);// 默认页面

        titleTv.setVisibility(View.GONE);

        // 注册未读提醒角标广播
        tabUnreadIconReceiver = new TabUnreadIconReceiver();
        IntentFilter tabUnreadIconFilter = new IntentFilter();
        tabUnreadIconFilter.addAction(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
        tabUnreadIconFilter.addAction(Constants.BroadCastAction.NEW_FRIEND_ACTION);
        registerReceiver(tabUnreadIconReceiver, tabUnreadIconFilter);

        // 判断新登录账户是否是老用户，否则清空会话消息
        conversationMsp = getSharedPreferences("conversation_list", 0);//初始化共享参数
        conversationEditor = conversationMsp.edit();
        if (!conversationMsp.getString("loginEmail", "").equals(RenheApplication.getInstance().getUserInfo().getEmail())) {
            // 清空会话数据
            conversationEditor.clear();
            conversationEditor.putString("loginEmail", RenheApplication.getInstance().getUserInfo().getEmail());
            conversationEditor.commit();
        }
        registerReceiver();//监听悟空的会话、消息相关事件
        new CheckUpdateUtil(this).checkUpdate(false);
        initMaiKeXun();//初始化脉可寻名片扫描
        if (getIntent().getBooleanExtra("fromNotify", false)) {
            one.setSelected(true);
            mViewPager.setCurrentItem(1);// 默认页面
//            sendBroadcast(new Intent(ChatActivity.FINISH_CHAT_ACTION));
            Conversation mConversation = (Conversation) getIntent().getSerializableExtra("notifyconversation");
            Intent mintent = new Intent(this, ChatMainActivity.class);
            mintent.putExtra("conversation", mConversation);
            startActivity(mintent);
        } else if (getIntent().getBooleanExtra(Constants.XING_NOTIFY_ACTION, false)) {//来源信鸽notification
            handleXinGeNotify(getIntent());
        }

        String browserData = getIntent().getStringExtra("browserData");
        if (Constants.DEBUG_MODE) {
            Log.e("heliaoapp", browserData + "");
        }
        if (!TextUtils.isEmpty(browserData)) {
            handleBrowseData(browserData);
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ((Intent.ACTION_SEND_MULTIPLE.equals(action) || (Intent.ACTION_SEND.equals(action)) && type != null)) {
            int appshareToType = intent.getIntExtra("appshareTo", 0);
            switch (appshareToType) {
                case Constants.APP_SHARE_TO.APP_SHARE_TO_RENMAIQUAN:
                    intent.setClass(this, AddMessageBoardActivity.class);
                    startActivity(intent);
                    break;
                default:
                    intent.setClass(this, ToShareWithRecentContactsActivity.class);
                    startActivity(intent);
                    break;
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_indicator_three:
                if (mViewPager.getCurrentItem() == 0) {
                    EventBus.getDefault().post(new TopRmqEvent());
                }
                mViewPager.setCurrentItem(0, false);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_menu_menu1_click), 0, "", null);
                break;
            case R.id.id_indicator_one:
                if (mViewPager.getCurrentItem() == 1) {
                    EventBus.getDefault().post(new TopImEvent());
                }
                mViewPager.setCurrentItem(1, false);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_menu_menu2_click), 0, "", null);
                break;
            case R.id.id_indicator_two:
                if (mViewPager.getCurrentItem() == 2) {
                    EventBus.getDefault().post(new TopContactEvent());
                }
                mViewPager.setCurrentItem(2, false);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_menu_menu3_click), 0, "", null);
                break;
            case R.id.id_indicator_four:
                mViewPager.setCurrentItem(3, false);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_menu_menu4_click), 0, "", null);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPageSelected(int arg0) {
        resetOtherTabs();
        titleTv.setVisibility(View.VISIBLE);
        switch (arg0) {
            case 0:
                toolbar.setVisibility(View.VISIBLE);
                three.setTextColor(getResources().getColor(R.color.renhe_theme_color));
                three.setCompoundDrawables(null, tab_icon_feed_on, null, null);
                diytitle.setVisibility(View.VISIBLE);
                titleTv.setText(getString(R.string.tab_room));
                titleTv.setVisibility(View.GONE);
                break;
            case 1:
                toolbar.setVisibility(View.VISIBLE);
                one.setTextColor(getResources().getColor(R.color.renhe_theme_color));
                one.setCompoundDrawables(null, tab_icon_im_on, null, null);
                diytitle.setVisibility(View.VISIBLE);
                titleTv.setText(getString(R.string.tab_dialog));
                titleTv.setVisibility(View.GONE);
                break;
            case 2:
                toolbar.setVisibility(View.VISIBLE);
                two.setTextColor(getResources().getColor(R.color.renhe_theme_color));
                two.setCompoundDrawables(null, tab_icon_search_on, null, null);
                diytitle.setVisibility(View.GONE);
                titleTv.setVisibility(View.VISIBLE);
                titleTv.setText(getString(R.string.tab_maillist));
                break;
            case 3:
                toolbar.setVisibility(View.GONE);
                four.setTextColor(getResources().getColor(R.color.renhe_theme_color));
                four.setCompoundDrawables(null, tab_icon_me_on, null, null);
                diytitle.setVisibility(View.GONE);
                titleTv.setVisibility(View.VISIBLE);
                titleTv.setText(getString(R.string.tab_me));
                break;
        }

        scanBtn = arg0 + 1;
    }

    private void resetOtherTabs() {
        one.setCompoundDrawables(null, tab_icon_im, null, null);
        two.setCompoundDrawables(null, tab_icon_search, null, null);
        three.setCompoundDrawables(null, tab_icon_feed, null, null);
        four.setCompoundDrawables(null, tab_icon_me, null, null);

        one.setTextColor(getResources().getColor(R.color.main_tab_item_normal_color));
        two.setTextColor(getResources().getColor(R.color.main_tab_item_normal_color));
        three.setTextColor(getResources().getColor(R.color.main_tab_item_normal_color));
        four.setTextColor(getResources().getColor(R.color.main_tab_item_normal_color));
    }

    private void initListener() {
        // 拍名片
        scanImgBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabMainFragmentActivity.this, OcrActivityCamera.class);
                startActivity(intent);
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                statisticsMap.put("type", "2");
                MobclickAgent.onEvent(TabMainFragmentActivity.this, "tabmain_ocr_taken_type2");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu1_scan_card_click), 0, "", statisticsMap);
            }
        });

        // 搜索
        searchEdt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TabMainFragmentActivity.this, SearchForContactsActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                MobclickAgent.onEvent(TabMainFragmentActivity.this, "search_index_click");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu1_search_click), 0, "", null);
            }
        });

        titleTv.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (titleTv.getText().toString().trim().equalsIgnoreCase(getString(R.string.tab_room))
                                || titleTv.getText().toString().trim().equalsIgnoreCase(getString(R.string.tab_dialog))
                                || titleTv.getText().toString().trim().equalsIgnoreCase(getString(R.string.tab_maillist))) {
                            // 如果第二次点击 距离第一次点击时间过长 那么将第二次点击看为第一次点击
                            if (firstClick != 0 && System.currentTimeMillis() - firstClick > 300) {
                                count = 0;
                            }
                            count++;
                            if (count == 1) {
                                firstClick = System.currentTimeMillis();
                            } else if (count == 2) {
                                lastClick = System.currentTimeMillis();
                                // 两次点击小于300ms 也就是连续点击
                                if (lastClick - firstClick < 300) {// 判断是否是执行了双击事件
                                    if (titleTv.getText().toString().trim().equalsIgnoreCase(getString(R.string.tab_dialog)))
                                        sendBroadcast(new Intent(CONVERSATION_ACTIONBAR_DOUBLE_TAP_ACTION));// 通知对话列表滑动到顶部
                                    else if (titleTv.getText().toString().trim().equalsIgnoreCase(getString(R.string.tab_maillist)))
                                        sendBroadcast(new Intent(CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION));// 通知通讯录列表滑动到顶部
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onDestroy() {
        // 注销IM广播接收器
        if (mReceiver != null && mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mReceiver);
        }
        RenheIMUtil.dismissProgressDialog(); // activity出现异常退出后如果对话框正在显示，需要关闭
        // 注销未读提醒角标广播
        if (tabUnreadIconReceiver != null) {
            unregisterReceiver(tabUnreadIconReceiver);
        }
        if (null != guideDoubleTapReceiver) {
            unregisterReceiver(guideDoubleTapReceiver);
        }
        if (null != content)
            this.getContentResolver().unregisterContentObserver(content);
        super.onDestroy();
    }

    @Override
    public void onSuccess(int type, Object result) {
        TaskManager.getInstance().removeTask(type);
        if (null != result) {
            if (result instanceof MyModuleNotice.MyModuleNoticeResponse) {//我的页面，完善资料是否需要显示小红点
                MyModuleNotice.MyModuleNoticeResponse response = (MyModuleNotice.MyModuleNoticeResponse) result;
                //获取完善资料状态 并判断是否在我（tab）上出现小红点 并发送广播通知Tab(我)页面 完善资料选项
                if (response.getIsPerfectInfo()) {
                    if (data_complete.getVisibility() == View.GONE) {
                        data_complete.setVisibility(View.VISIBLE);//让小红点我（Tab）书签上小红点出现
                    }
                    Intent intent = new Intent();
                    intent.setAction(TabMainFragmentActivity.COMPLETE_RECEIVER_ACTION);
                    intent.putExtra("dataComplete", "dataComplete");
                    sendBroadcast(intent);
                }
            } else if (result instanceof HeliaoNotice.NewFriendNearByResponse) {
                HeliaoNotice.NewFriendNearByResponse response = (HeliaoNotice.NewFriendNearByResponse) result;
                if (response.getResponseCode() == HeliaoNotice.NewFriendNearByResponse.ResultCode.HASNEW) {//时间符合，提示新人脉
                    if (newContacts.getVisibility() != View.VISIBLE) {
                        search_newicon.setVisibility(View.VISIBLE);
                    } else {
                        search_newicon.setVisibility(View.GONE);
                    }
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.NEARBY_HAS_NEW, true, true);
                }
            }
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

    // 适配器
    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mTabs = new ArrayList<Fragment>();

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> _mTabs) {
            super(fm);
            this.mTabs = _mTabs;
        }

        @Override
        public Fragment getItem(int arg0) {
            return mTabs.get(arg0);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }
    }

    /**
     * 使用LocalBroadcastManager注册registerReceiver，监听悟空的会话、消息相关事件
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_KIKOUT_DIALOG_ACTION);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);

        guideDoubleTapReceiver = new GuideDoubleTapReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTIONBAR_DOUBLE_TAP_ACTION);
        filter2.addAction(CANCLE_ACTIONBAR_DOUBLE_TAP_ACTION);
        registerReceiver(guideDoubleTapReceiver, filter2);
    }

    private void handleKickOutEvent(String tiker) {
        RenheIMUtil.showAlertDialog(this, tiker, new RenheIMUtil.DialogCallback() {
            @Override
            public void onPositive() {
            }

            @Override
            public void onCancle() {
                new LogoutUtil(TabMainFragmentActivity.this, rootRl).closeLogin(false);
            }
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    class TabUnreadIconReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(TAB_ICON_UNREAD_RECEIVER_ACTION)) {
                switch (arg1.getExtras().getInt(TabMainFragmentActivity.TAB_FLAG))
                // 1,2,3,4分别表示第几个tab
                {
                    case 1:
                        if (arg1.getIntExtra(TAB_ICON_CONVERSATION_UNREAD_NUM, 0) > 0) {
                            newDialogue.setVisibility(View.VISIBLE);
                            if (arg1.getIntExtra(TAB_ICON_CONVERSATION_UNREAD_NUM, 0) > 99) {
                                newDialogue.setTextSize(TAB_ICON_NUM_TOO_LARGE_TEXTSIZE);
                                newDialogue.setText(TAB_ICON_NUM_TOO_LARGE);
                            } else {
                                newDialogue.setTextSize(TAB_ICON_NUM_NORMAL_TEXTSIZE);
                                newDialogue.setText(arg1.getIntExtra(TAB_ICON_CONVERSATION_UNREAD_NUM, 0) + "");
                            }
                        } else {
                            newDialogue.setVisibility(View.GONE);
                            newDialogue.setText(0 + "");
                        }
                        break;
                    case 2:
                        handleNewFriendCount(arg1.getIntExtra(TAB_ICON_SEARCH_UNREAD_NUM, 0));
                        break;
                    case 3:
                        if (arg1.getIntExtra(TAB_ICON_RENMAIQUAN_UNREAD_NUM, 0) > 0) {
                            room_newicon.setVisibility(View.GONE);
                            newRoom.setVisibility(View.VISIBLE);
                            if (arg1.getIntExtra(TAB_ICON_RENMAIQUAN_UNREAD_NUM, 0) > 99) {
                                newRoom.setTextSize(TAB_ICON_NUM_TOO_LARGE_TEXTSIZE);
                                newRoom.setText(TAB_ICON_NUM_TOO_LARGE);
                            } else {
                                newRoom.setTextSize(TAB_ICON_NUM_NORMAL_TEXTSIZE);
                                newRoom.setText(arg1.getIntExtra(TAB_ICON_RENMAIQUAN_UNREAD_NUM, 0) + "");
                            }
                        } else {
                            // -1 表示人麦圈刷新通知红点消失
                            if (arg1.getIntExtra(TAB_ICON_RENMAIQUAN_UNREAD_NUM, 0) == -1) {
                                if (room_newicon.getVisibility() == View.VISIBLE) {
                                    room_newicon.setVisibility(View.GONE);
                                }
                            } else {
                                room_newicon.setVisibility(View.GONE);
                                newRoom.setVisibility(View.GONE);
                                newRoom.setText(0 + "");
                            }
                        }
                        break;
                    case 4:
                        if (arg1.getIntExtra(TAB_ICON_ME_UNREAD_NUM, 0) > 0) {
                            newMynumb.setVisibility(View.VISIBLE);
                            if (arg1.getIntExtra(TAB_ICON_ME_UNREAD_NUM, 0) > 99) {
                                newMynumb.setTextSize(TAB_ICON_NUM_TOO_LARGE_TEXTSIZE);
                                newMynumb.setText(TAB_ICON_NUM_TOO_LARGE);
                            } else {
                                newMynumb.setTextSize(TAB_ICON_NUM_NORMAL_TEXTSIZE);
                                newMynumb.setText(arg1.getIntExtra(TAB_ICON_ME_UNREAD_NUM, 0) + "");
                            }
                        } else {
                            newMynumb.setVisibility(View.GONE);
                            newMynumb.setText(0 + "");
                        }
                        //接收来自我（Tab）页面点击完善资料选项的发送的广播
                        String hideComplete = arg1.getExtras().getString("hideComplete");
                        boolean showComplete = arg1.getExtras().getBoolean("showComplete", false);
                        if (!TextUtils.isEmpty(hideComplete)) {
                            if (data_complete.getVisibility() == View.VISIBLE) {
                                data_complete.setVisibility(View.GONE);
                            }
                        } else if (showComplete) {
                            data_complete.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            } else if (arg1.getAction().equals(Constants.BroadCastAction.NEW_FRIEND_ACTION)) {//收到新的好友推送，发送广播，通知主页面调用获取新的好友数量的接口
                new getTabNewInfoTask().executeOnExecutor(Executors.newCachedThreadPool(),
                        RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId());
//                        "" + maxDate);
            }
        }

    }

    // 返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (logoutFlag) {
                    exitApp(this);
                } else {
                    ToastUtil.showToast(this, "请再点击一次退出程序");
                    logoutFlag = true;
                    handler.postDelayed(run, 2000);
                }
            } catch (Exception e) {
            }
            return true;
        }
        return super.onKeyDown(keyCode, event); // 最后，一定要做完以后返回
    }

    public static void exitApp(Context context) {
        AsyncImageLoader.getInstance().clearCache();
        if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                .getBoolean("clearcache", false)) {
            CacheManager.getInstance().populateData(context).clearCache(RenheApplication.getInstance().getUserInfo().getEmail(),
                    true);
        }
        RenheApplication.getInstance().exit();
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {
        @Override
        public void run() {
            logoutFlag = false;
        }
    };

    /**
     * @author chan
     * @createtime 2014-11-4
     * @功能说明 获取Tab下面的红点或数字
     */
    class getTabNewInfoTask extends AsyncTask<String, Void, TabHasNewBean> {

        @Override
        protected TabHasNewBean doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("bundle", DeviceUitl.getDeviceInfo());
            try {
                TabHasNewBean mb = (TabHasNewBean) HttpUtil.doHttpRequest(Constants.Http.HAS_NEW_NOTIFY, reqParams,
                        TabHasNewBean.class, TabMainFragmentActivity.this);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TabHasNewBean result) {
            super.onPostExecute(result);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        // 获取上次保存的数据，与这次的比对，如果相等则不更新
                        int newNotify = result.getHasNew();
                        int notifyCount = result.getNotifyCount();
                        String senderUserface = result.getSenderUserface();
                        if (notifyCount > 0) {
                            RenheApplication.getInstance().getUserEditor().putString(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_USERFACE, senderUserface);
                            RenheApplication.getInstance().getUserEditor().putInt(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_COUNT, notifyCount);
                            RenheApplication.getInstance().getUserEditor().commit();
                            //发送通知
                            Intent unReadintent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_UNREAD_NOTICE);
                            sendBroadcast(unReadintent);
                        }
                        if (newNotify == 1 && notifyCount > 0) {
                            room_newicon.setVisibility(View.GONE);
                            newRoom.setText("" + notifyCount);
                            newRoom.setVisibility(View.VISIBLE);
                        } else if (newNotify == 1 && notifyCount < 1) {
                            // 发送广播，通知人脉推荐界面更新动态
                            room_newicon.setVisibility(View.VISIBLE);
                            newRoom.setVisibility(View.GONE);
                        } else if (newNotify == 0 && notifyCount > 0) {
                            newRoom.setText("" + notifyCount);
                            room_newicon.setVisibility(View.GONE);
                            newRoom.setVisibility(View.VISIBLE);
                        } else {
                            room_newicon.setVisibility(View.GONE);
                            newRoom.setVisibility(View.GONE);
                        }
                        //人脉里新的好友数量
                        int newFriendCount = result.getNewfriendcount();
//                        if (newFriendCount > 0) {
                        handleNewFriendCount(newFriendCount);
                        // 通讯录tab数字角标、通讯录列表"新的好友"角标的显示
                        Intent intent = new Intent(Constants.BroadCastAction.NEWFRIENDS_COUNT);
                        intent.putExtra("newFri_numb", newFriendCount);
                        sendBroadcast(intent);
                        break;
                    default:
                        break;
                }
            } else {
                return;
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    /**
     * 处理信鸽notification点击派发逻辑
     */
    private void handleXinGeNotify(Intent intent) {
        int xgNotifyType = intent.getIntExtra(Constants.XING_NOTIFY_TYPE, 0);
        switch (xgNotifyType) {
            case Constants.XINGE_NOTIFY_TYPE.INNERMSG_PUSH:
                one.setSelected(true);
                mViewPager.setCurrentItem(1);// 默认页面
                break;
            case Constants.XINGE_NOTIFY_TYPE.MESSAGENUM_PUSH:
                handleMsgNoticeNotify(intent);
                one.setSelected(true);
                mViewPager.setCurrentItem(0);// 默认页面
                break;
            case Constants.XINGE_NOTIFY_TYPE.VIPCHECKPASS_PUSH:
                break;
            case Constants.XINGE_NOTIFY_TYPE.NOTICE_MESSAGENUM_PUSH:
                handleMsgNoticeNotify(intent);
                one.setSelected(true);
                mViewPager.setCurrentItem(0);// 默认页面
                break;
            case Constants.XINGE_NOTIFY_TYPE.NOTICE_NEW_FRIEND_PUSH:
                Intent newFriendIntent = new Intent();
                newFriendIntent.setClass(this, NewFriendsAct.class);
                startActivity(newFriendIntent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                Intent newFriendBroadcastIntent = new Intent(Constants.XING_NOTIFY_BROADCAST_ACTION);
                newFriendBroadcastIntent.putExtra(Constants.XING_NOTIFY_TYPE, Constants.XINGE_NOTIFY_TYPE.NOTICE_NEW_FRIEND_PUSH);
                sendBroadcast(newFriendBroadcastIntent);
                one.setSelected(true);
                mViewPager.setCurrentItem(1);// 默认页面
                break;
//		case Constants.XINGE_NOTIFY_TYPE.NOTICE_NEW_INNERMSG_PUSH:
//			Intent innerMsgintent = new Intent(this, InnerMsgConversationActivity.class);
//			startActivity(innerMsgintent);
//			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
//			Intent innerMsgBroadcastIntent = new Intent(Constants.XING_NOTIFY_BROADCAST_ACTION);
//			innerMsgBroadcastIntent.putExtra(Constants.XING_NOTIFY_TYPE, Constants.XINGE_NOTIFY_TYPE.NOTICE_NEW_INNERMSG_PUSH);
//			sendBroadcast(innerMsgBroadcastIntent);
//			one.setSelected(true);
//			mViewPager.setCurrentItem(1);// 默认页面
//			break;
            case Constants.XINGE_NOTIFY_TYPE.NOTICE_SYSTEMMSG_PUSH:
                Intent sysMsgIntent = new Intent();
                sysMsgIntent.setClass(this, SystemMsgActivity.class);
                startActivity(sysMsgIntent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                Intent sysMsgBroadcastIntent = new Intent(Constants.XING_NOTIFY_BROADCAST_ACTION);
                sysMsgBroadcastIntent.putExtra(Constants.XING_NOTIFY_TYPE, Constants.XINGE_NOTIFY_TYPE.NOTICE_SYSTEMMSG_PUSH);
                sendBroadcast(sysMsgBroadcastIntent);
                one.setSelected(true);
                mViewPager.setCurrentItem(1);// 默认页面
                break;
            case Constants.XINGE_NOTIFY_TYPE.NOTICE_TOUTIAO_PUSH:
                Intent touTiaoIntent = new Intent();
                touTiaoIntent.setClass(this, TouTiaoActivity.class);
                startActivity(touTiaoIntent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                Intent touTiaoBroadcastIntent = new Intent(Constants.XING_NOTIFY_BROADCAST_ACTION);
                touTiaoBroadcastIntent.putExtra(Constants.XING_NOTIFY_TYPE, Constants.XINGE_NOTIFY_TYPE.NOTICE_TOUTIAO_PUSH);
                sendBroadcast(touTiaoBroadcastIntent);
                one.setSelected(true);
                mViewPager.setCurrentItem(1);// 默认页面
                break;
            default:
                break;
        }
    }

    private void handleMsgNoticeNotify(Intent intent) {
        int noticeType = intent.getExtras().getInt("type");
        String objectId = intent.getExtras().getString("objectId");
        String sid = intent.getExtras().getString("sid");
        boolean isFromNoticeList = intent.getExtras().getBoolean("isFromNoticeList", true);
        int loadType = intent.getExtras().getInt("loadType");
        Bundle bundle = new Bundle();
        bundle.putString("sid", sid);
        bundle.putInt("type", noticeType);
        bundle.putString("objectId", objectId);
        bundle.putBoolean("isFromNoticeList", isFromNoticeList);
        bundle.putInt("loadType", loadType);
        Intent msgIntent = new Intent(this, TwitterShowMessageBoardActivity.class);
        msgIntent.putExtras(bundle);
        startActivity(msgIntent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void handleBrowseData(String browserData) {
        TransferUrl2Drawable transferUrl2Drawable = new TransferUrl2Drawable(this);
        if (browserData.contains("heliaoapp")) {
            browserData.replace("heliaoapp", "http");
        }
        if (browserData.contains(Constants.BrowserToHeliaoUrl.RENHE_PROFILE)) {//跳转到用户档案
            String sid = transferUrl2Drawable.getUrlPramNameAndValue(browserData).get("sid");
            Intent intent2 = new Intent(this, MyHomeArchivesActivity.class);
            intent2.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, sid);
            startActivity(intent2);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else if (browserData.contains(Constants.BrowserToHeliaoUrl.RENHE_CIRCLE)) {
            String circleId = transferUrl2Drawable.getUrlPramNameAndValue(browserData).get("id");
            //跳转圈子详情
            Intent intent2 = new Intent();
            intent2.putExtra("circleId", circleId);
            intent2.setClass(this, ActivityCircleDetail.class);
            startActivity(intent2);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else if (browserData.contains(Constants.BrowserToHeliaoUrl.RENHE_RENMAIQUAN)) {
            String msgId = transferUrl2Drawable.getUrlPramNameAndValue(browserData).get("id");
            //跳转人脉圈详情
            Intent intent2 = new Intent(this, TwitterShowMessageBoardActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("sid", RenheApplication.getInstance().getUserInfo().getSid());
            bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
            bundle.putString("objectId", msgId);
            bundle.putBoolean("isFromNoticeList", true);
            bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
            intent2.putExtras(bundle);
            startActivity(intent2);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 初始化脉可寻
     */
    private void initMaiKeXun() {
        //初始化server
        maiKeXunServer = RenheApplication.getInstance().getMaiKeXunServer();
        if (null == maiKeXunServer)
            return;
        //判断是否通过验证
        isMaiKeXunInit = maiKeXunServer.isAuth();
        if (isMaiKeXunInit) {
            Logger.d("脉可寻已经验证！");
            registerOcrListener();
        } else {
            Logger.e("脉可寻未验证！");
            authMaiKeXunAccount();
        }
        //设置名片保存的路径
        maiKeXunServer.setSdcardPath(Constants.CACHE_PATH.MAI_KE_XUN_CARD_PATH);
    }

    /**
     * 验证账户
     */
    private void authMaiKeXunAccount() {
        if (mkxAuthCount <= 10) {
            if (!isMaiKeXunInit) {
                maiKeXunServer.auth(Constants.MAI_KE_XUN.KEY, Constants.MAI_KE_XUN.SECRET,
                        RenheApplication.getInstance().getUserInfo().getSid(), new OcrBackAuth() {
                            @Override
                            public void onBack(int code, String errInfo) {
                                if (code == OcrErrorCode.CODE_SUCCESS) {
                                    isMaiKeXunInit = maiKeXunServer.isAuth();
                                    RenheApplication.getInstance().setIsMaiKeXunInit(isMaiKeXunInit);
                                    if (isMaiKeXunInit) {
                                        Logger.d("验证成功!！");
                                        registerOcrListener();
                                    } else {
                                        Logger.e("验证失败！");
                                        authMaiKeXunAccount();
                                    }
                                } else {
                                    Logger.e("脉可寻验证账户时的错误信息=====》 " + errInfo);
                                    authMaiKeXunAccount();
                                }
                            }
                        });
            } else {
                Logger.w("已经验证过了，不能重复验证！");
            }
            mkxAuthCount++;
        } else {
            ToastUtil.showToast(TabMainFragmentActivity.this, R.string.ocr_mkx_auth_error);
        }
    }

    /**
     * 注册上传名片
     */
    private void registerOcrListener() {
        if (isMaiKeXunInit) {
            maiKeXunServer.setUploadListener(new OcrBackUpload() {
                @Override
                public void onBack(int code, String errInfo, String uuid, int status) {
                    OcrLocalCard ocrLocalCard = OcrLocalCardUtil.instanceNewCardAndSave(uuid);
                    switch (status) {
                        case STATUS_ERROR://上传出错
                            if (uploadTimes <= 5) {//由于重新上传与拍照上传都是调用统一上传函数，因此需要限制重传次数，避免上传图片时产生无限递归
                                uploadTimes++;
                                maiKeXunServer.uploadImage(uuid);//重新上传
                            } else {//重新上传的次数达到上限时，不再上传，保存不能上传图片的uuid
                                uploadTimes = 0;
                                OcrLocalCardUtil.delete(uuid);
//                                upLoadFails.add(uuid);
                                Logger.e("上传失败，等待网络通畅时再重新上传");
                                ToastUtil.showToast(TabMainFragmentActivity.this, R.string.ocr_card_upload_error);
                            }
                            break;
                        case STATUS_START://开始上传
                            isNeedToOcrCardList = true;
                            EventBus.getDefault().post(new FinishOcrActivityEvent());//通过eventbus通知名片列表以及详情页finish
                            OcrLocalCardUtil.save(ocrLocalCard);//保存到数据库
                            break;
                        case STATUS_SUCESS://上传成功
                            Logger.d("上传成功！");
                            uploadOcrCard(uuid);
                            break;
                    }
                }
            });
        } else {
            Logger.e("还未验证账户，请验证账户再获取数据");
        }
    }

    /**
     * 上传名片uuid
     *
     * @param uuid 需要上传名片的uuid
     */
    private void uploadOcrCard(final String uuid) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("uuid", uuid);
        reqParams.put("type", 1);//1.表示上传，2.表示删除
        OkHttpClientManager.postAsyn(Constants.Http.UPLOADORDELETE_OCR_CARD, reqParams, Object.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(Object response) {
                Logger.d("名片上传至服务器成功！uuid是==》 " + uuid);
                EventBus.getDefault().post(new RefreshNewFriendListEvent());//通过eventbus通知新的好友界面加载新数据
            }
        });
    }

    class GuideDoubleTapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTIONBAR_DOUBLE_TAP_ACTION)) {
                titleTv.setVisibility(View.GONE);
            } else if (arg1.getAction().equals(CANCLE_ACTIONBAR_DOUBLE_TAP_ACTION)) {
                titleTv.setVisibility(View.VISIBLE);
            }
        }

    }

    class ShowKikOutReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(SHOW_KIKOUT_DIALOG_ACTION)) {
                String tiker = intent.getStringExtra("tiker");
                if (TextUtils.isEmpty(tiker))
                    tiker = getString(R.string.account_kikout);
                if (SHOW_KIKOUT_DIALOG_ACTION.equals(intent.getAction())) {
                    handleKickOutEvent(tiker); // 放到application监听
                }
            }
        }

    }

    /**
     * 获取服务端的配置
     */
    private class GetConfigTask extends AsyncTask<String, Void, TextSize> {
        @Override
        protected TextSize doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            try {
                TextSize ts = (TextSize) HttpUtil.doHttpRequest(Constants.Http.GET_CONFIG, reqParams, TextSize.class,
                        TabMainFragmentActivity.this);
                return ts;
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(TextSize result) {
            super.onPostExecute(result);
            if (null != result && result.getState() == 1) {
                textSize = TextSize.getInstance();
                textSize.setRenMaiQuanContentSize(result.getRenMaiQuanContentSize());
                textSize.setRenMaiQuanCommentSize(result.getRenMaiQuanCommentSize());
                textSize.setCircleTitleSize(result.getCircleTitleSize());
                textSize.setCircleDescriptionSize(result.getCircleDescriptionSize());
                textSize.setSearchPlaceholder(result.getSearchPlaceholder());
                textSize.setMember(result.getMember());
                textSize.setOrder(result.getOrder());

                //上传日志时间写到sharePerf
                SharedPreferences sp = TabMainFragmentActivity.this.getSharedPreferences("uploadlog_interval", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("interval_length", result.getUploadLogTimeInterval());
                editor.commit();

                //由服务端控制左上角拍名片的图标显示哪个
//                if (result.getScanImgIndex() == 2) {
//                    scanImgIndex = 2;
//                    scanImgBtn.setImageResource(R.drawable.ic_action_markman_scan_selector_type2);
//                } else {
//                    scanImgBtn.setImageResource(R.drawable.ic_action_markman_scan_selector_type1);
//                }
            }
        }
    }

    /**
     * 获取常用联系人
     */
    class getCommonContactListTask extends AsyncTask<String, Void, CommonContactList> {

        @Override
        protected CommonContactList doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            try {
                return (CommonContactList) HttpUtil.doHttpRequest(Constants.Http.GET_COMMON_CONTACT_LIST, reqParams,
                        CommonContactList.class, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(CommonContactList result) {
            super.onPostExecute(result);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        CommonContactList.MemberSidList[] memberSidList = result.getMemberSidList();
                        //						commonSids = new ArrayList<>();
                        String ids = "";
                        for (CommonContactList.MemberSidList aMemberSidList : memberSidList) {
                            //							commonSids.add(aMemberSidList.getSid());
                            ids += aMemberSidList.getSid() + ";";
                        }
                        if (!TextUtils.isEmpty(ids) && ids.endsWith(";")) {
                            ids = ids.substring(0, ids.length() - 1);
                        }
                        //存sharePf下面
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_OFEN_USERD, ids, true);
//                        sendBroadcast(new Intent(Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handleNewFriendCount(int count) {
        if (count > 0) {
            SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.NEW_FRIEND_UNREAD_COUNT, count, true);
            if (search_newicon.getVisibility() == View.VISIBLE) {
                search_newicon.setVisibility(View.GONE);
            }
            newContacts.setVisibility(View.VISIBLE);
            if (count > 99) {
                newContacts.setTextSize(TAB_ICON_NUM_TOO_LARGE_TEXTSIZE);
                newContacts.setText(TAB_ICON_NUM_TOO_LARGE);
            } else {
                newContacts.setTextSize(TAB_ICON_NUM_NORMAL_TEXTSIZE);
                newContacts.setText(count + "");
            }
        } else {
            SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.NEW_FRIEND_UNREAD_COUNT, 0, true);
            newContacts.setVisibility(View.GONE);
            newContacts.setText("");
            initTabView();
        }
    }

    /**
     * 将cookie保存到webview中，实现 sid，adSid保存到cookie中
     */
    public void syncCookie(String[] id) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(this.getApplicationContext());
            }
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            //循环 setCookie，系统会自动检查之前这个.renhe.cn有没有cookie，如果有，就替换掉，如果没有，就直接写入
            for (String cookie : id) {
                StringBuilder sbCookie = new StringBuilder();
                String heliao = cookie.endsWith(sid) ? "heliao_member_sid" : "heliao_member_token";
                sbCookie.append(String.format("%s=%s", heliao, cookie));
                sbCookie.append(String.format(";domain=%s", ".renhe.cn"));
                sbCookie.append(";path=/");
                cookieManager.setCookie(".renhe.cn", sbCookie.toString());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush();
            } else {
                CookieSyncManager.getInstance().sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 我的页面，完善资料是否需要显示小红点
     */
    private void getMyModuleCompleteArchiveNotice() {
        if (TaskManager.getInstance().exist(ID_TASK_MYMODULE_NOTICE)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_MYMODULE_NOTICE);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.myModuleCompleteArchiveNotice(ID_TASK_MYMODULE_NOTICE);
    }

    /**
     * 和聊应用内所有的消息提醒
     */
    private void getHeLiaoNotice() {
        if (TaskManager.getInstance().exist(ID_TASK_HELIAO_NOTICE)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_HELIAO_NOTICE);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.getHeLiaoNotice(ID_TASK_HELIAO_NOTICE);
    }

    /**
     * 初始化tab的小红点/数字提醒
     */
    private void initTabView() {
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.NEARBY_HAS_NEW, false, true)) {
            if (newContacts.getVisibility() != View.VISIBLE) {
                search_newicon.setVisibility(View.VISIBLE);
            } else {
                search_newicon.setVisibility(View.GONE);
            }
        } else {
            search_newicon.setVisibility(View.GONE);
        }
    }
}
