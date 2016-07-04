package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.GuessInterestAdapter;
import com.itcalf.renhe.bean.GuessInterestBean;
import com.itcalf.renhe.bean.NFuserInfo;
import com.itcalf.renhe.bean.NewFriendsInfo;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * description :猜你感兴趣
 * Created by Chans Renhenet
 * 2015/11/26
 */
public class GuessInterestActivity extends BaseActivity {
    private ListView newfriendList;
    private TextView newfriendEmptytip;

    private GuessInterestAdapter sAdapter;
    List<NewFriendsInfo> contactslist;

    private View mFooterView;
    private View divideline;

    private int maxCount = 20;//请求数量
    private String sid = "", adSid = "";
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;

    public static final String UPDATE_LIST_ITEM = "update_guess_interest_list_item";
    private UpdateListItem updatelistitem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.newfriend_list);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        newfriendList = (ListView) findViewById(R.id.newfriend_list);
        newfriendEmptytip = (TextView) findViewById(R.id.newfriend_emptytip);

        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        divideline = (View) mFooterView.findViewById(R.id.divideline);
        divideline.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();

        setTextValue(R.id.title_txt, getResources().getString(R.string.new_friend_list_guess_interest));
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        contactslist = new ArrayList<>();
        sAdapter = new GuessInterestAdapter(this, contactslist);
        newfriendList.addFooterView(mFooterView, null, false);
        newfriendList.setAdapter(sAdapter);

        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        sid = userInfo.getSid();
        adSid = userInfo.getAdSId();
        new getInterestListTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid, "" + maxCount);

        updatelistitem = new UpdateListItem();
        IntentFilter intentFilter = new IntentFilter(UPDATE_LIST_ITEM);
        registerReceiver(updatelistitem, intentFilter);
    }

    @Override
    protected void initListener() {
        super.initListener();

//        mFooterView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleFooterView(1);
//                page = page + 1;
//                new getInterestListTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid,
//                        "" + page);
//            }
//        });

        newfriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewFriendsInfo item = (NewFriendsInfo) newfriendList.getAdapter().getItem(position);
                if (item != null) {
                    NFuserInfo ct = item.getUserInfo();
                    if (item.getStatus() == 1) {
                        //已添加
                        Intent intent = new Intent(GuessInterestActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                        intent.putExtra("name", ct.getName());
                        intent.putExtra("position", position);
                        startHlActivity(intent);
                    } else if (item.getStatus() == 0) {
                        //接受
                        Intent intent = new Intent(GuessInterestActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                        intent.putExtra("from", item.getFromType());
                        intent.putExtra("addfriend_from", "newFriend");
                        intent.putExtra("position", position);
                        startHlActivity(intent);
                    } else if (item.getStatus() == 4) {
                        //添加
                        Intent intent = new Intent(GuessInterestActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                        intent.putExtra("position", position);
                        intent.putExtra("from", Constants.ADDFRIENDTYPE[6]);//页面跳转类型
                        intent.putExtra("addfriend_from", "guessInterest");
                        startHlActivity(intent);
                    } else {
                        //拒绝，等待验证
                        Intent intent = new Intent(GuessInterestActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                        intent.putExtra("name", ct.getName());
                        startHlActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updatelistitem) {
            unregisterReceiver(updatelistitem);
            updatelistitem = null;
        }
    }

    /**
     * @author chan
     * @createtime 2014-11-4
     * @功能说明 获取的兴趣列表
     */
    class getInterestListTask extends AsyncTask<String, Void, GuessInterestBean> {

        @Override
        protected GuessInterestBean doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("maxCount", params[2]);
//            page = Integer.parseInt(params[2]);
            try {
                GuessInterestBean mb = (GuessInterestBean) HttpUtil.doHttpRequest(Constants.Http.GETINTEREST_LIST, reqParams,
                        GuessInterestBean.class, null);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(GuessInterestBean result) {
            super.onPostExecute(result);
            fadeUitl.removeFade(rootRl);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        List<NewFriendsInfo> nf = result.getInterestList();
                        if (null != nf && nf.size() > 0) {
                            //判断是否还有数据
                            //						if(nf.size()){
                            //							newfriendList.addFooterView(mFooterView, null, false);
                            //						}
                            newfriendEmptytip.setVisibility(View.GONE);
                            contactslist.addAll(nf);
//                            toggleFooterView(2);
                        } else {
//                            if (page == 1) {
                            newfriendEmptytip.setVisibility(View.VISIBLE);
//                            } else {
//                                toggleFooterView(3);
//                            }
                        }
                        if (nf.size() > 10)
                            toggleFooterView(3);
                        sAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            } else {
                ToastUtil.showErrorToast(GuessInterestActivity.this, getString(R.string.connect_server_error));
                return;
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 != NetworkUtil.hasNetworkConnection(GuessInterestActivity.this)) {
            } else {
                ToastUtil.showNetworkError(GuessInterestActivity.this);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 3:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.new_friend_loading).cancelable(false)
                        .build();
            case 2:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.request_handling).cancelable(false)
                        .build();
            default:
                return null;
        }
    }

    /**
     * 显示列表底部 1，加载中；2，查看更多；3，已经到底
     *
     * @param show
     */
    private void toggleFooterView(int show) {
        mFooterView.setVisibility(View.VISIBLE);
        switch (show) {
            case 0:
                mFooterView.setVisibility(View.GONE);
                break;
            case 1:
                ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("加载中...");
                mFooterView.findViewById(R.id.waitPb).setVisibility(View.VISIBLE);
                break;
            case 2:
                mFooterView.setEnabled(true);// 不可点
                ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("查看更多");
                mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
                break;
            case 3:
                mFooterView.setEnabled(false);// 不可点
                ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("已经到底了！");
                mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    //广播，更新头像
    class UpdateListItem extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(UPDATE_LIST_ITEM)) {
                int position = intent.getIntExtra("position", 0);
                boolean isReceive = intent.getBooleanExtra("isReceive", false);//是否是添加/接受好友
                if (isReceive) {
                    //接受好友邀请
                    contactslist.get(position).setStatus(1);//已添加
                } else {
                    //添加好友邀请
                    contactslist.get(position).setStatus(3);//等待验证
                }
                sAdapter.notifyDataSetChanged();
            }
        }
    }
}
