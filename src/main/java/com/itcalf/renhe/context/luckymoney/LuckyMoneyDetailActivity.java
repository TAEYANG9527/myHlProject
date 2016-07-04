package com.itcalf.renhe.context.luckymoney;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.LuckyMoneyDetailListAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.view.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.renhe.heliao.idl.money.red.HeliaoRobRed;

/**
 * 红包详情页，红包被抢详情
 * Created by wangning on 2016/5/3.
 */
public class LuckyMoneyDetailActivity extends BaseActivity {
    @BindView(R.id.rob_detail_lv)
    ListView robDetailLv;
    private TextView bottomTipTv;
    private ImageView avatarIv;
    private TextView nameTv;
    private TextView tipTv;
    private LinearLayout robMoneyLl;
    private TextView robMoneyTv;
    private TextView getcrashTv;
    private TextView robProgressTipTv;
    private View footerView;
    //数据
    private ImageLoader imageLoader;
    private String luckySid;
    private List<HeliaoRobRed.RedRecordItem> redRecordItems;
    private LuckyMoneyDetailListAdapter luckyMoneyDetailListAdapter;
    //常量
    private int ID_TASK_GETREDRESULT = TaskManager.getTaskId();//获取红包详情

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.luckymoney_detail_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("红包详情");
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        super.initData();
        imageLoader = ImageLoader.getInstance();
        luckySid = getIntent().getStringExtra("luckySid");
        redRecordItems = new ArrayList<>();
        addHeaderView();
        addFooterView();
        footerView.setVisibility(View.GONE);
        luckyMoneyDetailListAdapter = new LuckyMoneyDetailListAdapter(this, redRecordItems);
        robDetailLv.setAdapter(luckyMoneyDetailListAdapter);
        if (!TextUtils.isEmpty(luckySid))
            getLuckyMoneyResult(luckySid);
    }

    @Override
    protected void initListener() {
        super.initListener();
        getcrashTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LuckyMoneyDetailActivity.this, MyWalletDetailActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoRobRed.GetRedResultResponse) {
                HeliaoRobRed.GetRedResultResponse redResultResponse = (HeliaoRobRed.GetRedResultResponse) result;
                initLuckySenderInfo(redResultResponse);
                if (null != redResultResponse.getRedRecordItemList() && !redResultResponse.getRedRecordItemList().isEmpty()) {
                    redRecordItems.addAll(redResultResponse.getRedRecordItemList());
                    luckyMoneyDetailListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
    }

    /**
     * 获取红包详情
     */
    private void getLuckyMoneyResult(String redSid) {
        if (checkGrpcBeforeInvoke(ID_TASK_GETREDRESULT)) {
            showMaterialLoadingDialog();
            grpcController.getLuckyMoneyResult(ID_TASK_GETREDRESULT, redSid);
        }
    }

    /**
     * 添加headerview
     */
    private void addHeaderView() {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.luckymoney_detail_header_layout, robDetailLv, false);
        avatarIv = (ImageView) header.findViewById(R.id.avatar_iv);
        nameTv = (TextView) header.findViewById(R.id.name_tv);
        tipTv = (TextView) header.findViewById(R.id.tip_tv);
        robMoneyLl = (LinearLayout)header.findViewById(R.id.rob_money_ll);
        robMoneyTv = (TextView) header.findViewById(R.id.rob_money_tv);
        getcrashTv = (TextView) header.findViewById(R.id.getcrash_tv);
        robProgressTipTv = (TextView) header.findViewById(R.id.rob_progress_tip_tv);
        robDetailLv.addHeaderView(header, null, false);
    }

    /**
     * 添加headerview
     */
    private void addFooterView() {
        LayoutInflater inflater = getLayoutInflater();
        footerView = inflater.inflate(R.layout.luckymoney_detail_footer_layout, robDetailLv, false);
        bottomTipTv = (TextView) footerView.findViewById(R.id.tip_tv);
        robDetailLv.addFooterView(footerView, null, false);
    }

    /**
     * 初始化头部view
     *
     * @param redResultResponse
     */
    private void initLuckySenderInfo(HeliaoRobRed.GetRedResultResponse redResultResponse) {
        if (!TextUtils.isEmpty(redResultResponse.getUserface())) {
            avatarIv.setVisibility(View.VISIBLE);
            try {
                imageLoader.displayImage(redResultResponse.getUserface(), avatarIv, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(redResultResponse.getName())) {
            nameTv.setVisibility(View.VISIBLE);
            nameTv.setText(redResultResponse.getName());
            if (redResultResponse.getType() == 1) {//红包类型，0为定向单个红包 1为拼手气红包，2为普通红包
                nameTv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.luckymoney_detail_pin), null);
            } else {
                nameTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        }
        if (!TextUtils.isEmpty(redResultResponse.getNote())) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(redResultResponse.getNote());
        }
        if (redResultResponse.getRobbed()) {
            robMoneyLl.setVisibility(View.VISIBLE);
            getcrashTv.setVisibility(View.VISIBLE);
            robMoneyTv.setText(redResultResponse.getAmount());
            getcrashTv.setText(redResultResponse.getMoneyUse());
//            Spannable span = new SpannableString(robMoneyTv.getText());
//            span.setSpan(new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.luckymoney_detail_robmoney_size)),
//                    0, robMoneyTv.getText().toString().trim().indexOf("元"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            span.setSpan(new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.luckymoney_detail_robmoney_unit_size)),
//                    robMoneyTv.getText().toString().trim().indexOf("元"), robMoneyTv.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            robMoneyTv.setText(span);
        }
        if (!TextUtils.isEmpty(redResultResponse.getProgress())) {
            robProgressTipTv.setVisibility(View.VISIBLE);
            robProgressTipTv.setText(redResultResponse.getProgress());
        }
        if (!TextUtils.isEmpty(redResultResponse.getBottomNote())) {
            footerView.setVisibility(View.VISIBLE);
            bottomTipTv.setVisibility(View.VISIBLE);
            bottomTipTv.setText(redResultResponse.getBottomNote());
        }

    }
}
