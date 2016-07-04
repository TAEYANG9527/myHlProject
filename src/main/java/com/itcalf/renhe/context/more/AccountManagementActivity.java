package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.edit.EditContactInfo;
import com.itcalf.renhe.context.register.BindPhoneActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.ContactInfo;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

public class AccountManagementActivity extends BaseActivity implements OnClickListener {
    private RelativeLayout phoneNumberRl;
    private TextView phoneNumberTv;
    private TextView isBindTv;
    private RelativeLayout emailRl;
    private TextView emailTv;
    private RelativeLayout telRl;
    private TextView telTv;
    private RelativeLayout blacklistRl;
    private String phoneNumber, email, tel;
    private boolean isBindMobile;
    private String sid, adSId;
    private UserInfo userInfo;
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.account_management);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("账号管理"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("账号管理"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        phoneNumberRl = (RelativeLayout) findViewById(R.id.phone_number_rl);
        phoneNumberTv = (TextView) findViewById(R.id.phone_number_tv);
        isBindTv = (TextView) findViewById(R.id.is_bind_tv);
        emailRl = (RelativeLayout) findViewById(R.id.email_rl);
        emailTv = (TextView) findViewById(R.id.email_tv);
        telRl = (RelativeLayout) findViewById(R.id.tel_rl);
        telTv = (TextView) findViewById(R.id.tel_tv);
        blacklistRl = (RelativeLayout) findViewById(R.id.blacklist_rl);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "账号管理");
        userInfo = RenheApplication.getInstance().getUserInfo();
        sid = userInfo.getSid();
        adSId = userInfo.getAdSId();
        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, sid, adSId);
    }

    @Override
    protected void initListener() {
        super.initListener();
        phoneNumberRl.setOnClickListener(this);
        emailRl.setEnabled(false);
        telRl.setOnClickListener(this);
        blacklistRl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.phone_number_rl:
                intent = new Intent(this, BindPhoneActivity.class);
                intent.putExtra("isBindMobile", isBindMobile);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.blacklist_rl:
                startActivity(BlacklistActivity.class);
                break;
            case R.id.tel_rl:
                intent = new Intent(this, EditContactInfo.class);
                intent.putExtra("Profile", mProfile);
                startActivityForResult(intent, 2);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }

    }

    class ProfileTask extends AsyncTask<String, Void, Profile> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Profile doInBackground(String... params) {
            try {
                return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
                        AccountManagementActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    mProfile = result;
                    isBindMobile = result.getUserInfo().isBindMobile();
                    ContactInfo contactInfo = result.getUserInfo().getContactInfo();
                    phoneNumber = contactInfo.getMobile();
                    email = contactInfo.getEmail();
                    tel = contactInfo.getTel();
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        if (isBindMobile) {
                            isBindTv.setVisibility(View.GONE);
                            phoneNumberTv.setText(phoneNumber);
                            isBindTv.setText("已绑定");
                            isBindTv.setTextColor(getResources().getColor(R.color.CL));
                        } else {
                            isBindTv.setVisibility(View.VISIBLE);
                            phoneNumberTv.setText(phoneNumber);
                            isBindTv.setText("去绑定");
                            isBindTv.setTextColor(getResources().getColor(R.color.C2));
                        }

                    } else {
                        isBindTv.setVisibility(View.VISIBLE);
                        isBindTv.setText("立即绑定");
                        isBindTv.setTextColor(getResources().getColor(R.color.C2));
                    }
                    if (!TextUtils.isEmpty(email)) {
                        emailTv.setText(email);
                    } else {
                        emailTv.setText("");
                    }
                    if (!TextUtils.isEmpty(tel)) {
                        telTv.setText(tel);
                    } else {
                        telTv.setText("");
                    }
                } else {
                    phoneNumberTv.setText("未绑定");
                    isBindTv.setTextColor(getResources().getColor(R.color.C2));
                    emailTv.setText("");
                    telTv.setText("");
                }
            } else {
                phoneNumberTv.setText("未绑定");
                emailTv.setText("");
                telTv.setText("");
                ToastUtil.showNetworkError(AccountManagementActivity.this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        phoneNumber = data.getStringExtra("phoneNumber");
                        phoneNumberTv.setText(phoneNumber);
                        isBindTv.setText("已绑定");
                        isBindTv.setTextColor(getResources().getColor(R.color.CL));
                        isBindMobile = true;
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        tel = data.getStringExtra("tel");
                        if (null != mProfile && null != mProfile.getUserInfo() && null != mProfile.getUserInfo().getContactInfo())
                            mProfile.getUserInfo().getContactInfo().setTel(tel);
                        telTv.setText(tel);
                    }
                }
                break;
            default:
                break;
        }
    }
}
