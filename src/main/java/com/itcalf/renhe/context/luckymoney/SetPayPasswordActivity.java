package com.itcalf.renhe.context.luckymoney;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;

/**
 * 设置支付密码
 * Title: NewRegisterActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-11 下午1:14:36 <br>
 *
 * @author wangning
 */
public class SetPayPasswordActivity extends BaseActivity {
    @BindView(R.id.psw_et)
    EditText pswEt;
    @BindView(R.id.sure_psw_et)
    EditText surePswEt;
    @BindView(R.id.sureBt)
    Button sureBt;
    @BindView(R.id.psw_not_match_previous_error_tv)
    TextView pswNotMatchPreviousErrorTv;

    //数据
    private String firstPassword = "";//首次输入的支付密码
    private String secondPassword = "";//再次确认输入的支付密码

    //常量
    private int ID_TASK_SET_PAY_PASSWORD = TaskManager.getTaskId();//设置支付密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.set_pay_password_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("支付密码");
        sureBt.setEnabled(false);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpItem = menu.findItem(R.id.item_help);
        helpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        helpItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_help:
                Intent intent = new Intent(SetPayPasswordActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKY_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initListener() {
        super.initListener();
        pswEt.addTextChangedListener(new EditTextWatcher(R.id.psw_et));
        surePswEt.addTextChangedListener(new EditTextWatcher(R.id.sure_psw_et));
    }

    /**
     * 设置支付密码
     */

    private void setPayPassword(String password) {
        if (checkGrpcBeforeInvoke(ID_TASK_SET_PAY_PASSWORD)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.setOrRestPayPassword(ID_TASK_SET_PAY_PASSWORD, password, "");
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoTrade.ResetPayPasswordResponse) {
                ToastUtil.showToast(this, R.string.lucky_money_set_psw_success);
                onBackPressed();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    @OnClick(R.id.sureBt)
    public void onClick() {
        setPayPassword(firstPassword);
    }

    private void checkTwicePassword() {
        if (TextUtils.isEmpty(pswEt.getText().toString().trim()) || pswEt.getText().toString().trim().length() != 6) {
            pswNotMatchPreviousErrorTv.setVisibility(View.GONE);
            sureBt.setEnabled(false);
            return;
        }
        if (TextUtils.isEmpty(surePswEt.getText().toString().trim()) || surePswEt.getText().toString().trim().length() != 6) {
            pswNotMatchPreviousErrorTv.setVisibility(View.GONE);
            sureBt.setEnabled(false);
            return;
        }
        if (TextUtils.isEmpty(firstPassword) || TextUtils.isEmpty(secondPassword)) {
            pswNotMatchPreviousErrorTv.setVisibility(View.GONE);
            sureBt.setEnabled(false);
        } else {
            if (secondPassword.equals(firstPassword)) {
                sureBt.setEnabled(true);
            } else {
                pswNotMatchPreviousErrorTv.setVisibility(View.VISIBLE);
                sureBt.setEnabled(false);
            }
        }
    }

    class EditTextWatcher implements TextWatcher {

        private int viewId;

        public EditTextWatcher(int viewId) {
            this.viewId = viewId;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (viewId) {
                case R.id.psw_et://支付密码
                    if (s.length() == 6) {
                        firstPassword = s.toString().trim();
                    }
                    break;
                case R.id.sure_psw_et://确认支付密码
                    if (s.length() == 6) {
                        secondPassword = s.toString().trim();
                    }
                    break;
            }
            checkTwicePassword();
        }
    }
}