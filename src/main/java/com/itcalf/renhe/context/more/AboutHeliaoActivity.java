package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.portal.ClauseActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.CheckUpdateUtil;
import com.itcalf.renhe.utils.ToastUtil;

/**
 * description :关于和聊页面
 * Created by Chans Renhenet
 * 2015/10/15
 */
public class AboutHeliaoActivity extends BaseActivity {

    private RelativeLayout commonProblemsRl, feedbackRl, serviceTermsRl, checkUpdateRl, clearCacheRl;
    private TextView cacheSizeTv;

    private double cacheSize = 0;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.about_heliao);
    }

    @Override
    protected void findView() {
        super.findView();
        //commonProblemsRl = (RelativeLayout) findViewById(R.id.common_problems_Rl);//常见问题
        feedbackRl = (RelativeLayout) findViewById(R.id.feedback_Rl);
        serviceTermsRl = (RelativeLayout) findViewById(R.id.service_terms_Rl);
        checkUpdateRl = (RelativeLayout) findViewById(R.id.check_update_Rl);
        clearCacheRl = (RelativeLayout) findViewById(R.id.clear_cache_Rl);

        cacheSizeTv = (TextView) findViewById(R.id.cache_size);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "关于和聊");
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0) {
                    if (cacheSize > 0.01)
                        cacheSizeTv.setText("" + String.format("%.2f", cacheSize) + "M ");
                }
                return true;
            }
        });
        getCache();
    }

    /**
     * 获取缓存
     */
    private void getCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cacheSize = CacheManager.getInstance().populateData(AboutHeliaoActivity.this)
                            .getCacheSize();
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void initListener() {
        super.initListener();
        //常见问题
       /* commonProblemsRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(AboutHeliaoActivity.this, WebViewActWithTitle.class);
                i.putExtra("url", Constants.HlUseHelpUrl.HL_USE_HELP_URL);
                i.putExtra("shareable", false);
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });*/
        //意见反馈
        feedbackRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutHeliaoActivity.this, FeedBackActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //服务条款
        serviceTermsRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutHeliaoActivity.this, ClauseActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //检查更新
        checkUpdateRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckUpdateUtil(AboutHeliaoActivity.this).checkUpdate(true);
            }
        });
        //清除缓存
        clearCacheRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCache();
            }
        });
    }

    private void clearCache() {
        AsyncImageLoader.getInstance().clearCache();
        CacheManager.getInstance().populateData(this)
                .clearCache(((RenheApplication) getApplicationContext()).getUserInfo().getEmail(), true);
        sendBroadcast(new Intent(Constants.BrocastAction.REST_CIRCLE_MAX_MIN_RANK_ACTION));
//        RenheApplication.getInstance().setNetInitVCard(true);
        cacheSizeTv.setText("");
        ToastUtil.showToast(this, "清除成功");
    }

}
