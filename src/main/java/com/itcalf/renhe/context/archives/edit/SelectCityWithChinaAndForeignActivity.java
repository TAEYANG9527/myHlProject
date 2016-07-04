package com.itcalf.renhe.context.archives.edit;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.umeng.analytics.MobclickAgent;

public class SelectCityWithChinaAndForeignActivity extends TabActivity {
    private LinearLayout backLL;
    private TextView backTxt;
    private TabHost mTabHost;
    private final static int SELECT_CITY_CHINA = 1;
    private final static int SELECT_CITY_FOREIGN = 2;

    private RelativeLayout chinaTabRl, foreignTabRl;
    private TextView chinaTabTxt, foreignTabTxt;
    private View chinaTabDivide, foreignTabDivide;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.itcalf.renhe.R.layout.select_city_tab);
        initTab();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("编辑个人资料——选择所在地"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("编辑个人资料——选择所在地"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    private void initTab() {
        mTabHost = getTabHost();
        Intent intent = new Intent(this, SelectChinaCityMainActivity.class);
        intent.putExtra("type", SELECT_CITY_CHINA);
        intent.putExtra("isFromArcheveEdit", true);
        mTabHost.addTab(mTabHost.newTabSpec("china").setIndicator("china").setContent(intent));

        intent = new Intent(this, SelectForeignCityMainActivity.class);
        intent.putExtra("type", SELECT_CITY_FOREIGN);
        intent.putExtra("isFromArcheveEdit", true);
        mTabHost.addTab(mTabHost.newTabSpec("foreign").setIndicator("foreign").setContent(intent));

        //初始化为msg选中
        setupUI();
        mTabHost.setCurrentTab(0);
        setCurrentTab(0);
    }

    private void setupUI() {
        backLL = (LinearLayout) findViewById(R.id.backLL);
        backTxt =(TextView)findViewById(R.id.back_txt);
        chinaTabRl = (RelativeLayout) findViewById(R.id.china_tab_rl);
        foreignTabRl = (RelativeLayout) findViewById(R.id.foreign_tab_rl);
        chinaTabTxt = (TextView) findViewById(R.id.china_tab_txt);
        foreignTabTxt = (TextView) findViewById(R.id.foreign_tab_txt);
        chinaTabDivide = findViewById(R.id.china_tab_divide);
        foreignTabDivide = findViewById(R.id.foreign_tab_divide);

    }

    private void initListener() {
        backTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        chinaTabRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTabHost.setCurrentTab(0);
                setCurrentTab(0);
            }
        });
        foreignTabRl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTabHost.setCurrentTab(1);
                setCurrentTab(1);
            }
        });
    }

    private void setCurrentTab(int index) {
        chinaTabTxt.setTextColor(index == 0 ? getResources().getColor(R.color.BP_1) : getResources().getColor(R.color.C2));
        chinaTabDivide.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        foreignTabTxt.setTextColor(index == 1 ? getResources().getColor(R.color.BP_1) : getResources().getColor(R.color.C2));
        foreignTabDivide.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }

    public static final int REQUEST_CODE_PUBLIC_MSG = 1005;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PUBLIC_MSG:
                if (resultCode == RESULT_OK) {
                    setCurrentTab(0);
                }
                break;
        }

    }

    /**
     * 重写finish方法
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
