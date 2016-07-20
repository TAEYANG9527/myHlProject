package com.itcalf.renhe.context.luckymoney;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.eventbusbean.FinishPayPasswordActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by wangning on 2016/5/13.
 */
public class ForgetPayPasswordActivity extends BaseActivity {
    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.psw_et)
    EditText pswEt;
    @BindView(R.id.sureBt)
    Button sureBt;
    @BindView(R.id.pwdisvisible_iv)
    ImageView pwdisvisibleIv;
    //常量
    private int ID_TASK_FORGET_PAY_PASSWORD = TaskManager.getTaskId();//忘记支付密码
    private boolean pwdIsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.forget_pay_password_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("忘记支付密码");
        sureBt.setEnabled(false);
    }

    @Override
    protected void initData() {
        super.initData();
        //注册EventBus
        EventBus.getDefault().register(this);
        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        if (!TextUtils.isEmpty(userInfo.getLoginAccountType()))
            accountEt.setText(userInfo.getLoginAccountType());
        else
            accountEt.setText(userInfo.getEmail());
    }

    @Override
    protected void initListener() {
        super.initListener();
        pswEt.addTextChangedListener(new EditTextWatcher(R.id.psw_et));
    }

    /**
     * 忘记支付密码
     */

    private void forgetPayPassword(String password) {
        if (checkGrpcBeforeInvoke(ID_TASK_FORGET_PAY_PASSWORD)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.forgetPayPassword(ID_TASK_FORGET_PAY_PASSWORD, password);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoTrade.CheckLoginPasswordResponse) {
                HeliaoTrade.CheckLoginPasswordResponse checkLoginPasswordResponse = (HeliaoTrade.CheckLoginPasswordResponse) result;
                String checkToken = checkLoginPasswordResponse.getCheckToken();
                Intent intent = new Intent(ForgetPayPasswordActivity.this, InputPayPasswordActivity.class);
                intent.putExtra("type", Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD);
                intent.putExtra("checkToken", checkToken);
                startHlActivity(intent);
            }
        }
    }

    @Override
    public void onFailure(int taskType, String msg) {
        super.onFailure(taskType, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    /**
     * eventBus
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FinishPayPasswordActivity event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    @OnClick({R.id.sureBt, R.id.pwdisvisible_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sureBt:
                forgetPayPassword(pswEt.getText().toString().trim());
                break;
            case R.id.pwdisvisible_iv:
                if (pwdIsVisible) {
                    pwdIsVisible = false;
                    pwdisvisibleIv.setImageResource(R.drawable.icon_follow_clicked);
                    pswEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    pwdIsVisible = true;
                    pwdisvisibleIv.setImageResource(R.drawable.icon_follow);
                    pswEt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
//                pswEt.setTypeface(Constants.APP_TYPEFACE);
                pswEt.requestFocus();
                pswEt.setSelection(pswEt.length());
                break;
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
                case R.id.psw_et://密码
                    if (!TextUtils.isEmpty(s.toString().trim())) {
                        if (!TextUtils.isEmpty(accountEt.getText().toString().trim())) {
                            sureBt.setEnabled(true);
                        } else {
                            sureBt.setEnabled(false);
                        }
                    }
                    break;
            }
        }
    }
}
