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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.FinishActivityEvent;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;

import java.math.BigDecimal;
import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;
import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2016/5/16.
 */
public class CashActivity extends BaseActivity {
    @BindView(R.id.account_cash_et)
    EditText accountCashEt;
    @BindView(R.id.card_no_et)
    EditText cardNoEt;
    @BindView(R.id.money_cash_et)
    EditText moneyCashEt;
    @BindView(R.id.cashBt)
    Button cashBt;
    @BindView(R.id.cash_tv_hint)
    TextView cashTvHint;
    @BindView(R.id.cash_charge_help_iv)
    ImageView cashChargeHelpIv;
    @BindView(R.id.registerRl)
    RelativeLayout registerRl;


    private String balance;  // 余额
    private String charge; // 提现需求扣除的手续费比率
    private BigDecimal realCash;
    //常量
    private int ID_TASK_GET_CASH_INFO = TaskManager.getTaskId();// 获取提现信息
    private int ID_TASK_GO_CASH = TaskManager.getTaskId();// 提现

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.cash_layout);
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
                Intent intent = new Intent(CashActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKY_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("提现");
        cashBt.setEnabled(false);
    }

    @Override
    protected void initData() {
        super.initData();
        //注册EventBus
        EventBus.getDefault().register(this);
        accountCashEt.setText(RenheApplication.getInstance().getUserInfo().getName() + "（已实名认证姓名）");
        viewWithdrawal();
    }

    @Override
    protected void initListener() {
        super.initListener();
        cardNoEt.addTextChangedListener(new EditTextWatcher(R.id.card_no_et));
        moneyCashEt.addTextChangedListener(new EditTextWatcher(R.id.money_cash_et));
    }

    /**
     * eventBus
     *
     * @param event
     */
    public void onEventMainThread(FinishActivityEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    /**
     * 获取提现信息
     */

    private void viewWithdrawal() {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_CASH_INFO)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.viewWithdrawal(ID_TASK_GET_CASH_INFO);
        }
    }

    /**
     * 提现
     */

    private void withdrawal(String cardNo, String moneyAmount) {
        if (checkGrpcBeforeInvoke(ID_TASK_GO_CASH)) {
            showMaterialLoadingDialog(R.string.waitting,false);
            grpcController.withdrawal(ID_TASK_GO_CASH, cardNo, moneyAmount);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoTrade.ViewWithdrawalResponse) {
                HeliaoTrade.ViewWithdrawalResponse viewWithdrawalResponse = (HeliaoTrade.ViewWithdrawalResponse) result;
                if (!TextUtils.isEmpty(viewWithdrawalResponse.getCardNo()))
                    cardNoEt.setText(viewWithdrawalResponse.getCardNo());
                balance = viewWithdrawalResponse.getBalance();
                charge = viewWithdrawalResponse.getCharge();
            } else if (result instanceof HeliaoTrade.WithdrawalResponse) {
                Intent intent = new Intent(CashActivity.this, CashResultActivity.class);
                intent.putExtra("cardNo", cardNoEt.getText().toString().trim());
                if (null != realCash)
                    intent.putExtra("money", realCash.toString());
                startHlActivity(intent);
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
    }

    @OnClick({R.id.cashBt, R.id.cash_charge_help_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cashBt:
                withdrawal(cardNoEt.getText().toString().trim(), moneyCashEt.getText().toString().trim());
                break;
            case R.id.cash_charge_help_iv:
                Intent intent = new Intent(CashActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.CASH_HELP_URL);
                startHlActivity(intent);
                break;
        }
    }

    /**
     * 提供精确减法运算的sub方法
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 两个参数的差
     */
    public static BigDecimal sub(String value1, String value2) {
        BigDecimal b1 = null;
        try {
            b1 = new BigDecimal(value1);
        } catch (Exception e) {
            e.printStackTrace();
            b1 = new BigDecimal("0.00");
        }
        BigDecimal b2 = null;
        try {
            b2 = new BigDecimal(value2);
        } catch (Exception e) {
            e.printStackTrace();
            b2 = new BigDecimal("0.00");
        }
        return b1.subtract(b2);
    }

    /**
     * 提供精确乘法运算的mul方法
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(String value1, String value2) {
        BigDecimal b1 = null;
        try {
            b1 = new BigDecimal(value1);
        } catch (Exception e) {
            e.printStackTrace();
            b1 = new BigDecimal("0.00");
        }
        BigDecimal b2 = null;
        try {
            b2 = new BigDecimal(value2);
        } catch (Exception e) {
            e.printStackTrace();
            b2 = new BigDecimal("0.00");
        }
        return b1.multiply(b2);
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
                case R.id.card_no_et://卡号
                    break;
                case R.id.money_cash_et://提现金额
                    if (!TextUtils.isEmpty(moneyCashEt.getText().toString().trim())) {
                        if (!TextUtils.isEmpty(charge)) {
                            BigDecimal charged = mul(moneyCashEt.getText().toString().trim(), charge);
                            realCash = sub(moneyCashEt.getText().toString().trim(), charged.toString());
                            realCash = realCash.setScale(2, BigDecimal.ROUND_HALF_UP);//设置精度
                            cashTvHint.setText(MessageFormat.format(getString(R.string.wallet_real_crash_money_tip), realCash));
                        } else {
                            try {
                                realCash = new BigDecimal(moneyCashEt.getText().toString().trim());
                            } catch (Exception e) {
                                e.printStackTrace();
                                realCash = new BigDecimal("0.00");
                            }
                            realCash = realCash.setScale(2, BigDecimal.ROUND_HALF_UP);//设置精度
                            cashTvHint.setText(MessageFormat.format(getString(R.string.wallet_real_crash_money_tip), realCash.toString()));
                        }
                    } else {
                        cashTvHint.setText(getString(R.string.wallet_crash_tip));
                    }
                    break;
            }
            if (!TextUtils.isEmpty(cardNoEt.getText().toString().trim()) && !TextUtils.isEmpty(moneyCashEt.getText().toString().trim())) {
                cashBt.setEnabled(true);
            } else {
                cashBt.setEnabled(false);
            }
        }
    }
}
