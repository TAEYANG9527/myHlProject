package com.itcalf.renhe.context.archives;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.contact.AddFriend;

/**
 * 人脉秘书
 * Created by wangning on 2016/6/21.
 */
public class SecretaryInviteActivity extends BaseActivity {
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
    @BindView(R.id.sure_bt)
    Button sureBt;
    @BindView(R.id.srcollview)
    ScrollView srcollview;
    @BindView(R.id.phone_number_et)
    EditText phoneNumberEt;
    @BindView(R.id.bottom_tip_tv)
    TextView bottomTipTv;
    @BindView(R.id.phone_number_tv)
    TextView phoneNumberTv;
    @BindView(R.id.avatar_iv)
    ImageView avatarIv;
    @BindView(R.id.desc_tv)
    TextView descTv;
    @BindView(R.id.see_more_tv)
    TextView seeMoreTv;
    @BindView(R.id.secretary_need_upgrade_ll)
    LinearLayout secretaryNeedUpgradeLl;
    @BindView(R.id.root_ll)
    LinearLayout rootLl;
    @BindView(R.id.upgrade_bt)
    Button upgradeBt;

    /**
     * 变量
     */
    private Profile profile;
    private int adContentMaxLength = 300;
    private AddFriend.SecretaryResponse secretaryResponse;//服务端返回的支付数据
    private String seeMoreUrl;
    /**
     * 常量
     */
    private int ID_TASK_GET_GETPRIVILEGE = TaskManager.getTaskId();// 获取发送人脉快递特权
    private int ID_TASK_CONNECTIONS_COURIER = TaskManager.getTaskId();// 极速邀请添加好友

    //工具

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.secretary_invite_layout);
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
                Intent intent = new Intent(SecretaryInviteActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.COURIER_INVITE_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("人脉秘书");
    }

    @Override
    protected void initData() {
        super.initData();
        showLoadingDialog();
        if (null != getIntent().getSerializableExtra("profile")) {
            profile = (Profile) getIntent().getSerializableExtra("profile");
            initViewByProfile();
            checkSendSecretary();
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
    private void initTipViewByCheckSendResponse(AddFriend.CheckSendSecretaryResponse checkSendResponse) {
        if (checkSendResponse.getType() == 1) {
            rootLl.setVisibility(View.VISIBLE);
            secretaryNeedUpgradeLl.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(checkSendResponse.getSecretaryHelpful()))
                bottomTipTv.setText(checkSendResponse.getSecretaryHelpful());
            else
                bottomTipTv.setVisibility(View.GONE);
        } else {
            rootLl.setVisibility(View.GONE);
            secretaryNeedUpgradeLl.setVisibility(View.VISIBLE);
            try {
                imageLoader.displayImage(RenheApplication.getInstance().getUserInfo().getUserface(), avatarIv, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkSendResponse.getUpgradeStatus() == 1) {
                upgradeBt.setText("马上升级");
            } else {
                upgradeBt.setText("马上续费");
            }
            if (!TextUtils.isEmpty(checkSendResponse.getServiceDesc())) {
                descTv.setText(checkSendResponse.getServiceDesc());
            } else {
                descTv.setVisibility(View.GONE);
            }
            seeMoreUrl = checkSendResponse.getLearnMoreUrl();
        }
    }

    private void handleAddActionByConnectionsCourierResponse() {
        refreshProfile();
    }

    /**
     * 获取发送人脉快递特权
     */

    private void checkSendSecretary() {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_GETPRIVILEGE)) {
            grpcController.checkSendSecretary(ID_TASK_GET_GETPRIVILEGE);
        }
    }

    /**
     * 极速邀请添加好友
     */

    private void secretary() {
        if (checkGrpcBeforeInvoke(ID_TASK_CONNECTIONS_COURIER)) {
            showMaterialLoadingDialog(R.string.waitting, true);
            grpcController.secretary(ID_TASK_CONNECTIONS_COURIER, profile.getUserInfo().getSid()
                    , adContentEt.getText().toString().trim(), phoneNumberEt.getText().toString().trim());
        }
    }

    private void refreshProfile() {
        ToastUtil.showToast(SecretaryInviteActivity.this, R.string.success_friend_request);
        Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
        sendBroadcast(brocastIntent);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof AddFriend.CheckSendSecretaryResponse) {
                hideLoadingDialog();
                srcollview.setVisibility(View.VISIBLE);
                AddFriend.CheckSendSecretaryResponse checkSendSecretaryResponse = (AddFriend.CheckSendSecretaryResponse) result;
                initTipViewByCheckSendResponse(checkSendSecretaryResponse);
            } else if (result instanceof AddFriend.SecretaryResponse) {
                secretaryResponse = (AddFriend.SecretaryResponse) result;
                handleAddActionByConnectionsCourierResponse();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    /**
     * 跳转到升级页面
     */
    private void goToUpgrade() {
        startActivity(new Intent(SecretaryInviteActivity.this, UpgradeActivity.class));
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @OnClick({R.id.sure_bt, R.id.see_more_tv, R.id.upgrade_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sure_bt:
                secretary();
                break;
            case R.id.see_more_tv:
                Intent intent = new Intent(SecretaryInviteActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", seeMoreUrl);
                startHlActivity(intent);
                break;
            case R.id.upgrade_bt:
                goToUpgrade();
                break;
        }
    }
}
