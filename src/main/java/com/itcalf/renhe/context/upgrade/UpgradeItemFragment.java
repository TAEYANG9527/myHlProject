package com.itcalf.renhe.context.upgrade;

/**
 * 升级到铂金会员
 * Created by wangning on 2016/3/28.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity;
import com.itcalf.renhe.context.pay.PayResultActivity;
import com.itcalf.renhe.context.template.BaseFragment;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.renhe.heliao.idl.vip.HeliaoVipInfo;

public class UpgradeItemFragment extends BaseFragment implements ChoosePayWayActivity.PayCallback {
    /**
     * VIEW
     */
    private LinearLayout latestUpgradedMemberLl;//最新升级的会员列表，暂时是3个人
    private TextView upgradeTitleTv, upgradeTitleMoreTv;
    private ImageView badgeIv, contactIv, inviteIv, viewIv;
    private TextView badgeTitleTv, contactTitleTv, inviteTitleTv, viewTitleTv;
    private TextView badgeSubTitleTv, contactSubTitleTv, inviteSubTitleTv, viewSubTitleTv;
    private TextView latestUpgradedMemberTv;
    private LinearLayout upgradeLl;
    private TextView upgradeLlTitleTv, upgradeLlSubTitleTv;
    /**
     * 数据
     */
    private int currentAccountType;//当前会员等级
    private int remainVipDays;//remainVipDays
    private HeliaoVipInfo.VipInfoItem vipInfoItem;
    private ImageLoader imageLoader;
    /**
     * 常量
     */
    private final static String VIP_INFO_ARG = "vip_info_arg";
    private final static String VIP_CURRENT_ACCOUNTTYPE = "vip_current_accounttype";
    private final static String VIP_REMAIN_VIPDAYS = "vip_remain_vipdays";
    private final static String VIP_MORE_INFO_URL = "http://m.renhe.cn/vip/vip_privileges.htm";//VIP会员查看更多特权的url
    private final static String GOLD_MORE_INFO_URL = "http://m.renhe.cn/vip/gold_privileges.htm";
    private final static String PT_MORE_INFO_URL = "http://m.renhe.cn/vip/plat_privileges.htm";

    /**
     * @param vipInfoItem
     * @return
     */
    public static UpgradeItemFragment newInstance(int accountType, int remainVipDays, HeliaoVipInfo.VipInfoItem vipInfoItem) {
        Bundle args = new Bundle();
        args.putSerializable(VIP_INFO_ARG, vipInfoItem);
        args.putInt(VIP_CURRENT_ACCOUNTTYPE, accountType);
        args.putInt(VIP_REMAIN_VIPDAYS, remainVipDays);
        UpgradeItemFragment fragment = new UpgradeItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initLayoutId() {
        layoutId = R.layout.upgrade_viewpager_item_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments().getSerializable(VIP_INFO_ARG))
            vipInfoItem = (HeliaoVipInfo.VipInfoItem) getArguments().getSerializable(VIP_INFO_ARG);
        currentAccountType = getArguments().getInt(VIP_CURRENT_ACCOUNTTYPE);
        remainVipDays = getArguments().getInt(VIP_REMAIN_VIPDAYS);
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        latestUpgradedMemberLl = (LinearLayout) view.findViewById(R.id.latest_upgraded_member_ll);
        upgradeTitleTv = (TextView) view.findViewById(R.id.upgrade_title_tv);
        upgradeTitleMoreTv = (TextView) view.findViewById(R.id.upgrade_title_more_tv);
        badgeIv = (ImageView) view.findViewById(R.id.badge_iv);
        contactIv = (ImageView) view.findViewById(R.id.contact_iv);
        inviteIv = (ImageView) view.findViewById(R.id.invite_iv);
        viewIv = (ImageView) view.findViewById(R.id.view_iv);
        badgeTitleTv = (TextView) view.findViewById(R.id.badge_title_tv);
        contactTitleTv = (TextView) view.findViewById(R.id.contact_title_tv);
        inviteTitleTv = (TextView) view.findViewById(R.id.invite_title_tv);
        viewTitleTv = (TextView) view.findViewById(R.id.view_title_tv);
        badgeSubTitleTv = (TextView) view.findViewById(R.id.badge_sub_title_tv);
        contactSubTitleTv = (TextView) view.findViewById(R.id.contact_sub_title_tv);
        inviteSubTitleTv = (TextView) view.findViewById(R.id.invite_sub_title_tv);
        viewSubTitleTv = (TextView) view.findViewById(R.id.view_sub_title_tv);
        upgradeLl = (LinearLayout) view.findViewById(R.id.upgrade_ll);
        upgradeLlTitleTv = (TextView) view.findViewById(R.id.upgrade_ll_title_tv);
        upgradeLlSubTitleTv = (TextView) view.findViewById(R.id.upgrade_ll_sub_title_tv);
        latestUpgradedMemberTv = (TextView) view.findViewById(R.id.latest_upgraded_member_tv);
    }

    @Override
    protected void initData() {
        super.initData();
        if (null == vipInfoItem)
            return;
        if (null != latestUpgradedMemberLl)
            latestUpgradedMemberLl.removeAllViews();
        imageLoader = ImageLoader.getInstance();
        initPrivilegeTitle();
        initLatestUpgradeMembersView();
        initUpgradeBtView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        upgradeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
                if (userInfo == null) {
                    ToastUtil.showToast(getActivity(), "用户登录信息为空");
                    return;
                }
                goToUpgrade();
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                if (null != vipInfoItem) {
                    statisticsMap.put("type", vipInfoItem.getAccountType() + "");
                }
                StatisticsUtil.statisticsCustomClickEvent(getActivity().getString(R.string.android_btn_menu4_vip_pay_click), 0, "", statisticsMap);
            }
        });
        upgradeTitleMoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActWithTitle.class);
                switch (vipInfoItem.getAccountType()) {
                    case 1:
                        intent.putExtra("url", VIP_MORE_INFO_URL);
                        break;
                    case 2:
                        intent.putExtra("url", GOLD_MORE_INFO_URL);
                        break;
                    case 3:
                        intent.putExtra("url", PT_MORE_INFO_URL);
                        break;
                    default:
                        intent.putExtra("url", VIP_MORE_INFO_URL);
                        break;
                }
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();
    }

    @Override
    protected void unRegisterReceiver() {
        super.unRegisterReceiver();
    }

    private void goToUpgrade() {
        new AccountUpgradeTask(getActivity(), userInfo.getSid(), userInfo.getAdSId(), vipInfoItem.getAccountType(), this).execute();
    }

    /**
     * 初始化会员特权部分VIEW
     */
    private void initPrivilegeTitle() {
        upgradeTitleTv.setText(vipInfoItem.getVipTitle());
        switch (vipInfoItem.getAccountType()) {
            case 1:
                badgeIv.setImageResource(R.drawable.badge_vip);
                contactIv.setImageResource(R.drawable.contact_vip);
                inviteIv.setImageResource(R.drawable.invite_vip);
                viewIv.setImageResource(R.drawable.view_vip);
                break;
            case 2:
                badgeIv.setImageResource(R.drawable.badge_gold);
                contactIv.setImageResource(R.drawable.contact_gold);
                inviteIv.setImageResource(R.drawable.invite_gold);
                viewIv.setImageResource(R.drawable.view_gold);
                break;
            case 3:
                badgeIv.setImageResource(R.drawable.badge_pt);
                contactIv.setImageResource(R.drawable.contact_pt);
                inviteIv.setImageResource(R.drawable.invite_pt);
                viewIv.setImageResource(R.drawable.view_pt);
                break;
        }
        List<HeliaoVipInfo.TitleItem> titleItemList = vipInfoItem.getTitleItemList();
        if (null != titleItemList && titleItemList.size() >= 4) {
            badgeTitleTv.setText(titleItemList.get(0).getTitle());
            badgeSubTitleTv.setText(titleItemList.get(0).getSubTitle());
            contactTitleTv.setText(titleItemList.get(1).getTitle());
            contactSubTitleTv.setText(titleItemList.get(1).getSubTitle());
            inviteTitleTv.setText(titleItemList.get(2).getTitle());
            inviteSubTitleTv.setText(titleItemList.get(2).getSubTitle());
            viewTitleTv.setText(titleItemList.get(3).getTitle());
            viewSubTitleTv.setText(titleItemList.get(3).getSubTitle());
        }
    }

    /**
     * 初始化最新升级的会员列表部分view
     */
    private void initLatestUpgradeMembersView() {
        List<HeliaoVipInfo.VipMember> vipMemberList = vipInfoItem.getVipMemberList();
        if (null != vipMemberList && !vipMemberList.isEmpty()) {
            for (final HeliaoVipInfo.VipMember vipMember : vipMemberList) {
                View latestUpgradedMemberItemView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.latest_upgrade_member_list_item, null);
                ImageView avatarIv = (ImageView) latestUpgradedMemberItemView.findViewById(R.id.avatar_img);
                ImageView vipIv = (ImageView) latestUpgradedMemberItemView.findViewById(R.id.vipImage);
                TextView nameTv = (TextView) latestUpgradedMemberItemView.findViewById(R.id.username_txt);
                TextView jobTv = (TextView) latestUpgradedMemberItemView.findViewById(R.id.job_txt);
                String userjob = vipMember.getTitle();
                String userCompany = vipMember.getCompany();
                String userface = vipMember.getUserface();
                nameTv.setText(vipMember.getName());
                if (!TextUtils.isEmpty(userjob)) {
                    jobTv.setText(userjob);
                }
                if (!TextUtils.isEmpty(userCompany)) {
                    if (!TextUtils.isEmpty(jobTv.getText().toString())) {
                        jobTv.setText(jobTv.getText().toString() + " / " + userCompany.trim());
                    } else {
                        jobTv.setText(userCompany.trim());
                    }
                }
                if (TextUtils.isEmpty(userjob) && TextUtils.isEmpty(userCompany)) {
                    jobTv.setVisibility(View.GONE);
                } else {
                    jobTv.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(userface)) {
                    try {
                        imageLoader.displayImage(userface, avatarIv, CacheManager.circleImageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                switch (vipInfoItem.getAccountType()) {
                    case 1:
                        latestUpgradedMemberTv.setText(getString(R.string.upgrade_latest_upgraded_member_vip));
                        vipIv.setImageResource(R.drawable.archive_vip_1);
                        break;
                    case 2:
                        latestUpgradedMemberTv.setText(getString(R.string.upgrade_latest_upgraded_member_gold));
                        vipIv.setImageResource(R.drawable.archive_vip_2);
                        break;
                    case 3:
                        latestUpgradedMemberTv.setText(getString(R.string.upgrade_latest_upgraded_member_pt));
                        vipIv.setImageResource(R.drawable.archive_vip_3);
                        break;
                }
                latestUpgradedMemberItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, vipMember.getSid());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });
                latestUpgradedMemberLl.addView(latestUpgradedMemberItemView);
            }
        }
    }

    /**
     * 初始化升级按钮部分VIEW
     */
    private void initUpgradeBtView() {
        if (currentAccountType == vipInfoItem.getAccountType()) {
            upgradeLlSubTitleTv.setVisibility(View.VISIBLE);
            upgradeLlSubTitleTv.setText(MessageFormat.format(getString(R.string.upgrade_member_limit_remain_days), remainVipDays + ""));
            upgradeLlTitleTv.setText(getString(R.string.upgrade_renew_now));
        } else if (currentAccountType < vipInfoItem.getAccountType()) {
            upgradeLlSubTitleTv.setVisibility(View.GONE);
            upgradeLlTitleTv.setText(getString(R.string.upgrade_upgrade_now));
        } else {
            upgradeLlSubTitleTv.setVisibility(View.GONE);
            upgradeLlTitleTv.setText(getString(R.string.upgrade_upgrade_now));
            upgradeLlTitleTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.archive_editbt_text_selected));
            upgradeLl.setEnabled(false);
        }
    }

    @Override
    public void onPayResult(int type, int flag) {
        if (flag == 1) {
            if (ChoosePayWayActivity.payResultBean != null) {
                Intent intent = new Intent(Constants.BroadCastAction.ACTION_NAMEAUTHSTATUS);
                intent.putExtra("vipType", ChoosePayWayActivity.payResultBean.vipType);
                getActivity().sendBroadcast(intent);

                //更新userInfo信息
                final UserInfo uInfo = RenheApplication.getInstance().getUserInfo();
                if (uInfo != null) {
                    uInfo.setAccountType(ChoosePayWayActivity.payResultBean.vipType);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            RenheApplication.getInstance().getUserCommand().insertOrUpdate(uInfo);
                        }
                    }).start();
                }

                PayResultActivity.launch(getActivity(), ChoosePayWayActivity.payResultBean.goodName,
                        ChoosePayWayActivity.payResultBean.price, ChoosePayWayActivity.payResultBean.payTime, type, vipInfoItem.getAccountType());
            }
            getActivity().finish();
        }
    }
}
