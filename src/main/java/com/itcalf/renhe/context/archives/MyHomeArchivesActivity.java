package com.itcalf.renhe.context.archives;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.cache.ExternalStorageUtil;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.archives.cover.UpdateCoverActivity;
import com.itcalf.renhe.context.contacts.GuessInterestActivity;
import com.itcalf.renhe.context.contacts.MailBoxList;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.contacts.NewFriendsAct;
import com.itcalf.renhe.context.contacts.SearchContactsFragmentActivity;
import com.itcalf.renhe.context.contacts.SearchResultByContactsActivity;
import com.itcalf.renhe.context.innermsg.ReceiveFriend;
import com.itcalf.renhe.context.more.TwoDimencodeActivity;
import com.itcalf.renhe.context.register.BindPhoneGuideActivity;
import com.itcalf.renhe.context.relationship.SearchResultActivity;
import com.itcalf.renhe.context.room.PersonalMessageBoardActivity;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.AimTagInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.ContactInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.EduExperienceInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.PreferredTagInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.SpecialtiesInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.SummaryInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.WorkExperienceInfo;
import com.itcalf.renhe.dto.ReceiveAddFriend;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.dto.UploadCover;
import com.itcalf.renhe.eventbusbean.NotifyListRefreshWithPositionEvent;
import com.itcalf.renhe.imageUtil.DeleteSDCardImageUtil;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.UserInfoUtil;
import com.itcalf.renhe.view.ExpandableTextView;
import com.itcalf.renhe.view.ShareProfilePopupWindow;
import com.itcalf.renhe.view.WebViewCompanyAuthActivity;
import com.itcalf.renhe.widget.FlowLayout;
import com.itcalf.widget.scrollview.ScrollViewX;
import com.itcalf.widget.scrollview.ScrollViewX.OnScrollListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * 个人档案界面
 */
@SuppressLint("InlinedApi")
public class MyHomeArchivesActivity extends BaseActivity implements OnScrollListener {

    public static String FLAG_INTENT_DATA = "profileSid";
    // 登出标识（两次按下返回退出时的标识字段）
    private ImageView mAvatarImgV;
    private TextView mNameTv;// 姓名
    private TextView mCompanyTv;// 公司
    private TextView mIndustryTv;// 公司地址
    private TextView mCityTv;// 公司地址
    private ImageView mRightImage;
    private ImageView mVipImage;
    private ImageView mRealNameImage;
    private LinearLayout mContactLl;// 联系信息布局
    private LinearLayout mContactLayout;// 联系信息内容
    private LinearLayout mSummaryInfoLayout;// 概要信息
    private LinearLayout mEduExperienceInfoLayout;// 教育经历
    private LinearLayout mWorkExperienceInfoLayout;// 工作经历
    private RelativeLayout mRoomBt;// 客厅
    private RelativeLayout mContactBt;// 联系人
    private TextView roomNumTv;
    private TextView contactNumTv;
    private LinearLayout llLevelIntroduce;
    private ScrollViewX mScrollView;
    private String mOtherSid;
    private Profile mProfile;
    private IWXAPI api;
    private String userFaceUrl;
    private String userName;
    private String userCompany;
    private String userIndustry;
    private String userDesp;
    private int accountType;// 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealName;// 是否是实名认证的会员
    private TextView bindPhoneTv;
    private Button newEditButton;
    private RelativeLayout rootRl;
    private ImageView cameraIv;
    public static final String REFRESH_ARCHIEVE_RECEIVER_ACTION = "com.renhe.refresh_archieve";
    private LinearLayout mProvideGetInfoLayout;// 提供、得到
    public static final String ITEM_SELEPARATOR = "      ";
    // 本地浏览所引用的Bitmap
    private Bitmap localBitmap = null;
    // 回调照片剪切引用的Bitmap
    private Bitmap shearBitmap = null;
    // 提交更新所引用的Bitmap
    private Bitmap requestBitmap = null;
    // 用于更新头像广播中接收的Bitmap
    private Bitmap updateAvatarBitmap = null;
    // 头像背景所引用的Bitmap
    private View seleparateView;
    /* 头像名称 */
    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
    public static final int REQUEST_CODE_CHOOSE_PICTURE_KITKAT = 1002;
    private ImageView corverIv;
    private UpdateAvarImage updateAvarImage;
    private ImageView hunterIv;
    private ImageView expertIv;
    private ImageView emailIv;
    private int DEFAULT_IMAGE;
    private RelativeLayout coverDefaultRl;
    boolean isInit = false;
    boolean isShowShortText = true;
    private FadeUitl fadeUitl;
    private int imId;
    private Dialog mAlertDialog;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private DisplayImageOptions coverOptions;
    private UpdateCoverImage updateCoverImage;
    public static final String UPLOAD_COVER_ARCHIEVE_ACTION = "com.renhe.upload_cover_archieve";
    public static final String UPLOAD_AVARTAR_ARCHIEVE_ACTION = "com.renhe.upload_image_archieve";
    private RefreshArchieveReceiver reFreshArchieveReceiver;
    public static final int REQUEST_CODE_UPDATE_COVER = 2004;
    public final static String UPDATE_AVATAR_ACTION = "update_avatar_image";
    private int position;
    private int cityCode = -1;
    private int industryCode = -1;
    private int coverHeight;
    private RelativeLayout topRl;
    private TextView titleTv;
    private ImageView shareIv;
    private TextView checkMoreArchiveTv;
    private LinearLayout moreArchiveLl;
    /**
     * 好友的IM openId
     */
    private long openId;
    private boolean isFriend = false;
    private IContactCommand contactCommand;
    //好友来源
    private int isFrom = 0;
    //页面跳转
    private String fromClass;
    private int index = -1;//从黑名单页面跳转，第几行
    private TextView tvLevelIntroduce;
    private TextView tvLevelIntroduceImg;
    private PopupWindow pop;
    private View popView;
    private ListView listView;
    private List<PupBean> list;
    private PupBean shareBean, collectBean, reportBean;//不是好友情况下，分享、收藏、举报
    private PupAdapter pupAdapter;
    private boolean isBlocked;//该好友是否被拉黑
    private String selfTwoDimenCodeUrl;
    private Dialog selfTwoCodeAlertDialog;
    private ImageView dialogAvatarIv, dialogTwoCodeIv;
    private TextView dialogNameTv, dialogJobTv, dialogAddressTv, despTv;
    private LinearLayout noNetorkLl;
    //    private MenuItem share, moreItem;
    private CheckUpgradeUtil checkUpgradeUtil;
    private com.itcalf.renhe.view.Button topspeedInviteBt;//极速邀请
    private ImageView newIcon;//右上角小红点

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.archives_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("个人档案"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("个人档案"); // 保证 onPageEnd 在onPause 之前调用,因为
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        mAvatarImgV = (ImageView) findViewById(R.id.avatarImage);
        mNameTv = (TextView) findViewById(R.id.nameTv);
        mCompanyTv = (TextView) findViewById(R.id.companyTv);
        mIndustryTv = (TextView) findViewById(R.id.industryTv);
        mCityTv = (TextView) findViewById(R.id.cityTv);
        mSummaryInfoLayout = (LinearLayout) findViewById(R.id.summaryInfoLayout);
        mEduExperienceInfoLayout = (LinearLayout) findViewById(R.id.eduExperienceInfoLayout);
        mWorkExperienceInfoLayout = (LinearLayout) findViewById(R.id.workExperienceInfoLayout);
        mRightImage = (ImageView) findViewById(R.id.rightImage);
        mVipImage = (ImageView) findViewById(R.id.vipImage);
        mRealNameImage = (ImageView) findViewById(R.id.realnameImage);
        mRoomBt = (RelativeLayout) findViewById(R.id.roomBt);
        mContactBt = (RelativeLayout) findViewById(R.id.contactBt);
        roomNumTv = (TextView) findViewById(R.id.roomNum);
        contactNumTv = (TextView) findViewById(R.id.contactNum);
        mScrollView = (ScrollViewX) findViewById(R.id.scrollView);
        newEditButton = (Button) findViewById(R.id.new_editBt);
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        cameraIv = (ImageView) findViewById(R.id.cameraImage);
        seleparateView = (View) findViewById(R.id.room_contact_seleprate);
        mProvideGetInfoLayout = (LinearLayout) findViewById(R.id.provideGetInfoLayout);
        newEditButton.setEnabled(true);
        corverIv = (ImageView) findViewById(R.id.corver_iv);
        hunterIv = (ImageView) findViewById(R.id.hunterImage);
        expertIv = (ImageView) findViewById(R.id.expertImage);
        emailIv = (ImageView) findViewById(R.id.emailImage);
        DEFAULT_IMAGE = R.drawable.cover_bg;
        coverDefaultRl = (RelativeLayout) findViewById(R.id.corver_defalut_rl);
        mScrollView.setVisibility(View.GONE);
        topRl = (RelativeLayout) findViewById(R.id.top_rl);
        topRl.setVisibility(View.GONE);
        titleTv = (TextView) findViewById(R.id.title_tv);
        shareIv = (ImageView) findViewById(R.id.share_iv);
        checkMoreArchiveTv = (TextView) findViewById(R.id.checkMoreTv);
        moreArchiveLl = (LinearLayout) findViewById(R.id.extraLayout);
        llLevelIntroduce = (LinearLayout) findViewById(R.id.level_introduce);
        tvLevelIntroduce = (TextView) findViewById(R.id.tv_level_introduce);
        tvLevelIntroduceImg = (TextView) findViewById(R.id.tv_level_introduce_img);
        noNetorkLl = (LinearLayout) findViewById(R.id.no_network_ll);
        topspeedInviteBt = (com.itcalf.renhe.view.Button) findViewById(R.id.topspeed_invite_bt);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem moreItem = menu.findItem(R.id.archive_more);
        moreItem.setVisible(true);
        moreItem.setTitle("更多");

        LinearLayout moreActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.redcircle_badge_actionbar_item_view, null);
        ImageView moreIcon = (ImageView) moreActionView.findViewById(R.id.ic_action);
        moreIcon.setImageResource(R.drawable.ic_action_navigation_more_selector);
        moreItem.setActionView(moreActionView);
        newIcon = (ImageView) moreActionView.findViewById(R.id.newicon);
        if (null != mProfile && !mProfile.isSelf() && !mProfile.isConnection() && SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_UREAD_NEW, true, true))
            newIcon.setVisibility(View.VISIBLE);
        moreActionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProfile != null) {
                    if (mProfile.isSelf()) {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "friend_archivve_share");
                        Intent intent = new Intent(MyHomeArchivesActivity.this, TwoDimencodeActivity.class);
                        intent.putExtra("type", TwoDimencodeActivity.SHARE_TYPE_ARCHIVE);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        Intent intent = new Intent(MyHomeArchivesActivity.this, ArchiveMoreActivity.class);
                        intent.putExtra("profile", mProfile);
                        startHlActivityForResult(intent, Constants.ARCHIVE_REQUEST_CODE.REQUEST_CODE_ARCHIVE_MORE);
                        newIcon.setVisibility(View.INVISIBLE);
                        if (null != mProfile && !mProfile.isSelf() && !mProfile.isConnection())
                            SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_UREAD_NEW, false, true);
                    }
                }
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                MobclickAgent.onEvent(MyHomeArchivesActivity.this, "friend_archivve_share");
                if (null != mProfile) {
                    if (mProfile.isSelf()) {
                        Intent intent = new Intent(MyHomeArchivesActivity.this, TwoDimencodeActivity.class);
                        intent.putExtra("type", TwoDimencodeActivity.SHARE_TYPE_ARCHIVE);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        new ShareProfilePopupWindow(MyHomeArchivesActivity.this, rootRl, userName, userDesp, userFaceUrl, mOtherSid,
                                mProfile, true);
                    }
                }
                return true;
            case R.id.item_edit:
            case R.id.item_write_msg:
            case R.id.item_invite_friend:
                if (mProfile != null) {
                    if (mProfile.isSelf()) {
                        //跳转到编辑档案页面
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "edit_profile");
                        Intent intent = new Intent(MyHomeArchivesActivity.this, EditMyHomeArchivesActivity.class);
                        intent.putExtra("Profile", mProfile);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if (mProfile.isInvite()) {

                    } else if (!mProfile.isInvite()) {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "add_friend");
                        Intent i = new Intent(MyHomeArchivesActivity.this, AddFriendAct.class);
                        i.putExtra("mSid", mOtherSid);
                        i.putExtra("from", isFrom);
                        i.putExtra("friendName", mProfile.getUserInfo().getName());
                        startActivityForResult(i, 99);
                    }
                }
                return true;
            case R.id.archive_more:
                if (mProfile != null) {
                    if (mProfile.isSelf()) {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "friend_archivve_share");
                        Intent intent = new Intent(MyHomeArchivesActivity.this, TwoDimencodeActivity.class);
                        intent.putExtra("type", TwoDimencodeActivity.SHARE_TYPE_ARCHIVE);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        Intent intent = new Intent(MyHomeArchivesActivity.this, ArchiveMoreActivity.class);
                        intent.putExtra("profile", mProfile);
                        startHlActivityForResult(intent, Constants.ARCHIVE_REQUEST_CODE.REQUEST_CODE_ARCHIVE_MORE);
                        if (null != mProfile && !mProfile.isSelf() && !mProfile.isConnection())
                            SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_UREAD_NEW, false, true);
//                        if (pop == null) {
//                            createPopupwindow();
//                        }
//                        if (pop.isShowing()) {
//                            pop.dismiss();
//                            return true;
//                        }
//                        Rect frame = new Rect();
//                        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//                        int statusBarHeight = frame.top;
//                        if (null != getSupportActionBar()) {
//                            pop.showAtLocation(popView, Gravity.RIGHT | Gravity.TOP, 20,
//                                    getSupportActionBar().getHeight() + statusBarHeight);
//                        }
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initData() {
        super.initData();
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);

        /**
         * 注册到微信
         */
        api = WXAPIFactory.createWXAPI(getApplicationContext(), Constants.WEI_XIN_SHARE.WEI_XIN_APP_ID, true);
        api.registerApp(Constants.WEI_XIN_SHARE.WEI_XIN_APP_ID);

        isFrom = getIntent().getIntExtra("from", 0);//好友来源
        fromClass = getIntent().getStringExtra("addfriend_from");//页面跳转来源
        index = getIntent().getIntExtra("index", -1);

        mOtherSid = getIntent().getStringExtra(FLAG_INTENT_DATA);
        openId = getIntent().getLongExtra("openId", 0L);
        if (TextUtils.isEmpty(mOtherSid))
            mOtherSid = "";
        if (mOtherSid.equals(RenheApplication.getInstance().getUserInfo().getSid())
                || openId == RenheApplication.getInstance().getUserInfo().getImId()) {
            setTextValue(R.id.title_txt, "档案详情");
        } else {
            setTextValue(R.id.title_txt, "人脉详情");
        }
        position = getIntent().getIntExtra("position", 0);
        // showDialog(2);
        // 判断是不是自己
        getUsrInfo(false);
        updateAvarImage = new UpdateAvarImage();
        IntentFilter intentFilter = new IntentFilter(BroadCastAction.UPLOAD_AVARTAR_ARCHIEVE_ACTION);
        registerReceiver(updateAvarImage, intentFilter);

        updateCoverImage = new UpdateCoverImage();
        IntentFilter intentFilter1 = new IntentFilter(UPLOAD_COVER_ARCHIEVE_ACTION);
        registerReceiver(updateCoverImage, intentFilter1);

        reFreshArchieveReceiver = new RefreshArchieveReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(REFRESH_ARCHIEVE_RECEIVER_ACTION);
        registerReceiver(reFreshArchieveReceiver, intentFilter2);

        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar).showImageForEmptyUri(R.drawable.avatar)
                .showImageOnFail(R.drawable.avatar).cacheInMemory(false).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                // .displayer(new RoundedBitmapDisplayer(20))
                .build();
        coverOptions = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                // .displayer(new RoundedBitmapDisplayer(20))
                .build();
        coverHeight = DensityUtil.dip2px(this, 270);
        checkUpgradeUtil = new CheckUpgradeUtil(this);
    }

    private void getUsrInfo(boolean isRefresh) {
        if (!TextUtils.isEmpty(mOtherSid)) {
            new ProfileTask(isRefresh, false).executeOnExecutor(Executors.newCachedThreadPool(), mOtherSid,
                    RenheApplication.getInstance().getUserInfo().getSid(),
                    RenheApplication.getInstance().getUserInfo().getAdSId());
        } else if (openId > 0) {
            new ProfileTask(isRefresh, true).executeOnExecutor(Executors.newCachedThreadPool(), openId + "",
                    getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId());
        }
    }

    class ProfileTask extends AsyncTask<String, Void, Profile> {
        private boolean isRefresh = false;
        private boolean isByOpenId = false;

        public ProfileTask(boolean isRefresh, boolean isByOpenId) {
            this.isRefresh = isRefresh;
            this.isByOpenId = isByOpenId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isRefresh) {
                mScrollView.setVisibility(View.GONE);
                newEditButton.setVisibility(View.GONE);
            }
        }

        @Override
        protected Profile doInBackground(String... params) {
            try {
                if (!isByOpenId) {
                    return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
                            MyHomeArchivesActivity.this);
                } else {
                    return getRenheApplication().getProfileCommand().showIMNoFriendsProfile(params[0], params[1], params[2],
                            MyHomeArchivesActivity.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            try {
                fadeUitl.removeFade(rootRl);
            } catch (Exception e) {
            }
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    mOtherSid = result.getUserInfo().getSid();
                    mProfile = result;
                    populateData(mProfile);
                    mScrollView.setVisibility(View.VISIBLE);
                    newEditButton.setVisibility(View.VISIBLE);
                    String from = getIntent().getStringExtra("fromTag");
                    if (!TextUtils.isEmpty(from) && from.equals("MyFragment")) {
                        checkUserFace();
                    }
                } else if (-6 == result.getState()) {
                    ToastUtil.showToast(MyHomeArchivesActivity.this, R.string.view_profile_user_uncheck, true);
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
            } else {
                noNetorkLl.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 根据服务端返回的profile实体类初始化档案界面
     *
     * @param result 服务端返回的profile实体类
     */
    private void populateData(Profile result) {
        if (null != newIcon) {
            if (!result.isSelf() && !mProfile.isConnection() && SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_UREAD_NEW, true, true))
                newIcon.setVisibility(View.VISIBLE);
            else {
                newIcon.setVisibility(View.INVISIBLE);
            }
        }
        isInit = false;
        isShowShortText = true;//是否需要隐藏联系方式之外的其他内容
        pupAdapter = new PupAdapter();
        list = new ArrayList<>();
        shareBean = new PupBean("分享", R.drawable.archive_more_share_friend);
        collectBean = new PupBean("收藏", R.drawable.archive_more_share_friend);
        reportBean = new PupBean("举报", R.drawable.archive_more_share_friend);
        list.add(shareBean);
        list.add(collectBean);
        list.add(reportBean);
        this.userFaceUrl = result.getUserInfo().getUserface();
        this.userName = result.getUserInfo().getName();
        if (null != result.getUserInfo().getTitle()) {
            this.userCompany = result.getUserInfo().getTitle().trim() + " ";
        }
        if (null != result.getUserInfo().getCompany()) {
            this.userCompany = mCompanyTv.getText().toString() + result.getUserInfo().getCompany().trim();
        }
        if (null != result.getUserInfo().getLocation()) {
            this.userIndustry = result.getUserInfo().getLocation().trim() + " ";
        }
        if (null != result.getUserInfo().getIndustry()) {
            this.userIndustry = mIndustryTv.getText().toString() + result.getUserInfo().getIndustry().trim();
        }
        this.userDesp = this.userCompany + this.userIndustry;
        this.accountType = result.getUserInfo().getAccountType();
        this.isRealName = result.getUserInfo().isRealName();
        mProfile = result;

        if (result.isSelf()) {
            //发广播到主界面通知更新
            Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
            brocastIntent.putExtra("From", "MyhomeArchivesActivity");
            brocastIntent.putExtra("Profile", result);
            brocastIntent.putExtra("userface", result.getUserInfo().getUserface());
            sendBroadcast(brocastIntent);
            cameraIv.setVisibility(View.GONE);
            if (result.isUnapproved()) {
                newEditButton.setText("资料审核中");
                newEditButton.setTextColor(getResources().getColor(R.color.archive_editbt_text_selected));
                newEditButton.setEnabled(false);
            } else {
                newEditButton.setTextColor(getResources().getColorStateList(R.color.archive_editbt_textcolor_selected));
                newEditButton.setText("更新档案");
                newEditButton.setEnabled(true);
            }

            if (result.getUserInfo().getLocation() != null) {
                SearchCity searchCity = new SearchCity(-111, result.getUserInfo().getLocation());
                RenheApplication.getInstance().setCurrentCity(searchCity);
            }
            CacheManager.getInstance().populateData(this).saveObject(result,
                    RenheApplication.getInstance().getUserInfo().getEmail(), CacheManager.PROFILE);
        } else if (result.isConnection()) {
            isBlocked = result.getUserInfo().isBlocked();
            if (isBlocked) {
                newEditButton.setBackgroundColor(getResources().getColor(R.color.BC_1));
                newEditButton.setEnabled(false);
            }
            isFriend = true;
            checkMoreArchiveTv.setVisibility(View.VISIBLE);
            moreArchiveLl.setVisibility(View.GONE);
            newEditButton.setText("写站内信");
        } else if (result.getBeInvitedInfo().isBeInvited()) {// 是否被邀请
            newEditButton.setText("接受邀请");
        } else {
            if (result.isInvite()) {
                newEditButton.setText("已邀请");
                newEditButton.setTextColor(getResources().getColor(R.color.archive_editbt_text_selected));
                newEditButton.setEnabled(false);
            } else {
                newEditButton.setText("加好友");
            }
        }
        pupAdapter.notifyDataSetChanged();
        initBottomBtView(result);
        if (result.getUserInfo().getFriendDegree() == 1) {
            mRightImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_1st));
        } else if (result.getUserInfo().getFriendDegree() == 2) {
            mRightImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_2nd));
        } else {
            mRightImage.setVisibility(View.GONE);
        }
        switch (this.accountType) {
            case 0:
                mVipImage.setVisibility(View.GONE);
                break;
            case 1://VIP会员
                mVipImage.setVisibility(View.VISIBLE);
                mVipImage.setImageResource(R.drawable.archive_vip_2x);
                tvLevelIntroduce.setText("级别");
                tvLevelIntroduceImg.setText("VIP会员");
                tvLevelIntroduceImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_vip_1, 0, 0, 0);
                break;
            case 2://黄金会员
                mVipImage.setVisibility(View.VISIBLE);
                mVipImage.setImageResource(R.drawable.archive_vip_2_2x);
                tvLevelIntroduce.setText("级别");
                tvLevelIntroduceImg.setText("黄金会员");
                tvLevelIntroduceImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_vip_2, 0, 0, 0);
                break;
            case 3://铂金会员
                mVipImage.setVisibility(View.VISIBLE);
                mVipImage.setImageResource(R.drawable.archive_vip_3_2x);
                tvLevelIntroduce.setText("级别");
                tvLevelIntroduceImg.setText("铂金会员");
                tvLevelIntroduceImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_vip_3, 0, 0, 0);
                break;
            default:
                break;
        }
        if (this.isRealName && accountType <= 0) {
            mRealNameImage.setVisibility(View.VISIBLE);
            mRealNameImage.setImageResource(R.drawable.archive_realname2x);

            tvLevelIntroduce.setText("认证");
            tvLevelIntroduceImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_realname, 0, 0, 0);
            tvLevelIntroduceImg.setText("实名认证");
        } else {
            mRealNameImage.setVisibility(View.GONE);
            if (accountType != 1 && accountType != 2 && accountType != 3) {
                llLevelIntroduce.setVisibility(View.GONE);
            }
        }
        if (result.getUserInfo().isPassCardValidate()) {
            hunterIv.setVisibility(View.GONE);
        }
        if (result.getUserInfo().isPassEventExpertValidate()) {
            expertIv.setVisibility(View.GONE);
        }
        if (result.getUserInfo().isPassWorkEmailValidate()) {
            emailIv.setVisibility(View.GONE);
        }
        try {
            imageLoader.displayImage(result.getUserInfo().getUserface(), mAvatarImgV, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 下载封面图
        if (!TextUtils.isEmpty(result.getUserInfo().getCover())) {
            coverDefaultRl.setVisibility(View.GONE);
            try {

                imageLoader.displayImage(result.getUserInfo().getCover(), corverIv, coverOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 如果是自己
            if (result.isSelf()) {
                coverDefaultRl.setVisibility(View.VISIBLE);
                coverDefaultRl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        createCoverDialog(MyHomeArchivesActivity.this);
                    }
                });
            }
        }
        if (null != result.getUserInfo().getName()) {
            mNameTv.setText(result.getUserInfo().getName().trim());
        }
        if (!TextUtils.isEmpty(result.getUserInfo().getTitle())) {
            mCompanyTv.setText(result.getUserInfo().getTitle().trim());
        }
        if (!TextUtils.isEmpty(result.getUserInfo().getCompany())) {
            mCompanyTv.setText(mCompanyTv.getText().toString() + " / " + result.getUserInfo().getCompany().trim());
        }
        if (!TextUtils.isEmpty(mCompanyTv.getText().toString())) {
            mCompanyTv.setVisibility(View.VISIBLE);
        } else {
            mCompanyTv.setVisibility(View.GONE);
        }
        if (result.isCertification()) {
            mCompanyTv.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.company_auth_icon), null);
        }
        if (!TextUtils.isEmpty(result.getUserInfo().getLocation())) {
            mCityTv.setText(result.getUserInfo().getLocation().trim() + " ");
            cityCode = result.getUserInfo().getAddressId();
        } else {
            mCityTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(result.getUserInfo().getIndustry())) {
            mIndustryTv.setText(result.getUserInfo().getIndustry().trim());
            industryCode = result.getUserInfo().getIndustryId();
        } else {
            mIndustryTv.setVisibility(View.GONE);
        }
        int messageNum = result.getUserInfo().getMessageBoardNum();
        int connectionNum = result.getUserInfo().getConnectionNum();
        roomNumTv.setText(getResources().getString(R.string.record_new_dynamic) + messageNum);
        contactNumTv.setText(getResources().getString(R.string.record_renmai) + connectionNum);
        if (messageNum <= 0) {
            mRoomBt.setVisibility(View.GONE);
            seleparateView.setVisibility(View.GONE);
        }
        if (connectionNum <= 0) {
            mContactBt.setVisibility(View.GONE);
            seleparateView.setVisibility(View.GONE);
        }
        if (result != null && result.getUserInfo() != null) {
            SummaryInfo sInfo = result.getUserInfo().getSummaryInfo();
            mSummaryInfoLayout.removeAllViews();
            boolean isSpecialtiesNull = true;
            boolean isProfessionalNull = true;
            SpecialtiesInfo[] specialtiesInfo = result.getUserInfo().getSpecialtiesInfo();
            View summaryInfoView = LayoutInflater.from(this).inflate(R.layout.summary_info, null);
            FlowLayout professionFl = (FlowLayout) summaryInfoView.findViewById(R.id.professionLl);
            if (null != sInfo) {
                if (sInfo.getProfessional() != null && !TextUtils.isEmpty(sInfo.getProfessional().trim())) {
                    isProfessionalNull = false;
                }
            }
            if (null != specialtiesInfo) {
                for (int i = 0; i < specialtiesInfo.length; i++) {
                    if (!TextUtils.isEmpty(specialtiesInfo[i].getTitle())) {
                        isSpecialtiesNull = false;
                        if (i != specialtiesInfo.length - 1) {
                            View view = LayoutInflater.from(this).inflate(R.layout.archive_provide_get_item, null);
                            TextView textView = (TextView) view.findViewById(R.id.professionalTv);
                            textView.setText(specialtiesInfo[i].getTitle().trim());
                            professionFl.addView(view);
                        } else {
                            View view = LayoutInflater.from(this).inflate(R.layout.archive_provide_get_item, null);
                            TextView textView = (TextView) view.findViewById(R.id.professionalTv);
                            textView.setText(specialtiesInfo[i].getTitle().trim());
                            professionFl.addView(view);
                        }
                    }
                }
            }
            if (!isSpecialtiesNull || !isProfessionalNull) {
                findViewById(R.id.summaryLl).setVisibility(View.VISIBLE);

                if (!isProfessionalNull) {
                    ((LinearLayout) summaryInfoView.findViewById(R.id.self_info_ll)).setVisibility(View.GONE);
                } else {
                    ((LinearLayout) summaryInfoView.findViewById(R.id.self_info_ll)).setVisibility(View.GONE);
                    ((View) summaryInfoView.findViewById(R.id.separate_line)).setVisibility(View.GONE);
                }
                if (!isSpecialtiesNull) {
                    ((LinearLayout) summaryInfoView.findViewById(R.id.self_profession_ll)).setVisibility(View.VISIBLE);
                } else {
                    ((View) summaryInfoView.findViewById(R.id.separate_line)).setVisibility(View.GONE);
                    ((LinearLayout) summaryInfoView.findViewById(R.id.self_profession_ll)).setVisibility(View.GONE);
                }
                mSummaryInfoLayout.addView(summaryInfoView);
            }
            // 供求信息
            AimTagInfo[] aimTagInfo = result.getUserInfo().getAimTagInfo();
            PreferredTagInfo[] preferredTagInfo = result.getUserInfo().getPreferredTagInfo();
            boolean isAimNull = true;
            boolean isPreferNull = true;
            if (null != aimTagInfo) {
                for (AimTagInfo aInfo : aimTagInfo) {
                    if (!TextUtils.isEmpty(aInfo.getTitle())) {
                        isAimNull = false;
                    }
                }
            }
            if (null != preferredTagInfo) {
                for (PreferredTagInfo prInfo : preferredTagInfo) {
                    if (!TextUtils.isEmpty(prInfo.getTitle())) {
                        isPreferNull = false;
                    }
                }
            }
            mProvideGetInfoLayout.removeAllViews();
            findViewById(R.id.provideGetLl).setVisibility(View.GONE);
            if (!isAimNull || !isPreferNull || !isProfessionalNull) {
                findViewById(R.id.provideGetLl).setVisibility(View.VISIBLE);
                View provideGetInfoView = LayoutInflater.from(this).inflate(R.layout.provide_get_info, null);
                FlowLayout aimFl = (FlowLayout) provideGetInfoView.findViewById(R.id.professionalLl);
                FlowLayout preferFl = (FlowLayout) provideGetInfoView.findViewById(R.id.preferLl);
                if (!isAimNull) {// 我想得到
                    for (int i = 0; i < aimTagInfo.length; i++) {
                        if (!TextUtils.isEmpty(aimTagInfo[i].getTitle())) {
                            View view = LayoutInflater.from(this).inflate(R.layout.archive_provide_get_item, null);
                            final TextView textView = (TextView) view.findViewById(R.id.professionalTv);
                            textView.setText(aimTagInfo[i].getTitle().trim());
                            preferFl.addView(view);
                            /**** tag 可点击 功能点 ******/
                            textView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    Intent i = new Intent(MyHomeArchivesActivity.this, SearchResultActivity.class);
                                    i.putExtra("keyword", "" + textView.getText().toString().trim());
                                    i.putExtra("excluding", false);//不统计搜索结果
                                    startHlActivity(i);
                                }
                            });
                        }
                    }
                } else {
                    ((View) provideGetInfoView.findViewById(R.id.separate_line)).setVisibility(View.INVISIBLE);
                    ((LinearLayout) provideGetInfoView.findViewById(R.id.self_profession_ll)).setVisibility(View.GONE);
                }
                if (!isPreferNull) {
                    for (int i = 0; i < preferredTagInfo.length; i++) {
                        if (!TextUtils.isEmpty(preferredTagInfo[i].getTitle())) {
                            View view = LayoutInflater.from(this).inflate(R.layout.archive_provide_get_item, null);
                            final TextView textView = (TextView) view.findViewById(R.id.professionalTv);
                            textView.setText(preferredTagInfo[i].getTitle().trim());
                            aimFl.addView(view);
                            /**** tag 可点击 功能点 ******/
                            textView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    Intent i = new Intent(MyHomeArchivesActivity.this, SearchResultActivity.class);
                                    i.putExtra("keyword", "" + textView.getText().toString().trim());
                                    startHlActivity(i);
                                }
                            });
                        }
                    }
                } else {
                    ((View) provideGetInfoView.findViewById(R.id.separate_line)).setVisibility(View.INVISIBLE);
                    ((LinearLayout) provideGetInfoView.findViewById(R.id.self_info_ll)).setVisibility(View.GONE);
                }
                if (isAimNull && isPreferNull) {
                    ((View) provideGetInfoView.findViewById(R.id.separate_line1)).setVisibility(View.INVISIBLE);
                }
                if (!isProfessionalNull) {
                    ((LinearLayout) provideGetInfoView.findViewById(R.id.self_info_ll1)).setVisibility(View.VISIBLE);
                    ExpandableTextView professionalTv = (ExpandableTextView) provideGetInfoView
                            .findViewById(R.id.professional_tv);
                    String content = sInfo.getProfessional().trim();
                    professionalTv.setText(content);
                } else {
                    ((LinearLayout) provideGetInfoView.findViewById(R.id.self_info_ll1)).setVisibility(View.GONE);
                    ((View) provideGetInfoView.findViewById(R.id.separate_line1)).setVisibility(View.INVISIBLE);
                }
                mProvideGetInfoLayout.addView(provideGetInfoView);
            }

            ContactInfo cInfo = result.getUserInfo().getContactInfo();
            if (result.isSelf()) {
                contactView(result, cInfo, true);
            } else {
                contactView(result, cInfo, false);
            }

            WorkExperienceInfo[] weInfos = result.getUserInfo().getWorkExperienceInfo();
            mWorkExperienceInfoLayout.removeAllViews();
            if (null != weInfos && weInfos.length > 0) {
                for (int i = 0; i < weInfos.length; i++) {
                    final WorkExperienceInfo weInfo = weInfos[i];
                    View workExperienceInfoView = LayoutInflater.from(this).inflate(R.layout.workexperience_info, null);
                    if (!TextUtils.isEmpty(weInfo.getCompany()) || !TextUtils.isEmpty(weInfo.getTitle())
                            || !TextUtils.isEmpty(weInfo.getTimeInfo()) || !TextUtils.isEmpty(weInfo.getContent())) {
                        findViewById(R.id.workLl).setVisibility(View.VISIBLE);
                        ImageView companyAuthedIv = ((ImageView) workExperienceInfoView.findViewById(R.id.company_authed_iv));
                        com.itcalf.renhe.view.TextView companyAuthTv = ((com.itcalf.renhe.view.TextView) workExperienceInfoView.findViewById(R.id.company_auth_tv));
                        if (!TextUtils.isEmpty(weInfo.getTitle())) {
                            ((TextView) workExperienceInfoView.findViewById(R.id.title_tv)).setText(weInfo.getTitle());
                        } else {
                            ((TextView) workExperienceInfoView.findViewById(R.id.title_tv)).setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(weInfo.getCompany())) {
                            ((TextView) workExperienceInfoView.findViewById(R.id.company_tv)).setText(weInfo.getCompany());
                        } else {
                            ((TextView) workExperienceInfoView.findViewById(R.id.company_tv)).setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(weInfo.getTimeInfo())) {
                            ((TextView) workExperienceInfoView.findViewById(R.id.time_info_tv)).setText(weInfo.getTimeInfo());
                        } else {
                            ((TextView) workExperienceInfoView.findViewById(R.id.time_info_tv)).setVisibility(View.GONE);
                        }

                        if (result.isSelf()) {
                            ((ExpandableTextView) workExperienceInfoView.findViewById(R.id.experience_tv))
                                    .setVisibility(View.GONE);
                        } else {
                            if (!TextUtils.isEmpty(weInfo.getContent())) {
                                ((ExpandableTextView) workExperienceInfoView.findViewById(R.id.experience_tv))
                                        .setText(weInfo.getContent());
                            } else {
                                ((ExpandableTextView) workExperienceInfoView.findViewById(R.id.experience_tv))
                                        .setVisibility(View.GONE);
                            }
                        }
                        if (i > 0) {
                            workExperienceInfoView.findViewById(R.id.work_tv).setVisibility(View.INVISIBLE);
                        }
                        if (i == weInfos.length - 1) {
                            workExperienceInfoView.findViewById(R.id.separate_line).setVisibility(View.INVISIBLE);
                        }
                        if (weInfo.isCertification()) {
                            companyAuthTv.setVisibility(View.GONE);
                            companyAuthedIv.setVisibility(View.VISIBLE);
                        } else {
                            companyAuthTv.setVisibility(View.VISIBLE);
                            companyAuthedIv.setVisibility(View.GONE);
                            if (result.isSelf()) {
                                companyAuthTv.setText("去验证 >");
                            } else {
                                companyAuthTv.setText("去查询 >");
                            }
                        }
                        final Profile finalResult = result;
                        companyAuthTv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MyHomeArchivesActivity.this, WebViewCompanyAuthActivity.class);
                                if (finalResult.isSelf()) {
                                    intent.putExtra("toAuth", true);
                                    intent.putExtra("url", Constants.ARCHIVE_COMPANY_AUTH_URL + weInfo.getCompany() + "&viewMemberId=" + RenheApplication.getInstance().getUserInfo().getId());
                                } else {
                                    intent.putExtra("url", Constants.ARCHIVE_COMPANY_SEARCH_URL + weInfo.getCompany() + "&viewMemberId=" + mProfile.getUserInfo().getId());
                                }
                                startHlActivityForResult(intent, Constants.ARCHIVE_REQUEST_CODE.REQUEST_CODE_COMPANY_AUTH);
                            }
                        });
                        mWorkExperienceInfoLayout.addView(workExperienceInfoView);
                    }
                }
            }
            EduExperienceInfo[] edInfos = result.getUserInfo().getEduExperienceInfo();
            mEduExperienceInfoLayout.removeAllViews();
            if (null != edInfos && edInfos.length > 0) {
                for (int i = 0; i < edInfos.length; i++) {
                    EduExperienceInfo edInfo = edInfos[i];
                    if (!TextUtils.isEmpty(edInfo.getSchoolName()) || !TextUtils.isEmpty(edInfo.getStudyField())
                            || !TextUtils.isEmpty(edInfo.getTimeInfo())) {
                        findViewById(R.id.eduLl).setVisibility(View.VISIBLE);
                        View eduExperienceInfoView = LayoutInflater.from(this).inflate(R.layout.eduexperience_info, null);
                        if (!TextUtils.isEmpty(edInfo.getSchoolName())) {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.school_name_tv)).setText(edInfo.getSchoolName());
                        } else {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.school_name_tv)).setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(edInfo.getStudyField())) {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.study_field_tv)).setText(edInfo.getStudyField());
                        } else {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.study_field_tv)).setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(edInfo.getTimeInfo())) {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.time_info_tv)).setText(edInfo.getTimeInfo());
                        } else {
                            ((TextView) eduExperienceInfoView.findViewById(R.id.time_info_tv)).setVisibility(View.GONE);
                        }
                        if (i > 0) {
                            eduExperienceInfoView.findViewById(R.id.edu_tv).setVisibility(View.INVISIBLE);
                        }
                        if (i == edInfos.length - 1) {
                            eduExperienceInfoView.findViewById(R.id.separate_line).setVisibility(View.INVISIBLE);
                        }
                        mEduExperienceInfoLayout.addView(eduExperienceInfoView);
                    }
                }
            }

        }
        result = null;
    }

    /**
     * 绘制底部按钮的UI、由服务端控制
     *
     * @param result
     */
    private void initBottomBtView(Profile result) {
        final Profile.AddFriendProfileInfo[] addFriendProfileInfos = result.getAddFriendProfileInfo();
        if (null != addFriendProfileInfos && addFriendProfileInfos.length > 0) {
            if (addFriendProfileInfos.length == 1) {
                topspeedInviteBt.setVisibility(View.GONE);
                newEditButton.setVisibility(View.VISIBLE);
                newEditButton.setText(addFriendProfileInfos[0].getName());
            } else if (addFriendProfileInfos.length == 2) {
                topspeedInviteBt.setVisibility(View.VISIBLE);
                newEditButton.setVisibility(View.VISIBLE);
                topspeedInviteBt.setText(addFriendProfileInfos[0].getName());
                newEditButton.setText(addFriendProfileInfos[1].getName());
                topspeedInviteBt.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addFriendProfileInfos[0].getType() == 1) {//极速邀请
                            Intent intent = new Intent(MyHomeArchivesActivity.this, TopSpeedInviteActivity.class);
                            intent.putExtra("profile", mProfile);
                            intent.putExtra("isFrom", isFrom);
                            startHlActivityForResult(intent, 99);
                        }
                    }
                });
            }
        }
    }

    /**
     * 如果上传的头像未审核通过，更新本地头像为默认头像
     */
    private void checkUserFace() {
        String tempUrl = getIntent().getStringExtra("userface");
        if (!TextUtils.isEmpty(userFaceUrl) && !userFaceUrl.equals(tempUrl)) {
            UserInfoUtil.chengeUserInfo(UserInfoUtil.USERFACE, userFaceUrl);
        }
    }

    /**
     * 显示联系方式
     *
     * @param result
     * @param cInfo
     * @param showAll 是否显示全部联系方式
     */
    private void contactView(Profile result, ContactInfo cInfo, boolean showAll) {
        if (result.isSelf()) {
            mContactLl = (LinearLayout) findViewById(R.id.contactLl_self);
            mContactLayout = (LinearLayout) findViewById(R.id.contactLayout_self);
        } else {
            mContactLl = (LinearLayout) findViewById(R.id.contactLl_other);
            mContactLayout = (LinearLayout) findViewById(R.id.contactLayout_other);
        }
        mContactLl.setVisibility(View.GONE);
        mContactLayout.removeAllViews();
        if (null != cInfo && (result.isSelf() || result.isConnection())) {
            mContactLl.setVisibility(View.VISIBLE);
            final String email = cInfo.getEmail();
            final String mobile = cInfo.getMobile();
            final String tel = cInfo.getTel();
            if (!TextUtils.isEmpty(mobile)) {
                final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.contact_info, null);
                ((TextView) contactInfoView.findViewById(R.id.titleTv)).setText("手机");
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setText(mobile);
                bindPhoneTv = ((TextView) contactInfoView.findViewById(R.id.bindPhone));
                if (result.isSelf()) {
                    // 如果未绑定
                    if (result.getUserInfo().isBindMobile()) {
                        bindPhoneTv.setVisibility(View.VISIBLE);
                        bindPhoneTv.setCompoundDrawablesWithIntrinsicBounds(
                                getResources().getDrawable(R.drawable.bind_phone_small), null, null, null);
                        bindPhoneTv.setText("已绑定");
                        contactInfoView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // 绑定手机号
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isbind", true);
                                bundle.putString("mobile", mobile);
                                Intent intent = new Intent(MyHomeArchivesActivity.this, BindPhoneGuideActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        });
                    } else {
                        bindPhoneTv.setVisibility(View.VISIBLE);
                        bindPhoneTv.setText("未绑定");
                        contactInfoView.setClickable(true);
                        contactInfoView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // 绑定手机号
                                Intent intent = new Intent(MyHomeArchivesActivity.this, BindPhoneGuideActivity.class);
                                startActivityForResult(intent, 1);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        });
                    }
                    ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View arg0) {
                            new ContentUtil().createCopyDialog(MyHomeArchivesActivity.this, mobile);
                            return true;
                        }
                    });
                    ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            contactInfoView.performClick();
                        }

                    });
                } else {
                    contactInfoView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            new PopupWindows(MyHomeArchivesActivity.this, mAvatarImgV, mobile, true);
                        }
                    });
                }

                mContactLayout.addView(contactInfoView);
            } else {
                if (result.isSelf()) {
                    final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.contact_info, null);
                    ((TextView) contactInfoView.findViewById(R.id.titleTv)).setText("手机");
                    bindPhoneTv = ((TextView) contactInfoView.findViewById(R.id.bindPhone));
                    // 如果未绑定
                    if (result.getUserInfo().isBindMobile()) {
                        bindPhoneTv.setVisibility(View.VISIBLE);
                        bindPhoneTv.setCompoundDrawablesWithIntrinsicBounds(
                                getResources().getDrawable(R.drawable.bind_phone_small), null, null, null);
                        bindPhoneTv.setText("已绑定");
                        contactInfoView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // 绑定手机号
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isbind", true);
                                bundle.putString("mobile", mobile);
                                Intent intent = new Intent(MyHomeArchivesActivity.this, BindPhoneGuideActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        });
                    } else {
                        bindPhoneTv.setVisibility(View.VISIBLE);
                        bindPhoneTv.setText("未绑定");
                        contactInfoView.setClickable(true);
                        contactInfoView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // 绑定手机号
                                Intent intent = new Intent(MyHomeArchivesActivity.this, BindPhoneGuideActivity.class);
                                startActivityForResult(intent, 1);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        });
                    }
                    ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View arg0) {
                            new ContentUtil().createCopyDialog(MyHomeArchivesActivity.this, mobile);
                            return true;
                        }
                    });
                    ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            contactInfoView.performClick();
                        }

                    });
                    mContactLayout.addView(contactInfoView);
                }
            }
            if (!TextUtils.isEmpty(email)) {
                View contactInfoView = LayoutInflater.from(this).inflate(R.layout.contact_info, null);
                ((TextView) contactInfoView.findViewById(R.id.titleTv)).setText("邮箱");
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setText(email);
                if (!result.isSelf()) {
                    ((TextView) contactInfoView.findViewById(R.id.valueTv))
                            .setTextColor(getResources().getColor(R.color.room_at_color));
                    ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Intent data = new Intent(Intent.ACTION_SENDTO);
                            data.setData(Uri.parse("mailto:" + email));
                            startActivity(data);
                        }
                    });
                }
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View arg0) {
                        new ContentUtil().createCopyDialog(MyHomeArchivesActivity.this, email);
                        return true;
                    }
                });
                if (TextUtils.isEmpty(tel)) {
                    contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
                }
                mContactLayout.addView(contactInfoView);
            }
            if (!TextUtils.isEmpty(tel)) {
                View contactInfoView = LayoutInflater.from(this).inflate(R.layout.contact_info, null);
                ((TextView) contactInfoView.findViewById(R.id.titleTv)).setText("固话");
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setText(tel);
                if (!result.isSelf()) {
                    ((TextView) contactInfoView.findViewById(R.id.valueTv))
                            .setTextColor(getResources().getColor(R.color.room_at_color));
                }
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                        startActivity(intent);
                    }
                });
                ((TextView) contactInfoView.findViewById(R.id.valueTv)).setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View arg0) {
                        new ContentUtil().createCopyDialog(MyHomeArchivesActivity.this, tel);
                        return true;
                    }
                });
                contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
                mContactLayout.addView(contactInfoView);
            }

            if (result.isConnection()) {
                if (cInfo.isImValid()) {
                    imId = cInfo.getImId();
                    newEditButton.setText("聊天");
                } else {
                    newEditButton.setText("写站内信");
                }
            }
        }
    }

    /**
     * 更换封面
     *
     * @param context
     */
    public void createCoverDialog(Context context) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(R.array.create_cover_item).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "update_archive_cover");
                        Intent intent = new Intent(MyHomeArchivesActivity.this, UpdateCoverActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_UPDATE_COVER);
                        MyHomeArchivesActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                    default:
                        break;
                }

            }
        });
        materialDialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderIcon(android.R.drawable.ic_menu_more);
        menu.setHeaderTitle("请选择");
        menu.add(0, 0, 0, "通过站内信分享");
        menu.add(0, 1, 0, "通过手机短信分享");
        menu.add(0, 2, 0, "通过email分享");
        menu.add(0, 3, 0, "发送给微信好友");
    }

    // 选择本地图片回调标识
    public static final int REQUEST_CODE_CHOOSE_PICTURE = 1001;
    public static final int REQUEST_CODE_CAPTURE_CUT = 1003;
    public static final int REQUEST_CODE_CHOOSE_CAPTURE = 1004;
    private String mFilePath = "";

    /**
     * 启动照相机
     */
    @SuppressLint("SimpleDateFormat")
    private void startCamera() {
        try {
            // 获取照片保存的文件路径
            mFilePath = ExternalStorageUtil.getPicCacheDataPath(this, getRenheApplication().getUserInfo().getEmail());
            String lFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            mFilePath = mFilePath + File.separator + lFileName + ".jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = Uri.fromFile(new File(mFilePath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_CAPTURE);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * 启动照片浏览
     */
    @SuppressWarnings("unused")
    private void startGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "请选择照片"), REQUEST_CODE_CHOOSE_PICTURE);
    }

    @SuppressWarnings("unused")
    private void sendMail() {
        if (mProfile != null && mProfile.getUserInfo() != null && mProfile.getUserInfo().getContactInfo() != null) {
            Uri uri = Uri.parse("mailto:" + mProfile.getUserInfo().getContactInfo().getEmail());
            Intent email = new Intent(Intent.ACTION_SENDTO, uri);
            startActivity(email);
        } else {
            ToastUtil.showToast(this, "无邮件箱信息");
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAvatarImgV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(MyHomeArchivesActivity.this, "see_big_avartar");
                if (!TextUtils.isEmpty(mProfile.getUserInfo().getLargeUserface())) {
                    CharSequence[] middlePics = new CharSequence[1];
                    middlePics[0] = mProfile.getUserInfo().getLargeUserface();
                    Intent intent = new Intent(MyHomeArchivesActivity.this, ViewPhotoActivity.class);
                    intent.putExtra("ID", 0);
                    intent.putExtra("middlePics", middlePics);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_enter, 0);
                }
            }
        });
        //档案页面动态按钮点击事件
        mRoomBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(MyHomeArchivesActivity.this, "see_friend_msg");
                if (mProfile != null) {
                    Intent intent = new Intent(MyHomeArchivesActivity.this, PersonalMessageBoardActivity.class);
                    intent.putExtra("viewSid", mOtherSid);
                    if (!mProfile.isSelf()) {
                        intent.putExtra("friendName", mProfile.getUserInfo().getName());
                    }
                    intent.putExtra("userinfo", mProfile.getUserInfo());
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        });

        mContactBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(MyHomeArchivesActivity.this, "see_friend_contact");
                if (mProfile != null) {

                    if (mProfile.isConnection()) {
                        // 别人
                        Bundle bundle = new Bundle();
                        bundle.putString("sid", mOtherSid);
                        if (mProfile.isConnection()) {
                            bundle.putString("friendName", mProfile.getUserInfo().getName());
                        }
                        startActivity(SearchResultByContactsActivity.class, bundle);// 界面换
                    } else if (mProfile.isSelf()) {
                        // 自己
                        //更新本地联系人列表，maxid
                        new ContactsUtil(MyHomeArchivesActivity.this).SyncContacts();
                        Bundle bundle = new Bundle();
                        bundle.putString("sid", mOtherSid);
                        Intent intent = new Intent(MyHomeArchivesActivity.this, SearchContactsFragmentActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        if (RenheApplication.getInstance().getUserInfo().getAccountType() > 0) {//VIP及以上会员可查看陌生人的人脉
                            Bundle bundle = new Bundle();
                            bundle.putString("sid", mOtherSid);
                            if (mProfile.isConnection()) {
                                bundle.putString("friendName", mProfile.getUserInfo().getName());
                            }
                            startActivity(SearchResultByContactsActivity.class, bundle);// 界面换
                        } else {
                            //普通会员查看陌生人人脉，给予弹框升级引导提示
                            CheckUpgradeUtil.showUpgradeGuideDialog(MyHomeArchivesActivity.this, R.drawable.upgrade_guide_3, R.string.upgrade_guide_check_strangeness_contacts_title,
                                    R.string.upgrade_guide_check_strangeness_contacts_sub_title, R.string.upgrade_upgrade_now, UpgradeActivity.class, 5);
                        }
                    }
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        });

        newEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mProfile != null) {
                    if (mProfile.isSelf()) {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "edit_profile");
                        Intent intent = new Intent(MyHomeArchivesActivity.this, EditMyHomeArchivesActivity.class);
                        intent.putExtra("Profile", mProfile);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if (mProfile.isConnection()) {
                        // 发Im消息
                        if (mProfile.getUserInfo().getContactInfo().isImValid()) {
                            createConversation();
                        }
                    } else if (mProfile.isInvite()) {

                    } else if (!mProfile.isInvite() && !mProfile.getBeInvitedInfo().isBeInvited()) {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "add_friend");
                        Intent i = new Intent(MyHomeArchivesActivity.this, AddFriendAct.class);
                        i.putExtra("mSid", mOtherSid);
                        i.putExtra("from", isFrom);
                        i.putExtra("position", position);
                        i.putExtra("addfriend_from", fromClass);
                        i.putExtra("friendName", mProfile.getUserInfo().getName());
                        String circleName = getIntent().getStringExtra("circleName");
                        if (!TextUtils.isEmpty(circleName)) {
                            i.putExtra("circleName", circleName);
                        }
                        startActivityForResult(i, 99);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if (mProfile.getBeInvitedInfo().isBeInvited()) {
                        int inviteId = mProfile.getBeInvitedInfo().getInviteId();
                        int inviteType = mProfile.getBeInvitedInfo().getInviteType();
                        // 接受邀请，成功-改变按钮状态
                        new ReceiveFriend(MyHomeArchivesActivity.this) {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void doPre() {
                                showDialog(4);
                            }

                            @SuppressWarnings("deprecation")
                            @Override
                            public void doPost(ReceiveAddFriend result) {
                                removeDialog(4);
                                if (result == null) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this,
                                            getString(R.string.connect_server_error));
                                } else if (result.getState() == 1) {
                                    // 掉本界面接口刷新
                                    new ProfileTask(true, false).execute(mOtherSid,
                                            RenheApplication.getInstance().getUserInfo().getSid(),
                                            RenheApplication.getInstance().getUserInfo().getAdSId());
                                    if ("newFriend".equals(fromClass)) {
                                        // 通知新的好友界面更新
                                        Intent intent = new Intent(NewFriendsAct.UPDATE_LISTITEM);
                                        intent.putExtra("position", position);
                                        intent.putExtra("isReceive", true);
                                        sendBroadcast(intent);
                                    } else if ("guessInterest".equals(fromClass)) {
                                        //通知感兴趣列表
                                        Intent intent = new Intent(GuessInterestActivity.UPDATE_LIST_ITEM);
                                        intent.putExtra("position", position);
                                        intent.putExtra("isReceive", true);
                                        sendBroadcast(intent);
                                    } else if ("mobile".equals(fromClass)) {
                                        //通知手机通讯录界面
                                        Intent intent2 = new Intent(MobileMailList.UPDATE_LISTITEM);
                                        intent2.putExtra("position", position);
                                        sendBroadcast(intent2);
                                    } else if ("email".equals(fromClass)) {
                                        Intent intent2 = new Intent(MailBoxList.UPDATE_LISTITEM);
                                        intent2.putExtra("position", position);
                                        sendBroadcast(intent2);
                                    } else if (position != -1 && "advanceSearchResult".equals(fromClass)) {//来自高级搜索的结果页面
                                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.ADVANCE_SEARCH, position, NotifyListRefreshWithPositionEvent.MODE_ACCEPT));
                                    } else if (position != -1 && "renmaiSearchResultMore".equals(fromClass)) {//来自人脉搜索，发现人脉查看更多的页面
                                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.RENMAI_SEARCH_MORE, position, NotifyListRefreshWithPositionEvent.MODE_ACCEPT));
                                    } else if (position != -1 && "renmaiSearchResult".equals(fromClass)) {//来自人脉搜索的页面
                                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.RENMAI_SEARCH, position, NotifyListRefreshWithPositionEvent.MODE_ACCEPT));
                                    }
                                    checkUpgradeUtil.checkUpgrade();//检测是否要提醒升级的弹框
                                    //更新本地联系人列表，maxid
                                    new ContactsUtil(MyHomeArchivesActivity.this).SyncContacts();
                                } else if (result.getState() == -1) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, R.string.lack_of_privilege);
                                } else if (result.getState() == -2) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, R.string.sorry_of_unknow_exception);
                                } else if (result.getState() == -3) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "邀请序号不存在！");
                                } else if (result.getState() == -4) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "邀请类型不存在！");
                                } else if (result.getState() == -5) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "接受类型不存在！");
                                } else if (result.getState() == -6) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "您无权进行此操作！");
                                } else if (result.getState() == -7) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "您已经通过该请求了！");
                                } else if (result.getState() == -8) {
                                    ToastUtil.showErrorToast(MyHomeArchivesActivity.this, "您已经拒绝过该请求！");
                                }
                            }
                        }.execute("" + inviteId, "" + inviteType, "true");

                    }
                }
            }
        });

        corverIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mProfile != null) {
                    if (mProfile.isSelf()) {
                        createCoverDialog(MyHomeArchivesActivity.this);
                    }
                }
            }
        });

        mCityTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MyHomeArchivesActivity.this, SearchResultActivity.class);
                i.putExtra("citycode", cityCode);
                startActivity(i);
            }
        });

        mIndustryTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MyHomeArchivesActivity.this, SearchResultActivity.class);
                i.putExtra("industryCode", industryCode);
                startActivity(i);
            }
        });

        mScrollView.setOnScrollListener(this);
        titleTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });
        shareIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(MyHomeArchivesActivity.this, "friend_archivve_share");
                new ShareProfilePopupWindow(MyHomeArchivesActivity.this, rootRl, userName, userDesp, userFaceUrl, mOtherSid,
                        mProfile, true);
            }
        });
        checkMoreArchiveTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                checkMoreArchiveTv.setVisibility(View.GONE);
                //显示全部联系方式
                contactView(mProfile, mProfile.getUserInfo().getContactInfo(), true);
                moreArchiveLl.setVisibility(View.VISIBLE);
                //				initView(mProfile);
                //和聊统计
                String content = "5.128" + LoggerFileUtil.getConstantInfo(MyHomeArchivesActivity.this) + "|" + mOtherSid;
                LoggerFileUtil.writeFile(content, true);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.uploading_avtar).cancelable(false)
                        .build();
            case 2:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.data_loading).cancelable(false).build();
            case 3:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.sending).cancelable(false).build();
            case 4:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.handling).cancelable(false).build();
            case 5:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.corver_uploading).cancelable(false)
                        .build();
            default:
                return null;
        }
    }

    Handler handler = new Handler();

    class RefreshArchieveReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String comeFrom = arg1.getStringExtra("From");
            if (!TextUtils.isEmpty(comeFrom) && comeFrom.equals("MyhomeArchivesActivity")) {
                //本页面发的广播不做处理
            } else {
                if (arg1.getSerializableExtra("Profile") != null) {
                    mProfile = (Profile) arg1.getSerializableExtra("Profile");
                    populateData(mProfile);
                } else {
                    getUsrInfo(true);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updateAvarImage) {
            unregisterReceiver(updateAvarImage);
        }
        if (null != reFreshArchieveReceiver) {
            unregisterReceiver(reFreshArchieveReceiver);
            reFreshArchieveReceiver = null;
        }
        if (null != updateCoverImage) {
            unregisterReceiver(updateCoverImage);
        }

        if (localBitmap != null && !localBitmap.isRecycled()) {
            localBitmap.recycle();
            localBitmap = null;
        }
        AsyncImageLoader.getInstance().clearCache();
    }

    private void stripUnderlines(TextView textView) {
        if (textView.getText() instanceof Spannable) {
            Spannable s = (Spannable) textView.getText();
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span : spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }
            textView.setText(s);
        }
    }

    // 需要一个自定义的URLSpan，不用启动TextPaint的“下划线”属性:

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.gc();
        switch (requestCode) {
            // 回调好友请求发送结果
            case 99:
                if (resultCode == 99) {
                    if (null != checkUpgradeUtil)
                        checkUpgradeUtil.checkUpgrade();//检测是否要提醒升级的弹框
                    getUsrInfo(true);
//                    newEditButton.setText("已邀请");
//                    newEditButton.setEnabled(false);
//                    newEditButton.setTextColor(getResources().getColor(R.color.archive_editbt_text_selected));
                } else if (resultCode == 88) {
                }
                break;
            // 回调照片本地浏览
            case REQUEST_CODE_CHOOSE_PICTURE:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        localBitmap = null;
                        ContentResolver resolver = getContentResolver();
                        try {
                            Uri originalUri = data.getData(); // 获得图片的uri
                            localBitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri); // 显得到bitmap图片
                            //		cacheBitmapList.add(localBitmap);
                            // 这里开始的第二部分，获取图片的路径：
                            String[] proj = {MediaStore.Images.Media.DATA};
                            Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                            // 按我个人理解 这个是获得用户选择的图片的索引值
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            // 最后根据索引值获取图片路径
                            String path = cursor.getString(column_index);
                            startCustomPhotoZoom(path, false);
                        } catch (IOException e) {
                        }
                    }
                }
                break;
            case REQUEST_CODE_CHOOSE_PICTURE_KITKAT:
                if (resultCode == RESULT_OK) {
                    startCustomPhotoZoom(getPath(this, data.getData()), false);
                }
                break;
            // 回调照片剪切
            case REQUEST_CODE_CAPTURE_CUT:
                break;
            // 回调照相机
            case REQUEST_CODE_CHOOSE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    File tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                    startCustomPhotoZoom(tempFile.getPath(), false);
                }
                break;
            // 绑定手机
            case 1:
                if (resultCode == RESULT_OK) {
                    new ProfileTask(false, false).executeOnExecutor(Executors.newCachedThreadPool(), mOtherSid,
                            RenheApplication.getInstance().getUserInfo().getSid(),
                            RenheApplication.getInstance().getUserInfo().getAdSId());
                }
                break;

            case REQUEST_CODE_UPDATE_COVER:
                if (resultCode == RESULT_OK) {
                    final String path = data.getStringExtra("cropImagePath");
                    if (!TextUtils.isEmpty(path)) {
                        requestBitmap = BitmapFactory.decodeFile(path);
                        final File file = new File(path);
                        if (null != file && file.exists()) {
                            final String url = Constants.Http.UPLOAD_COVER + "?sid="
                                    + RenheApplication.getInstance().getUserInfo().getSid() + "&adSId="
                                    + RenheApplication.getInstance().getUserInfo().getAdSId();
                            new AsyncTask<Object, Void, UploadCover>() {

                                @Override
                                protected UploadCover doInBackground(Object... params) {
                                    try {
                                        return (UploadCover) HttpUtil.uploadFile(url, file, "image", UploadCover.class);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    showDialog(5);
                                }

                                @Override
                                protected void onPostExecute(UploadCover result) {
                                    super.onPostExecute(result);
                                    removeDialog(5);
                                    if (null != result && (1 == result.getState())) {
                                        coverDefaultRl.setVisibility(View.GONE);
                                        com.itcalf.renhe.dto.Profile.UserInfo userInfo = mProfile.getUserInfo();
                                        userInfo.setCover(result.getCover());
                                        mProfile.setUserInfo(userInfo);
                                        CacheManager.getInstance().populateData(MyHomeArchivesActivity.this).saveObject(mProfile,
                                                RenheApplication.getInstance().getUserInfo().getEmail(), CacheManager.PROFILE);

                                        boolean flag = false;
                                        if (!TextUtils.isEmpty(path)) {
                                            File file = new File(path);
                                            if (null != file && file.exists()) {
                                                flag = true;
                                                Uri uri = Uri.fromFile(file);
                                                corverIv.setImageURI(uri);
                                            }
                                        }
                                        if (!flag) {
                                            String userFace = result.getCover();
                                            if (!TextUtils.isEmpty(userFace)) {
                                                try {
                                                    imageLoader.displayImage(userFace, corverIv, coverOptions);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        DeleteSDCardImageUtil.sdScan(MyHomeArchivesActivity.this,
                                                Environment.getExternalStorageDirectory() + File.separator
                                                        + UpdateCoverActivity.COVER_IMAGE_FILE_NAME);//删除缓存crop图片

                                    } else {
                                        ToastUtil.showToast(MyHomeArchivesActivity.this, "更换封面失败!");
                                    }
                                }

                            }.executeOnExecutor(Executors.newCachedThreadPool());
                        }
                    }
                }
                break;
            case Constants.ARCHIVE_REQUEST_CODE.REQUEST_CODE_ARCHIVE_MORE:
                if (resultCode == RESULT_OK && null != data) {
                    if (data.getBooleanExtra("isDeleted", false)) {
                        onBackPressed();
                        break;
                    }

                    Profile.UserInfo userInfo = mProfile.getUserInfo();
                    if (data.getBooleanExtra("isBlocked", false)) {
                        userInfo.setBlocked(true);
                        newEditButton.setBackgroundColor(getResources().getColor(R.color.BC_1));
                        newEditButton.setEnabled(false);
                    } else {
                        userInfo.setBlocked(false);
                        newEditButton.setBackgroundColor(getResources().getColor(R.color.hl_button_color));
                        newEditButton.setEnabled(true);
                    }
                    mProfile.setUserInfo(userInfo);
                    if (data.getBooleanExtra("isCollected", false)) {
                        mProfile.setIsCollected(true);
                    } else {
                        mProfile.setIsCollected(false);
                    }
                }
                break;
            case Constants.ARCHIVE_REQUEST_CODE.REQUEST_CODE_COMPANY_AUTH:
                getUsrInfo(true);
                break;
        }

    }

    /**
     * 调用自定义裁剪工具裁剪图片方法实现
     */
    public void startCustomPhotoZoom(String path, boolean isToCover) {
        Intent intent = new Intent(MyHomeArchivesActivity.this, com.itcalf.renhe.context.cropImage.CropImage.class);
        // 设置裁剪
        intent.putExtra("path", path);
        intent.putExtra("istocover", false);
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_CUT);
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public class PopupWindows extends PopupWindow {

        public PopupWindows(Context mContext, View parent, final String mobile, final boolean isToTel) {

            final View view = View.inflate(mContext, R.layout.item_popupwindows, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

            setWidth(LayoutParams.MATCH_PARENT);
            setHeight(LayoutParams.WRAP_CONTENT);
            ColorDrawable cd = new ColorDrawable(0000);
            setBackgroundDrawable(cd);
            // setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();
            view.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = view.findViewById(R.id.ll_popup).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
            Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
            Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
            Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
            if (isToTel) {
                bt1.setText("拨打电话");
                bt2.setText("发送短信");
                bt3.setText("复制");
            }
            bt1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (isToTel) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
                        startActivity(intent);
                    } else {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "refresh_avartar_camera");
                        // photo();
                        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 判断存储卡是否可以用，可用进行存储
                        // if (hasSdcard()) {
                        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                        // }
                        startActivityForResult(intentFromCapture, REQUEST_CODE_CHOOSE_CAPTURE);
                    }
                    dismiss();
                }
            });
            bt2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (isToTel) {
                        Uri uri = Uri.parse("smsto:" + mobile);
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        startActivity(it);
                    } else {
                        MobclickAgent.onEvent(MyHomeArchivesActivity.this, "refresh_avartar_pic");
                        if (Build.VERSION.SDK_INT < 19) {
                            Intent intentFromGallery = new Intent();
                            intentFromGallery.setType("image/*"); // 设置文件类型
                            intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICTURE);
                        } else {
                            Intent intentFromGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
                            intentFromGallery.setType("image/*");
                            startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICTURE_KITKAT);
                        }

                    }
                    dismiss();
                }
            });
            bt3.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (isToTel) {
                        ContentUtil.copy(mobile, MyHomeArchivesActivity.this);
                        ContentUtil.showToast(MyHomeArchivesActivity.this, "内容已经复制到剪贴板");
                    }
                    dismiss();
                }
            });

        }
    }

    class UpdateAvarImage extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String cropPath = FileUtil.SDCARD_PAHT + "/crop.png";
            boolean flag = false;
            if (!TextUtils.isEmpty(cropPath)) {
                File file = new File(cropPath);
                if (null != file && file.exists()) {
                    flag = true;
                    Uri uri = Uri.fromFile(file);
                    mAvatarImgV.setImageURI(uri);
                }
            }
            if (!flag) {
                String userFace = arg1.getStringExtra("userface");
                if (!TextUtils.isEmpty(userFace)) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    try {
                        imageLoader.displayImage(userFace, mAvatarImgV);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    // 头像背景
    class UpdateCoverImage extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            coverDefaultRl.setVisibility(View.GONE);
            String cover = arg1.getStringExtra("cover");
            String coverPath = arg1.getStringExtra("cover_path");

            if (!TextUtils.isEmpty(coverPath)) {
                try {
                    imageLoader.displayImage(coverPath, corverIv, coverOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (!TextUtils.isEmpty(cover)) {
                    try {
                        imageLoader.displayImage(cover, corverIv, coverOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!TextUtils.isEmpty(cover)) {
                com.itcalf.renhe.dto.Profile.UserInfo userInfo = mProfile.getUserInfo();
                userInfo.setCover(cover);
                mProfile.setUserInfo(userInfo);
                CacheManager.getInstance().populateData(MyHomeArchivesActivity.this).saveObject(mProfile,
                        RenheApplication.getInstance().getUserInfo().getEmail(), CacheManager.PROFILE);
            }

        }
    }

    /**
     * 创建IM会话
     */
    private void createConversation() {
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(this, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(mProfile.getUserInfo().getName());
        com.alibaba.wukong.im.Message message = null; // 创建会话发送的系统消息,可以不设置
        int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                ConversationListUtil.updateChatConversationExtension(conversation, title.toString(), mProfile.getUserInfo().getUserface());
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Intent intent = new Intent(MyHomeArchivesActivity.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                intent.putExtra("userName", title.toString());
                intent.putExtra("userFace", mProfile.getUserInfo().getUserface());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                if (code.equals("101004") || reason.contains("LOGIN")) {
                    ConversationListUtil.loginIMError(R.string.account_exception);
                }
                Toast.makeText(MyHomeArchivesActivity.this, "创建会话失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), mProfile.getUserInfo().getUserface(), message, convType, Long.parseLong(imId + ""));
    }

    @Override
    public void onScrollChanged(int x, int y, int oldX, int oldY) {

    }

    @Override
    public void onScrollStopped() {

    }

    @Override
    public void onScrolling() {
        //		if (mScrollView.getScrollY() >= coverHeight) {
        //			if(null != toolbar){
        //				toolbar.setBackground(getResources().getDrawable(R.drawable.archive_actionbar_bcg_shape));
        //			}
        //		} else {
        //			if(null != toolbar){
        //				toolbar.getBackground().setAlpha(0);
        //			}
        //		}

    }

    class PupBean {
        public String title;
        public int icon;

        public PupBean(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

    }

    class PupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(MyHomeArchivesActivity.this).inflate(R.layout.item_popup_add_layout, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
            PupBean bean = list.get(position);
            textView.setText(bean.title);
            textView.setCompoundDrawablesWithIntrinsicBounds(bean.icon, 0, 0, 0);
            return convertView;
        }
    }

    /**
     * 二维码图片下载
     *
     * @author Administrator
     */
    public class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            createSelfTwoCodeDialog(MyHomeArchivesActivity.this, mProfile.getUserInfo().getUserface(), selfTwoDimenCodeUrl,
                    mProfile.getUserInfo().getName(), mProfile.getUserInfo().getTitle(), mProfile.getUserInfo().getCompany(),
                    mProfile.getUserInfo().getLocation());
        }
    }

    public void createSelfTwoCodeDialog(Context context, String userFaceUrl, String twoCodeUrl, String name, String job,
                                        String company, String addressCity) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.custom_two_dimencode_dialog, null);
        selfTwoCodeAlertDialog = new Dialog(this, R.style.TranslucentUnfullwidthWinStyle);
        selfTwoCodeAlertDialog.setContentView(view);
        selfTwoCodeAlertDialog.setCanceledOnTouchOutside(true);
        android.view.Window dialogWindow = selfTwoCodeAlertDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = (int) (dm.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);

        dialogAvatarIv = (ImageView) view.findViewById(R.id.avatar_img);
        dialogTwoCodeIv = (ImageView) view.findViewById(R.id.showImg);
        dialogNameTv = (TextView) view.findViewById(R.id.nickname_txt);
        dialogJobTv = (TextView) view.findViewById(R.id.company_txt);
        dialogAddressTv = (TextView) view.findViewById(R.id.job_txt);
        despTv = (TextView) view.findViewById(R.id.desp_tv);

        dialogNameTv.setText(name);
        if (!TextUtils.isEmpty(job)) {
            dialogJobTv.setText(job);
        }
        if (company != null && company != "") {
            dialogJobTv.setText(dialogJobTv.getText().toString().trim() + " / " + company);
        }
        if (TextUtils.isEmpty(dialogJobTv.getText().toString().trim())) {
            dialogJobTv.setVisibility(View.GONE);
        } else {
            dialogJobTv.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(addressCity) && !TextUtils.isEmpty(addressCity.trim())) {
            dialogAddressTv.setVisibility(View.VISIBLE);
            dialogAddressTv.setText(addressCity);
        } else {
            dialogAddressTv.setVisibility(View.GONE);
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(userFaceUrl, dialogAvatarIv, CacheManager.options, CacheManager.animateFirstDisplayListener);
        imageLoader.displayImage(twoCodeUrl, dialogTwoCodeIv, CacheManager.imageOptions,
                CacheManager.imageAnimateFirstDisplayListener);
        selfTwoCodeAlertDialog.show();
    }
}
