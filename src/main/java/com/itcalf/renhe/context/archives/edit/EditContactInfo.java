package com.itcalf.renhe.context.archives.edit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.edit.task.EditContactInfoTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.ContactInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * Title: EditSelfInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-14 下午4:38:14 <br>
 *
 * @author wangning
 */
public class EditContactInfo extends EditBaseActivity {
    private String qq;
    private String tel;
    private String weixin;
    private EditText qqEt;
    private EditText telEt;
    private EditText weixinEt;
    private boolean isModify = false;
    private Profile pf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.archieve_eidt_contactinfo);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("编辑联系方式"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("编辑联系方式"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(R.id.title_txt, "编辑联系方式");
        qqEt = (EditText) findViewById(R.id.company_qq_et);
        telEt = (EditText) findViewById(R.id.company_tel_et);
        weixinEt = (EditText) findViewById(R.id.company_weichat_et);
    }

    @Override
    protected void initData() {
        super.initData();
        if (getIntent().getSerializableExtra("Profile") != null) {
            pf = (Profile) getIntent().getSerializableExtra("Profile");
            ContactInfo contactInfo = pf.getUserInfo().getContactInfo();
            if (null != contactInfo) {
                qq = contactInfo.getQq();
                tel = contactInfo.getTel();
                weixin = contactInfo.getWeixin();
                if (!TextUtils.isEmpty(qq)) {
                    qqEt.setText(qq);
                    qqEt.setSelection(qq.length());
                }
                if (!TextUtils.isEmpty(tel)) {
                    telEt.setText(tel);
                    telEt.setSelection(tel.length());
                }
                if (!TextUtils.isEmpty(weixin)) {
                    weixinEt.setText(weixin);
                    weixinEt.setSelection(weixin.length());
                }

            }
            telEt.addTextChangedListener(new EditTextListener());
            qqEt.addTextChangedListener(new EditTextListener());
            weixinEt.addTextChangedListener(new EditTextListener());
        }
    }

    @Override
    public void goBack() {
        super.goBack();
        if (isModify) {
            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
            materialDialog
                    .getBuilder(R.string.material_dialog_title, R.string.is_save, R.string.material_dialog_sure,
                            R.string.material_dialog_cancel, R.string.material_dialog_give_up)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            goSave();
                        }
                    });
            materialDialog.show();
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    @Override
    public void goSave() {
        super.goSave();
        if (!checkPreSave()) {
            return;
        }
        new EditContactInfoTask(EditContactInfo.this) {
            public void doPre() {
                showDialog(1);
            }

            ;

            public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
                super.doPost(result);
                if (result == null) {
                    removeDialog(1);
                    ToastUtil.showToast(EditContactInfo.this, R.string.network_anomaly);
                } else if (result.getState() == 1) {
                    Intent intent = new Intent();
                    intent.putExtra("tel", tel);
                    setResult(RESULT_OK, intent);
                    removeDialog(1);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                } else if (result.getState() == -3) {
                    removeDialog(1);
                    ToastUtil.showToast(EditContactInfo.this, R.string.telphone_too_long);
                } else if (result.getState() == -4) {
                    removeDialog(1);
                    ToastUtil.showToast(EditContactInfo.this, R.string.qq_too_long);
                } else if (result.getState() == -5) {
                    removeDialog(1);
                    ToastUtil.showToast(EditContactInfo.this, R.string.weixin_too_long);
                }

            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                getRenheApplication().getUserInfo().getAdSId(), tel, qq, weixin);
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    private boolean checkPreSave() {
        tel = telEt.getText().toString().trim();
        qq = qqEt.getText().toString().trim();
        weixin = weixinEt.getText().toString().trim();
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
            default:
                return null;
        }
    }

    class EditTextListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            isModify = true;
        }

    }

}
