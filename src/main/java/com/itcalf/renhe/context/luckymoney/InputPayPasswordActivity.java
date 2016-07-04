package com.itcalf.renhe.context.luckymoney;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.FinishPayPasswordActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;
import com.jungly.gridpasswordview.GridPasswordView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;
import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2016/5/13.
 */
public class InputPayPasswordActivity extends BaseActivity {
    @BindView(R.id.password_view)
    GridPasswordView passwordView;
    @BindView(R.id.tip_tv)
    TextView tipTv;
    @BindView(R.id.sureBt)
    Button sureBt;
    @BindView(R.id.password_ll)
    LinearLayout passwordLl;

    private int type = Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD;//标示该页面的用途，目前包括忘记支付密码、修改支付密码
    private String checkToken;//成功验证登录密码token
    private String firstPassword;//修改密码时有用，第一次输入的密码
    //常量
    private int ID_TASK_CHECK_PAY_PASSWORD = TaskManager.getTaskId();//验证原支付密码
    private int ID_TASK_SET_PAY_PASSWORD = TaskManager.getTaskId();//设置支付密码
    private int ERROR_TYPE_SURE_PSW_NOT_MATCH_PREVIOUS = 1;//确认密码和第一次密码不一致
    private int ERROR_TYPE_SURE_PSW_OTHERS = 2;//其他错误类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.reset_forget_pay_password_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("设置支付密码");
        sureBt.setEnabled(false);
        passwordLl.requestFocus();
    }

    @Override
    protected void initData() {
        super.initData();
        //注册EventBus
        EventBus.getDefault().register(this);
        type = getIntent().getIntExtra("type", Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD);
        checkToken = getIntent().getStringExtra("checkToken");
        switch (type) {
            case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD://输入新的支付密码
                setTextValue("设置新的支付密码");
                tipTv.setText(R.string.reset_psw_input_newpsw_tip);
                break;
            case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_OLD_PASSWORD://输入老的支付密码
                setTextValue("输入原支付密码");
                tipTv.setText(R.string.reset_psw_input_oldpsw_tip);
                break;
            case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD://确认新的支付密码
                setTextValue("确认新的支付密码");
                tipTv.setText(R.string.reset_psw_sure_newpsw_tip);
                sureBt.setVisibility(View.VISIBLE);
                firstPassword = getIntent().getStringExtra("firstPassword");
                break;
            default:
                break;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        passwordView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                if (psw.length() == 6) {
                    switch (type) {
                        case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD://输入新的支付密码
                            firstPassword = psw;
                            jumpToNextActivity(Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD);
                            break;
                        case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_OLD_PASSWORD://输入老的支付密码
                            checkOldPayPassword(psw);
                            break;
                        case Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD://确认新的支付密码
                            sureBt.setEnabled(true);
                            break;
                        default:
                            break;
                    }
                } else {
                    sureBt.setEnabled(false);
                    if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD) {
                        firstPassword = "";
                    }
                }
            }

            @Override
            public void onInputFinish(String psw) {
            }
        });
    }

    /**
     * 忘记支付密码
     */

    private void checkOldPayPassword(String password) {
        if (checkGrpcBeforeInvoke(ID_TASK_CHECK_PAY_PASSWORD)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.checkOldPayPassword(ID_TASK_CHECK_PAY_PASSWORD, password);
        }
    }

    /**
     * 设置新的支付密码
     */

    private void setPayPassword(String password) {
        if (checkGrpcBeforeInvoke(ID_TASK_SET_PAY_PASSWORD)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.setOrRestPayPassword(ID_TASK_SET_PAY_PASSWORD, password, checkToken);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoTrade.CheckPayPasswordResponse) {
                HeliaoTrade.CheckPayPasswordResponse checkPayPasswordResponse = (HeliaoTrade.CheckPayPasswordResponse) result;
                String checkToken = checkPayPasswordResponse.getCheckToken();
                Intent intent = new Intent(InputPayPasswordActivity.this, InputPayPasswordActivity.class);
                intent.putExtra("type", Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD);
                intent.putExtra("checkToken", checkToken);
                startHlActivity(intent);
            } else if (result instanceof HeliaoTrade.ResetPayPasswordResponse) {
                ToastUtil.showToast(this, R.string.lucky_money_set_psw_success);
                EventBus.getDefault().post(new FinishPayPasswordActivity());
                onBackPressed();
            }
        }
    }

    @Override
    public void onFailure(int taskType, String msg) {
        super.onFailure(taskType, msg);
        hideMaterialLoadingDialog();
        if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_OLD_PASSWORD) {
            ToastUtil.showToast(this, msg);
            passwordView.clearPassword();
        } else if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD) {
            passwordView.clearPassword();
            showErrorDialog(msg, ERROR_TYPE_SURE_PSW_OTHERS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    @OnClick(R.id.sureBt)
    public void onClick() {
        if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD) {
            if (!TextUtils.isEmpty(passwordView.getPassWord()) && passwordView.getPassWord().length() == 6) {
                if ((passwordView.getPassWord().equals(firstPassword))) {
                    setPayPassword(firstPassword);
                } else {
                    showErrorDialog(getString(R.string.reset_psw_sure_newpsw_not_match_previous_error_tip), ERROR_TYPE_SURE_PSW_NOT_MATCH_PREVIOUS);
                }
            }
        }
    }

    /**
     * eventBus
     *
     * @param event
     */
    public void onEventMainThread(FinishPayPasswordActivity event) {
        if (type != Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD)
            finish();
    }

    private void jumpToNextActivity(int newType) {
        Intent intent = new Intent(this, InputPayPasswordActivity.class);
        intent.putExtra("type", newType);
        intent.putExtra("checkToken", checkToken);
        if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_NEW_PASSWORD && !TextUtils.isEmpty(firstPassword))
            intent.putExtra("firstPassword", firstPassword);
        startHlActivity(intent);
    }

    private void showErrorDialog(String errormsg, final int errorType) {
        materialDialogsUtil.getBuilder(getString(R.string.material_dialog_title), errormsg, getString(R.string.material_dialog_sure))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (type == Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_SURE_NEW_PASSWORD) {
                            if (errorType == ERROR_TYPE_SURE_PSW_OTHERS)
                                onBackPressed();
                            else if (errorType == ERROR_TYPE_SURE_PSW_NOT_MATCH_PREVIOUS) {
                                passwordView.clearPassword();
                                sureBt.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).cancelable(false);
        materialDialogsUtil.show();
    }
}
