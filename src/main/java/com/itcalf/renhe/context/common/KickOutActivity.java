package com.itcalf.renhe.context.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.portal.LogOutTask;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.LogoutUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.view.TextView;

import java.util.concurrent.Executors;

public class KickOutActivity extends BaseActivity implements OnClickListener {

    private TextView tvContent;
    private TextView tvConfirm;

    private boolean isClick;
    private MaterialDialogsUtil materialDialogsUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.kickout_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        new LogOutTask(this).executeOnExecutor(Executors.newCachedThreadPool());
        // 删除信鸽推送设置
        delMyJPush(RenheApplication.getInstance().getUserInfo());
        //		tvContent = (TextView) findViewById(R.id.tv_content);
        //		tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        String content = getIntent().getStringExtra("tiker");
        if (TextUtils.isEmpty(content))
            content = getString(R.string.account_kikout);
        //		tvContent.setText(content);
        //		tvConfirm.setOnClickListener(this);
        materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder("提示", content, "确定").callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                isClick = true;
                new LogoutUtil(KickOutActivity.this, null).closeLogin(false);
                finish();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                isClick = true;
                new LogoutUtil(this, null).closeLogin(false);
                finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isClick) {
            new LogoutUtil(this, null).closeLogin(false);
        }
        finish();
    }
    private void delMyJPush(UserInfo userInfo) {
        if (userInfo != null) {
            PushUtil.deletePush();
            PushUtil.registerDevicePush();
        }
    }
}
