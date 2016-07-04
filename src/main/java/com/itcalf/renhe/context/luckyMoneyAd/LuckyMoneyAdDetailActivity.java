package com.itcalf.renhe.context.luckyMoneyAd;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.widget.AdLogoClipView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.red.HeliaoSendAdRed;

/**
 * Created by wangning on 2016/6/17.
 */
public class LuckyMoneyAdDetailActivity extends BaseActivity {

    @BindView(R.id.ad_logo_iv)
    ImageView adLogoIv;
    @BindView(R.id.ad_content_tv)
    TextView adContentTv;
    @BindView(R.id.ad_content_dash_line_view)
    View adContentDashLineView;
    @BindView(R.id.avatar_iv)
    ImageView avatarIv;
    @BindView(R.id.username_tv)
    TextView usernameTv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.userinfo_rl)
    RelativeLayout userinfoRl;
    @BindView(R.id.rob_bt)
    Button robBt;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    /**
     * 变量
     */
    private String luckyMoneyAdSid;//红包广告sid
    private HeliaoSendAdRed.AdRedInfoResponse adRedInfoResponse;//红包广告返回值
    private HeliaoSendAdRed.ReceiveAdRedResponse receiveAdRedResponse;//红包广告返回值
    private HeliaoSendAdRed.AdRedStatus adRedStatus;
    /**
     * 常量
     */
    private int ID_TASK_LUCKY_AD_GETINFO = TaskManager.getTaskId();//获取广告红包信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.lucky_money_ad_detail_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("红包广告");
    }

    @Override
    protected void initData() {
        super.initData();
        luckyMoneyAdSid = getIntent().getStringExtra("luckyAdSid");
        if (!TextUtils.isEmpty(luckyMoneyAdSid))
            getAdRedInfo(luckyMoneyAdSid);
        else {
            ToastUtil.showToast(this, getString(R.string.lucky_money_ad_infoerror_tip));
            finish();
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    /**
     * 获取红包广告配置
     */
    private void getAdRedInfo(String luckyAdSid) {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_AD_GETINFO)) {
            showMaterialLoadingDialog();
            grpcController.getAdRedInfo(ID_TASK_LUCKY_AD_GETINFO, luckyAdSid);
        }
    }

    /**
     * 领取广告红包
     */
    private void receiveAdRed(String luckyAdSid) {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_AD_GETINFO)) {
            showMaterialLoadingDialog();
            grpcController.receiveAdRed(ID_TASK_LUCKY_AD_GETINFO, luckyAdSid);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoSendAdRed.AdRedInfoResponse) {
                adRedInfoResponse = (HeliaoSendAdRed.AdRedInfoResponse) result;
                adRedStatus = adRedInfoResponse.getRedStatus();
                initViewInfo();
            } else if (result instanceof HeliaoSendAdRed.ReceiveAdRedResponse) {
                receiveAdRedResponse = (HeliaoSendAdRed.ReceiveAdRedResponse) result;
                adRedStatus = receiveAdRedResponse.getRedStatus();
                refreshViewInfo();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    private void initViewInfo() {
        if (null == adRedInfoResponse)
            return;
        scrollView.setVisibility(View.VISIBLE);
        try {
            imageLoader.displayImage(adRedInfoResponse.getUserface(), avatarIv, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(adRedInfoResponse.getName())) {
            titleTv.setText(adRedInfoResponse.getName());
        }
        if (!TextUtils.isEmpty(adRedInfoResponse.getAdRedInfo())) {
            usernameTv.setText(adRedInfoResponse.getAdRedInfo());
        }
        if (!TextUtils.isEmpty(adRedInfoResponse.getThumbnail())) {
            ViewGroup.LayoutParams linearParams = adLogoIv.getLayoutParams();
            linearParams.width = (int) DensityUtil.getMetricsWidth(this) - 100;
            linearParams.height = (int) (linearParams.width * AdLogoClipView.heightScale);
            adLogoIv.setLayoutParams(linearParams);
            try {
                imageLoader.displayImage(adRedInfoResponse.getThumbnail(), adLogoIv, CacheManager.imageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            adLogoIv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(adRedInfoResponse.getContent())) {
            adContentTv.setText(adRedInfoResponse.getContent());
        }
        changeRobBtState();
    }

    private void refreshViewInfo() {
        if (null == receiveAdRedResponse)
            return;
        ToastUtil.showToast(this, receiveAdRedResponse.getAdRedInfo());
        changeRobBtState();
        if (!TextUtils.isEmpty(receiveAdRedResponse.getAdRedInfo())) {
            usernameTv.setText(receiveAdRedResponse.getAdRedInfo());
        }
    }

    private void changeRobBtState() {
        switch (adRedStatus) {
            case DEFAULT:
                if (TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                    robBt.setText(getString(R.string.lucky_money_ad_error_default_tip));
                    robBt.setBackgroundResource(R.drawable.c3_bt_normal_shape);
                } else {
                    robBt.setText(getString(R.string.lucky_money_ad_tosee_tip));
                }
                break;
            case REMAIN:
                break;
            case FINISH:
                if (TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                    robBt.setText(getString(R.string.lucky_money_ad_error_finish_tip));
                    robBt.setBackgroundResource(R.drawable.c3_bt_normal_shape);
                } else {
                    robBt.setText(getString(R.string.lucky_money_ad_tosee_tip));
                }
                break;
            case EXPIRED:
                if (TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                    robBt.setText(getString(R.string.lucky_money_ad_error_expired_tip));
                    robBt.setBackgroundResource(R.drawable.c3_bt_normal_shape);
                } else {
                    robBt.setText(getString(R.string.lucky_money_ad_tosee_tip));
                }
                break;
            case RECEIVED:
                if (TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                    robBt.setText(getString(R.string.lucky_money_ad_error_received_tip));
                    robBt.setBackgroundResource(R.drawable.c3_bt_normal_shape);
                } else {
                    robBt.setText(getString(R.string.lucky_money_ad_tosee_tip));
                }
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.ad_logo_iv, R.id.rob_bt, R.id.avatar_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ad_logo_iv:
                if (!TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                    Intent intent = new Intent(LuckyMoneyAdDetailActivity.this, WebViewActWithTitle.class);
                    intent.putExtra("url", adRedInfoResponse.getUrl());
                    startHlActivity(intent);
                }
                break;
            case R.id.rob_bt:
                if (adRedStatus == HeliaoSendAdRed.AdRedStatus.REMAIN) {
                    if (!TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                        Intent intent = new Intent(LuckyMoneyAdDetailActivity.this, WebViewActWithTitle.class);
                        intent.putExtra("url", adRedInfoResponse.getUrl());
                        startHlActivity(intent);
                    }
                    receiveAdRed(luckyMoneyAdSid);
                } else {
                    if (!TextUtils.isEmpty(adRedInfoResponse.getUrl())) {
                        Intent intent = new Intent(LuckyMoneyAdDetailActivity.this, WebViewActWithTitle.class);
                        intent.putExtra("url", adRedInfoResponse.getUrl());
                        startHlActivity(intent);
                    }
                }
                break;
            case R.id.avatar_iv:
                if (!TextUtils.isEmpty(adRedInfoResponse.getSenderSid())) {
                    Intent intent = new Intent(LuckyMoneyAdDetailActivity.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, adRedInfoResponse.getSenderSid());
                    startHlActivity(intent);
                }
                break;
        }
    }
}
