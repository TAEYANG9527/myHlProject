package com.itcalf.renhe;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.Environment;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.wukong.AuthConstants;
import com.alibaba.wukong.WKManager;
import com.alibaba.wukong.auth.AuthInfo;
import com.alibaba.wukong.auth.AuthService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.MessageService;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.command.IMessageBoardCommand;
import com.itcalf.renhe.command.IPhoneCommand;
import com.itcalf.renhe.command.IProfileCommand;
import com.itcalf.renhe.command.IUserCommand;
import com.itcalf.renhe.command.impl.ContactCommandImpl;
import com.itcalf.renhe.command.impl.MessageBoardCommandImpl;
import com.itcalf.renhe.command.impl.PhoneCommandImpl;
import com.itcalf.renhe.command.impl.ProfileCommandImpl;
import com.itcalf.renhe.command.impl.UserCommandImpl;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity;
import com.itcalf.renhe.context.portal.AddMemberDeviceTokenTask;
import com.itcalf.renhe.context.portal.AddNewDeviceTokenTask;
import com.itcalf.renhe.context.wukong.im.kit.MessageSenderImpl;
import com.itcalf.renhe.database.sqlite.TablesConstant;
import com.itcalf.renhe.dto.FollowState;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.receiver.ChatMessageListener;
import com.itcalf.renhe.receiver.IMPushReceiver;
import com.itcalf.renhe.utils.DownloadServiceTool;
import com.itcalf.renhe.utils.ManifestUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.widget.emojitextview.EmotionsDB;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.litepal.LitePalApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import cn.ocrsdk.uploadSdk.OcrServer;

public class RenheApplication extends GlobalContext {
    public static final String PREFS_NAME = "PrefsFile";
    // 该变量为站内信删除后加入id,回退列表时回调，动态删除
    private UserInfo mUserInfo;
    private IUserCommand mUser;
    private IMessageBoardCommand mMessageBoard;
    private IProfileCommand mProfile;
    private IContactCommand mContact;
    private IPhoneCommand mPhone;
    private FollowState mFollowState;// 人和网关注状态：服务中初始化，每次点击更多的时候后台更新
    public static RenheApplication renheApplication;
    private static final String LOG_NAME = "renhe_android_log.TXT";
    private int accountType = 0;
    private int enterFound = 0;
    private int login = 0;
    private int newFriendsNumb = 0;
    //IM
    public long currentOpenId;
    public String currentNickName;

    //是否初始化人脉圈fragment信息
    private boolean initRMQFragment;
    //是否初始化聊天Fragment信息
    private boolean initIMFragment;
    //是否初始化人脉Fragment信息
    private boolean initContactFragment;
    //是否初始化我Fragment信息
    private boolean initMyFragment;
    private String inChatOpenId = Constants.CURRENT_IS_NOT_IN_CHAT;
    private boolean isFromLoginIn = false;
    //IM广播接收器
    private LocalBroadcastManager mLocalBroadcastManager;
    private IMPushReceiver imPushReceiver;
    private int logOut = 0;
    public static final int IMAGE_CACHE_MAX_SIZE_IN_BYTE = 1024 * 1024 * 15;
    /******
     * 客户端IMEI
     ***/
    public String DeviceIMEI = "";
    /***
     * 客户端IMSI
     ****/
    public String DeviceIMSI = "";
    //文件下载类，目前供IM发送文件使用
    public DownloadServiceTool downloadService;

    /*应用公用SharedPreferences*/
    private SharedPreferences hlSharedPreferences;
    private SharedPreferences.Editor hlEditor;
    /*用户私有SharedPreferences*/
    private SharedPreferences userSharedPreferences;
    private SharedPreferences.Editor userEditor;

    private OcrServer maiKeXunServer;//脉可寻 上传名片server
    private boolean isMaiKeXunInit;//账户是否初始化（key，secret）
    private String versionName;//和聊版本号

    @Override
    public void onCreate() {
        super.onCreate();
        if (renheApplication == null) {
            renheApplication = this;
        }
        com.orhanobut.logger.Logger.e("开启RenheApplication");
        //开启守护进程
//        Daemon.run(this, RenheService.class, Daemon.INTERVAL_ONE_MINUTE);
        //初始化数据库开源框架LitePal
        LitePalApplication.initialize(this);
        //初始化首页4个Fragment的加载状态
        initTabFragmentState();
        //初始化IMEngine
        IMEngine.launch(this);
        WKManager.setEnvironment(AuthConstants.Environment.ONLINE);
        IMEngine.setUserAvailable(true);
        //注册IM广播
        registerImReceiver();
        //自动登录一次的账户
        AuthInfo authInfo = AuthService.getInstance().latestAuthInfo();
        if (authInfo != null) {
            AuthService.getInstance().autoLogin(authInfo.getOpenId());
            currentOpenId = authInfo.getOpenId();
            currentNickName = authInfo.getNickname();
        }
        MessageSenderImpl.getInstance().init(this);//初始化IM工具包
        //收集crash日志
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        //友盟统计初始化
        MobclickAgent.openActivityDurationTrack(false);
        //Universal Imageloader初始化
        initImageLoader(getApplicationContext());
        //字体设置
        Constants.TypefaceParams(getApplicationContext());
        // default PRETTY LOGGER or use just init()
        Logger.init(Constants.HL_TAG).hideThreadInfo().setLogLevel(Constants.DEBUG_MODE ? LogLevel.FULL : LogLevel.NONE);
        //初始化脉可寻server
        maiKeXunServer = OcrServer.getServer(this);
        //判断是否通过验证
        isMaiKeXunInit = maiKeXunServer.isAuth();
        // 初始化图片加载
        BitmapLoader.newInstance(this, getImagePath());
        // 检查表情
        try {
            EmotionsDB.checkEmotions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //初始化SharePreference
        SharedPreferencesUtil.config(this);
        //初始化阿里云统计sdk
        initAlibabaSdk();
    }

    /**
     * 应用起来的时候初始化Fragment的状态(用户退出后登入)
     */
    public void initTabFragmentState() {
        //true表示初次加载
        setInitRMQFragment(true);
        setInitIMFragment(true);
        setInitContactFragment(true);
        setInitMyFragment(true);
    }

    /**
     * 初始化AlibabaSDK
     * * 启动移动推送包含两个步骤，首先你需要在应用中初始化AlibabaSDK。 然后通过AlibabaSDK去获得到移动推送服务实例，并初始化移动推送。
     * <p>
     * （为确保AlibabaSDK加载正常，请你在成功返回的回调方法中初始化移动推送。）
     */
    private void initAlibabaSdk() {
        if (Constants.DEBUG_MODE)
            AlibabaSDK.turnOnDebug();
        AlibabaSDK.setEnvironment(Environment.ONLINE);
         /* 【注意】建议您在Application中初始化OneSDK，以保证正常获取MANService*/

        // OneSDK的初始化方法之一
//        AlibabaSDK.asyncInit(getApplicationContext());
        AlibabaSDK.asyncInit(getApplicationContext(), new InitResultCallback() {

            @Override
            public void onSuccess() {
                Logger.w("init onesdk success");
                //alibabaSDK初始化成功后，初始化移动推送通道
                initCloudChannel(getApplicationContext());
            }

            @Override
            public void onFailure(int code, String message) {
                Logger.e("init onesdk failed,errorCode:" + code + " ," + message);
            }
        });
        /* 【注意】建议您在Application中初始化MAN，以保证正常获取MANService*/
        // 获取MAN服务
        MANService manService = MANServiceProvider.getService();
        // 若AndroidManifest.xml 中的 android:versionName 不能满足需求，可在此指定
        // 若在上述两个地方均没有设置appversion，上报的字段默认为null
        if (null != manService && null != manService.getMANAnalytics()) {
            if (Constants.DEBUG_MODE)
                // 打开调试日志
                manService.getMANAnalytics().turnOnDebug();
            // 打开调试日志，线上版本建议关闭
            // manService.getMANAnalytics().turnOnDebug();
            manService.getMANAnalytics().turnOffCrashHandler();
            // MAN初始化方法之一，从AndroidManifest.xml中获取appKey和appSecret初始化
            manService.getMANAnalytics().init(this, getApplicationContext());
            // MAN另一初始化方法，手动指定appKey和appSecret
            // String appKey = "******";
            // String appSecret = "******";
            // manService.getMANAnalytics().init(this, getApplicationContext(), appKey, appSecret);
            // 若需要关闭 SDK 的自动异常捕获功能可进行如下操作,详见文档5.4
            // 设置渠道（用以标记该app的分发渠道名称），如果不关心可以不设置即不调用该接口，渠道设置将影响控制台【渠道分析】栏目的报表展现。如果文档3.3章节更能满足您渠道配置的需求，就不要调用此方法，按照3.3进行配置即可
            manService.getMANAnalytics().setChannel(ManifestUtil.getChannel(getApplicationContext()));
            manService.getMANAnalytics().setAppVersion(getString(R.string.versionname));
        }
        // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
        MiPushRegister.register(getApplicationContext(), Constants.MI_PUSH.MI_PUSH_APP_ID, Constants.MI_PUSH.MI_PUSH_APP_KEY);
        // 注册方法会自动判断是否支持华为系统推送，如不支持会跳过注册。
        HuaWeiRegister.register(getApplicationContext());
    }

    /**
     * 初始化移动推送通道
     *
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        final CloudPushService cloudPushService = AlibabaSDK.getService(CloudPushService.class);
        if (cloudPushService != null) {
            cloudPushService.setCrashHandleEnabled(false);
            if (Constants.DEBUG_MODE)
                cloudPushService.setLogLevel(CloudPushService.DEBUG);
            cloudPushService.register(applicationContext, new CommonCallback() {

                @Override
                public void onSuccess() {
                    Logger.w("init cloudchannel success" + cloudPushService.getDeviceId());//启动正常确认方法,确认DeviceId获取正常: 在代码中使用 cloudPushService.getDeviceId() 获取deviceId，应该能够成功获取
                    com.itcalf.renhe.Constants.PUSH_DFVICE_TOKEN = cloudPushService.getDeviceId();
                    addDeviceToken();
                }

                @Override
                public void onFailed(String errorCode, String errorMessage) {
                    Logger.e("init cloudchannel fail" + "err:" + errorCode + " - message:" + errorMessage);
                }
            });
        } else {
            Logger.e("CloudPushService is null");
        }
    }

    private void addDeviceToken() {
        if (getLogin() == 1) {
            if (null != getUserInfo()) {
                String email = getUserInfo().getLoginAccountType();
                if (null != email) {
                    // 判断上次是否退出登入
                    SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
                    boolean islogined = false;
                    if (null != prefs) {
                        islogined = prefs.getBoolean("islogined", true);
                    }
                    if (islogined) {
                        PushUtil.registerPush(getUserInfo());
                        new AddMemberDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                    } else {
                        new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                    }
                } else {
                    new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                }
            } else {
                new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
            }
        }
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory().diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).defaultDisplayImageOptions(CacheManager.options)
                .memoryCache(new LRULimitedMemoryCache(IMAGE_CACHE_MAX_SIZE_IN_BYTE))
                //				.writeDebugLogs() // Remove for release app
                .build();
        // Initialize FragmentImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * 捕获错误信息的handler
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String info = null;
            ByteArrayOutputStream baos = null;
            PrintStream printStream = null;
            try {
                baos = new ByteArrayOutputStream();
                printStream = new PrintStream(baos);
                ex.printStackTrace(printStream);
                byte[] data = baos.toByteArray();
                info = new String(data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (printStream != null) {
                        printStream.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            writeErrorLog(info);
            ex.printStackTrace();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };

    /**
     * 向文件中写入错误信息
     *
     * @param info
     */

    protected void writeErrorLog(String info) {
        String logPath = Constants.CACHE_PATH.HL_CRASH_LOG_PATH;
        File dir = new File(logPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, getCurrentDateString() + LOG_NAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(info.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取当前日期
     *
     * @return
     */
    private static String getCurrentDateString() {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.getDefault());
        Date nowDate = new Date();
        result = sdf.format(nowDate);
        result = result.replaceAll(" ", "_");
        return result.trim();
    }

    public static RenheApplication getInstance() {
        return renheApplication;
    }

    public UserInfo getUserInfo() {
        if (null == mUserInfo) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            mUserInfo = new UserInfo();
            mUserInfo.setAdSId(settings.getString("adSId", ""));
            mUserInfo.setCompany(settings.getString("company", ""));
            mUserInfo.setEmail(settings.getString("email", ""));
            mUserInfo.setId(settings.getLong("id", 0));
            mUserInfo.setName(settings.getString("name", ""));
            mUserInfo.setSid(settings.getString("sid", ""));
            mUserInfo.setTitle(settings.getString("title", ""));
            mUserInfo.setLocation(settings.getString("location", ""));
            mUserInfo.setUserface(settings.getString("userface", ""));
            mUserInfo.setLoginAccountType(settings.getString(TablesConstant.USER_TABLE_COLUMN_LOGINACCOUNT, ""));
            mUserInfo.setAccountType(settings.getInt(TablesConstant.USER_TABLE_COLUMN_ACCOUNT_TYPE, 0));
            mUserInfo.setImId((int) settings.getLong(TablesConstant.USER_TABLE_COLUMN_IMOPENID, 0L));
            mUserInfo.setImValid(settings.getBoolean(TablesConstant.USER_TABLE_COLUMN_IMVALID, false));
            mUserInfo.setMobile(settings.getString(TablesConstant.USER_TABLE_COLUMN_MOBILE, ""));
            mUserInfo.setRealName(settings.getBoolean(TablesConstant.USER_TABLE_COLUMN_REALNAME, false));
            mUserInfo.setLogintime(settings.getString(TablesConstant.USER_TABLE_COLUMN_LOGINTIME, ""));
            mUserInfo.setRemember(settings.getBoolean(TablesConstant.USER_TABLE_COLUMN_REMEMBER, false));
            mUserInfo.setProv(settings.getInt(TablesConstant.USER_TABLE_COLUMN_PRO, 0));
        }
        return mUserInfo;
    }

    public void setUserInfo(UserInfo mUserInfo) {
        // this.mUserInfo = mUserInfo;
        setmUserInfo(mUserInfo);
        if (null != mUserInfo) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("name", mUserInfo.getName());
            editor.putString("adSId", mUserInfo.getAdSId());
            editor.putString("company", mUserInfo.getCompany());
            editor.putString("email", mUserInfo.getEmail());
            editor.putString("sid", mUserInfo.getSid());
            editor.putString("title", mUserInfo.getTitle());
            editor.putString("userface", mUserInfo.getUserface());
            editor.putString("location", mUserInfo.getLocation());
            editor.putLong("id", mUserInfo.getId());
            editor.putInt(TablesConstant.USER_TABLE_COLUMN_ACCOUNT_TYPE, mUserInfo.getAccountType());
            editor.putString(TablesConstant.USER_TABLE_COLUMN_LOGINACCOUNT, mUserInfo.getLoginAccountType());
            editor.putLong(TablesConstant.USER_TABLE_COLUMN_IMOPENID, mUserInfo.getId());
            editor.putBoolean(TablesConstant.USER_TABLE_COLUMN_IMVALID, mUserInfo.isImValid());
            editor.putString(TablesConstant.USER_TABLE_COLUMN_MOBILE, mUserInfo.getMobile());
            editor.putBoolean(TablesConstant.USER_TABLE_COLUMN_REALNAME, mUserInfo.isRealName());
            editor.putString(TablesConstant.USER_TABLE_COLUMN_LOGINTIME, mUserInfo.getLogintime());
            editor.putBoolean(TablesConstant.USER_TABLE_COLUMN_REMEMBER, mUserInfo.isRemember());
            editor.putInt(TablesConstant.USER_TABLE_COLUMN_PRO, mUserInfo.getProv());
            editor.commit();
        } else {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();
        }
    }

    public UserInfo getmUserInfo() {
        return mUserInfo;
    }

    public SearchCity currentCity;

    public SearchCity getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(SearchCity currentCity) {
        this.currentCity = currentCity;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public void setmUserInfo(UserInfo mUserInfo) {
        this.mUserInfo = mUserInfo;
    }

    public FollowState getFollowState() {

        if (null == mFollowState) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            mFollowState = new FollowState();
            mFollowState.setState(settings.getInt("follow", 1));
            mFollowState.setFollowState(settings.getInt("followState", 1));
            settings = null;
        }
        return mFollowState;
    }

    public void setFollowState(FollowState mFollowState) {
        this.mFollowState = mFollowState;
        if (null != mFollowState) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("follow", mFollowState.getState());
            editor.putInt("followState", mFollowState.getFollowState());
            editor.commit();
            editor = null;
            settings = null;
        } else {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();
            editor = null;
            settings = null;
        }
    }

    public IUserCommand getUserCommand() {
        if (null == mUser) {
            mUser = new UserCommandImpl(this);
        }
        return mUser;
    }

    public IMessageBoardCommand getMessageBoardCommand() {
        if (null == mMessageBoard) {
            mMessageBoard = new MessageBoardCommandImpl();
        }
        return mMessageBoard;
    }

    public IProfileCommand getProfileCommand() {
        if (null == mProfile) {
            mProfile = new ProfileCommandImpl();
        }
        return mProfile;
    }

    public IContactCommand getContactCommand() {
        if (null == mContact) {
            mContact = new ContactCommandImpl(this);
        }
        return mContact;
    }

    public IPhoneCommand getPhoneCommand() {
        if (null == mPhone) {
            mPhone = new PhoneCommandImpl();
        }
        return mPhone;
    }

    private List<Activity> activityList = new LinkedList<Activity>();

    // 添加Activity 到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 遍历所有Activity 并finish
    public void clearActivity() {
        for (Activity mactivity : activityList) {
            mactivity.finish();
        }
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public void finshPayActivity() {
        for (Activity mactivity : activityList) {
            if (mactivity instanceof ChoosePayWayActivity) {
                mactivity.finish();
            }
        }
    }

    public void exit() {
        // CacheManager.IMAGE_CACHE.clear();
        for (Activity activity : activityList) {
            activity.finish();
        }
        userSharedPreferences = null;
        userEditor = null;
        hlSharedPreferences = null;
        hlEditor = null;
        setEnterFound(0);
        setLogin(0);
        initTabFragmentState();
        setInChatOpenId(Constants.CURRENT_IS_NOT_IN_CHAT);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        // 如果想让应用退出后，不再接受推送通知，就解除下面的注释，因为他会杀死进程，监听推送的广播将失效
        // System.exit(0);
    }

    public int getEnterFound() {
        return enterFound;
    }

    public void setEnterFound(int enterFound) {
        this.enterFound = enterFound;
    }

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }

    public int getNewFriendsNumb() {
        return newFriendsNumb;
    }

    public void setNewFriendsNumb(int newFriendsNumb) {
        this.newFriendsNumb = newFriendsNumb;
    }

    public boolean isInitRMQFragment() {
        return initRMQFragment;
    }

    public void setInitRMQFragment(boolean initRMQFragment) {
        this.initRMQFragment = initRMQFragment;
    }

    public boolean isInitIMFragment() {
        return initIMFragment;
    }

    public void setInitIMFragment(boolean initIMFragment) {
        this.initIMFragment = initIMFragment;
    }

    public boolean isInitContactFragment() {
        return initContactFragment;
    }

    public void setInitContactFragment(boolean initContactFragment) {
        this.initContactFragment = initContactFragment;
    }

    public boolean isInitMyFragment() {
        return initMyFragment;
    }

    public void setInitMyFragment(boolean initMyFragment) {
        this.initMyFragment = initMyFragment;
    }

    public String getInChatOpenId() {
        return inChatOpenId;
    }

    public void setInChatOpenId(String inChatOpenId) {
        this.inChatOpenId = inChatOpenId;
    }

    public boolean isFromLoginIn() {
        return isFromLoginIn;
    }

    public void setFromLoginIn(boolean isFromLoginIn) {
        this.isFromLoginIn = isFromLoginIn;
    }

    public int getLogOut() {
        return logOut;
    }

    public void setLogOut(int logOut) {
        this.logOut = logOut;
    }

    public void registerImReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AuthConstants.Event.EVENT_AUTH_KICKOUT);
        filter.addAction(AuthConstants.Event.EVENT_AUTH_LOGOUT);
        filter.addAction(TabMainFragmentActivity.KIK_OUT_ACTION);

        imPushReceiver = new IMPushReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(imPushReceiver, filter);

        registerMessageListener();
    }

    /**
     * 注册接收消息监听器
     * 放在这里的原因:杀掉进程重启的时候未进入MainActivity就接收到消息了，
     * 所以如果放在主页处理会出现未读消息数异常
     */
    private void registerMessageListener() {
        IMEngine.getIMService(MessageService.class).addMessageListener(new ChatMessageListener());
    }

    public String getDeviceIMEI() {
        return DeviceIMEI;
    }

    public void setDeviceIMEI(String deviceIMEI) {
        DeviceIMEI = deviceIMEI;
    }

    public String getDeviceIMSI() {
        return DeviceIMSI;
    }

    public void setDeviceIMSI(String deviceIMSI) {
        DeviceIMSI = deviceIMSI;
    }

    public DownloadServiceTool getDownloadService() {
        return downloadService;
    }

    public void setDownloadService(DownloadServiceTool downloadService) {
        this.downloadService = downloadService;
    }

    /**
     * 判断用户是否存在
     */
    public boolean isUserExist() {
        if (null != getInstance().getUserInfo() && !TextUtils.isEmpty(getInstance().getUserInfo().getAdSId())
                && !TextUtils.isEmpty(getInstance().getUserInfo().getSid())) {
            return true;
        }
        return false;
    }

    public SharedPreferences.Editor getHlEditor() {
        if (null == hlEditor) {
            //初始化SharedPreferences
            if (null == hlSharedPreferences) {
                hlSharedPreferences = getSharedPreferences(Constants.HL_SHAREDPREFERENCES, 0);
            }
            hlEditor = hlSharedPreferences.edit();
        }
        return hlEditor;
    }

    public void setHlEditor(SharedPreferences.Editor hlEditor) {
        this.hlEditor = hlEditor;
    }

    public SharedPreferences getHlSharedPreferences() {
        if (null == hlSharedPreferences) {
            //初始化SharedPreferences
            hlSharedPreferences = getSharedPreferences(Constants.HL_SHAREDPREFERENCES, 0);
            hlEditor = hlSharedPreferences.edit();
        }
        return hlSharedPreferences;
    }

    public void setHlSharedPreferences(SharedPreferences hlSharedPreferences) {
        this.hlSharedPreferences = hlSharedPreferences;
    }

    public SharedPreferences getUserSharedPreferences() {
        if (null == userSharedPreferences) {
            //初始化用户SharedPreferences
            if (null != mUserInfo) {
                userSharedPreferences = getSharedPreferences(Constants.USER_SHAREDPREFERENCES + mUserInfo.getSid(), 0);
            } else {
                userSharedPreferences = getSharedPreferences(Constants.USER_SHAREDPREFERENCES, 0);
            }
            userEditor = userSharedPreferences.edit();
        }
        return userSharedPreferences;
    }

    public void setUserSharedPreferences(SharedPreferences userSharedPreferences) {
        this.userSharedPreferences = userSharedPreferences;
    }

    public SharedPreferences.Editor getUserEditor() {
        if (null == userEditor) {
            if (null == userSharedPreferences) {
                //初始化用户SharedPreferences
                if (null != mUserInfo) {
                    userSharedPreferences = getSharedPreferences(Constants.USER_SHAREDPREFERENCES + mUserInfo.getSid(), 0);
                } else {
                    userSharedPreferences = getSharedPreferences(Constants.USER_SHAREDPREFERENCES, 0);
                }
            }
            userEditor = userSharedPreferences.edit();
        }
        return userEditor;
    }

    public void setUserEditor(SharedPreferences.Editor userEditor) {
        this.userEditor = userEditor;
    }

    public boolean isMaiKeXunInit() {
        return isMaiKeXunInit;
    }

    public void setIsMaiKeXunInit(boolean isMaiKeXunInit) {
        this.isMaiKeXunInit = isMaiKeXunInit;
    }

    public OcrServer getMaiKeXunServer() {
        return maiKeXunServer;
    }

    public void setMaiKeXunServer(OcrServer maiKeXunServer) {
        this.maiKeXunServer = maiKeXunServer;
    }

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            versionName = ManifestUtil.getVersionName(this);
        }
        return versionName;
    }
}
