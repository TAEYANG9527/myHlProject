package com.itcalf.renhe.context.upgrade;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

import cn.renhe.heliao.idl.vip.HeliaoVipInfo;

public class UpgradeActivity extends BaseActivity implements View.OnClickListener {
    /**
     * VIEW
     */
    private LinearLayout contentMainLayout;
    private ViewPager viewPager;
    private PagerAdaper pagerAdaper;
    private ImageView tabPtIv, tabGoldIv, tabVipIv;
    private View tabBacgView;
    private TabLayout tabLayout;
    private List<Fragment> mFragments;
    private String[] mTitles;
    private ImageButton navigationIb;
    private LinearLayout noNetWorkLl;
    /**
     * 数据
     */
    private int upgradeSortType = 1;//1: 顺序——铂金>黄金>VIP 2：顺序——VIP>黄金>铂金
    /**
     * 常量
     */
    private int ID_TASK_GET_UPGRADE_INDO = TaskManager.getTaskId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.upgrade_main_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("升级");
        contentMainLayout = (LinearLayout) findViewById(R.id.content_main_layout);
        contentMainLayout.setVisibility(View.GONE);
        showLoadingDialog();
        viewPager = (ViewPager) findViewById(R.id.vp_content);
        mFragments = new ArrayList<>();
        mTitles = new String[3];

        tabLayout = (TabLayout) findViewById(R.id.tl_sliding);
        tabBacgView = findViewById(R.id.tab_bg_view);
        tabPtIv = (ImageView) findViewById(R.id.tab_pt_iv);
        tabGoldIv = (ImageView) findViewById(R.id.tab_gold_iv);
        tabVipIv = (ImageView) findViewById(R.id.tab_vip_iv);
        navigationIb = (ImageButton) findViewById(R.id.navigation_ib);
        noNetWorkLl = (LinearLayout) findViewById(R.id.no_network_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        getUpgradeInfo();
    }

    @Override
    protected void initListener() {
        super.initListener();
        tabPtIv.setOnClickListener(this);
        tabGoldIv.setOnClickListener(this);
        tabVipIv.setOnClickListener(this);
        navigationIb.setOnClickListener(this);
    }

    private void changeTabView(int checkedIndex) {
        switch (checkedIndex) {
            case 0:
                if (upgradeSortType == 1) {
                    tabPtIv.setImageResource(R.drawable.pt);
                    tabGoldIv.setImageResource(R.drawable.gold_normal);
                    tabVipIv.setImageResource(R.drawable.vip_normal);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_pt_bcg_shape);
                    changeStatusBarColor(3);
                } else {
                    tabPtIv.setImageResource(R.drawable.vip);
                    tabGoldIv.setImageResource(R.drawable.gold_normal);
                    tabVipIv.setImageResource(R.drawable.pt_normal);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_vip_bcg_shape);
                    changeStatusBarColor(1);
                }
                break;
            case 1:
                if (upgradeSortType == 1) {
                    tabPtIv.setImageResource(R.drawable.pt_normal);
                    tabGoldIv.setImageResource(R.drawable.gold);
                    tabVipIv.setImageResource(R.drawable.vip_normal);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_gold_bcg_shape);
                    changeStatusBarColor(2);
                } else {
                    tabPtIv.setImageResource(R.drawable.vip_normal);
                    tabGoldIv.setImageResource(R.drawable.gold);
                    tabVipIv.setImageResource(R.drawable.pt_normal);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_gold_bcg_shape);
                    changeStatusBarColor(2);
                }
                break;
            case 2:
                if (upgradeSortType == 1) {
                    tabPtIv.setImageResource(R.drawable.pt_normal);
                    tabGoldIv.setImageResource(R.drawable.gold_normal);
                    tabVipIv.setImageResource(R.drawable.vip);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_vip_bcg_shape);
                    changeStatusBarColor(1);
                } else {
                    tabPtIv.setImageResource(R.drawable.vip_normal);
                    tabGoldIv.setImageResource(R.drawable.gold_normal);
                    tabVipIv.setImageResource(R.drawable.pt);
                    tabBacgView.setBackgroundResource(R.drawable.upgrade_pt_bcg_shape);
                    changeStatusBarColor(3);
                }
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_pt_iv:
                viewPager.setCurrentItem(0);
                break;
            case R.id.tab_gold_iv:
                viewPager.setCurrentItem(1);
                break;
            case R.id.tab_vip_iv:
                viewPager.setCurrentItem(2);
                break;
            case R.id.navigation_ib:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideLoadingDialog();
        if (null != result && result instanceof HeliaoVipInfo.VipInfoResponse) {
            HeliaoVipInfo.VipInfoResponse response = (HeliaoVipInfo.VipInfoResponse) result;
            initViewByVipInfoResponse(response);
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideLoadingDialog();
        noNetWorkLl.setVisibility(View.VISIBLE);
    }

    /**
     * 获取升级界面
     */
    private void getUpgradeInfo() {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_UPGRADE_INDO))
            grpcController.getUpgradeInfo(ID_TASK_GET_UPGRADE_INDO);
    }

    private void initViewByVipInfoResponse(HeliaoVipInfo.VipInfoResponse response) {
        List<HeliaoVipInfo.VipInfoItem> vipInfoItemList = response.getVipInfoItemList();
        if (null != vipInfoItemList && !vipInfoItemList.isEmpty()) {
            for (HeliaoVipInfo.VipInfoItem vipInfoItem : vipInfoItemList) {
                UpgradeItemFragment upgradeFragment = UpgradeItemFragment.newInstance(response.getAccountType(), response.getRemainVipDay(), vipInfoItem);
                mFragments.add(upgradeFragment);
            }
            if (vipInfoItemList.size() >= 3) {
                if (vipInfoItemList.get(0).getAccountType() == 1) {
                    upgradeSortType = 2;
                    mTitles[0] = getString(R.string.upgrade_VIP);
                    mTitles[1] = getString(R.string.upgrade_GOLD);
                    mTitles[2] = getString(R.string.upgrade_PT);
                } else {
                    upgradeSortType = 1;
                    mTitles[0] = getString(R.string.upgrade_PT);
                    mTitles[1] = getString(R.string.upgrade_GOLD);
                    mTitles[2] = getString(R.string.upgrade_VIP);
                }
            }
            pagerAdaper = new PagerAdaper(getSupportFragmentManager(), this, mFragments, mTitles);
            viewPager.setAdapter(pagerAdaper);
            /**让标题栏和viewpager联动起来*/
            tabLayout.setupWithViewPager(viewPager);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    changeTabView(tab.getPosition());
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        if (!mFragments.isEmpty()) {
            pagerAdaper.notifyDataSetChanged();
            changeTabView(0);
            viewPager.setCurrentItem(0);
        }
        contentMainLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 改变状态栏颜色 针对Android5.0+
     *
     * @param accountType 当前选择的会员等级
     */
    private void changeStatusBarColor(int accountType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = R.color.upgrade_pt_bcg_start_color;
            switch (accountType) {
                case 1:
                    color = R.color.upgrade_vip_bcg_start_color;
                    break;
                case 2:
                    color = R.color.upgrade_gold_bcg_start_color;
                    break;
                case 3:
                    color = R.color.upgrade_pt_bcg_start_color;
                    break;
            }
            getWindow().setStatusBarColor(ContextCompat.getColor(this, color));
//            getWindow().setNavigationBarColor(ContextCompat.getColor(this, color));//修改底部操作栏（返回键）
        }
    }

    /**
     * 改变状态栏颜色 针对Android5.0以下
     *
     * @param accountType 当前选择的会员等级
     */
    private void changeStatusBarColorBelowAndroidL(int accountType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            int color = R.color.upgrade_pt_bcg_start_color;
            switch (accountType) {
                case 1:
                    color = R.color.upgrade_vip_bcg_start_color;
                    break;
                case 2:
                    color = R.color.upgrade_gold_bcg_start_color;
                    break;
                case 3:
                    color = R.color.upgrade_pt_bcg_start_color;
                    break;
            }
            tintManager.setStatusBarTintResource(color);
        }
    }

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
}
