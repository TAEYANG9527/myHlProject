package com.itcalf.renhe.context.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecommendListAdapter;
import com.itcalf.renhe.adapter.RecommendListAdapter.ViewHolder;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.RecommendedUser;
import com.itcalf.renhe.dto.Recommends;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @description 注册的推荐人脉
 * @author Chan
 * @date 2015-4-29
 */
public class RecommendListAct extends BaseActivity {

	private RelativeLayout recommendRl;
	private ListView recommendLv;
	private RelativeLayout skipRl;
	private Button skipTv;
	private CheckBox checkAllCB;

	private FadeUitl fadeUitl;

	private List<RecommendedUser> recommendedUsers;
	private RecommendListAdapter recommendListAdapter;
	private int totalCheckedCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.recommend_list);

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("注册成功——推荐人脉"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("注册成功——推荐人脉"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		setTextValue(1, "推荐人脉");
		recommendRl = (RelativeLayout) findViewById(R.id.recommendRl);
		recommendLv = (ListView) findViewById(R.id.recommend_lv);
		skipTv = (Button) findViewById(R.id.skipBt);
		checkAllCB = (CheckBox) findViewById(R.id.checkAll_CB);
		skipRl = (RelativeLayout) findViewById(R.id.skipBtRl);
		super.findView();
	}

	@Override
	protected void initData() {
		super.initData();

		fadeUitl = new FadeUitl(this, "加载中...");
		recommendedUsers = new ArrayList<RecommendedUser>();
		recommendListAdapter = new RecommendListAdapter(this, recommendedUsers);
		recommendLv.setAdapter(recommendListAdapter);

		//		if (null != getIntent().getSerializableExtra("recommends")) {
		//			Recommends result = (Recommends) getIntent().getSerializableExtra("recommends");
		//			List<RecommendedUser> list = new ArrayList<RecommendedUser>();
		//			RecommendedUser[] memberList = result.getMemberList();
		//			if (memberList.length > 0) {
		//				skipTv.setVisibility(View.VISIBLE);
		//				skipRl.setVisibility(View.VISIBLE);
		//			}
		//			for (RecommendedUser recommendedUser : memberList) {
		//				list.add(recommendedUser);
		//			}
		//			recommendedUsers.addAll(list);
		//			recommendListAdapter.notifyDataSetChanged();
		//		}

		new RecommendTask(this) {
			@Override
			public void doPre() {
				skipRl.setVisibility(View.GONE);
				fadeUitl.addFade(recommendRl);
			}

			@Override
			public void doPost(Recommends result) {
				fadeUitl.removeFade(recommendRl);
				skipRl.setVisibility(View.VISIBLE);
				if (null != result && result.getState() == 1) {
					List<RecommendedUser> list = new ArrayList<RecommendedUser>();
					RecommendedUser[] memberList = result.getMemberList();
					if (memberList.length > 0) {
						totalCheckedCount = memberList.length > 6 ? 6 : memberList.length;//条件 最多5条
					}
					for (RecommendedUser recommendedUser : memberList) {
						recommendedUser.setChecked(true);
						list.add(recommendedUser);
					}
					recommendedUsers.addAll(list);
					recommendListAdapter.notifyDataSetChanged();
				}
				checkCount();
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
				getRenheApplication().getUserInfo().getAdSId());
	}

	@Override
	protected void initListener() {
		super.initListener();
		//添加消息处理  
		recommendLv.setOnItemClickListener(new ItemClickListener());
		checkAllCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isCheckAll(arg1);
			}
		});

		skipTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//选中的好友数
				int numb = 0;
				List<RecommendedUser> temList = new ArrayList<RecommendedUser>();
				for (RecommendedUser recommendedUser : recommendedUsers) {
					if (recommendedUser.isChecked()) {
						temList.add(recommendedUser);
					}
				}
				if (temList != null && temList.size() > 0) {
					numb = temList.size();
					String[] users = new String[temList.size()];
					for (int i = 0; i < users.length; i++) {
						users[i] = temList.get(i).getSid();
					}
					addUsers(users);
					MobclickAgent.onEvent(RecommendListAct.this, "add_register_recommendlist_click");
				} else {
					//未选，直接跳过
					startActivity(TabMainFragmentActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
					MobclickAgent.onEvent(RecommendListAct.this, "add_register_recommendlist_unclick");
				}
				//和聊统计
				String content = "5.103.1.2.1.1" + LoggerFileUtil.getConstantInfo(RecommendListAct.this) + "|" + numb;
				LoggerFileUtil.writeFile(content, true);
			}
		});
	}

	//当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件  
	class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, //The AdapterView where the click happened   
				View arg1, //The view within the AdapterView that was clicked  
				int arg2, //The position of the view in the adapter  
				long arg3//The row id of the item that was clicked  
		) {
			RecommendedUser recommendedUser = recommendedUsers.get(arg2);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.isCheckedIv = (ImageView) recommendLv.findViewWithTag(recommendedUser.getUserface() + arg2);
			if (null != recommendedUser) {
				if (recommendedUser.isChecked()) {
					if (totalCheckedCount > 0) {
						totalCheckedCount--;
					}
					recommendedUser.setChecked(false);
					if (null != viewHolder.isCheckedIv) {
						viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_unsel);
					}
				} else {
					if (totalCheckedCount < recommendedUsers.size()) {
						totalCheckedCount++;
					}
					recommendedUser.setChecked(true);
					if (null != viewHolder.isCheckedIv) {
						viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_sel);
					}
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
				viewHolder.isCheckedIv = (ImageView) recommendLv.findViewWithTag(recommendedUser.getUserface() + i);
				if (null != recommendedUser) {
					recommendedUser.setChecked(true);
					if (null != viewHolder.isCheckedIv) {
						viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_sel);
					}
				}
			}

		} else {
			totalCheckedCount = 0;
			for (int i = 0; i < recommendedUsers.size(); i++) {
				RecommendedUser recommendedUser = recommendedUsers.get(i);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.isCheckedIv = (ImageView) recommendLv.findViewWithTag(recommendedUser.getUserface() + i);
				if (null != recommendedUser) {
					recommendedUser.setChecked(false);
					if (null != viewHolder.isCheckedIv) {
						viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_unsel);
					}
				}
			}
		}
		checkCount();
	}

	private void checkCount() {
		if (totalCheckedCount > 0) {
			skipTv.setText("加好友");
		} else {
			skipTv.setText("跳过");
		}
	}

	private void addUsers(String[] users) {
		new AddRecommendUsersTask(this, users) {
			@Override
			public void doPre() {
				fadeUitl.addFade(recommendRl);
			}

			@Override
			public void doPost(MessageBoardOperation result) {
				fadeUitl.removeFade(recommendRl);
				if (null != result) {
					if (result.getState() == 1) {
						startActivity(TabMainFragmentActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						RenheApplication.getInstance().clearActivity();
					} else {
						ToastUtil.showToast(RecommendListAct.this, "添加失败");
					}
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
				getRenheApplication().getUserInfo().getAdSId());
	}

	//注册成功之后按返回键跳到主页
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(TabMainFragmentActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			startActivity(TabMainFragmentActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
