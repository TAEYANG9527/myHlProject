package com.itcalf.renhe.context.relationship;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AdvanceSearchSelectIndustryMainActivity extends ListActivity implements OnClickListener {
	private static final String path = Constants.CACHE_PATH.ASSETS_DB_PATH;
	private static final String DBNAME = "industry";
	private static final String TABLE_NAME = "industry";
	private SQLiteDatabase db;
	private SearchCity[] industryArrays;
	private SearchCity[] chileIndustryArrays;
	private Map<String, List<SearchCity>> mCitysMap = new TreeMap<String, List<SearchCity>>();
	private FastScrollAdapter sAdapter;
	private ProgressBar loadingProgressBar;
	private Handler handler;
	private Handler mHandler;
	private Runnable run;
	protected ImageView mBackBt;
	public static final int ALL_INDUSTRY = -1;
	private boolean isFromArcheveEdit = false;
	private static final int NO_SELECT = -10;
	private int selectedId = NO_SELECT;
	private String selectedIndustry = "";
	private int selectedPosition = 0;

	class SimpleAdapter extends ArrayAdapter<Item>implements PinnedSectionListAdapter {

		// private final int[] COLORS = new int[] {
		// R.color.green_light, R.color.orange_light,
		// R.color.blue_light, R.color.red_light };
		private final int[] COLORS = new int[] { R.color.search_city_item_nomal_bacg };

		public SimpleAdapter(Context context, int resource, int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		protected void prepareSections(int sectionsNumber) {
		}

		protected void onSectionAdded(Item section, int sectionPosition) {
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = (View) super.getView(position, convertView, parent);
			RelativeLayout textRl = (RelativeLayout) view.findViewById(R.id.text_rl);
			TextView textView = (TextView) view.findViewById(R.id.city_name);
			ImageView checkedIv = (ImageView) view.findViewById(R.id.checkedIv);
			textView.setTextColor(Color.DKGRAY);
			textView.setTag("" + position);
			Item item = getItem(position);
			if (item.type == Item.SECTION) {
				// view.setOnClickListener(PinnedSectionListActivity.this);
				view.setBackgroundColor(parent.getResources().getColor(COLORS[0]));
				textView.setTextSize(13);
				int padding = (int) parent.getResources().getDimension(R.dimen.advance_search_selectcity_item_section_padding);
				textRl.setPadding(padding, padding, padding, padding);
			}
			if (selectedId != NO_SELECT && item.id == selectedId) {
				checkedIv.setVisibility(View.VISIBLE);
			} else {
				checkedIv.setVisibility(View.GONE);
			}
			return view;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).type;
		}

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType == Item.SECTION;
		}

	}

	class FastScrollAdapter extends SimpleAdapter implements SectionIndexer {

		private Item[] sections;

		public FastScrollAdapter(Context context, int resource, int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		@Override
		protected void prepareSections(int sectionsNumber) {
			sections = new Item[sectionsNumber];
		}

		@Override
		protected void onSectionAdded(Item section, int sectionPosition) {
			sections[sectionPosition] = section;
		}

		@Override
		public Item[] getSections() {
			return sections;
		}

		@Override
		public int getPositionForSection(int section) {
			if (section >= sections.length) {
				section = sections.length - 1;
			}
			return sections[section].listPosition;
		}

		@Override
		public int getSectionForPosition(int position) {
			if (position >= getCount()) {
				position = getCount() - 1;
			}
			return getItem(position).sectionPosition;
		}

		public String getSectionNameForPosition(int position) {
			if (position >= getCount()) {
				position = getCount() - 1;
			}
			return sections[getItem(position).sectionPosition].text;
		}

		/**
		 * 通过标题获取标题的位置
		 * 
		 * @param tag
		 * @return
		 */
		public int getPositionForTag(String tag) {
			if (null != sections && sections.length > 0) {
				if (tag.equals("热")) {
					tag = "热门城市";
				}
				for (int i = 0; i < sections.length; i++) {
					if (sections[i].text.equals(tag))
						return i;
				}
			}
			return -1;
		}

	}

	static class Item {

		public static final int ITEM = 0;
		public static final int SECTION = 1;

		public final int type;
		public final String text;
		public final int id;// 城市的数据库id
		public int sectionPosition;
		public int listPosition;

		public Item(int type, String text, int id) {
			this.type = type;
			this.text = text;
			this.id = id;
		}

		@Override
		public String toString() {
			return text;
		}

	}

	private boolean hasHeaderAndFooter;
	private boolean isFastScroll;
	private boolean addPadding;
	private boolean isShadowVisible = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advance_search_select_industry_activity_main);
		if (savedInstanceState != null) {
			isFastScroll = savedInstanceState.getBoolean("isFastScroll");
			addPadding = savedInstanceState.getBoolean("addPadding");
			isShadowVisible = savedInstanceState.getBoolean("isShadowVisible");
			hasHeaderAndFooter = savedInstanceState.getBoolean("hasHeaderAndFooter");
		}
		selectedId = getIntent().getIntExtra("selectedId", NO_SELECT);
		selectedIndustry = getIntent().getStringExtra("selectedIndustry");
		loadingProgressBar = (ProgressBar) findViewById(R.id.loading);
		mBackBt = (ImageView) findViewById(R.id.backBt);
		((TextView) findViewById(R.id.title_txt)).setText("选择行业");
		isFromArcheveEdit = getIntent().getBooleanExtra("isFromArcheveEdit", false);
		handler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message arg0) {
				if (arg0.what == 1) {
					loadingProgressBar.setVisibility(View.GONE);
					initializeHeaderAndFooter();
					// initializeAdapter();
					initializePadding();
					//根据已选择的行业，将列表滚动到该位置
					if (!TextUtils.isEmpty(selectedIndustry) && null != sAdapter) {
						selectedPosition = selectedPosition - 2;
						if (selectedPosition <= 0) {
							selectedPosition = 0;
						}
						getListView().setSelection(selectedPosition);
					}
				}
				return false;
			}
		});
		mHandler = new Handler();
		mBackBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				//				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		});
		initIndustry(this, handler);
		// currentLocation();

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("人脉搜索——选择行业"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("人脉搜索——选择行业"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isFastScroll", isFastScroll);
		outState.putBoolean("addPadding", addPadding);
		outState.putBoolean("isShadowVisible", isShadowVisible);
		outState.putBoolean("hasHeaderAndFooter", hasHeaderAndFooter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) getListView().getAdapter().getItem(position);
		if (item != null) {
			//			Toast.makeText(this,
			//					"Item " + position + ": " + item.text + ": " + item.id,
			//					Toast.LENGTH_SHORT).show();
			String industrySection = "";
			if (null != sAdapter) {
				industrySection = sAdapter.getSectionNameForPosition(position);
			}
			Intent intent = new Intent();
			intent.putExtra("yourindustry", item.text);
			intent.putExtra("yourindustrycode", item.id + "");
			intent.putExtra("yourindustrySection", industrySection);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			//			Toast.makeText(this, "Item " + position, Toast.LENGTH_SHORT).show();
		}
	}

	private void initializePadding() {
		float density = getResources().getDisplayMetrics().density;
		int padding = addPadding ? (int) (16 * density) : 0;
		getListView().setPadding(padding, padding, padding, padding);
	}

	private void initializeHeaderAndFooter() {
		setListAdapter(null);
		if (hasHeaderAndFooter) {
			ListView list = getListView();

			LayoutInflater inflater = LayoutInflater.from(this);
			TextView header1 = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, list, false);
			header1.setText("First header");
			list.addHeaderView(header1);

			TextView header2 = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, list, false);
			header2.setText("Second header");
			list.addHeaderView(header2);

			TextView footer = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, list, false);
			footer.setText("Single footer");
			list.addFooterView(footer);
		}
		initializeAdapter();
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
				try {
					if (null != industryArrays && industryArrays.length > 0) {
						//						List<SearchCity> allInduList = new ArrayList<SearchCity>();
						//						SearchCity allIndu = new SearchCity(ALL_INDUSTRY,
						//								"所有行业");
						//						allInduList.add(allIndu);
						//						mCitysMap.put("所有行业", allInduList);
						for (int i = 0; i < industryArrays.length; i++) {
							List<SearchCity> induList = new ArrayList<SearchCity>();
							if (industryArrays[i] != null) {
								if (industryArrays[i].getName().equals("金融业")) {
									industryArrays[i].setId(1);
								}
								chileIndustryArrays = AdvanceSearchUtil.getChildIndustry(db, TABLE_NAME,
										industryArrays[i].getId(), industryArrays[i].getName());
								for (SearchCity searchCity : chileIndustryArrays) {
									if (searchCity != null) {
										induList.add(searchCity);
										mCitysMap.put(industryArrays[i].getName() + "//" + industryArrays[i].getId(), induList);
									}
								}
							}

						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				handler.sendEmptyMessage(1);
			}
		}).start();
	}

	@SuppressLint("NewApi")
	private void initializeAdapter() {
		getListView().setFastScrollEnabled(isFastScroll);
		sAdapter = new FastScrollAdapter(this, R.layout.city_list_item, R.id.city_name);
		populateCity(sAdapter);
		setListAdapter(sAdapter);
	}

	@SuppressLint("DefaultLocale")
	private void populateCity(SimpleAdapter mAdapter) {
		final int sectionsNumber = mCitysMap.size() + 1;
		mAdapter.prepareSections(sectionsNumber);
		int sectionPosition = 0, listPosition = 0;
		Set<Entry<String, List<SearchCity>>> set = mCitysMap.entrySet();
		Iterator<Entry<String, List<SearchCity>>> it = set.iterator();
		//添加所有行业
		if (!isFromArcheveEdit) {
			Item allSection = new Item(Item.SECTION, "所有行业", ALL_INDUSTRY);
			allSection.sectionPosition = sectionPosition;
			allSection.listPosition = listPosition++;
			mAdapter.onSectionAdded(allSection, sectionPosition);
			mAdapter.add(allSection);
			Item allItem = new Item(Item.ITEM, "所有行业", ALL_INDUSTRY);
			allItem.sectionPosition = sectionPosition;
			allItem.listPosition = listPosition++;
			if (allItem.id == selectedId) {
				selectedPosition = listPosition;
			}
			mAdapter.add(allItem);
			sectionPosition++;
		}
		//添加其它行业
		while (it.hasNext()) {
			Map.Entry<java.lang.String, java.util.List<SearchCity>> entry = (Map.Entry<java.lang.String, java.util.List<SearchCity>>) it
					.next();
			String sectionName = entry.getKey().substring(0, entry.getKey().indexOf("//"));
			int sectionId;
			try {
				sectionId = Integer.parseInt(entry.getKey().substring(entry.getKey().indexOf("//") + 2, entry.getKey().length()));
			} catch (NumberFormatException e) {
				sectionId = ALL_INDUSTRY;
				e.printStackTrace();
			}
			Item section = new Item(Item.SECTION, sectionName, sectionId);
			section.sectionPosition = sectionPosition;
			section.listPosition = listPosition++;
			mAdapter.onSectionAdded(section, sectionPosition);
			mAdapter.add(section);
			if (section.id == selectedId) {
				selectedPosition = listPosition;
			}
			List<SearchCity> ctList = entry.getValue();
			for (int j = 0; j < ctList.size(); j++) {
				Item item = new Item(Item.ITEM, ctList.get(j).getName(), ctList.get(j).getId());
				item.sectionPosition = sectionPosition;
				item.listPosition = listPosition++;
				mAdapter.add(item);
				if (item.id == selectedId) {
					selectedPosition = listPosition;
				}
			}
			sectionPosition++;
		}
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, "Item: " + v.getTag(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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