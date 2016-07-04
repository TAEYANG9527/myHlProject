package com.itcalf.renhe.context.luckymoney;

import android.os.Bundle;
import android.widget.EditText;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.FinishActivityEvent;
import com.itcalf.renhe.view.Button;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2016/5/16.
 */
public class CashResultActivity extends BaseActivity {
    @BindView(R.id.account_cash_et)
    EditText accountCashEt;
    @BindView(R.id.card_no_et)
    EditText cardNoEt;
    @BindView(R.id.cashBt)
    Button cashBt;

    private String cardNo, money;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.cash_result_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("提现详情");
    }

    @Override
    protected void initData() {
        super.initData();
        cardNo = getIntent().getStringExtra("cardNo");
        money = getIntent().getStringExtra("money");
        accountCashEt.setText(cardNo);
        cardNoEt.setText(money + "元");
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new FinishActivityEvent());
        super.onBackPressed();
    }

    @OnClick(R.id.cashBt)
    public void onClick() {
        onBackPressed();
    }
}
