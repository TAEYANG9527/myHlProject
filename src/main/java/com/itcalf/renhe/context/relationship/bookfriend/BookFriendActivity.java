package com.itcalf.renhe.context.relationship.bookfriend;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.relationship.AdvanceSearchUtil;
import com.itcalf.renhe.context.relationship.selectindustry.SelectCityExpandableListActivity;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.BookFriend;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

/**
   * Title: AdvancedSearchActivity.java<br>
   * Description: <br>
   * Copyright (c) 人和网版权所有 2014    <br>
   * Create DateTime: 2014-4-11 上午10:08:47 <br>
   * @author wangning
   */
public class BookFriendActivity extends BaseActivity {
	//登出标识（两次按下返回退出时的标识字段）
	private EditText keywordEt;
	private RelativeLayout areaLayout;
	private TextView areaTv;
	private RelativeLayout industryLayout;
	private TextView industryTv;
	private int provinceCode = -1;
	private int cityCode = -1;
	private int parentIndustryCode = -1;
	private int industryCode = -1;
	public static final int ALL_AREA = -1;
	public static final int ALL_CHINA = -2;
	public static final int ALL_FORGIGN = -3;
	public static final int ALL_INDUSTRY = -1;
	public static final String ALL_AREA_STRING = "全部城市";
	public static final String ALL_INDUSTRY_STRING = "全部行业";
	public static final String ALL_INDUSTRY_STRING2 = "所有行业";
	private Map<String, List<SearchCity>> mCitysMap = new TreeMap<String, List<SearchCity>>();

	private SQLiteDatabase db;
	private RelativeLayout rootRl;
	private String selectedIndustry = "";
	private int selectSectionId = BookFriendActivity.ALL_INDUSTRY;

	private String selectedCity = "";
	private int selectCitySectionId = BookFriendActivity.ALL_CHINA;

	private String keyword;
	private int type;//0代表新增，1代表更新
	private int id;
	private int state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.book_friend);
	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(R.id.title_txt, "人脉订阅");
		keywordEt = (EditText) findViewById(R.id.advance_keywork_et);
		areaLayout = (RelativeLayout) findViewById(R.id.advance_area_rl);
		areaTv = (TextView) findViewById(R.id.advance_area_rl_tv);
		industryLayout = (RelativeLayout) findViewById(R.id.advance_industry_rl);
		industryTv = (TextView) findViewById(R.id.advance_industry_rl_tv);

		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem countItem = menu.findItem(R.id.item_count);
		countItem.setVisible(true);
		countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		countItem.setTitle("保存");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_count:
			goSave();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initData() {
		super.initData();
		type = getIntent().getIntExtra("type", 0);
		id = getIntent().getIntExtra("id", -1);
		state = getIntent().getIntExtra("state", 1);
		if (type == 1) {
			keyword = getIntent().getStringExtra("keyword");
			cityCode = getIntent().getIntExtra("city", -1);
			industryCode = getIntent().getIntExtra("industry", -1);
			selectedCity = getIntent().getStringExtra("cityName");
			selectedIndustry = getIntent().getStringExtra("industryName");
			selectCitySectionId = cityCode;
			selectSectionId = industryCode;
			keywordEt.setText(keyword);
			areaTv.setText(selectedCity);
			industryTv.setText(selectedIndustry);
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		areaLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MobclickAgent.onEvent(BookFriendActivity.this, "bookfriend_selectcity");
				Intent intent = new Intent(BookFriendActivity.this, SelectCityExpandableListActivity.class);
				intent.putExtra("isFromAdvanceSearch", true);
				intent.putExtra("selectedId", selectCitySectionId);
				intent.putExtra("selectedIndustry", selectedCity);
				startActivityForResult(intent, 10);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		industryLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(BookFriendActivity.this, "bookfriend_selectindustry");
				Intent intent = new Intent(BookFriendActivity.this, SelectIndustryExpandableListActivity.class);
				intent.putExtra("select_wheel", "industry");
				intent.putExtra("isFromAdvanceSearch", true);
				intent.putExtra("selectedId", selectSectionId);
				intent.putExtra("selectedIndustry", selectedIndustry);
				startActivityForResult(intent, 11);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		keywordEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				return true;
			}
		});
	}

	private void goSave() {
		keyword = keywordEt.getText().toString().trim();
		if (!TextUtils.isEmpty(keyword)) {
			if (type == 0) {
				new BookFriendTask(this) {
					public void doPre() {
						showDialog(1);
					};

					public void doPost(com.itcalf.renhe.dto.AddBookFriendOperation result) {
						removeDialog(1);
						if (null != result) {
							if (result.getState() == 1) {
								BookFriend bookFriend = new BookFriend();
								bookFriend.setId(result.getId());
								bookFriend.setCity(cityCode);
								bookFriend.setCountry(AdvanceSearchUtil.CHINAL_CODE);
								bookFriend.setIndustry(industryCode);
								bookFriend.setIndustryParents(parentIndustryCode);
								bookFriend.setProv(provinceCode);
								bookFriend.setKeywords(keyword);
								bookFriend.setState(1);
								Intent intent = new Intent();
								intent.putExtra("added_bookfriend", bookFriend);
								intent.putExtra("added_bookfriend_keyword", keyword);
								if (cityCode > 0) {
									intent.putExtra("added_bookfriend_city", areaTv.getText().toString().trim());
								}
								if (industryCode > 0) {
									intent.putExtra("added_bookfriend_industry", industryTv.getText().toString().trim());
								}
								setResult(RESULT_OK, intent);
								finish();
								overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
							} else if (result.getState() == -5) {
								Toast.makeText(BookFriendActivity.this, "关键字重复，请更换后重试", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(BookFriendActivity.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(BookFriendActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
						}
					};
				}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), AdvanceSearchUtil.CHINAL_CODE + "",
						provinceCode + "", cityCode + "", parentIndustryCode + "", industryCode + "", keyword);
			} else if (type == 1) {
				new UpdateBookFriendTask(this) {
					public void doPre() {
						showDialog(1);
					};

					public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
						removeDialog(1);
						if (null != result) {
							if (result.getState() == 1) {
								BookFriend bookFriend = new BookFriend();
								bookFriend.setId(id);
								bookFriend.setCity(cityCode);
								bookFriend.setCountry(AdvanceSearchUtil.CHINAL_CODE);
								bookFriend.setIndustry(industryCode);
								bookFriend.setIndustryParents(parentIndustryCode);
								bookFriend.setProv(provinceCode);
								bookFriend.setKeywords(keyword);
								bookFriend.setState(state);
								Intent intent = new Intent();
								intent.putExtra("added_bookfriend", bookFriend);
								intent.putExtra("added_bookfriend_keyword", keyword);
								if (cityCode > 0) {
									intent.putExtra("added_bookfriend_city", areaTv.getText().toString().trim());
								}
								if (industryCode > 0) {
									intent.putExtra("added_bookfriend_industry", industryTv.getText().toString().trim());
								}
								setResult(RESULT_OK, intent);
								finish();
								overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
							} else if (result.getState() == -5) {
								Toast.makeText(BookFriendActivity.this, "关键字重复，请更换后重试", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(BookFriendActivity.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(BookFriendActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
						}
					};
				}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), AdvanceSearchUtil.CHINAL_CODE + "",
						provinceCode + "", cityCode + "", parentIndustryCode + "", industryCode + "", keyword, id + "",
						state + "");

			}
		} else {
			Toast.makeText(this, "关键字不能为空", Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10) {
			if (resultCode == RESULT_OK) {
				String yourIndustry = data.getStringExtra("yourindustry");
				String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
				String yourParentIndustryCodetemp = data.getStringExtra("yourParentindustrycode");
				if (yourIndustry != null && yourIndustryCodetemp != null) {
					areaTv.setText(yourIndustry);
					cityCode = Integer.parseInt(yourIndustryCodetemp);
					selectCitySectionId = cityCode;
					selectedCity = yourIndustry;
				}
				if (!TextUtils.isEmpty(yourParentIndustryCodetemp)) {
					provinceCode = Integer.parseInt(yourParentIndustryCodetemp);
				}
			}
		} else if (requestCode == 11) {
			if (resultCode == RESULT_OK) {
				String yourIndustry = data.getStringExtra("yourindustry");
				String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
				String yourParentIndustryCodetemp = data.getStringExtra("yourParentindustrycode");
				if (yourIndustry != null && yourIndustryCodetemp != null) {
					industryTv.setText(yourIndustry);
					industryCode = Integer.parseInt(yourIndustryCodetemp);
					selectSectionId = industryCode;
					selectedIndustry = yourIndustry;
				}
				if (!TextUtils.isEmpty(yourParentIndustryCodetemp)) {
					parentIndustryCode = Integer.parseInt(yourParentIndustryCodetemp);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mCitysMap) {
			mCitysMap.clear();
			mCitysMap = null;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
		default:
			return null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("人脉搜索——人脉订阅"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("人脉搜索——人脉订阅"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}
}
