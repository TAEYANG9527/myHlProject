package com.itcalf.renhe.context.relationship.bookfriend;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.relationship.AdvanceSearchUtil;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.BookFriend;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.view.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Title: BookFriendListActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-3 下午4:57:17 <br>
 * @author wangning
 */
public class BookFriendListActivity extends BaseActivity {
	private LinearLayout bookFriendLl;
	private static final String TABLE_NAME = "mycity";
	private SQLiteDatabase db;
	private static final String path = Constants.CACHE_PATH.ASSETS_DB_PATH;
	private static final String DBNAME = "city.db";

	private static final String IND_DBNAME = "industry";
	private static final String IND_TABLE_NAME = "industry";
	private SQLiteDatabase ind_db;
	private static final int ADD_BOOK = 1001;
	private static final int UPDATE_BOOK = 1002;
	private RelativeLayout bookFriendEmptyRl;
	private ScrollView scrollView;
	private Button goAddBt;
	private static final int BOOK_FRIEND_MOST_NUM = 5;//最多订阅5个
	private int bookFriendNum = 0;
	private AlertDialog mAlertDialog;
	private FadeUitl fadeUitl;
	private RelativeLayout rootRl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.book_friend_list);
	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(R.id.title_txt, "人脉订阅");
		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
		bookFriendLl = (LinearLayout) findViewById(R.id.book_friend_ll);
		bookFriendEmptyRl = (RelativeLayout) findViewById(R.id.first_enter_renmaiquan_rl);
		scrollView = (ScrollView) findViewById(R.id.editscrview);
		goAddBt = (Button) findViewById(R.id.bindBt);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem countItem = menu.findItem(R.id.item_count);
		if (bookFriendNum < BOOK_FRIEND_MOST_NUM) {
			countItem.setVisible(true);
		} else {
			countItem.setVisible(false);
		}
		countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		countItem.setTitle("添加");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_count:
			goAddBt.performClick();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initData() {
		super.initData();
		fadeUitl = new FadeUitl(this, "加载中...");
		fadeUitl.addFade(rootRl);
		try {
			AdvanceSearchUtil.copyDB(this, DBNAME);
			AdvanceSearchUtil.copyDB(this, IND_DBNAME);
			if (db == null) {
				db = SQLiteDatabase.openOrCreateDatabase(path + DBNAME, null);
			}
			if (ind_db == null) {
				ind_db = SQLiteDatabase.openOrCreateDatabase(path + IND_DBNAME, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new GetBookFriendListTask(this) {
			public void doPre() {
				//				showDialog(1);
				if (null != bookFriendLl) {
					bookFriendLl.removeAllViews();
				}
			};

			public void doPost(com.itcalf.renhe.dto.BookFriendOperation result) {
				//				removeDialog(1);
				fadeUitl.removeFade(rootRl);
				if (null != result) {
					if (result.getState() == 1) {
						BookFriend[] bookFriends = result.getMemberRssList();
						if (null != bookFriends && bookFriends.length > 0) {
							scrollView.setVisibility(View.VISIBLE);
							bookFriendEmptyRl.setVisibility(View.GONE);
							bookFriendNum = bookFriends.length;
							invalidateOptionsMenu();
							for (final BookFriend bookFriend : bookFriends) {
								View bookItem = LayoutInflater.from(BookFriendListActivity.this)
										.inflate(R.layout.book_friend_list_item, null);

								TextView keyWordTv = (TextView) bookItem.findViewById(R.id.area_all_tv);
								TextView cityTv = (TextView) bookItem.findViewById(R.id.advance_city_tv);
								TextView industryTv = (TextView) bookItem.findViewById(R.id.advance_industry_tv);
								RelativeLayout industryRl = (RelativeLayout) bookItem.findViewById(R.id.advance_industry_rl);
								SwitchButton sButton = (SwitchButton) bookItem.findViewById(R.id.item_sb);
								View seperateLine = (View) bookItem.findViewById(R.id.seperateline);
								RelativeLayout itemRl = (RelativeLayout) bookItem.findViewById(R.id.item_rl);
								keyWordTv.setText(bookFriend.getKeywords());
								if (bookFriend.getState() == 1) {
									sButton.setChecked(true);
								} else {
									sButton.setChecked(false);
								}
								final int prov = bookFriend.getProv();
								final int city = bookFriend.getCity();
								final int childIndustry = bookFriend.getIndustry();
								final int parentIndustry = bookFriend.getIndustryParents();
								final String keyword = bookFriend.getKeywords();
								final int id = bookFriend.getId();
								sButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

									@Override
									public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
										int state = 1;
										if (arg1) {
											state = 1;
										} else {
											state = 2;
										}
										bookFriend.setState(state);
										new UpdateBookFriendTask(BookFriendListActivity.this) {
											public void doPre() {
											};

											public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
											};
										}.executeOnExecutor(Executors.newCachedThreadPool(),
												RenheApplication.getInstance().getUserInfo().getSid(),
												RenheApplication.getInstance().getUserInfo().getAdSId(),
												AdvanceSearchUtil.CHINAL_CODE + "", prov + "", city + "", parentIndustry + "",
												childIndustry + "", keyword, id + "", state + "");
									}
								});
								int selectedIndustry = 0;
								int selectedCity = 0;
								if (bookFriend.getIndustry() != 0) {
									selectedIndustry = bookFriend.getIndustry();
									String industryName = AdvanceSearchUtil.getChildIndustryName(ind_db, IND_TABLE_NAME,
											bookFriend.getIndustry());
									if (!TextUtils.isEmpty(industryName)) {
										industryTv.setText(industryName);
									} else {
										if (bookFriend.getIndustryParents() != 0) {
											selectedIndustry = bookFriend.getIndustryParents();
											String parendIndustryName = AdvanceSearchUtil.getChildIndustryName(ind_db,
													IND_TABLE_NAME, bookFriend.getIndustryParents());
											if (!TextUtils.isEmpty(parendIndustryName)) {
												industryTv.setText(parendIndustryName);
											}
										}
									}
								} else {
									if (bookFriend.getIndustryParents() != 0) {
										selectedIndustry = bookFriend.getIndustryParents();
										String parendIndustryName = AdvanceSearchUtil.getChildIndustryName(ind_db, IND_TABLE_NAME,
												bookFriend.getIndustryParents());
										if (!TextUtils.isEmpty(parendIndustryName)) {
											industryTv.setText(parendIndustryName);
										}
									}
								}
								if (bookFriend.getCity() != 0) {
									selectedCity = bookFriend.getCity();
									String industryName = AdvanceSearchUtil.getCityName(db, TABLE_NAME, bookFriend.getCity());
									if (!TextUtils.isEmpty(industryName)) {
										cityTv.setText(industryName);
									} else {
										if (bookFriend.getProv() != 0) {
											selectedCity = bookFriend.getProv();
											String parendIndustryName = AdvanceSearchUtil.getChildIndustryName(db, TABLE_NAME,
													bookFriend.getProv());
											if (!TextUtils.isEmpty(parendIndustryName)) {
												cityTv.setText(parendIndustryName);
											}
										}
									}
								} else {
									if (bookFriend.getProv() != 0) {
										selectedCity = bookFriend.getProv();
										String parendIndustryName = AdvanceSearchUtil.getChildIndustryName(db, TABLE_NAME,
												bookFriend.getProv());
										if (!TextUtils.isEmpty(parendIndustryName)) {
											cityTv.setText(parendIndustryName);
										}
									}
								}
								if (TextUtils.isEmpty(cityTv.getText()) && TextUtils.isEmpty(industryTv.getText())) {
									seperateLine.setVisibility(View.GONE);
									industryRl.setVisibility(View.GONE);
								}
								if (TextUtils.isEmpty(cityTv.getText())) {
									cityTv.setVisibility(View.GONE);
								}
								final int doneCity = selectedCity;
								final int doneIndustry = selectedIndustry;
								final String doneCityName = cityTv.getText().toString().trim();
								final String doneIndustryName = industryTv.getText().toString().trim();
								itemRl.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View arg0) {
										Intent intent = new Intent(BookFriendListActivity.this, BookFriendActivity.class);
										intent.putExtra("type", 1);
										intent.putExtra("id", id);
										intent.putExtra("keyword", keyword);
										intent.putExtra("city", doneCity);
										intent.putExtra("industry", doneIndustry);
										intent.putExtra("cityName", doneCityName);
										intent.putExtra("industryName", doneIndustryName);
										intent.putExtra("state", bookFriend.getState());
										startActivityForResult(intent, UPDATE_BOOK);
										overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
									}
								});
								itemRl.setOnLongClickListener(new OnLongClickListener() {

									@Override
									public boolean onLongClick(View arg0) {
										createDialog(BookFriendListActivity.this, id);
										return false;
									}
								});
								bookItem.setTag(id + "");
								bookFriendLl.addView(bookItem);
							}
						} else {
							//TODO bookFriendList is empty
							scrollView.setVisibility(View.GONE);
							bookFriendEmptyRl.setVisibility(View.VISIBLE);
						}
					}
				} else {
					Toast.makeText(BookFriendListActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId());
	}

	@Override
	protected void initListener() {
		super.initListener();
		goAddBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BookFriendListActivity.this, BookFriendActivity.class);
				intent.putExtra("type", 0);
				startActivityForResult(intent, ADD_BOOK);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ADD_BOOK:
				if (null != data.getSerializableExtra("added_bookfriend")) {
					bookFriendNum += 1;
					invalidateOptionsMenu();
					scrollView.setVisibility(View.VISIBLE);
					bookFriendEmptyRl.setVisibility(View.GONE);
					final BookFriend bookFriend = (BookFriend) data.getSerializableExtra("added_bookfriend");
					String city = data.getStringExtra("added_bookfriend_city");
					String industry = data.getStringExtra("added_bookfriend_industry");
					View bookItem = LayoutInflater.from(BookFriendListActivity.this).inflate(R.layout.book_friend_list_item,
							null);

					TextView keyWordTv = (TextView) bookItem.findViewById(R.id.area_all_tv);
					TextView cityTv = (TextView) bookItem.findViewById(R.id.advance_city_tv);
					TextView industryTv = (TextView) bookItem.findViewById(R.id.advance_industry_tv);
					RelativeLayout industryRl = (RelativeLayout) bookItem.findViewById(R.id.advance_industry_rl);
					SwitchButton sButton = (SwitchButton) bookItem.findViewById(R.id.item_sb);
					View seperateLine = (View) bookItem.findViewById(R.id.seperateline);
					RelativeLayout itemRl = (RelativeLayout) bookItem.findViewById(R.id.item_rl);
					keyWordTv.setText(bookFriend.getKeywords());
					if (bookFriend.getState() == 1) {
						sButton.setChecked(true);
					} else {
						sButton.setChecked(false);
					}
					final int prov = bookFriend.getProv();
					final int cityCode = bookFriend.getCity();
					final int childIndustry = bookFriend.getIndustry();
					final int parentIndustry = bookFriend.getIndustryParents();
					final String keyword = bookFriend.getKeywords();
					final int id = bookFriend.getId();
					int selectedIndustry = 0;
					int selectedCity = 0;
					if (cityCode > 0) {
						selectedCity = cityCode;
					} else if (prov > 0) {
						selectedCity = prov;
					}
					if (childIndustry > 0) {
						selectedIndustry = childIndustry;
					} else if (parentIndustry > 0) {
						selectedIndustry = parentIndustry;
					}
					sButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
							int state = 1;
							if (arg1) {
								state = 1;
							} else {
								state = 2;
							}
							bookFriend.setState(state);
							new UpdateBookFriendTask(BookFriendListActivity.this) {
								public void doPre() {
								};

								public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
								};
							}.executeOnExecutor(Executors.newCachedThreadPool(),
									RenheApplication.getInstance().getUserInfo().getSid(),
									RenheApplication.getInstance().getUserInfo().getAdSId(), AdvanceSearchUtil.CHINAL_CODE + "",
									prov + "", cityCode + "", parentIndustry + "", childIndustry + "", keyword, id + "",
									state + "");
						}
					});
					if (!TextUtils.isEmpty(industry)) {
						industryTv.setText(industry);
					}
					if (!TextUtils.isEmpty(city)) {
						cityTv.setText(city);
					}
					if (TextUtils.isEmpty(cityTv.getText()) && TextUtils.isEmpty(industryTv.getText())) {
						seperateLine.setVisibility(View.GONE);
						industryRl.setVisibility(View.GONE);
					}
					if (TextUtils.isEmpty(cityTv.getText())) {
						cityTv.setVisibility(View.GONE);
					}
					final int state = bookFriend.getState();
					final int doneCity = selectedCity;
					final int doneIndustry = selectedIndustry;
					final String doneCityName = cityTv.getText().toString().trim();
					final String doneIndustryName = industryTv.getText().toString().trim();
					itemRl.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(BookFriendListActivity.this, BookFriendActivity.class);
							intent.putExtra("type", 1);
							intent.putExtra("id", id);
							intent.putExtra("state", state);
							intent.putExtra("keyword", keyword);
							intent.putExtra("city", doneCity);
							intent.putExtra("industry", doneIndustry);
							intent.putExtra("cityName", doneCityName);
							intent.putExtra("industryName", doneIndustryName);
							startActivityForResult(intent, UPDATE_BOOK);
							overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
						}
					});
					itemRl.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View arg0) {
							createDialog(BookFriendListActivity.this, id);
							return false;
						}
					});
					bookItem.setTag(id + "");
					bookFriendLl.addView(bookItem);
				}
				break;
			case UPDATE_BOOK:
				if (null != data.getSerializableExtra("added_bookfriend")) {
					final BookFriend bookFriend = (BookFriend) data.getSerializableExtra("added_bookfriend");
					String city = data.getStringExtra("added_bookfriend_city");
					String industry = data.getStringExtra("added_bookfriend_industry");
					for (int i = 0; i < bookFriendLl.getChildCount(); i++) {
						View bookItem = bookFriendLl.getChildAt(i);
						if (bookItem.getTag().equals(bookFriend.getId() + "")) {
							TextView keyWordTv = (TextView) bookItem.findViewById(R.id.area_all_tv);
							TextView cityTv = (TextView) bookItem.findViewById(R.id.advance_city_tv);
							TextView industryTv = (TextView) bookItem.findViewById(R.id.advance_industry_tv);
							RelativeLayout industryRl = (RelativeLayout) bookItem.findViewById(R.id.advance_industry_rl);
							SwitchButton sButton = (SwitchButton) bookItem.findViewById(R.id.item_sb);
							View seperateLine = (View) bookItem.findViewById(R.id.seperateline);
							RelativeLayout itemRl = (RelativeLayout) bookItem.findViewById(R.id.item_rl);
							keyWordTv.setText(bookFriend.getKeywords());
							if (bookFriend.getState() == 1) {
								sButton.setChecked(true);
							} else {
								sButton.setChecked(false);
							}
							final int prov = bookFriend.getProv();
							final int cityCode = bookFriend.getCity();
							final int childIndustry = bookFriend.getIndustry();
							final int parentIndustry = bookFriend.getIndustryParents();
							final String keyword = bookFriend.getKeywords();
							final int id = bookFriend.getId();
							sButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
									int state = 1;
									if (arg1) {
										state = 1;
									} else {
										state = 2;
									}
									bookFriend.setState(state);
									new UpdateBookFriendTask(BookFriendListActivity.this) {
										public void doPre() {
										};

										public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
										};
									}.executeOnExecutor(Executors.newCachedThreadPool(),
											RenheApplication.getInstance().getUserInfo().getSid(),
											RenheApplication.getInstance().getUserInfo().getAdSId(),
											AdvanceSearchUtil.CHINAL_CODE + "", prov + "", cityCode + "", parentIndustry + "",
											childIndustry + "", keyword, id + "", state + "");
								}
							});
							if (!TextUtils.isEmpty(industry)) {
								industryTv.setText(industry);
							} else {
								industryTv.setText("");
							}
							if (!TextUtils.isEmpty(city)) {
								cityTv.setText(city);
							} else {
								cityTv.setText("");
							}
							if (TextUtils.isEmpty(cityTv.getText()) && TextUtils.isEmpty(industryTv.getText())) {
								seperateLine.setVisibility(View.GONE);
								industryRl.setVisibility(View.GONE);
							} else {
								seperateLine.setVisibility(View.VISIBLE);
								industryRl.setVisibility(View.VISIBLE);
							}
							if (TextUtils.isEmpty(cityTv.getText())) {
								cityTv.setVisibility(View.GONE);
							}
							int selectedIndustry = 0;
							int selectedCity = 0;
							if (cityCode > 0) {
								selectedCity = cityCode;
							} else if (prov > 0) {
								selectedCity = prov;
							}
							if (childIndustry > 0) {
								selectedIndustry = childIndustry;
							} else if (parentIndustry > 0) {
								selectedIndustry = parentIndustry;
							}
							final int state = bookFriend.getState();
							final int doneCity = selectedCity;
							final int doneIndustry = selectedIndustry;
							final String doneCityName = cityTv.getText().toString().trim();
							final String doneIndustryName = industryTv.getText().toString().trim();
							itemRl.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Intent intent = new Intent(BookFriendListActivity.this, BookFriendActivity.class);
									intent.putExtra("type", 1);
									intent.putExtra("id", id);
									intent.putExtra("state", state);
									intent.putExtra("keyword", keyword);
									intent.putExtra("city", doneCity);
									intent.putExtra("industry", doneIndustry);
									intent.putExtra("cityName", doneCityName);
									intent.putExtra("industryName", doneIndustryName);
									startActivityForResult(intent, UPDATE_BOOK);
									overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
								}
							});
							itemRl.setOnLongClickListener(new OnLongClickListener() {

								@Override
								public boolean onLongClick(View arg0) {
									createDialog(BookFriendListActivity.this, id);
									return false;
								}
							});
							break;
						}

					}

				}
				break;
			default:
				break;
			}
		}
	}

	public void createDialog(Context context, final int id) {
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.report_shield_dialog, null);

		Builder mDialog = new AlertDialog.Builder(context);
		//		mDialog.setView(view,0,0,0,0);
		LinearLayout reportLl = (LinearLayout) view.findViewById(R.id.reportLl);
		LinearLayout shieldLl = (LinearLayout) view.findViewById(R.id.shieldLl);
		TextView reportTv = (TextView) view.findViewById(R.id.report_tv);
		TextView shiledTv = (TextView) view.findViewById(R.id.shiledTv);
		reportTv.setText("删除");
		shiledTv.setText("取消");
		mAlertDialog = mDialog.create();
		mAlertDialog.setView(view, 0, 0, 0, 0);
		mAlertDialog.setCanceledOnTouchOutside(true);
		mAlertDialog.show();
		reportLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MobclickAgent.onEvent(BookFriendListActivity.this, "delete_book_friend");
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
				new DeleteBookFriendTask(BookFriendListActivity.this) {
					public void doPre() {
						showDialog(2);
					};

					public void doPost(MessageBoardOperation result) {
						removeDialog(2);
						if (null != result) {
							if (result.getState() == 1) {
								bookFriendNum -= 1;
								invalidateOptionsMenu();
								for (int i = 0; i < bookFriendLl.getChildCount(); i++) {
									View bookItem = bookFriendLl.getChildAt(i);
									if (bookItem.getTag().equals(id + "")) {
										bookFriendLl.removeViewAt(i);
										break;
									}
								}
							} else {
								Toast.makeText(BookFriendListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(BookFriendListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
						}
					};
				}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), id + "");
			}
		});
		shieldLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog findPd = new ProgressDialog(this);
			findPd.setMessage("正在加载...");
			findPd.setCanceledOnTouchOutside(false);
			return findPd;
		case 2:
			ProgressDialog findPd2 = new ProgressDialog(this);
			findPd2.setMessage("正在删除...");
			findPd2.setCanceledOnTouchOutside(false);
			return findPd2;
		default:
			return null;
		}
	}
}
