package com.itcalf.renhe.context.luckyMoneyAd;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;

import butterknife.BindView;

/**
 * #红包广告#
 * 填写红包广告信息页面，包括广告语、广告logo、广告链接等
 * Created by wangning on 2016/6/3.
 */
public class LuckyMoneyAdFillUrlActivity extends BaseActivity {
    @BindView(R.id.ad_url_et)
    EditText adUrlEt;
    @BindView(R.id.ad_url_maxlength_tv)
    TextView adUrlMaxlengthTv;

    /**
     * 常量
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.luckymoney_ad_fillurl_layout);
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
                Intent intent = new Intent(LuckyMoneyAdFillUrlActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKYAD_INVITE_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("发红包广告");
    }

    @Override
    protected void initData() {
        super.initData();
        if (!TextUtils.isEmpty(getIntent().getStringExtra("adUrl"))) {
            adUrlEt.setText(getIntent().getStringExtra("adUrl"));
            adUrlEt.setSelection(adUrlEt.getText().toString().length());
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("adUrl", adUrlEt.getText().toString().trim());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
