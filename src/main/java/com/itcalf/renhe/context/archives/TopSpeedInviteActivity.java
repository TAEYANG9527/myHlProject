package com.itcalf.renhe.context.archives;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.pay.PayUtil;
import com.itcalf.renhe.context.pay.PayWaySelectDialogUtil;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.contact.AddFriend;
import cn.renhe.heliao.idl.money.pay.ConfirmPayStatusResponse;
import cn.renhe.heliao.idl.money.pay.PayBizType;
import cn.renhe.heliao.idl.money.pay.PayMethod;

/**
 * 极速邀请
 * Created by wangning on 2016/6/21.
 */
public class TopSpeedInviteActivity extends BaseActivity implements PayUtil.PayCallBack, PayWaySelectDialogUtil.SelectPayWayUtilCallBack {
    @BindView(R.id.avatarImage)
    ImageView avatarImage;
    @BindView(R.id.vipImage)
    ImageView vipImage;
    @BindView(R.id.avatarRl)
    RelativeLayout avatarRl;
    @BindView(R.id.nameTv)
    TextView nameTv;
    @BindView(R.id.companyTv)
    TextView companyTv;
    @BindView(R.id.cityTv)
    TextView cityTv;
    @BindView(R.id.industryTv)
    TextView industryTv;
    @BindView(R.id.layout1)
    RelativeLayout layout1;
    @BindView(R.id.ad_content_et)
    EditText adContentEt;
    @BindView(R.id.ad_content_maxlength_tv)
    TextView adContentMaxlengthTv;
    @BindView(R.id.pay_way_tip_tv)
    TextView payWayTipTv;
    @BindView(R.id.sure_bt)
    Button sureBt;
    @BindView(R.id.srcollview)
    ScrollView srcollview;

    /**
     * 变量
     */
    private Profile profile;
    private int fromType;
    private int adContentMaxLength = 300;
    private String payPassword;//余额支付时输入的密码
    private AddFriend.ConnectionsCourierResponse connectionsCourierResponse;//服务端返回的支付数据
    private PayBizType payBizType;//发起支付的业务类型
    private String payBizSid;//业务ID
    private PayMethod luckyPayWayType = PayMethod.WEIXIN;//支付方式,默认是微信支付
    private boolean needPay;
    /**
     * 常量
     */
    private int ID_TASK_GET_GETPRIVILEGE = TaskManager.getTaskId();// 获取发送人脉快递特权
    private int ID_TASK_CONNECTIONS_COURIER = TaskManager.getTaskId();// 极速邀请添加好友
    private int ID_TASK_LUCKY_PAY = TaskManager.getTaskId();//确认支付的红包

    //工具
    private PayUtil payUtil;
    private PayWaySelectDialogUtil payWaySelectDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.topspeed_invite_layout);
    }

    @Override
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
                Intent intent = new Intent(TopSpeedInviteActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.TOPSPEED_INVITE_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("极速邀请");
    }

    @Override
    protected void initData() {
        super.initData();
        this.payUtil = new PayUtil(this, this);
        this.payWaySelectDialogUtil = new PayWaySelectDialogUtil(this, materialDialogsUtil, this);
        showLoadingDialog();
        if (null != getIntent().getSerializableExtra("profile")) {
            profile = (Profile) getIntent().getSerializableExtra("profile");
            fromType = getIntent().getIntExtra("isFrom", 0);
            initViewByProfile();
            getPrivilege();
        } else {
            ToastUtil.showToast(this, R.string.lucky_money_send_memberinfo_error_tip);
            finish();
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        adContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (adContentMaxLength - s.toString().trim().length() <= 10) {
                    adContentMaxlengthTv.setVisibility(View.VISIBLE);
                    adContentMaxlengthTv.setText((adContentMaxLength - s.toString().trim().length()) + "");
                } else {
                    adContentMaxlengthTv.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initViewByProfile() {
        Profile.UserInfo userInfo = profile.getUserInfo();
        try {
            imageLoader.displayImage(userInfo.getUserface(), avatarImage, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != userInfo.getName()) {
            nameTv.setText(userInfo.getName().trim());
        }
        if (!TextUtils.isEmpty(userInfo.getTitle())) {
            companyTv.setText(userInfo.getTitle().trim());
        }
        if (!TextUtils.isEmpty(userInfo.getCompany())) {
            companyTv.setText(companyTv.getText().toString() + " / " + userInfo.getCompany().trim());
        }
        if (!TextUtils.isEmpty(companyTv.getText().toString())) {
            companyTv.setVisibility(View.VISIBLE);
        } else {
            companyTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(userInfo.getLocation())) {
            cityTv.setText(userInfo.getLocation().trim() + " ");
        } else {
            cityTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(userInfo.getIndustry())) {
            industryTv.setText(userInfo.getIndustry().trim());
        } else {
            industryTv.setVisibility(View.GONE);
        }
        switch (userInfo.getAccountType()) {
            case 0:
                if (userInfo.isRealName()) {
                    vipImage.setImageResource(R.drawable.archive_realname2x);
                } else {
                    vipImage.setVisibility(View.GONE);
                }
                break;
            case 1://VIP会员
                vipImage.setVisibility(View.VISIBLE);
                vipImage.setImageResource(R.drawable.archive_vip_2x);
                break;
            case 2://黄金会员
                vipImage.setVisibility(View.VISIBLE);
                vipImage.setImageResource(R.drawable.archive_vip_2_2x);
                break;
            case 3://铂金会员
                vipImage.setVisibility(View.VISIBLE);
                vipImage.setImageResource(R.drawable.archive_vip_3_2x);
                break;
            default:
                break;
        }
    }

    /**
     * 根据获取权限接口返回值初始化底部文案
     *
     * @param checkSendResponse
     */
    private void initTipViewByCheckSendResponse(AddFriend.CheckSendResponse checkSendResponse) {
        needPay = checkSendResponse.getNeedPay();
        payWayTipTv.setText(checkSendResponse.getDesc());
        if (!TextUtils.isEmpty(checkSendResponse.getConsulting()))
            adContentEt.setHint(checkSendResponse.getConsulting());
        if (checkSendResponse.getNeedPay()) {
            payWayTipTv.setTextColor(ContextCompat.getColor(this, R.color.C2));
        } else {
            payWayTipTv.setTextColor(ContextCompat.getColor(this, R.color.ff830a_color));
        }
    }

    private void handleAddActionByConnectionsCourierResponse(AddFriend.ConnectionsCourierResponse connectionsCourierResponse) {
        if (connectionsCourierResponse.getBase().getErrorCode() == 2) {//0 代表添加成功 2代表需要支付
            hideMaterialLoadingDialog();
            luckyPayWayType = connectionsCourierResponse.getDefaultPayMethod();
            payBizType = connectionsCourierResponse.getBizType();
            payBizSid = connectionsCourierResponse.getBizSid();
            payWaySelectDialogUtil.showPayWaySelectCustomDialog(PayUtil.PAY_BIZ_TYPE_TOPSPEED_INVITE,
                    connectionsCourierResponse.getFee(), connectionsCourierResponse.getBalance(), connectionsCourierResponse.getHasPayPassword(),
                    connectionsCourierResponse.getPayMethodList(), luckyPayWayType);
        } else {
            refreshProfile();
        }
    }

    /**
     * 获取发送人脉快递特权
     */

    private void getPrivilege() {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_GETPRIVILEGE)) {
            grpcController.getPrivilege(ID_TASK_GET_GETPRIVILEGE, profile.getUserInfo().getSid());
        }
    }

    /**
     * 极速邀请添加好友
     */

    private void connectionsCourier() {
        if (checkGrpcBeforeInvoke(ID_TASK_CONNECTIONS_COURIER)) {
            showMaterialLoadingDialog(R.string.waitting, true);
            grpcController.connectionsCourier(ID_TASK_CONNECTIONS_COURIER, profile.getUserInfo().getSid()
                    , adContentEt.getText().toString().trim(), fromType);
        }
    }

    /**
     * 确认支付的红包
     */

    private void confirmPayStatus() {
        if (!TextUtils.isEmpty(payBizSid) && payBizType.getNumber() >= 0) {
            if (TextUtils.isEmpty(payPassword))
                payPassword = "";
            if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_PAY)) {
                showMaterialLoadingDialog();
                grpcController.confirmPayStatus(ID_TASK_LUCKY_PAY, payBizSid, payBizType);
            }
        } else {
            ToastUtil.showToast(this, R.string.pay_error_tip);
        }
    }

    private void refreshProfile() {
        Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
        sendBroadcast(brocastIntent);
        sureBt.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideMaterialLoadingDialog();
                finish();
                ToastUtil.showToast(TopSpeedInviteActivity.this, R.string.success_friend_request);
            }
        }, 2000);
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        if (null != result) {
            if (result instanceof AddFriend.CheckSendResponse) {
                hideLoadingDialog();
                srcollview.setVisibility(View.VISIBLE);
                AddFriend.CheckSendResponse checkSendResponse = (AddFriend.CheckSendResponse) result;
                initTipViewByCheckSendResponse(checkSendResponse);
            } else if (result instanceof AddFriend.ConnectionsCourierResponse) {
                connectionsCourierResponse = (AddFriend.ConnectionsCourierResponse) result;
                handleAddActionByConnectionsCourierResponse(connectionsCourierResponse);
            } else if (result instanceof ConfirmPayStatusResponse) {
//                hideMaterialLoadingDialog();
                payWaySelectDialogUtil.hideDialog();
                //确认支付成功
                refreshProfile();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    @Override
    public void onWeiXinPaySuccess() {
        confirmPayStatus();
    }

    @Override
    public void onAliPaySuccess() {
        confirmPayStatus();
    }

    @Override
    public void onWeiXinPayFail() {

    }

    @Override
    public void onAliPayFail() {

    }

    @Override
    public void onPayBtClick(int payType) {
        if (null == connectionsCourierResponse)
            return;
        Logger.e("payluckyType-->" + luckyPayWayType);
        payUtil.initPay(payType, connectionsCourierResponse.getFee(), connectionsCourierResponse.getBizType().getNumber(),
                connectionsCourierResponse.getBizSid(), PayUtil.PAY_BIZ_TYPE_TOPSPEED_INVITE);
    }

    @Override
    public void onPayPasswordInputed(String psw) {
        payWaySelectDialogUtil.payByBalance(payBizType.getNumber(), payBizSid, PayUtil.PAY_BIZ_TYPE_TOPSPEED_INVITE, connectionsCourierResponse.getFee(), psw);
    }

    @Override
    public void onPayPasswordRetry() {
        sureBt.performClick();
    }

    @Override
    public void onPayWayTypeChanged(PayMethod payWayType) {
        luckyPayWayType = payWayType;
    }

    @Override
    public void onPayBalanceSuccess() {
        confirmPayStatus();
    }

    /**
     * 跳转到升级页面
     */
    private void goToUpgrade() {
        startActivity(new Intent(TopSpeedInviteActivity.this, UpgradeActivity.class));
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @OnClick({R.id.pay_way_tip_tv, R.id.sure_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pay_way_tip_tv:
                if (needPay) {
                    goToUpgrade();
                }
                break;
            case R.id.sure_bt:
                connectionsCourier();
                break;
        }
    }
}
