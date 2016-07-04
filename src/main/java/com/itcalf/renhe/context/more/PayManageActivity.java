package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.luckymoney.ForgetPayPasswordActivity;
import com.itcalf.renhe.context.luckymoney.InputPayPasswordActivity;
import com.itcalf.renhe.context.template.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by wangning on 2016/5/13.
 */
public class PayManageActivity extends BaseActivity {
    @BindView(R.id.reset_psw_Rl)
    RelativeLayout resetPswRl;
    @BindView(R.id.forget_psw_Rl)
    RelativeLayout forgetPswRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.pay_manage_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("支付管理");
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @OnClick({R.id.reset_psw_Rl, R.id.forget_psw_Rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_psw_Rl:
                Intent intent = new Intent(this, InputPayPasswordActivity.class);
                intent.putExtra("type", Constants.PAY_PASSWORD_SET.SET_TYPE_RESET_INPUT_OLD_PASSWORD);
                startHlActivity(intent);
                break;
            case R.id.forget_psw_Rl:
                Intent intent2 = new Intent(this, ForgetPayPasswordActivity.class);
                startHlActivity(intent2);
                break;
        }
    }
}
