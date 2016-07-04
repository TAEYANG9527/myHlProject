package com.itcalf.renhe.context.contacts;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;

/**
 * description :邀请成功界面
 * Created by Chans Renhenet
 * 2015/10/15
 */
public class InvitedSuccessActivity extends BaseActivity {

    private TextView invitedNumbTxt;
    private TextView voucherAmountTxt;
    private Button scanningBtn;

    private int inviteNumb = 0;
    private String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.invited_success_activity);
    }

    @Override
    protected void findView() {
        super.findView();
        invitedNumbTxt = (TextView) findViewById(R.id.invited_numb_Tv);
        voucherAmountTxt = (TextView) findViewById(R.id.voucher_amount_Tv);
        scanningBtn = (Button) findViewById(R.id.scanning_Btn);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "发送成功");
        inviteNumb = getIntent().getIntExtra("inviteNumb", 0);
        from = getIntent().getStringExtra("from");
        if (from.equals("mobile")) {
            invitedNumbTxt.setText("您已成功发送" + inviteNumb + "条邀请短信");
        } else {
            invitedNumbTxt.setText("您已成功发送" + inviteNumb + "封邀请邮件");
        }
    }

    @Override
    protected void initListener() {
        super.initListener();

        scanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(InvitedSuccessActivity.this, "查看代金券");
            }
        });
    }
}
