package com.itcalf.renhe.context.relationship.selectindustry;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.relationship.AdvanceSearchUtil;
import com.itcalf.renhe.context.relationship.AdvancedSearchActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.SearchCity;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCityExpandableListActivity extends BaseActivity {

	ExpandableListView expandableListView;
	TreeViewAdapter adapter;
	SuperTreeViewAdapter superAdapter;
	private static final String DBNAME = "city.db";
	private static final String TABLE_NAME = "mycity";
	private SQLiteDatabase db;
	private SearchCity[] industryArrays;
	private SearchCity[] chileIndustryArrays;
	private static final String path = Constants.CACHE_PATH.ASSETS_DB_PATH;
	/** Called when the activity is first created. */
	private Handler handler;
	List<TreeViewAdapter.TreeNode> treeNode;
	private boolean isFromAdvanceSearch = true;
	//	private static final String SUFFIX = "(全部)";
	private static final String SUFFIX = "全选";
	private static final int NO_SELECT = -10;
	private int selectedId = NO_SELECT;
	private String selectedIndustry = "";
	private int selectSectionId = AdvancedSearchActivity.ALL_CHINA;
	private int theSelection = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTemplate().doInActivity(this, R.layout.select_industry);
		setTextValue(R.id.title_txt, "选择城市");
		isFromAdvanceSearch = getIntent().getBooleanExtra("isFromAdvanceSearch", false);
		selectedId = getIntent().getIntExtra("selectedId", NO_SELECT);
		selectedIndustry = getIntent().getStringExtra("selectedIndustry");
		//		setContentView(R.layout.select_industry);
		expandableListView = (ExpandableListView) findViewById(R.id.expandablelistview);
		//		另外就是就是有些小技巧，比如说如何去除隐藏的箭头？如何设置某一列是铺开的？

		//		expandableListView.expandGroup(0);//设置第一组张开
		expandableListView.setGroupIndicator(null);//除去自带的箭头
		adapter = new TreeViewAdapter(this, 38, expandableListView, selectedId);
		superAdapter = new SuperTreeViewAdapter(this, stvClickEvent, expandableListView, selectedId);
		treeNode = adapter.getTreeNode();
		handler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message arg0) {
				if (arg0.what == 1) {
					adapter.updateTreeNode(treeNode);
					expandableListView.setAdapter(adapter);
					if (selectSectionId != AdvancedSearchActivity.ALL_CHINA) {
						expandableListView.expandGroup(selectSectionId);
						if (theSelection != -1) {
							expandableListView.setSelection(theSelection);
						}
					}
				}
				return false;
			}
		});

		adapter.removeAll();
		adapter.notifyDataSetChanged();
		superAdapter.RemoveAll();
		superAdapter.notifyDataSetChanged();
		initIndustry(SelectCityExpandableListActivity.this, handler);
		expandableListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

				String str = "parent_id = " + groupPosition + " child_id = " + childPosition;
				//				Toast.makeText(
				//						SelectIndustryExpandableListActivity.this,
				//						adapter.getChild(groupPosition, childPosition).getName() + "--"
				//								+ adapter.getChild(groupPosition, childPosition).getId(), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				String industryName = adapter.getChild(groupPosition, childPosition).getName();
				if (industryName.endsWith(SUFFIX)) {
					//					industryName = industryName.substring(0,industryName.length() - SUFFIX.length());
					industryName = adapter.getGroup(groupPosition).getName();
				}
				if (industryName.endsWith("其它")) {
					industryName = adapter.getGroup(groupPosition).getName() + ".其它";
				}
				intent.putExtra("yourindustry", industryName);
				intent.putExtra("yourindustrycode", adapter.getChild(groupPosition, childPosition).getId() + "");
				intent.putExtra("yourParentindustrycode", adapter.getChild(groupPosition, 0).getId() + "");
				//				intent.putExtra("yourindustrySection", industrySection);
				setResult(RESULT_OK, intent);
				finish();
				return false;
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("人脉订阅——选择城市"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("人脉订阅——选择城市"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	OnChildClickListener stvClickEvent = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			String msg = "parent_id = " + groupPosition + " child_id = " + childPosition;
			//			Toast.makeText(SelectIndustryExpandableListActivity.this, msg, Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	private void initIndustry(final Context context, final Handler handler) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (isFromAdvanceSearch) {
					TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
					SearchCity allCity = new SearchCity();
					allCity.setName(AdvancedSearchActivity.ALL_CITY_STRING);
					allCity.setId(AdvancedSearchActivity.ALL_CHINA);
					node.parent = allCity;
					SearchCity allCityChild = new SearchCity();
					allCityChild.setName(AdvancedSearchActivity.ALL_CITY_STRING);
					allCityChild.setId(AdvancedSearchActivity.ALL_CHINA);
					node.childs.add(allCityChild);
					treeNode.add(node);
				}
				try {
					AdvanceSearchUtil.copyDB(context, DBNAME);
					if (db == null) {
						db = SQLiteDatabase.openOrCreateDatabase(path + DBNAME, null);
					}
					industryArrays = AdvanceSearchUtil.getProvince(db, TABLE_NAME);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (null != industryArrays && industryArrays.length > 0) {
						for (int i = 0; i < industryArrays.length; i++) {
							List<SearchCity> induList = new ArrayList<SearchCity>();
							if (industryArrays[i] != null) {
								chileIndustryArrays = AdvanceSearchUtil.getChildCity(db, TABLE_NAME, industryArrays[i].getId());
								TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
								node.parent = industryArrays[i];
								if (isFromAdvanceSearch && !industryArrays[i].getName().equals("其它")) {
									SearchCity searchCity = new SearchCity();
									//									searchCity.setName(industryArrays[i].getName() + SUFFIX);
									searchCity.setName(SUFFIX);
									searchCity.setId(industryArrays[i].getId());
									if (industryArrays[i].getId() == selectedId) {
										selectSectionId = i + 1;
										theSelection = i + 1;
									}
									node.childs.add(searchCity);
								}
								if (null != chileIndustryArrays) {
									for (int j = 0; j < chileIndustryArrays.length; j++) {
										if (chileIndustryArrays[j] != null
												&& !TextUtils.isEmpty(chileIndustryArrays[j].getName())) {
											node.childs.add(chileIndustryArrays[j]);
											if (chileIndustryArrays[j].getId() == selectedId) {
												if (!isFromAdvanceSearch) {
													selectSectionId = i;
													theSelection = i + j;
												} else {
													selectSectionId = i + 1;
													theSelection = i + j + 1;
												}
											}
										}
									}
								}
								treeNode.add(node);
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				handler.sendEmptyMessage(1);
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != db) {
			db.close();
		}
	}

}