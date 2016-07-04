package com.itcalf.renhe.context.fragmentMain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.NameAuthStatusRes;
import com.itcalf.renhe.bean.TabHasNewBean;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.EditMyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.auth.NameAuthActivity;
import com.itcalf.renhe.context.auth.NameAuthStatusTask;
import com.itcalf.renhe.context.collect.CollectionsActivity;
import com.itcalf.renhe.context.luckymoney.MyWalletDetailActivity;
import com.itcalf.renhe.context.more.AccountLimitActivity;
import com.itcalf.renhe.context.more.MySettingActivity;
import com.itcalf.renhe.context.more.TwoDimencodeActivity;
import com.itcalf.renhe.context.template.BaseFragment;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.view.WebViewCompanyAuthActivity;
import com.itcalf.renhe.widget.pullzoomview.PullToZoomScrollViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import cn.renhe.heliao.idl.config.ModuleConfig;
import cn.renhe.heliao.idl.member.MyModuleNotice;

/**
 * Created by wangning on 2016/3/1.
 */
public class MyFragment extends BaseFragment implements View.OnClickListener {
    //UI
    private PullToZoomScrollViewEx scrollView;
    private ImageView avatarIv;
    private TextView userNameTv;
    private TextView userTitleTv;
    private ImageView acountTypeImage;
    private TextView identifyTv;
    private RelativeLayout upgradeRl;
    private TextView upgradeTv;
    private ImageView upgradeIv;
    private ImageView upgradeTopIv;
    private RelativeLayout hecaifuRl;
    private RelativeLayout zanfuwuRl;
    private TextView hecaifuTv;
    private TextView hecaifuTipTv;
    private TextView zanfuwuTv;
    private TextView zanfuwuTipTv;
    private RelativeLayout completeLl;
    private RelativeLayout twocodeLl;
    private RelativeLayout limitLl;
    private RelativeLayout collectLl;
    private RelativeLayout settingLl;
    private RelativeLayout walletLl;
    private RelativeLayout companyAuthRl;
    private ImageView completeIv;//完善资料小红点
    private TextView collectNewFlagTv;//收藏 new标签
    private TextView walletNewFlagTv;//钱包 new标签
    private TextView companyAuthNewFlagTv;//企业查询 new标签
    private LinearLayout webLl;//和财富、赞服务
    private ImageView hecaifuUnreadIv, zanfuwuUnreadIv;//和财富、赞服务小红点提示
    //数据
    private UserInfo userInfo;
    private ImageLoader imageLoader;
    public static int realNameStatus = -1; //-1 未认证  0认证中  1已认证
    private String hecaifuUrl = "";
    private String zanfuwuUrl = "";
    private int hecaifuModuleId, zanfuwuModuleId;
    //常量
    private int ID_TASK_MODULE_CONFIG = TaskManager.getTaskId();//获取和财富、赞服务
    private int ID_TASK_CLICK_MODULE = TaskManager.getTaskId();//点击和财富、赞服务
    private long lastRefreshTime = 0;
    private static final long RFRESH_TIME = 1000 * 60 * 60 * 24;
    //广播
    private UpdateAvarImage updateAvarImage;
    private RefreshArchieveReceiver reFreshArchieveReceiver;
    private RefreshStatusReceiver reFreshStatusReceiver;
    private DataCompleteReceiver dataCompleteReceiver;

    @Override
    protected void initLayoutId() {
        layoutId = R.layout.fragment_me_version2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        scrollView.setParallax(false);
        loadViewForCode(view);
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenWidth = localDisplayMetrics.widthPixels;
        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, (int) (9.0F * (mScreenWidth / 16.0F)));
        scrollView.setHeaderLayoutParams(localObject);
    }

    @Override
    protected void initData() {
        super.initData();
        this.userInfo = RenheApplication.getInstance().getUserInfo();
        this.imageLoader = ImageLoader.getInstance();
        initViewByInfo();
    }

    @Override
    protected void initListener() {
        super.initListener();
        avatarIv.setOnClickListener(this);
        userNameTv.setOnClickListener(this);
        userTitleTv.setOnClickListener(this);
        identifyTv.setOnClickListener(this);
        upgradeTv.setOnClickListener(this);
        upgradeRl.setOnClickListener(this);
        upgradeIv.setOnClickListener(this);
        upgradeTopIv.setOnClickListener(this);
        hecaifuRl.setOnClickListener(this);
        zanfuwuRl.setOnClickListener(this);
        completeLl.setOnClickListener(this);
        twocodeLl.setOnClickListener(this);
        limitLl.setOnClickListener(this);
        collectLl.setOnClickListener(this);
        settingLl.setOnClickListener(this);
        walletLl.setOnClickListener(this);
        companyAuthRl.setOnClickListener(this);
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();
        updateAvarImage = new UpdateAvarImage();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BroadCastAction.UPDATE_AVATAR_ACTION);
        getActivity().registerReceiver(updateAvarImage, intentFilter);

        reFreshArchieveReceiver = new RefreshArchieveReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
        getActivity().registerReceiver(reFreshArchieveReceiver, intentFilter2);

        reFreshStatusReceiver = new RefreshStatusReceiver();
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction(Constants.BroadCastAction.ACTION_NAMEAUTHSTATUS);
        statusFilter.addAction(Constants.BroadCastAction.ACTION_NAMEAUTHRES);
        getActivity().registerReceiver(reFreshStatusReceiver, statusFilter);

        //注册完善资料程度广播监听器
        dataCompleteReceiver = new DataCompleteReceiver();
        IntentFilter dataCompleteFilter = new IntentFilter();
        dataCompleteFilter.addAction(TabMainFragmentActivity.COMPLETE_RECEIVER_ACTION);
        getActivity().registerReceiver(dataCompleteReceiver, dataCompleteFilter);
    }

    @Override
    protected void unRegisterReceiver() {
        super.unRegisterReceiver();
        if (null != dataCompleteReceiver) {
            getActivity().unregisterReceiver(dataCompleteReceiver);
            dataCompleteReceiver = null;
        }
        if (null != updateAvarImage) {
            getActivity().unregisterReceiver(updateAvarImage);
            updateAvarImage = null;
        }
        if (null != reFreshArchieveReceiver) {
            getActivity().unregisterReceiver(reFreshArchieveReceiver);
            reFreshArchieveReceiver = null;
        }

        if (null != reFreshStatusReceiver) {
            getActivity().unregisterReceiver(reFreshStatusReceiver);
            reFreshStatusReceiver = null;
        }
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (TaskManager.getInstance().exist(type))
            TaskManager.getInstance().removeTask(type);
        if (null != result) {
            if (result instanceof ModuleConfig.ModuleListResponse) {
                ModuleConfig.ModuleListResponse response = (ModuleConfig.ModuleListResponse) result;
                List<ModuleConfig.ModuleItem> list = response.getModuleItemList();
                if (null != list && !list.isEmpty()) {
                    webLl.setVisibility(View.VISIBLE);
                    if (list.size() >= 1) {
                        hecaifuTv.setText(list.get(0).getName());
                        hecaifuTipTv.setText(list.get(0).getNote());
                        hecaifuUrl = list.get(0).getUrl();
                        hecaifuModuleId = list.get(0).getModuleId();
                        if (list.get(0).getNotify()) {
                            hecaifuUnreadIv.setVisibility(View.VISIBLE);
                        } else {
                            hecaifuUnreadIv.setVisibility(View.GONE);
                        }
                        SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_EXIST, true, true);
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_TITLE, list.get(0).getName(), true);
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_TIP, list.get(0).getNote(), true);
                        SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_URL, list.get(0).getUrl(), true);
                        if (list.size() >= 2) {
                            zanfuwuTv.setText(list.get(1).getName());
                            zanfuwuTipTv.setText(list.get(1).getNote());
                            zanfuwuUrl = list.get(1).getUrl();
                            zanfuwuModuleId = list.get(1).getModuleId();
                            if (list.get(1).getNotify()) {
                                zanfuwuUnreadIv.setVisibility(View.VISIBLE);
                            } else {
                                zanfuwuUnreadIv.setVisibility(View.GONE);
                            }
                            SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_EXIST, true, true);
                            SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_TITLE, list.get(1).getName(), true);
                            SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_TIP, list.get(1).getNote(), true);
                            SharedPreferencesUtil.putStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_URL, list.get(1).getUrl(), true);
                        } else {
                            zanfuwuRl.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    webLl.setVisibility(View.GONE);
                }
                lastRefreshTime = System.currentTimeMillis();
                isTabmeShowRedIcon();
            } else if (result instanceof MyModuleNotice.MyModuleNoticeResponse) {
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
    }

    private void loadViewForCode(View view) {
        PullToZoomScrollViewEx scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_me_head_view, null, false);
        View zoomView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_me_zoom_view, null, false);
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_me_content_view, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);

        avatarIv = (ImageView) headView.findViewById(R.id.iv_user_head);
        userNameTv = (TextView) headView.findViewById(R.id.tv_user_name);
        userTitleTv = (TextView) headView.findViewById(R.id.tv_user_desp);
        acountTypeImage = (ImageView) headView.findViewById(R.id.vipImage);
        identifyTv = (TextView) headView.findViewById(R.id.tv_identify);
        upgradeRl = (RelativeLayout) headView.findViewById(R.id.upgrade_rl);
        upgradeTv = (TextView) headView.findViewById(R.id.tv_upgrade);
        upgradeIv = (ImageView) headView.findViewById(R.id.upgrade_iv);
        upgradeTopIv = (ImageView) headView.findViewById(R.id.upgrade_top_iv);
        hecaifuRl = (RelativeLayout) contentView.findViewById(R.id.hecaifuRl);
        zanfuwuRl = (RelativeLayout) contentView.findViewById(R.id.zanfuwuRl);
        hecaifuTv = (TextView) contentView.findViewById(R.id.hecaifu_tv);
        hecaifuTipTv = (TextView) contentView.findViewById(R.id.hecaifu_tip_tv);
        zanfuwuTv = (TextView) contentView.findViewById(R.id.zanfuwu_tv);
        zanfuwuTipTv = (TextView) contentView.findViewById(R.id.zanfuwu_tip_tv);
        completeLl = (RelativeLayout) contentView.findViewById(R.id.ly_complete);
        twocodeLl = (RelativeLayout) contentView.findViewById(R.id.ly_twocode);
        limitLl = (RelativeLayout) contentView.findViewById(R.id.ly_limit);
        collectLl = (RelativeLayout) contentView.findViewById(R.id.ly_collect);
        walletLl = (RelativeLayout) contentView.findViewById(R.id.ly_wallet);
        settingLl = (RelativeLayout) contentView.findViewById(R.id.ly_setting);
        companyAuthRl = (RelativeLayout) contentView.findViewById(R.id.company_auth_rl);
        completeIv = (ImageView) contentView.findViewById(R.id.iv_complete);//完善资料小红点
        collectNewFlagTv = (TextView) contentView.findViewById(R.id.new_flag_tv);//收藏 new标签
        walletNewFlagTv = (TextView) contentView.findViewById(R.id.wallet_new_flag_tv);//钱包 new标签
        companyAuthNewFlagTv = (TextView) contentView.findViewById(R.id.company_auth_new_flag_tv);//企业查询 new标签
        webLl = (LinearLayout) contentView.findViewById(R.id.ll_top);//和财富、赞服务
        hecaifuUnreadIv = (ImageView) contentView.findViewById(R.id.hecaifu_unread_iv);//和财富、赞服务
        zanfuwuUnreadIv = (ImageView) contentView.findViewById(R.id.zanfuwu_unread_iv);//和财富、赞服务
    }

    /**
     * 初始化界面
     */
    private void initViewByInfo() {
        try {
            imageLoader.displayImage(userInfo.getUserface(), avatarIv, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userNameTv.setText(userInfo.getName());
        initUserTitleByInfo(userInfo.getTitle(), userInfo.getCompany());
        getIdentifyInfo();
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_COLLECT_NEW, true, true))
            collectNewFlagTv.setVisibility(View.VISIBLE);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_WALLET_NEW, true, true))
            walletNewFlagTv.setVisibility(View.VISIBLE);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_COMPANY_AUTH_NEW, true, true))
            companyAuthNewFlagTv.setVisibility(View.VISIBLE);
        setMemberValue(userInfo.getAccountType());
        initModuleView();
        getModuleConfig();
    }

    /**
     * 初始化用户的title栏信息
     *
     * @param job
     * @param company
     */
    private void initUserTitleByInfo(String job, String company) {
        StringBuilder userTitleBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(job)) {
            userTitleBuilder.append(job);
            if (!TextUtils.isEmpty(company)) {
                userTitleBuilder.append(" / " + company);
            }
        } else {
            userTitleBuilder.append(company);
        }
        userTitleTv.setText(userTitleBuilder.toString());
    }

    /**
     * 获取实名认证信息
     */
    private void getIdentifyInfo() {
        realNameStatus = -1;
        if (userInfo.isRealName()) {
            realNameStatus = 1;
        } else {
            new NameAuthStatusTask(new NameAuthStatusTask.NameAuthStatusTaskListener() {
                @Override
                public void postExecute(NameAuthStatusRes result) {
                    if (result != null && result.state == 1) {
                        realNameStatus = result.realNameStatus;
                        showRealNameStatus(realNameStatus);
                    }
                }
            }).executeOnExecutor(Executors.newCachedThreadPool(), userInfo.getSid(), userInfo.getAdSId());
        }
        showRealNameStatus(realNameStatus);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_user_head:
                goToProfile();
                break;
            case R.id.tv_user_name:
                goToProfile();
                break;
            case R.id.tv_user_desp:
                goToProfile();
                break;
            case R.id.tv_identify:
                NameAuthActivity.launch(getActivity(), realNameStatus);
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                statisticsMap.put("type", "1");
                StatisticsUtil.statisticsCustomClickEvent(getActivity().getString(R.string.android_btn_pop_realname_click), 0, "", statisticsMap);
                break;
            case R.id.tv_upgrade:
                goToUpgrade();
                break;
            case R.id.upgrade_rl:
                goToUpgrade();
                break;
            case R.id.upgrade_iv:
                goToUpgrade();
                break;
            case R.id.upgrade_top_iv:
                goToUpgrade();
                break;
            case R.id.hecaifuRl:
                goToWeb(hecaifuUrl);
                if (hecaifuUnreadIv.getVisibility() == View.VISIBLE)
                    hecaifuUnreadIv.setVisibility(View.GONE);
                isTabmeShowRedIcon();
                clickModule(hecaifuModuleId);
                break;
            case R.id.zanfuwuRl:
                goToWeb(zanfuwuUrl);
                if (zanfuwuUnreadIv.getVisibility() == View.VISIBLE)
                    zanfuwuUnreadIv.setVisibility(View.GONE);
                isTabmeShowRedIcon();
                clickModule(zanfuwuModuleId);
                break;
            case R.id.ly_complete:
                goToComplete();
                break;
            case R.id.ly_twocode:
                Intent intent = new Intent(getActivity(), TwoDimencodeActivity.class);
                intent.putExtra("type", TwoDimencodeActivity.SHARE_TYPE_ARCHIVE);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.ly_limit:
                startActivity(new Intent(getActivity(), AccountLimitActivity.class));
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.ly_collect:
                startActivity(new Intent(getActivity(), CollectionsActivity.class));
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                if (collectNewFlagTv.getVisibility() == View.VISIBLE) {
                    collectNewFlagTv.setVisibility(View.GONE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_COLLECT_NEW, false, true);
                }
                break;
            case R.id.ly_wallet:
                startActivity(new Intent(getActivity(), MyWalletDetailActivity.class));
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                if (walletNewFlagTv.getVisibility() == View.VISIBLE) {
                    walletNewFlagTv.setVisibility(View.GONE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_WALLET_NEW, false, true);
                }
                break;
            case R.id.ly_setting:
//                test();
                getActivity().startActivity(new Intent(getActivity(), MySettingActivity.class));
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.company_auth_rl:
                Intent companyAuthIntent = new Intent(getActivity(), WebViewCompanyAuthActivity.class);
                companyAuthIntent.putExtra("url", Constants.TAB_ME_COMPANY_SEARCH_URL);
                startActivity(companyAuthIntent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                if (companyAuthNewFlagTv.getVisibility() == View.VISIBLE) {
                    companyAuthNewFlagTv.setVisibility(View.GONE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_COMPANY_AUTH_NEW, false, true);
                }
                break;
        }
    }


    /**
     * 跳转到个人档案
     */
    private void goToProfile() {
        Intent intent = new Intent(getActivity(), MyHomeArchivesActivity.class);
        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, userInfo.getSid());
        intent.putExtra("fromTag", "MyFragment");
        intent.putExtra("userface", userInfo.getUserface());
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * 跳转到升级页面
     */
    private void goToUpgrade() {
        startActivity(new Intent(getActivity(), UpgradeActivity.class));
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu4_upgrade_vip_click), 0, "", null);
    }

    /**
     * 跳转到完善资料页面
     */
    private void goToComplete() {
        //点击完善资料选项触发 是否查看完善资料接口 通知服务端让Tab(我)书签上小红点消失
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("sid", userInfo.getSid());
                reqParams.put("adSId", userInfo.getAdSId());
                try {
                    TabHasNewBean mb = (TabHasNewBean) HttpUtil.doHttpRequest(Constants.Http.LOOK_PERFECINFO, reqParams
                            , TabHasNewBean.class, getActivity());
                    if (mb == null) {//网络不好的情况下 发送广播通知 我（Tab）上小红点消失

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        MobclickAgent.onEvent(getActivity(), "ly_complete");//点击完善资料按钮 友盟统计
        MobclickAgent.onEvent(getActivity(), "edit_profile");
        //点击完善资料选项跳转到更新档案页面
        Intent intent = new Intent(getActivity(), EditMyHomeArchivesActivity.class);
        intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_DATA, userInfo.getSid());
        intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO, EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        if (completeIv.getVisibility() == View.VISIBLE) {
            completeIv.setVisibility(View.GONE);
        }
        isTabmeShowRedIcon();
    }

    private void goToWeb(String url) {
        if (TextUtils.isEmpty(url))
            return;
        startActivity(new Intent(getActivity(), WebViewActWithTitle.class).putExtra("url", url).putExtra("shareable", false));
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * 设置实名认证信息
     */
    private void showRealNameStatus(int status) {
        switch (status) {
            case 0:
                break;
            case 1:
                //认证成功后更新本地账户数据
                if (!userInfo.isRealName()) {
                    userInfo.setRealName(true);
                    RenheApplication.getInstance().getUserCommand().insertOrUpdate(userInfo);
                }
                break;
        }
    }

    /**
     * 初始化会员标示
     */
    private void setMemberValue(int vipType) {
        switch (vipType) {
            case 0:
                if (userInfo.isRealName()) {
                    acountTypeImage.setVisibility(View.VISIBLE);
                    acountTypeImage.setImageResource(R.drawable.archive_realname2x);
                } else {
                    acountTypeImage.setVisibility(View.GONE);
                }
                break;
            case 1:
                acountTypeImage.setVisibility(View.VISIBLE);
                acountTypeImage.setImageResource(R.drawable.archive_vip_2x);
                break;
            case 2:
                acountTypeImage.setVisibility(View.VISIBLE);
                acountTypeImage.setImageResource(R.drawable.archive_vip_2_2x);
                break;
            case 3:
                acountTypeImage.setVisibility(View.VISIBLE);
                acountTypeImage.setImageResource(R.drawable.archive_vip_3_2x);
                break;
            default:
                acountTypeImage.setVisibility(View.GONE);
                break;
        }
        if (vipType < 3) {
            upgradeIv.setVisibility(View.VISIBLE);
            upgradeIv.clearAnimation();
            upgradeIv.setImageResource(R.drawable.myfragment_upgrade_frame);
            if (upgradeIv.getDrawable() instanceof AnimationDrawable) {
                ((AnimationDrawable) upgradeIv.getDrawable()).start();
            }
        } else {
            upgradeIv.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化 和财富、赞服务栏目
     */
    private void initModuleView() {
        boolean hecaifuExist = SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_EXIST, false, true);
        boolean zanfuwuExist = SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_EXIST, false, true);
        if (hecaifuExist || zanfuwuExist) {
            webLl.setVisibility(View.VISIBLE);
            if (hecaifuExist) {
                hecaifuTv.setText(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_TITLE, "和财富", true));
                hecaifuTipTv.setText(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_TIP, "您的私人财富管家", true));
                hecaifuUrl = SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_HECAIFU_URL, "", true);
                if (zanfuwuExist) {
                    zanfuwuTv.setText(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_TITLE, "赞服务", true));
                    zanfuwuTipTv.setText(SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_TIP, "专业服务交易平台", true));
                    zanfuwuUrl = SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.MY_FRAGMENT_ZANFUWU_URL, "", true);
                } else {
                    zanfuwuRl.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            webLl.setVisibility(View.GONE);
        }
    }

    /**
     * 获取我的界面和财富、赞服务item是否需要显示
     */
    private void getModuleConfig() {
        if (TaskManager.getInstance().exist(ID_TASK_MODULE_CONFIG)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_MODULE_CONFIG);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.getModuleConfig(ID_TASK_MODULE_CONFIG);
    }

    /**
     * 点击我的界面和财富、赞服务item通知服务端
     */
    private void clickModule(int moduleId) {
        if (TaskManager.getInstance().exist(ID_TASK_CLICK_MODULE)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_CLICK_MODULE);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.clickModuleConfig(ID_TASK_CLICK_MODULE, moduleId);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (System.currentTimeMillis() - lastRefreshTime >= RFRESH_TIME) {
                getModuleConfig();
            }
        }
    }

    /**
     * 判断是否需要显示/隐藏“我的”tab上的小红点
     *
     * @return
     */
    private void isTabmeShowRedIcon() {
        if (null == getActivity())
            return;
        boolean isNeedShow;
        if (hecaifuUnreadIv.getVisibility() == View.VISIBLE || zanfuwuUnreadIv.getVisibility() == View.VISIBLE ||
                completeIv.getVisibility() == View.VISIBLE) {
            isNeedShow = true;
        } else {
            isNeedShow = false;
        }
        if (isNeedShow) {
            Intent intentComplete = new Intent();
            intentComplete.setAction(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt(TabMainFragmentActivity.TAB_FLAG, 4);
            bundle.putBoolean("showComplete", true);
            intentComplete.putExtras(bundle);
            getActivity().sendBroadcast(intentComplete);
        } else {
            Intent intentComplete = new Intent();
            intentComplete.setAction(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt(TabMainFragmentActivity.TAB_FLAG, 4);
            bundle.putString("hideComplete", "hideComplete");
            intentComplete.putExtras(bundle);
            getActivity().sendBroadcast(intentComplete);
        }
    }

    //广播接收
    class RefreshStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BroadCastAction.ACTION_NAMEAUTHSTATUS)) {
                Logger.e("收到广播----");
                int status = intent.getIntExtra("realNameStatus", -2);
                if (status != -2) {
                    if (status != realNameStatus) {
                        realNameStatus = status;
                        showRealNameStatus(realNameStatus);
                    }
                }

                int vipType = intent.getIntExtra("vipType", -1);
                Logger.e("vipType----" + vipType + "--userinfo---" + userInfo.getAccountType());
                if (vipType != -1) {
                    setMemberValue(vipType);
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.ACTION_NAMEAUTHRES)) {
                int resstatus = intent.getIntExtra("realNameRes", -2);
                if (resstatus != -2) {
                    realNameStatus = resstatus == 1 ? 1 : -1;
                    showRealNameStatus(realNameStatus);
                }
            }
        }
    }

    //广播 ，监听我（Tab）上资料完善程度 是否出现小红点
    class DataCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String dataComplete = intent.getStringExtra("dataComplete");
            if (!TextUtils.isEmpty(dataComplete)) {
                if (completeIv.getVisibility() == View.GONE) {
                    completeIv.setVisibility(View.VISIBLE);
                }
                isTabmeShowRedIcon();
            }
        }
    }

    //广播，更新头像
    class UpdateAvarImage extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String cropPath = FileUtil.SDCARD_PAHT + "/crop.png";
            boolean flag = false;
            if (!TextUtils.isEmpty(cropPath)) {
                File file = new File(cropPath);
                if (null != file && file.exists()) {
                    flag = true;
//                    Uri uri = Uri.fromFile(file);
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    try {
                        imageLoader.displayImage("file://" + file.getAbsolutePath(), avatarIv, CacheManager.circleImageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!flag) {
                String userFace = arg1.getStringExtra("userface");
                if (!TextUtils.isEmpty(userFace)) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    try {
                        imageLoader.displayImage(userFace, avatarIv, CacheManager.circleImageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class RefreshArchieveReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String userFace = arg1.getStringExtra("userface");
            if (!TextUtils.isEmpty(userFace)) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                try {
                    imageLoader.displayImage(userFace, avatarIv, CacheManager.circleImageOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (arg1.getSerializableExtra("Profile") != null) {
                Profile pf = (Profile) arg1.getSerializableExtra("Profile");
                String username = pf.getUserInfo().getName();
                if (username != null && username != "") {
                    userNameTv.setText(username);
                }
                initUserTitleByInfo(pf.getUserInfo().getTitle(), pf.getUserInfo().getCompany());
            }
        }
    }

    private void test() {
        int ID_TASK_TEST = TaskManager.getTaskId();
        if (TaskManager.getInstance().exist(ID_TASK_TEST)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_TEST);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.test(ID_TASK_TEST);
//        grpcController.sendLocalRequest(ID_TASK_TEST);
    }
}
