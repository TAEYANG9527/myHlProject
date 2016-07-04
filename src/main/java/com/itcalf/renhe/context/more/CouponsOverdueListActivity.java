package com.itcalf.renhe.context.more;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.CouponsRecyclerViewAdapter;
import com.itcalf.renhe.bean.CouponsInfo;
import com.itcalf.renhe.bean.CouponsReturn;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description :过期优惠券
 * Created by Chans Renhenet
 * 2015/10/19
 */
public class CouponsOverdueListActivity extends BaseActivity {

    private RecyclerView couponsLv;
    private LinearLayout couponsNoneLl;
    private TextView coupon_none_Tv;

    private List<CouponsInfo> couponsList;
    private CouponsRecyclerViewAdapter mCouponsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.coupons_list);
    }

    @Override
    protected void findView() {
        super.findView();
        couponsLv = (RecyclerView) findViewById(R.id.coupons_RecyclerView);
        couponsNoneLl = (LinearLayout) findViewById(R.id.coupons_none_Ll);
        coupon_none_Tv = (TextView) findViewById(R.id.coupon_none_Tv);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        couponsLv.setLayoutManager(layoutManager);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "已失效的优惠券");
        showLoadingDialog();
        getCouponsList();
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    public void getCouponsList() {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("status", 3);
        try {
            OkHttpClientManager.postAsyn(Constants.Http.GET_COUPONS_LIST, reqParams, CouponsReturn.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    hideLoadingDialog();
                    ToastUtil.showConnectError(CouponsOverdueListActivity.this);
                }

                @Override
                public void onResponse(Object response) {
                    CouponsReturn result = (CouponsReturn) response;
                    if (null != result) {
                        switch (result.getState()) {
                            case 1:
                                couponsList = result.getCouponList();
                                if (null != couponsList && couponsList.size() > 0) {
                                    couponsLv.setVisibility(View.VISIBLE);
                                    couponsNoneLl.setVisibility(View.GONE);

                                    mCouponsRecyclerViewAdapter = new CouponsRecyclerViewAdapter(CouponsOverdueListActivity.this, couponsList, true);
                                    couponsLv.setAdapter(mCouponsRecyclerViewAdapter);
                                } else {
                                    couponsLv.setVisibility(View.GONE);
                                    couponsNoneLl.setVisibility(View.VISIBLE);
                                    coupon_none_Tv.setText("您没有过期的优惠券");
                                }
                                break;
                        }
                    }
                    hideLoadingDialog();
                }
            }, CouponsOverdueListActivity.this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            hideLoadingDialog();
        }
    }
}
