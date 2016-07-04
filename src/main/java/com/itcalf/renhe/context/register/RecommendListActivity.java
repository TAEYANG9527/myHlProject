package com.itcalf.renhe.context.register;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecommendGridlistAdapter;
import com.itcalf.renhe.adapter.RecommendGridlistAdapter.ViewHolder;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.RecommendedUser;
import com.itcalf.renhe.dto.Recommends;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Title: RecommendListActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-12 上午10:20:33 <br>
 *
 * @author wangning
 */
public class RecommendListActivity extends BaseActivity {
    private RecommendGridlistAdapter recommendGridlistAdapter;
    private ListView listView;
    private List<RecommendedUser> recommendedUsers;
    private Button skipTv;
    private int totalCheckedCount = 0;
    private CheckBox checkAllCB;
    private RelativeLayout skipRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.recommend_gridlist);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("注册成功——人脉推荐"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("注册成功——人脉推荐"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        setTextValue(1, "人脉推荐");
        listView = (ListView) findViewById(R.id.newfriend_list);
        skipTv = (Button) findViewById(R.id.skipBt);
        checkAllCB = (CheckBox) findViewById(R.id.checkAll_CB);
        skipRl = (RelativeLayout) findViewById(R.id.skipBtRl);
        super.findView();
    }

    @Override
    protected void initData() {
        super.initData();
        recommendedUsers = new ArrayList<RecommendedUser>();
        recommendGridlistAdapter = new RecommendGridlistAdapter(this, recommendedUsers, listView);
        listView.setAdapter(recommendGridlistAdapter);
        if (null != getIntent().getSerializableExtra("recommends")) {
            Recommends result = (Recommends) getIntent().getSerializableExtra("recommends");
            List<RecommendedUser> list = new ArrayList<RecommendedUser>();
            RecommendedUser[] memberList = result.getMemberList();
            if (memberList.length > 0) {
                skipTv.setVisibility(View.VISIBLE);
                skipRl.setVisibility(View.VISIBLE);
            }
            for (RecommendedUser recommendedUser : memberList) {
                list.add(recommendedUser);
            }
            recommendedUsers.addAll(list);
            recommendGridlistAdapter.notifyDataSetChanged();
        }
        checkCount();
    }

    @Override
    protected void initListener() {
        super.initListener();
        //添加消息处理  
        listView.setOnItemClickListener(new ItemClickListener());
        checkAllCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                isCheckAll(arg1);
            }
        });
        checkAllCB.setChecked(true);//默认全选
        skipTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(RecommendListActivity.this, "registered_addfriend");
                List<RecommendedUser> temList = new ArrayList<RecommendedUser>();
                for (RecommendedUser recommendedUser : recommendedUsers) {
                    if (recommendedUser.isChecked()) {
                        temList.add(recommendedUser);
                    }
                }
                String[] users = new String[temList.size()];
                for (int i = 0; i < users.length; i++) {
                    users[i] = temList.get(i).getSid();
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
        if (item.getItemId() == R.id.item_save) {
            MobclickAgent.onEvent(RecommendListActivity.this, "registered_skip");
            Intent intent = new Intent(RecommendListActivity.this, TabMainFragmentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            RenheApplication.getInstance().clearActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    //当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件  
    class ItemClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0, //The AdapterView where the click happened   
                                View arg1, //The view within the AdapterView that was clicked  
                                int arg2, //The position of the view in the adapter  
                                long arg3//The row id of the item that was clicked  
        ) {
            MobclickAgent.onEvent(RecommendListActivity.this, "注册成功后——人脉推荐");
            RecommendedUser recommendedUser = recommendedUsers.get(arg2);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.isCheckedIv = (ImageView) listView.findViewWithTag(recommendedUser.getUserface() + arg2);
            if (null != recommendedUser) {
                if (recommendedUser.isChecked()) {
                    if (totalCheckedCount > 0) {
                        totalCheckedCount--;
                    }
                    recommendedUser.setChecked(false);
                    if (null != viewHolder.isCheckedIv) {
                        viewHolder.isCheckedIv.setVisibility(View.GONE);
                    }
                } else {
                    if (totalCheckedCount < recommendedUsers.size()) {
                        totalCheckedCount++;
                    }
                    recommendedUser.setChecked(true);
                    viewHolder.isCheckedIv.setVisibility(View.VISIBLE);
                }
            }
            checkCount();
        }
    }

    private void isCheckAll(boolean isCheckAll) {
        if (isCheckAll) {
            totalCheckedCount = recommendedUsers.size();
            for (int i = 0; i < recommendedUsers.size(); i++) {
                RecommendedUser recommendedUser = recommendedUsers.get(i);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.isCheckedIv = (ImageView) listView.findViewWithTag(recommendedUser.getUserface() + i);
                if (null != recommendedUser) {
                    recommendedUser.setChecked(true);
                    if (null != viewHolder.isCheckedIv) {
                        viewHolder.isCheckedIv.setVisibility(View.VISIBLE);
                    }
                }
            }
            skipTv.setEnabled(true);
        } else {
            totalCheckedCount = 0;
            for (int i = 0; i < recommendedUsers.size(); i++) {
                RecommendedUser recommendedUser = recommendedUsers.get(i);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.isCheckedIv = (ImageView) listView.findViewWithTag(recommendedUser.getUserface() + i);
                if (null != recommendedUser) {
                    recommendedUser.setChecked(false);
                    if (null != viewHolder.isCheckedIv) {
                        viewHolder.isCheckedIv.setVisibility(View.GONE);
                    }
                }
            }
            skipTv.setEnabled(false);
        }
    }

    private void checkCount() {
        if (totalCheckedCount > 0) {
            skipTv.setEnabled(true);
        } else {
            skipTv.setEnabled(false);
        }
    }

    private void addUsers(String[] users) {
        new AddRecommendUsersTask(this, users) {

            @Override
            public void doPre() {
                showDialog(2);
            }

            @Override
            public void doPost(MessageBoardOperation result) {
                removeDialog(2);
                if (null != result) {
                    if (result.getState() == 1) {
                        //						startActivity(MainFragment.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(TabMainFragmentActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        RenheApplication.getInstance().clearActivity();
                    } else {
                        Toast.makeText(RecommendListActivity.this, "添加失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                getRenheApplication().getUserInfo().getAdSId());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.loading).cancelable(false).build();
            case 2:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.add_friend_sending).cancelable(false).build();
            default:
                return null;
        }
    }
}
