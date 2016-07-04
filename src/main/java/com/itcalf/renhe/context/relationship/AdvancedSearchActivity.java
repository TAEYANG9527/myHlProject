package com.itcalf.renhe.context.relationship;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.SearchHistoryAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.edit.SelectCityActivity;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.SearchHistoryItem;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.WriteLogThread;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: AdvancedSearchActivity.java<br>
 * Description: 人脉搜索界面<br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-4-11 上午10:08:47 <br>
 *
 * @author wangning
 */
public class AdvancedSearchActivity extends BaseActivity {
    //登出标识（两次按下返回退出时的标识字段）
    private Button sureButton;
    private EditText keywordEdt;
    //	private RelativeLayout areaLayout;
    private LinearLayout areaLayout;
    private TextView areaTv;
    //	private RelativeLayout industryLayout;
    private LinearLayout industryLayout;
    private TextView industryTv;
    private EditText companyEt;
    private EditText jobEt;
    private EditText nameEt;

    private int cityCode = -1;
    private int industryCode = -1;
    public static final int ALL_AREA = -1;
    public static final int ALL_CHINA = -2;
    public static final int ALL_FORGIGN = -3;
    public static final int ALL_INDUSTRY = -1;
    public static final String ALL_AREA_STRING = "全部城市";
    public static final String ALL_INDUSTRY_STRING = "全部行业";
    public static final String ALL_INDUSTRY_STRING2 = "所有行业";
    public static final String ALL_CITY_STRING = "全部城市";
    public static final String ALL_CITY_STRING2 = "所有城市";
//    private Map<String, List<SearchCity>> mCitysMap = new TreeMap<String, List<SearchCity>>();

    private SQLiteDatabase db;
    private List<SearchHistoryItem> searchHistoryList = new ArrayList<SearchHistoryItem>();
    private SearchHistoryAdapter historyAdapter;
    private ListView historyLv;
    private TextView noHistoryTv;
    private TextView clearHistoryTv;
    private RelativeLayout historyLl;
    private LinearLayout clearll;
    private RelativeLayout rootRl;
    private RelativeLayout historyRl;
    private String selectedIndustry = "";
    private int selectSectionId = AdvancedSearchActivity.ALL_INDUSTRY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.advanced_search_hl);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(R.id.title_txt, "高级搜索");
        sureButton = (Button) findViewById(R.id.advance_ok_btn);
        keywordEdt = (EditText) findViewById(R.id.advance_keywork_et);
        //		areaLayout = (RelativeLayout) findViewById(R.id.advance_area_rl);
        areaLayout = (LinearLayout) findViewById(R.id.advance_area_rl);
        areaTv = (TextView) findViewById(R.id.advance_area_rl_tv);
        //		industryLayout = (RelativeLayout) findViewById(R.id.advance_industry_rl);
        industryLayout = (LinearLayout) findViewById(R.id.advance_industry_rl);
        industryTv = (TextView) findViewById(R.id.advance_industry_rl_tv);
        companyEt = (EditText) findViewById(R.id.advance_company_et);
        jobEt = (EditText) findViewById(R.id.advance_job_et);
        nameEt = (EditText) findViewById(R.id.advance_name_et);

        historyLv = (ListView) findViewById(R.id.historylv);
        noHistoryTv = (TextView) findViewById(R.id.nohistoryTv);
        clearHistoryTv = (TextView) findViewById(R.id.clearhistoryTv);
        historyLl = (RelativeLayout) findViewById(R.id.histroyll);
        clearll = (LinearLayout) findViewById(R.id.clearll);
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        historyRl = (RelativeLayout) findViewById(R.id.historyRl);

    }

    @Override
    protected void initListener() {
        super.initListener();

        areaLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(AdvancedSearchActivity.this, "人脉搜索——选择城市");
//				Intent intent = new Intent(AdvancedSearchActivity.this, AdvanceSearchSelectCityMainActivity.class);
                Intent intent = new Intent(AdvancedSearchActivity.this, SelectCityActivity.class).putExtra("isShowAll", true);
//				intent.putExtra("mCitysMap", (Serializable) mCitysMap);
                startActivityForResult(intent, 10);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        industryLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(AdvancedSearchActivity.this, "人脉搜索——选择行业");
                //				Intent intent = new Intent(AdvancedSearchActivity.this,WheelActivity.class);
                //				Intent intent = new Intent(AdvancedSearchActivity.this, AdvanceSearchSelectIndustryMainActivity.class);
                Intent intent = new Intent(AdvancedSearchActivity.this, SelectIndustryExpandableListActivity.class);
                intent.putExtra("select_wheel", "industry");
                intent.putExtra("isFromAdvanceSearch", true);
                intent.putExtra("selectedId", selectSectionId);
                intent.putExtra("selectedIndustry", selectedIndustry);
                startActivityForResult(intent, 11);
                //				overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        sureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(AdvancedSearchActivity.this, "人脉搜索——点击搜索按钮");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_result_more_search_process_click), 0, "", null);

                String keyword = keywordEdt.getText().toString().trim();
                String area = areaTv.getText().toString().trim();
                String industry = industryTv.getText().toString().trim();
                String company = companyEt.getText().toString().trim();
                String job = jobEt.getText().toString().trim();
                String name = nameEt.getText().toString().trim();

                Intent intent = new Intent(AdvancedSearchActivity.this, SearchResultActivity.class);
                intent.putExtra("keyword", keyword);
                intent.putExtra("area", area);
                intent.putExtra("citycode", cityCode);
                intent.putExtra("industry", industry);
                intent.putExtra("industryCode", industryCode);
                intent.putExtra("company", company);
                intent.putExtra("job", job);
                intent.putExtra("name", name);//[XXX]
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                if (TextUtils.isEmpty(keyword) && (TextUtils.isEmpty(area) || area.equals(AdvancedSearchActivity.ALL_AREA_STRING))
                        && (TextUtils.isEmpty(industry) || industry.equals(ALL_INDUSTRY_STRING)
                        || industry.equals(ALL_INDUSTRY_STRING2))
                        && TextUtils.isEmpty(company) && TextUtils.isEmpty(job) && TextUtils.isEmpty(name)) {
                } else {
                    CacheManager.getInstance().saveSearchListoryItem(keyword, area, cityCode, industry, industryCode, company,
                            job, name);
                }
            }
        });
        clearll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(AdvancedSearchActivity.this, "clear_searchhistroy");
                Dialog alertDialog = new AlertDialog.Builder(AdvancedSearchActivity.this).setTitle("提示")
                        .setMessage("您确定删除历史搜索记录吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (null != searchHistoryList) {
                                    searchHistoryList.clear();
                                    if (null != historyAdapter) {
                                        historyAdapter.notifyDataSetChanged();
                                    }
                                }
                                historyLl.setVisibility(View.GONE);
                                noHistoryTv.setVisibility(View.VISIBLE);
                                historyRl.setVisibility(View.GONE);
                                CacheManager.getInstance().clearSearchHistory();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                alertDialog.show();

            }
        });
        historyLv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                MobclickAgent.onEvent(AdvancedSearchActivity.this, "selct_histry_item");
                SearchHistoryItem searchHistoryItem = searchHistoryList.get(arg2);
                //				keywordEt.setText(searchHistoryItem.getKewword());
                keywordEdt.setText(searchHistoryItem.getKewword());
                areaTv.setText(searchHistoryItem.getArea());

                cityCode = searchHistoryItem.getAreaCode();
                industryTv.setText(searchHistoryItem.getIndustry());
                industryCode = searchHistoryItem.getIndustryCode();
                companyEt.setText(searchHistoryItem.getCompany());
                jobEt.setText(searchHistoryItem.getJob());
                nameEt.setText(searchHistoryItem.getName());
                selectSectionId = industryCode;
                selectedIndustry = searchHistoryItem.getIndustry();
                sureButton.performClick();
            }

        });

        keywordEdt.setOnEditorActionListener(new CustomOnEditorActionListener());
        nameEt.setOnEditorActionListener(new CustomOnEditorActionListener());
        jobEt.setOnEditorActionListener(new CustomOnEditorActionListener());
        companyEt.setOnEditorActionListener(new CustomOnEditorActionListener());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (resultCode == -1) {
//                mCitysMap = new TreeMap<>(
//                        (HashMap<String, List<SearchCity>>) data.getSerializableExtra("mCitysMap"));
                String yourCity = data.getStringExtra("yourcity");
                String yourCityCodetemp = data.getStringExtra("yourcitycode");
                if (yourCity != null && yourCityCodetemp != null) {
                    areaTv.setText(yourCity);
                    cityCode = Integer.parseInt(yourCityCodetemp);
                }
            }
        } else if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                String yourIndustry = data.getStringExtra("yourindustry");
                String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
                if (yourIndustry != null && yourIndustryCodetemp != null) {
                    industryTv.setText(yourIndustry);
                    industryCode = Integer.parseInt(yourIndustryCodetemp);
                    selectSectionId = industryCode;
                    selectedIndustry = yourIndustry;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (null != mCitysMap) {
//            mCitysMap.clear();
//            mCitysMap = null;
//        }
    }

    //查询搜索结果
    public void initSearchHistory() {
        if (null != searchHistoryList) {
            searchHistoryList.clear();
        }
        if (null != historyAdapter) {
            historyAdapter = null;
        }
        db = CacheManager.getInstance().openSearchDB();
        CacheManager.getInstance().createSearchListoryTable();
        searchHistoryList = CacheManager.getInstance().querySearchHistory();
        historyAdapter = new SearchHistoryAdapter(this, searchHistoryList, historyLv);
        historyLv.setAdapter(historyAdapter);
        setListViewHeightBasedOnChildren(historyLv);
        if (null != searchHistoryList && searchHistoryList.size() <= 0) {
            historyLl.setVisibility(View.GONE);
            noHistoryTv.setVisibility(View.VISIBLE);
            historyRl.setVisibility(View.GONE);
        } else {
            historyLl.setVisibility(View.VISIBLE);
            noHistoryTv.setVisibility(View.GONE);
            //			historyRl.setVisibility(View.VISIBLE);
            historyRl.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != sureButton) {
            sureButton.setClickable(true);
        }
        initSearchHistory();
        MobclickAgent.onPageStart("人脉搜索——条件搜索页"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("人脉搜索——条件搜索页"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        if (historyAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = historyAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = historyAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (historyAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    class CustomOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            /*判断是否是“搜索”键*/
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    /*隐藏软键盘*/
                InputMethodManager imm = (InputMethodManager) v
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(
                            v.getApplicationWindowToken(), 0);
                }
                sureButton.performClick();
                return true;
            }
            return true;
        }
    }
}
