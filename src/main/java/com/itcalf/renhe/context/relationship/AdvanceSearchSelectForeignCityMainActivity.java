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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.itcalf.renhe.view.SelectCitySideBar;
import com.itcalf.renhe.view.SelectCitySideBar.OnTouchingLetterChangedListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AdvanceSearchSelectForeignCityMainActivity extends ListActivity implements OnClickListener {
    private static final String path = Constants.CACHE_PATH.ASSETS_DB_PATH;
    private static final String DBNAME = "city.db";
    private static final String TABLE_NAME = "mycity";
    private SQLiteDatabase db;
    private SearchCity[] cityArrays;
    private Map<String, List<SearchCity>> mCitysMap = new TreeMap<String, List<SearchCity>>();
    private SelectCitySideBar sideBar;
    // 字母显示
    private TextView mLetterTxt;
    private FastScrollAdapter sAdapter;
    private ProgressBar loadingProgressBar;
    private Handler handler;
    private Handler mHandler;
    private Runnable run;
    private EditText searchEt;
    protected ImageView mBackBt;
    private final static int SECTION_ID = -10;
    private final static int IS_LOCATING_ID = -100;
    private final static int RETRY_LOCATING_ID = -110;
    private LocationClient mLocationClient;
    private LocationMode tempMode = LocationMode.Hight_Accuracy;
    private String tempcoor = "gcj02";
    public GeofenceClient mGeofenceClient;
    public MyLocationListener mMyLocationListener;
    private SearchCity currentCity = null;
    private String locationCity;

    class SimpleAdapter extends ArrayAdapter<Item> implements PinnedSectionListAdapter {

        // private final int[] COLORS = new int[] {
        // R.color.green_light, R.color.orange_light,
        // R.color.blue_light, R.color.red_light };
        private final int[] COLORS = new int[]{R.color.search_city_item_nomal_bacg};

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
                if (tag.equals("当")) {
                    tag = "当前所在城市";
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
        public String text;
        public int id;//城市的数据库id
        public int sectionPosition;
        public int listPosition;

        public Item(int type, String text, int id) {
            this.type = type;
            this.text = text;
            this.id = id;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public int getId() {
            return id;
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

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advance_search_select_city_activity_main);
        if (savedInstanceState != null) {
            isFastScroll = savedInstanceState.getBoolean("isFastScroll");
            addPadding = savedInstanceState.getBoolean("addPadding");
            isShadowVisible = savedInstanceState.getBoolean("isShadowVisible");
            hasHeaderAndFooter = savedInstanceState.getBoolean("hasHeaderAndFooter");
        }
        sideBar = (SelectCitySideBar) findViewById(R.id.city_sidebar);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);
        searchEt = (EditText) findViewById(R.id.searchEt);
        mBackBt = (ImageView) findViewById(R.id.backBt);
        ((TextView) findViewById(R.id.title_txt)).setText("选择城市");
        sideBar.setTextView(mLetterTxt);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // // 该字母首次出现的位置
                int section = sAdapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int positon = sAdapter.getPositionForSection(section);
                    getListView().setSelection(positon);
                }
            }
        });
        searchEt.addTextChangedListener(tbxEdit_TextChanged);
        searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return true;
            }
        });
        handler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message arg0) {
                switch (arg0.what) {
                    case 1://查询全部城市回调
                        sideBar.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibility(View.GONE);
                        initializeHeaderAndFooter();
                        //					initializeAdapter();
                        initializePadding();
                        break;
                    case 2://查询当前城市回调
                        if (currentCity != null) {
                            int section = sAdapter.getPositionForTag("当前所在城市");
                            RenheApplication.getInstance().setCurrentCity(currentCity);
                            if (-1 != section) {
                                int positon = sAdapter.getPositionForSection(section);
                                //						sAdapter.remove(sAdapter.getItem(positon));
                                Item item = sAdapter.getItem(positon + 1);
                                item.setText(currentCity.getName());
                                item.setId(currentCity.getId());
                            }
                        } else {

                            int section = sAdapter.getPositionForTag("当前所在城市");
                            if (-1 != section) {
                                int positon = sAdapter.getPositionForSection(section);
                                Item item = sAdapter.getItem(positon + 1);
                                item.setId(RETRY_LOCATING_ID);
                                item.setText(getString(R.string.fail_locating));
                            }
                        }
                        sAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mHandler = new Handler();
        run = new Runnable() {

            @Override
            public void run() {
                populateCity(sAdapter, searchEt.getText().toString());
            }
        };
        mBackBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("mCitysMap", (Serializable) mCitysMap);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        if (null != getIntent().getSerializableExtra("mCitysMap")) {
            mCitysMap = new TreeMap<String, List<SearchCity>>(
                    (HashMap<String, List<SearchCity>>) getIntent().getSerializableExtra("mCitysMap"));
        }
        if (null != mCitysMap && mCitysMap.size() > 1) {
            handler.sendEmptyMessage(1);
        } else {
            if (null != mCitysMap) {
                mCitysMap.clear();
            }
            initCity(this, handler);
        }
        // currentLocation();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mGeofenceClient = new GeofenceClient(getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        InitLocation();

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
        if (item != null && item.id != IS_LOCATING_ID && item.id != SECTION_ID) {
            if (item.id == RETRY_LOCATING_ID) {
                item.setId(IS_LOCATING_ID);
                item.setText(getString(R.string.is_locating));
                sAdapter.notifyDataSetChanged();
                mLocationClient.start();
            } else {
                Intent intent = new Intent();
                intent.putExtra("yourcity", item.text);
                intent.putExtra("yourcitycode", item.id + "");
                intent.putExtra("mCitysMap", (Serializable) mCitysMap);
                setResult(RESULT_OK, intent);
                finish();
            }
            //			Toast.makeText(this, "Item " + position + ": " + item.text + ": " + item.id, Toast.LENGTH_SHORT).show();
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

    private void initCity(final Context context, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    AdvanceSearchUtil.copyDB(context, DBNAME);
                    if (db == null) {
                        db = SQLiteDatabase.openOrCreateDatabase(path + DBNAME, null);
                    }
                    cityArrays = AdvanceSearchUtil.getCity(db, TABLE_NAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (null != cityArrays && cityArrays.length > 0) {
                        for (int i = 0; i < cityArrays.length; i++) {
                            String namePinyin = PinyinUtil.cn2FirstSpell(cityArrays[i].getName());
                            if (null != namePinyin && namePinyin.length() > 0) {
                                String n = namePinyin.substring(0, 1).toUpperCase();
                                List<SearchCity> ctList = mCitysMap.get(n);
                                if (null == ctList) {
                                    ctList = new ArrayList<SearchCity>();
                                }
                                if (!cityArrays[i].getName().equals("其它")) {
                                    ctList.add(cityArrays[i]);
                                }
                                mCitysMap.put(n, ctList);

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
        populateCity(sAdapter, "");
        setListAdapter(sAdapter);
    }

    @SuppressLint("DefaultLocale")
    private void populateCity(SimpleAdapter mAdapter, String keyword) {
        mAdapter.clear();
        if (TextUtils.isEmpty(keyword)) {
            sideBar.setVisibility(View.VISIBLE);
            setListAdapter(sAdapter);
            final int sectionsNumber = mCitysMap.size() + 1;
            mAdapter.prepareSections(sectionsNumber);
            int sectionPosition = 0, listPosition = 0;

            //添加“全部城市”
            Item allCitySection = new Item(Item.SECTION, "ALL", SECTION_ID);
            allCitySection.sectionPosition = sectionPosition;
            allCitySection.listPosition = listPosition++;
            mAdapter.onSectionAdded(allCitySection, sectionPosition);
            mAdapter.add(allCitySection);
            Item allCityItem = new Item(Item.ITEM, "All Citys", AdvancedSearchActivity.ALL_FORGIGN);
            allCityItem.sectionPosition = sectionPosition;
            allCityItem.listPosition = listPosition++;
            mAdapter.add(allCityItem);
            sectionPosition++;

            Set<Entry<String, List<SearchCity>>> set = mCitysMap.entrySet();
            Iterator<Entry<String, List<SearchCity>>> it = set.iterator();

            while (it.hasNext()) {
                Map.Entry<java.lang.String, java.util.List<SearchCity>> entry = (Map.Entry<java.lang.String, java.util.List<SearchCity>>) it
                        .next();
                Item section = new Item(Item.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                mAdapter.onSectionAdded(section, sectionPosition);
                mAdapter.add(section);
                List<SearchCity> ctList = entry.getValue();
                for (int j = 0; j < ctList.size(); j++) {
                    Item item = new Item(Item.ITEM, ctList.get(j).getName(), ctList.get(j).getId());
                    item.sectionPosition = sectionPosition;
                    item.listPosition = listPosition++;
                    mAdapter.add(item);
                }
                sectionPosition++;
            }

        } else {
            sideBar.setVisibility(View.GONE);
            Map<String, List<SearchCity>> mResultsMap = new TreeMap<String, List<SearchCity>>();
            Set<Entry<String, List<SearchCity>>> set = mCitysMap.entrySet();
            Iterator<Entry<String, List<SearchCity>>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<java.lang.String, java.util.List<SearchCity>> entry = (Map.Entry<java.lang.String, java.util.List<SearchCity>>) it
                        .next();
                List<SearchCity> contactsList = entry.getValue();
                List<SearchCity> resultList = new ArrayList<SearchCity>();
                if (null != contactsList && !contactsList.isEmpty()) {
                    for (int i = 0; i < contactsList.size(); i++) {
                        SearchCity ct = contactsList.get(i);
                        if (null != keyword && null != ct.getName()
                                && (ct.getName().toUpperCase().startsWith(keyword.toUpperCase())
                                || PinyinUtil.cn2FirstSpell(ct.getName()).startsWith(keyword.toUpperCase()))) {
                            resultList.add(ct);
                            mResultsMap.put(entry.getKey(), resultList);
                        }
                    }
                }

            }

            FastScrollAdapter sResultAdapter = new FastScrollAdapter(this, R.layout.city_list_item, R.id.city_name);
            setListAdapter(sResultAdapter);

            sResultAdapter.prepareSections(mResultsMap.size());
            int sectionPosition = 0, listPosition = 0;
            Set<Entry<String, List<SearchCity>>> resultSet = mResultsMap.entrySet();
            Iterator<Entry<String, List<SearchCity>>> resultIt = resultSet.iterator();
            while (resultIt.hasNext()) {
                Map.Entry<java.lang.String, java.util.List<SearchCity>> entry = (Map.Entry<java.lang.String, java.util.List<SearchCity>>) resultIt
                        .next();
                Item section = new Item(Item.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                sResultAdapter.onSectionAdded(section, sectionPosition);
                sResultAdapter.add(section);
                List<SearchCity> ctList = entry.getValue();
                for (int j = 0; j < ctList.size(); j++) {
                    Item item = new Item(Item.ITEM, ctList.get(j).getName(), ctList.get(j).getId());
                    item.sectionPosition = sectionPosition;
                    item.listPosition = listPosition++;
                    sResultAdapter.add(item);
                }
                sectionPosition++;
            }

        }
    }

    /**
     * 搜索框输入状态监听
     **/
    private TextWatcher tbxEdit_TextChanged = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mHandler.postDelayed(run, 500);
        }

    };

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//设置定位模式
        option.setCoorType(tempcoor);//返回的定位结果是百度经纬度，默认值gcj02
        int span = 1000;
        option.setScanSpan(span);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                locationCity = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                locationCity = location.getCity();
            }
            String string = String.valueOf(locationCity.charAt(locationCity.length() - 1));
            if (string.equals("市")) {
                locationCity = locationCity.substring(0, locationCity.length() - 1);
            }
            if (null != locationCity && !TextUtils.isEmpty(locationCity)) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            AdvanceSearchUtil.copyDB(AdvanceSearchSelectForeignCityMainActivity.this, DBNAME);
                            if (db == null) {
                                db = SQLiteDatabase.openOrCreateDatabase(path + DBNAME, null);
                            }
                            currentCity = AdvanceSearchUtil.getCurrentCity(db, TABLE_NAME, locationCity);
                            handler.sendEmptyMessage(2);
                            mLocationClient.stop();
                        } catch (IOException e) {
                            mLocationClient.stop();
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                mLocationClient.stop();
                handler.sendEmptyMessage(2);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Item: " + v.getTag(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("mCitysMap", (Serializable) mCitysMap);
        setResult(RESULT_OK, intent);
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
        if (null != mLocationClient) {
            mLocationClient.stop();
        }
    }

}