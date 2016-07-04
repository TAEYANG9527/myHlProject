package com.itcalf.renhe.context.relationship;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.SearchRecommendGlAdapter;
import com.itcalf.renhe.bean.SearchRecommendedBean;
import com.itcalf.renhe.bean.SearchRecommendedList;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.relationship.bookfriend.BookFriendListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.widget.FlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Title: AdvancedSearchActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014 <br>
 * Create DateTime: 2014-4-11 上午10:08:47 <br>
 * Modify by chan 2014.11.13
 * 
 * @author wangning
 */
public class AdvancedSearchIndexActivity extends BaseActivity {

	private RelativeLayout searchRl;
	private FlowLayout hotSearchGroup;
	private GridView recognizeGridView;
	private ProgressBar loadingProBar;
	private TextView hotsearchTv;
	private RelativeLayout hotsearchRl;
	private List<SearchRecommendedBean> recommendedUsers;
	private SearchRecommendGlAdapter recommendGridlistAdapter;
	private String sid = "", adSid = "";
	private ProgressBar loadingProBar2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.advanced_search_index);
	}

	@Override
	protected void findView() {
		setTextValue(R.id.title_txt, "人脉搜索");
		searchRl = (RelativeLayout) findViewById(R.id.searchRl);
		hotSearchGroup = (FlowLayout) findViewById(R.id.hot_search_group);
		recognizeGridView = (GridView) findViewById(R.id.recognize_Gridview);
		loadingProBar = (ProgressBar) findViewById(R.id.loading);
		hotsearchTv = (TextView) findViewById(R.id.hot_search_tv);
		hotsearchRl = (RelativeLayout) findViewById(R.id.hot_search_ll);
		loadingProBar2 = (ProgressBar) findViewById(R.id.recommend_loading);
	}

	@Override
	protected void initData() {
		// initHotsearchView();

		recommendedUsers = new ArrayList<SearchRecommendedBean>();
		recommendGridlistAdapter = new SearchRecommendGlAdapter(this, recommendedUsers);
		recognizeGridView.setAdapter(recommendGridlistAdapter);

		UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
		sid = userInfo.getSid();
		adSid = userInfo.getAdSId();
		new getRecommendmembersTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid);
	}

	private void initHotsearchView() {
		new GetHotKeywordListTask(this) {
			public void doPre() {
				if (null != hotSearchGroup) {
					hotSearchGroup.removeAllViews();
				}
			};

			public void doPost(com.itcalf.renhe.dto.HotKeywordOperation result) {
				if (null != result) {
					if (result.getState() == 1) {
						if (null != result.getHotSearchList() && result.getHotSearchList().length > 0) {
							hotsearchTv.setVisibility(View.VISIBLE);
							hotsearchRl.setVisibility(View.VISIBLE);
							loadingProBar.setVisibility(View.GONE);
							String[] keywords = result.getHotSearchList();
							for (String keyword : keywords) {
								if (!TextUtils.isEmpty(keyword)) {
									View hotView = LayoutInflater.from(AdvancedSearchIndexActivity.this)
											.inflate(R.layout.regist_canprovide_info_item, null);
									Button item = (Button) hotView.findViewById(R.id.item);
									item.setText(keyword);
									final String mKeyWord = keyword;
									item.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											Intent intent = new Intent(AdvancedSearchIndexActivity.this,
													SearchResultActivity.class);
											intent.putExtra("keyword", mKeyWord);
											startActivity(intent);
											overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
										}
									});
									hotSearchGroup.addView(hotView);
								}
							}
						} else {
							hotsearchTv.setVisibility(View.GONE);
							hotsearchRl.setVisibility(View.GONE);
						}
					}
				} else {
					Toast.makeText(AdvancedSearchIndexActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId());

	}

	@Override
	protected void initListener() {
		searchRl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(AdvancedSearchIndexActivity.this, AdvancedSearchActivity.class));
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				MobclickAgent.onEvent(AdvancedSearchIndexActivity.this, "index_search_to_advance");
			}
		});

		recognizeGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// 点击进入相应的档案界面
				SearchRecommendedBean item = (SearchRecommendedBean) recognizeGridView.getAdapter().getItem(position);
				if (item != null) {
					Intent intent = new Intent(AdvancedSearchIndexActivity.this, MyHomeArchivesActivity.class);
					intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, item.getSid());
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			}
		});
	}

	class getRecommendmembersTask extends AsyncTask<String, Void, SearchRecommendedList> {
		@Override
		protected SearchRecommendedList doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			try {
				SearchRecommendedList mb = (SearchRecommendedList) HttpUtil.doHttpRequest(
						Constants.Http.GETHOTSEARCH_RECOMMENDED_LIST, reqParams, SearchRecommendedList.class, null);
				return mb;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(SearchRecommendedList result) {
			super.onPostExecute(result);
			if (result != null) {
				switch (result.getState()) {
				case 1:
					// 热门搜索关键字
					if (null != result.getHotSearchList() && result.getHotSearchList().length > 0) {
						hotsearchTv.setVisibility(View.VISIBLE);
						hotsearchRl.setVisibility(View.VISIBLE);
						loadingProBar.setVisibility(View.GONE);
						String[] keywords = result.getHotSearchList();
						for (String keyword : keywords) {
							if (!TextUtils.isEmpty(keyword)) {
								View hotView = LayoutInflater.from(AdvancedSearchIndexActivity.this)
										.inflate(R.layout.regist_canprovide_info_item, null);
								Button item = (Button) hotView.findViewById(R.id.item);
								item.setText(keyword);
								final String mKeyWord = keyword;
								item.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View arg0) {
										Intent intent = new Intent(AdvancedSearchIndexActivity.this, SearchResultActivity.class);
										intent.putExtra("keyword", mKeyWord);
										startActivity(intent);
										overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
									}
								});
								hotSearchGroup.addView(hotView);
							}
						}
					} else {
						hotsearchTv.setVisibility(View.GONE);
						hotsearchRl.setVisibility(View.GONE);
					}
					// 推荐人脉
					List<SearchRecommendedBean> nf = result.getMemberList();
					if (null != nf && nf.size() > 0) {
						loadingProBar2.setVisibility(View.GONE);
						for (int i = 0; i < nf.size(); i++) {
							SearchRecommendedBean srb = new SearchRecommendedBean();
							srb.setSid(nf.get(i).getSid());
							srb.setAccountType(nf.get(i).getAccountType());
							srb.setCompany(nf.get(i).getCompany());
							srb.setConnectionNum(nf.get(i).getConnectionNum());
							srb.setIndustry(nf.get(i).getIndustry());
							srb.setLocation(nf.get(i).getLocation());
							srb.setName(nf.get(i).getName());
							srb.setRealname(nf.get(i).isRealname());
							srb.setTitle(nf.get(i).getTitle());
							srb.setUserface(nf.get(i).getUserface());
							recommendedUsers.add(srb);
						}
					} else {
					}
					recommendGridlistAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
			} else {
				ToastUtil.showErrorToast(AdvancedSearchIndexActivity.this, getString(R.string.connect_server_error));
				return;
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingProBar.setVisibility(View.VISIBLE);
			loadingProBar2.setVisibility(View.VISIBLE);
			if (-1 != NetworkUtil.hasNetworkConnection(AdvancedSearchIndexActivity.this)) {
				if (null != hotSearchGroup) {
					hotSearchGroup.removeAllViews();
				}
			} else {
				ToastUtil.showNetworkError(AdvancedSearchIndexActivity.this);
				loadingProBar2.setVisibility(View.GONE);
				loadingProBar.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem searchItem = menu.findItem(R.id.item_save);
		searchItem.setVisible(true);
		searchItem.setTitle("人脉订阅");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_save:
			startActivity(new Intent(AdvancedSearchIndexActivity.this, BookFriendListActivity.class));
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			MobclickAgent.onEvent(AdvancedSearchIndexActivity.this, "book_friendship");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("人脉搜索首页"); // 统计页面
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("人脉搜索首页");
	}
}
