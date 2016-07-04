package com.itcalf.renhe.context.register;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ProvideGetListAdapter;
import com.itcalf.renhe.adapter.ProvideGetListAdapter.ViewHolder;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.RecommendIndustry;
import com.itcalf.renhe.dto.RecommendedUser;
import com.itcalf.renhe.dto.Recommends;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: EditSelfInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-14 下午4:38:14 <br>
 *
 * @author wangning
 */
public class RegisterProvideGetInfo extends BaseActivity {
    private ProvideGetListAdapter provideGetListAdapter;
    private ListView listView;
    private List<RecommendedUser> recommendedUsers;
    private List<RecommendIndustry> recommendIndustries;
    private Button sureBt;
    private int totalCheckedCount = 0;
    private boolean isClickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.provide_get_list);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("注册成功——定制我的人脉"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("注册成功——定制我的人脉"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        setTextValue(1, "定制我的人脉");
        listView = (ListView) findViewById(R.id.listview);
        sureBt = (Button) findViewById(R.id.new_editBt);
        super.findView();
    }

    @Override
    protected void initData() {
        super.initData();
        recommendedUsers = new ArrayList<RecommendedUser>();
        recommendIndustries = new ArrayList<RecommendIndustry>();
        RecommendIndustry recommendIndustry1 = new RecommendIndustry(1, "其它", 0, R.drawable.recommend_1);
        RecommendIndustry recommendIndustry2 = new RecommendIndustry(2, "项目找投资", 1, R.drawable.recommend_2);
        RecommendIndustry recommendIndustry3 = new RecommendIndustry(3, "资金找项目", 1, R.drawable.recommend_3);
        RecommendIndustry recommendIndustry4 = new RecommendIndustry(4, "招聘人才", 1, R.drawable.recommend_4);
        RecommendIndustry recommendIndustry5 = new RecommendIndustry(5, "寻找高薪职位", 1, R.drawable.recommend_5);
        RecommendIndustry recommendIndustry6 = new RecommendIndustry(6, "推广产品和服务", 1, R.drawable.recommend_6);
        RecommendIndustry recommendIndustry7 = new RecommendIndustry(7, "认识同行人脉", 1, R.drawable.recommend_7);
        recommendIndustries.add(recommendIndustry2);
        recommendIndustries.add(recommendIndustry3);
        recommendIndustries.add(recommendIndustry4);
        recommendIndustries.add(recommendIndustry5);
        recommendIndustries.add(recommendIndustry6);
        recommendIndustries.add(recommendIndustry7);
        recommendIndustries.add(recommendIndustry1);
        provideGetListAdapter = new ProvideGetListAdapter(this, recommendIndustries);
        listView.setAdapter(provideGetListAdapter);
        checkCount();
    }

    @Override
    protected void initListener() {
        super.initListener();
        //添加消息处理
        listView.setOnItemClickListener(new ItemClickListener());
        sureBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(RegisterProvideGetInfo.this, "dignzhirenmai");
                List<RecommendIndustry> temList = new ArrayList<RecommendIndustry>();
                for (RecommendIndustry recommendIndustry : recommendIndustries) {
                    if (recommendIndustry.isChecked()) {
                        temList.add(recommendIndustry);
                    }
                }
                String[] users = new String[temList.size()];
                for (int i = 0; i < users.length; i++) {
                    users[i] = temList.get(i).getId() + "";
                }
                addUsers(users);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.findItem(R.id.item_save);
        saveItem.setVisible(true);
        saveItem.setTitle("跳过");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_save) {//选择跳过，默认选择“其他”
            String[] users = new String[1];
            users[0] = "1";//默认选择“其他”
            addUsers(users);
        }
        return super.onOptionsItemSelected(item);
    }

    //当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
    class ItemClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened
                                View arg1,//The view within the AdapterView that was clicked
                                int arg2,//The position of the view in the adapter
                                long arg3//The row id of the item that was clicked
        ) {
            MobclickAgent.onEvent(RegisterProvideGetInfo.this, "注册成功后——定制人脉，选择行业");
            RecommendIndustry recommendIndustry = recommendIndustries.get(arg2);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.flagIv = (ImageView) listView.findViewWithTag(recommendIndustry.getBcgRe() + arg2);
            viewHolder.avatarIv = (ImageView) listView.findViewWithTag(recommendIndustry.getBcgRe() + "icon" + arg2);
            if (null != recommendIndustry) {
                if (recommendIndustry.isChecked()) {
                    if (totalCheckedCount > 0) {
                        totalCheckedCount--;
                    }
                    recommendIndustry.setChecked(false);
                    if (null != viewHolder.flagIv) {
                        viewHolder.flagIv.setVisibility(View.GONE);
                    }
                } else {
                    if (recommendIndustry.getType() == 0) {
                        totalCheckedCount = 1;
                        recommendIndustry.setChecked(true);
                        viewHolder.flagIv.setVisibility(View.VISIBLE);
                        for (int i = 0; i < recommendIndustries.size(); i++) {
                            RecommendIndustry recommendedUser = recommendIndustries.get(i);
                            ViewHolder viewHolder1 = new ViewHolder();
                            viewHolder1.flagIv = (ImageView) listView.findViewWithTag(recommendedUser.getBcgRe() + i);
                            viewHolder1.avatarIv = (ImageView) listView.findViewWithTag(recommendedUser.getBcgRe() + "icon" + i);
                            if (null != recommendedUser && recommendedUser.getType() == 1) {
                                recommendedUser.setChecked(false);
                                if (null != viewHolder1.flagIv) {
                                    viewHolder1.flagIv.setVisibility(View.GONE);
                                }
                            }
                        }
                    } else {
                        if (recommendIndustries.get(recommendIndustries.size() - 1).isChecked()) {
                            RecommendIndustry recommendedUser = recommendIndustries.get(recommendIndustries.size() - 1);
                            ViewHolder viewHolder1 = new ViewHolder();
                            viewHolder1.flagIv = (ImageView) listView.findViewWithTag(recommendedUser.getBcgRe() + recommendIndustries.size() - 1);
                            viewHolder1.avatarIv = (ImageView) listView.findViewWithTag(recommendedUser.getBcgRe() + "icon" + (recommendIndustries.size() - 1));
                            if (null != recommendedUser) {
                                recommendedUser.setChecked(false);
                                totalCheckedCount--;
                                if (null != viewHolder1.flagIv) {
                                    viewHolder1.flagIv.setVisibility(View.GONE);
                                }
                            }
                        }

                        if (totalCheckedCount < recommendIndustries.size()) {
                            totalCheckedCount++;
                        }
                        recommendIndustry.setChecked(true);
                        viewHolder.flagIv.setVisibility(View.VISIBLE);
                    }
                }
            }
            checkCount();
        }
    }

    private void checkCount() {
        if (totalCheckedCount > 0) {
            sureBt.setEnabled(true);
            isClickable = true;
        } else {
            sureBt.setEnabled(false);
            isClickable = false;
        }
        getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
    }

    private void addUsers(String[] users) {
        new AddRecommendIndustrysTask(this, users) {

            @Override
            public void doPre() {
                showDialog(1);
            }

            @Override
            public void doPost(Recommends result) {
                removeDialog(1);
                if (null != result) {
                    if (result.getState() == 1) {
                        Intent intent = new Intent(RegisterProvideGetInfo.this, RecommendListActivity.class);
                        intent.putExtra("recommends", result);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        Toast.makeText(RegisterProvideGetInfo.this, "添加失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute(getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
            default:
                return null;
        }
    }
}
