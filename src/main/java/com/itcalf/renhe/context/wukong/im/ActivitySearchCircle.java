package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.SearchCircleListAdapter;
import com.itcalf.renhe.bean.SearchCircleBean;
import com.itcalf.renhe.bean.SearchCircleInfo;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.renhe.heliao.idl.circle.SearchCircle;

/**
 * @author chan
 * @createtime 2015-1-22
 * @功能说明 搜索圈子
 */
public class ActivitySearchCircle extends BaseActivity {

    //	private TextView search_Tv;
    private EditText keyword_edt;
    private LinearLayout search_circle_Ll;
    private LinearLayout search_circle_bynumb;
    private TextView search_bynumb_Tv;
    private LinearLayout search_circle_byname;
    private TextView search_byname_Tv;
    private ListView search_circle_list;

    private List<SearchCircle.CircleInfo> searchCircleList;
    private SearchCircleListAdapter searchCircleListAdapter;
    private String search_keywordString = "";
    private int page = 1;
    private int pageCount = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRenheApplication().addActivity(this);
        getTemplate().doInActivity(this, R.layout.search_circle);

    }

    @Override
    protected void findView() {
        super.findView();
        //		search_Tv = (TextView) findViewById(R.id.search_Tv);
        keyword_edt = (ClearableEditText) findViewById(R.id.keyword_edt);
        search_circle_Ll = (LinearLayout) findViewById(R.id.search_circle_Ll);
        search_circle_bynumb = (LinearLayout) findViewById(R.id.search_circle_bynumb);
        search_bynumb_Tv = (TextView) findViewById(R.id.search_bynumb_Tv);
        search_circle_byname = (LinearLayout) findViewById(R.id.search_circle_byname);
        search_byname_Tv = (TextView) findViewById(R.id.search_byname_Tv);
        search_circle_list = (ListView) findViewById(R.id.search_circle_list);
    }

    @Override
    protected void initData() {
        super.initData();
        //		setTitle("圈子搜索");
        setTextValue(1, "圈子搜索");
        searchCircleList = new ArrayList<>();
        searchCircleListAdapter = new SearchCircleListAdapter(this, searchCircleList);
        search_circle_list.setAdapter(searchCircleListAdapter);
    }

    @Override
    protected void initListener() {
        super.initListener();

        keyword_edt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        search_circle_bynumb.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        search_circle_byname.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        search_circle_list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //加载条件控制，是否还有下一页
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && searchCircleList.size() >= pageCount) {
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        page++;
                        searchCircle(keyword_edt.getText().toString().trim(), page, pageCount);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        search_circle_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchCircle.CircleInfo item = (SearchCircle.CircleInfo) search_circle_list.getAdapter().getItem(position);
                if (item != null) {
                    //跳转圈子详情
                    Intent i = new Intent();
                    i.putExtra("circleId", item.getId() + "");
                    i.setClass(ActivitySearchCircle.this, ActivityCircleDetail.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    //延时2s，列表清空，还原到搜索条件
                }
            }
        });

        keyword_edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                /*判断是否是“GO”键*/
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    /*隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(
                                v.getApplicationWindowToken(), 0);
                    }
                    if (TextUtils.isEmpty(keyword_edt.getText().toString().trim()))
                        return true;
                    page = 1;
                    searchCircle(keyword_edt.getText().toString().trim(), page, pageCount);
                    return true;
                }
                return false;
            }
        });
    }

    //简单判断字符串是否为纯数字
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private void searchCircle(String keyWord, int page, int pageCount) {
        int ID_TASK_SEARCH_CIRCLE = TaskManager.getTaskId();
        if (TaskManager.getInstance().exist(ID_TASK_SEARCH_CIRCLE)) {
            return;
        }
        if (grpcController == null)
            grpcController = new GrpcController();
        TaskManager.getInstance().addTask(this, ID_TASK_SEARCH_CIRCLE);
        grpcController.searchCircles(ID_TASK_SEARCH_CIRCLE, keyWord, page, pageCount);
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (null != result && result instanceof SearchCircle.SearchCircleResponse) {
            SearchCircle.SearchCircleResponse searchCircleResponse = (SearchCircle.SearchCircleResponse) result;
            if (null != searchCircleResponse.getCircleInfoList() && searchCircleResponse.getCircleInfoList().size() > 0) {
                if (page == 1)
                    searchCircleList.clear();
                searchCircleList.addAll(searchCircleResponse.getCircleInfoList());
                searchCircleListAdapter.notifyDataSetChanged();
            } else {
                ToastUtil.showErrorToast(ActivitySearchCircle.this, "没有符合条件的圈子信息！");
            }
        } else {
            ToastUtil.showErrorToast(ActivitySearchCircle.this, getString(R.string.network_error_message));
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        ToastUtil.showErrorToast(ActivitySearchCircle.this, msg);
    }

    /**
     * 加载更多
     */
    class AsyncSearchCircleMoreTask extends AsyncTask<String, Void, SearchCircleBean> {

        @Override
        protected SearchCircleBean doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("searchType", params[2]);
            reqParams.put("circleId", params[3]);
            reqParams.put("name", params[4]);
            reqParams.put("page", params[5]);
            reqParams.put("pageCount", params[6]);
            try {
                SearchCircleBean mb = (SearchCircleBean) HttpUtil.doHttpRequest(Constants.Http.SEARCH_CIRCLE, reqParams,
                        SearchCircleBean.class, null);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SearchCircleBean result) {
            super.onPostExecute(result);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        List<SearchCircleInfo> nf = result.getCircleList();
                        if (null != nf && nf.size() > 0) {
                            //						newfriendEmptytip.setVisibility(View.GONE);
                            for (int i = 0; i < nf.size(); i++) {
                            }
                        } else {
                            ToastUtil.showErrorToast(ActivitySearchCircle.this, "没有符合条件的圈子信息！");
                        }
                        break;
                    default:
                        break;
                }
            } else {
                ToastUtil.showErrorToast(ActivitySearchCircle.this, getString(R.string.connect_server_error));
                return;
            }
            searchCircleListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 == NetworkUtil.hasNetworkConnection(ActivitySearchCircle.this)) {
                ToastUtil.showNetworkError(ActivitySearchCircle.this);
            }
        }
    }

}
