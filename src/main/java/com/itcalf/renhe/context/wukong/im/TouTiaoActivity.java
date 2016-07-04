package com.itcalf.renhe.context.wukong.im;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.MyPortal;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.db.TouTiaoManager;
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiaoList;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.view.XListView;
import com.itcalf.renhe.view.XListView.IXListViewListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Title: TouTiaoActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-5 下午2:01:14 <br>
 *
 * @author wangning
 */
public class TouTiaoActivity extends BaseActivity implements IXListViewListener {

    private XListView touTiaoListView;
    private List<TouTiaoList> mTouTiaoLists = new ArrayList<TouTiaoList>();
    private TouTiaoListAdapter touTiaoListAdapter;
    private long maxUpdatedDate;
    private long minUpdatedDate;
    private Handler mHandler = new Handler();
    private static final String REQUEST_TYPE_NEW = "new";
    private static final String REQUEST_TYPE_MORE = "more";
    private RelativeLayout rootRl;
    private FadeUitl fadeUitl;
    private boolean hasFirstRequest = false;
    private TouTiaoManager touTiaoManager;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.toutiao_msg_list);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(1, "行业头条");
        touTiaoListView = (XListView) findViewById(R.id.toutiao_list);
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
    }

    @Override
    protected void initData() {
        super.initData();
        touTiaoManager = new TouTiaoManager(this);
        msp = getSharedPreferences("conversation_list", 0);
        mEditor = msp.edit();

        maxUpdatedDate = msp.getLong("toutiao_maxUpdatedDate", 0);
        minUpdatedDate = msp.getLong("toutiao_minUpdatedDate", 0);

        touTiaoListAdapter = new TouTiaoListAdapter(this, mTouTiaoLists);
        touTiaoListView.setAdapter(touTiaoListAdapter);

        touTiaoListView.setPullRefreshEnable(true);
        touTiaoListView.setPullLoadEnable(false);
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        loadCache();
        getTouTiaoList(REQUEST_TYPE_NEW, maxUpdatedDate, minUpdatedDate);

    }

    @Override
    protected void initListener() {
        super.initListener();
        touTiaoListView.setXListViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (RenheApplication.getInstance().getLogin() == 0) {
            startHlActivity(new Intent(this, MyPortal.class));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadCache() {

        TouTiaoList[] touTiaosArray = touTiaoManager.getMessageBoardsFromCursor();
        if (null != touTiaosArray && touTiaosArray.length > 0) {
            for (int i = 0; i < touTiaosArray.length; i++) {
                mTouTiaoLists.add(touTiaosArray[i]);
            }
            touTiaoListAdapter.notifyDataSetChanged();
            //滚动到底部
            touTiaoListView.setSelection(touTiaoListView.getCount());
        }
    }

    private void getTouTiaoList(final String type, long maxCreatedDate, long minCreatedDate) {
        new GetTouTiaoListTask(this) {
            public void doPre() {
                touTiaoListView.setPullRefreshEnable(true);
                touTiaoListView.setPullLoadEnable(false);
            }

            ;

            public void doPost(final com.itcalf.renhe.dto.TouTiaoOperation result) {
                if (!hasFirstRequest) {
                    fadeUitl.removeFade(rootRl);
                }
                if (null != result && result.getState() == 1) {
                    if (type.equals(REQUEST_TYPE_NEW) && result.isClearCache()) {
                        mTouTiaoLists.clear();
                        try {
                            touTiaoManager.deleteDatabase();
                            deleteDatabase(CacheManager.CONVERSATION_DBNAME);
                            mEditor.putLong("toutiao_maxUpdatedDate", 0);
                            mEditor.putLong("toutiao_minUpdatedDate", 0);
                            mEditor.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    TouTiaoList[] touTiaosArray = result.getMessageList();
                    if (null != touTiaosArray && touTiaosArray.length > 0) {
                        if (result.getMaxUpdatedDate() > 0) {
                            maxUpdatedDate = result.getMaxUpdatedDate();
                        }
                        if (result.getMinUpdatedDate() > 0) {
                            minUpdatedDate = result.getMinUpdatedDate();
                        }
                        if (type.equals(REQUEST_TYPE_MORE)) {
                            for (int i = 0; i < touTiaosArray.length; i++) {
                                mTouTiaoLists.add(0, touTiaosArray[i]);
                            }
                        } else if (type.equals(REQUEST_TYPE_NEW)) {
                            for (int i = touTiaosArray.length - 1; i >= 0; i--) {
                                mTouTiaoLists.add(touTiaosArray[i]);
                            }
                        }
                        touTiaoListAdapter.notifyDataSetChanged();
                        if (!hasFirstRequest) {
                            //滚动到底部
                            touTiaoListView.setSelection(touTiaoListView.getBottom());
                        } else {
                            if (type.equals(REQUEST_TYPE_MORE)) {
                                //滚动到底部
                                touTiaoListView.setSelection(result.getCount());
                            }
                        }
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    touTiaoManager.insert(result.getMessageList());
                                    if (result.getMaxUpdatedDate() > 0) {
                                        mEditor.putLong("toutiao_maxUpdatedDate", result.getMaxUpdatedDate());
                                    }
                                    if (result.getMinUpdatedDate() > 0) {
                                        mEditor.putLong("toutiao_minUpdatedDate", result.getMinUpdatedDate());
                                    }
                                    mEditor.commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                touTiaoListView.stopRefresh();
                touTiaoListView.stopLoadMore();
                if (!hasFirstRequest)
                    hasFirstRequest = true;
            }

            ;
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), type, maxCreatedDate + "", minCreatedDate + "");
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 执行留言列表异步加载
                getTouTiaoList(REQUEST_TYPE_MORE, maxUpdatedDate, minUpdatedDate);
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getTouTiaoList(REQUEST_TYPE_NEW, maxUpdatedDate, minUpdatedDate);
            }
        }, 2000);
    }

}
