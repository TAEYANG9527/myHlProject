package com.itcalf.renhe.context.relationship.selectindustry;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.relationship.AdvanceSearchUtil;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.SearchCity;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectIndustryListActivity extends BaseActivity {
	private ListView industryList;
	private static final String DBNAME = "industry";
	private static final String TABLE_NAME = "industry";
	private SQLiteDatabase db;
	private SearchCity[] industryArrays;
	private static final String path = Constants.CACHE_PATH.ASSETS_DB_PATH;
	private IndustryAdapter mAdapter;
	private List<HashMap<String, Object>> industryData;
	private int index = -1; //记录被选中的行
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTemplate().doInActivity(this, R.layout.select_industry_list);
		setTextValue(R.id.title_txt, "按行业筛选");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("附近的人脉——按行业筛选"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("附近的人脉——按行业筛选"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		industryList = (ListView) findViewById(R.id.industry_list);
	}

	@Override
	protected void initData() {
		super.initData();
		Intent intent = getIntent();
		if (intent != null) {
			index = intent.getIntExtra("selected_position", -1);
		}
		industryData = new ArrayList<HashMap<String, Object>>();
		mAdapter = new IndustryAdapter();
		handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.what == 1) {
					try {
						if (null != industryArrays && industryArrays.length > 0) {
							for (SearchCity indus : industryArrays) {
								HashMap<String, Object> item = new HashMap<String, Object>();
								item.put("name", indus.getName());
								if (indus.getName().equals("金融业")) {
									item.put("id", 1);
								} else {
									item.put("id", indus.getId());
								}
								industryData.add(item);
							}
						}
					} catch (Exception e) {
						System.out.println(e);
					}
					mAdapter.notifyDataSetChanged();
				}
				return false;
			}

		});
		initIndustry(this, handler);
		industryList.setAdapter(mAdapter);
	}

	@Override
	protected void initListener() {
		super.initListener();
		industryList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ViewHolder holder = (ViewHolder) view.getTag();
				Intent data = new Intent();
				//该条目被点击过
				if (position == index) {
					if (holder.selectedIv.getVisibility() == View.INVISIBLE) {
						holder.selectedIv.setVisibility(View.VISIBLE);
						data.putExtra("industry_code", industryData.get(position).get("id").toString());
						data.putExtra("industry_name", industryData.get(position).get("name").toString());
						data.putExtra("selected_position", index);
						setResult(RESULT_OK, data);
						finish();
					} else {
						holder.selectedIv.setVisibility(View.INVISIBLE);
					}

				} else {
					index = position;
					mAdapter.notifyDataSetChanged();
					//					holder.selectedIv.setVisibility(View.VISIBLE);
					data.putExtra("industry_code", industryData.get(position).get("id").toString());
					data.putExtra("industry_name", industryData.get(position).get("name").toString());
					data.putExtra("selected_position", index);
					setResult(RESULT_OK, data);
					finish();
				}

			}
		});
	}

	private void initIndustry(final Context context, final Handler handler) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					AdvanceSearchUtil.copyDB(context, DBNAME);
					if (db == null) {
						db = SQLiteDatabase.openOrCreateDatabase(path + DBNAME, null);
					}
					industryArrays = AdvanceSearchUtil.getIndustry(db, TABLE_NAME);
				} catch (IOException e) {
					e.printStackTrace();
				}

				handler.sendEmptyMessage(1);
			}
		}).start();
	}

	class IndustryAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return industryData.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(SelectIndustryListActivity.this).inflate(R.layout.industry_list_item, null);
				holder.industryName = (TextView) convertView.findViewById(R.id.industry_name);
				holder.selectedIv = (ImageView) convertView.findViewById(R.id.selected_iv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.industryName.setText(industryData.get(position).get("name").toString());
			if (position == index) {
				holder.selectedIv.setVisibility(View.VISIBLE);
			} else {
				holder.selectedIv.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

	}

	class ViewHolder {
		TextView industryName;
		ImageView selectedIv;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
			db = null;
		}
	}
}
