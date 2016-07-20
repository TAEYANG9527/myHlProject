package com.itcalf.renhe.context.luckymoney;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.MyWalletDetailListAdapter;
import com.itcalf.renhe.context.auth.NameAuthActivity;
import com.itcalf.renhe.context.fragmentMain.MyFragment;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.FinishActivityEvent;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 红包详情页，红包被抢详情
 * Created by wangning on 2016/5/3.
 */
public class MyWalletDetailActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.rob_detail_lv)
    ListView robDetailLv;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView moneyTv;
    private TextView cantCrashTv;
    private LinearLayout crashLl;
    private LinearLayout cantCrashLl;
    private LinearLayout emptyLl;
    private View footerView;
    private TextView footerTipTv;
    private ProgressBar footerPb;
    //数据
    private List<HeliaoTrade.TradeRecord> tradeRecordList;
    private MyWalletDetailListAdapter myWalletDetailListAdapter;
    private int page = 1;//当前请求页
    private int lastItem;
    //常量
    private int ID_TASK_GET_BALANCE = TaskManager.getTaskId();//获取余额
    private int ID_TASK_GET_TRADERECORD_LIST = TaskManager.getTaskId();//获取交易明细
    private static final int PAGE_SIZE = 20;//每页请求的数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.mywallet_detail_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("我的余额");
//        ButterKnife.bind(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.wallet_swipe_ly);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.BP_1, R.color.BP_1,
                R.color.BP_1, R.color.BP_1);
        // 设置下拉监听，当用户下拉的时候会去执行回调
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        //注册EventBus
        EventBus.getDefault().register(this);
        tradeRecordList = new ArrayList<>();
        addHeaderView();
        addFooterView();
        footerView.setVisibility(View.GONE);
        myWalletDetailListAdapter = new MyWalletDetailListAdapter(this, tradeRecordList);
        robDetailLv.setAdapter(myWalletDetailListAdapter);
        getMemberBalance(true);
    }

    @Override
    protected void initListener() {
        super.initListener();
        robDetailLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Logger.e("lastItem:" + lastItem);
                if (lastItem == myWalletDetailListAdapter.getCount()
                        && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    page += 1;
                    getTradeRecordList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount - 2;
            }
        });
        cantCrashTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NameAuthActivity.launch(MyWalletDetailActivity.this, MyFragment.realNameStatus);
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                statisticsMap.put("type", "1");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_pop_realname_click), 0, "", statisticsMap);
            }
        });
        crashLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHlActivity(new Intent(MyWalletDetailActivity.this, CashActivity.class));
            }
        });
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpItem = menu.findItem(R.id.item_cash_help);
        helpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        helpItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_cash_help:
                Intent intent = new Intent(MyWalletDetailActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKY_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * eventBus
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FinishActivityEvent event) {
//        finish();
        onRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        if (taskId == ID_TASK_GET_TRADERECORD_LIST)
            hideMaterialLoadingDialog();
        else {
            getTradeRecordList();
        }
        if (null != result) {
            if (result instanceof HeliaoTrade.TradeRecordListResponse) {
                HeliaoTrade.TradeRecordListResponse tradeRecordListResponse = (HeliaoTrade.TradeRecordListResponse) result;
                if (null != tradeRecordListResponse.getTradeRecordList() && !tradeRecordListResponse.getTradeRecordList().isEmpty()) {
                    if (page == 1) {
                        emptyLl.setVisibility(View.GONE);
                        tradeRecordList.clear();
                    }
                    tradeRecordList.addAll(tradeRecordListResponse.getTradeRecordList());
                    myWalletDetailListAdapter.notifyDataSetChanged();
                    if (tradeRecordListResponse.getTradeRecordList().size() >= PAGE_SIZE) {
                        footerView.setVisibility(View.VISIBLE);
                        footerPb.setVisibility(View.VISIBLE);
                        footerTipTv.setText(getString(R.string.xlistview_header_hint_loading));
                    } else if (page > 0 && tradeRecordListResponse.getTradeRecordList().size() < PAGE_SIZE) {
                        footerView.setVisibility(View.VISIBLE);
                        footerPb.setVisibility(View.GONE);
                        footerTipTv.setText(getString(R.string.recyclerview_footer_hint_end));
                    }
                } else {
                    if (page == 1)
                        emptyLl.setVisibility(View.VISIBLE);
                    else if (page > 1) {
                        footerView.setVisibility(View.VISIBLE);
                        footerPb.setVisibility(View.GONE);
                        footerTipTv.setText(getString(R.string.recyclerview_footer_hint_end));
                    }
                }
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (result instanceof HeliaoTrade.MemberBalanceResponse) {
                HeliaoTrade.MemberBalanceResponse memberBalanceResponse = (HeliaoTrade.MemberBalanceResponse) result;
                if (memberBalanceResponse.getWithdrawal()) {
                    crashLl.setVisibility(View.VISIBLE);
                    cantCrashLl.setVisibility(View.GONE);
                } else {
                    crashLl.setVisibility(View.GONE);
                    cantCrashLl.setVisibility(View.VISIBLE);
                }
                moneyTv.setText(memberBalanceResponse.getBalance());
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        if (type == ID_TASK_GET_TRADERECORD_LIST)
            hideMaterialLoadingDialog();
        else {
            getTradeRecordList();
        }
    }

    /**
     * 获取余额
     *
     * @param isAuto 是否是自动刷新（即是否是打开该页面自动加载数据)，false代表是用户手动下拉刷新
     */
    private void getMemberBalance(boolean isAuto) {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_BALANCE)) {
            if (isAuto)
                showMaterialLoadingDialog();
            grpcController.getMemberBalance(ID_TASK_GET_BALANCE);
        }
    }

    /**
     * 获取红包详情
     */
    private void getTradeRecordList() {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_TRADERECORD_LIST)) {
//            showMaterialLoadingDialog();
            grpcController.getTradeRecordList(ID_TASK_GET_TRADERECORD_LIST, page, PAGE_SIZE);
        }
    }

    /**
     * 添加headerview
     */
    private void addHeaderView() {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.mywallet_detail_header_layout, robDetailLv, false);
        moneyTv = (TextView) header.findViewById(R.id.money_tv);
        crashLl = (LinearLayout) header.findViewById(R.id.crash_ll);
        cantCrashTv = (TextView) header.findViewById(R.id.cant_crash_tv);
        cantCrashLl = (LinearLayout) header.findViewById(R.id.cant_crash_ll);
        emptyLl = (LinearLayout) header.findViewById(R.id.empty_ll);
        Logger.e("robDetailLv==>" + robDetailLv);
        Logger.e("header==>" + header);
        robDetailLv.addHeaderView(header, null, false);
    }

    /**
     * 添加headerview
     */
    private void addFooterView() {
        LayoutInflater inflater = getLayoutInflater();
        footerView = inflater.inflate(R.layout.view_footer, robDetailLv, false);
        footerPb = (ProgressBar) footerView.findViewById(R.id.progress);
        footerTipTv = (TextView) footerView.findViewById(R.id.text);
        robDetailLv.addFooterView(footerView, null, false);
    }

    @Override
    public void onRefresh() {
        page = 1;
        getMemberBalance(false);
    }
}
