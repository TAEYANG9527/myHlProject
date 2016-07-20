package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.NewFriendAdapter;
import com.itcalf.renhe.bean.NFuserInfo;
import com.itcalf.renhe.bean.NewFriendsInfo;
import com.itcalf.renhe.bean.NewFriendsListBean;
import com.itcalf.renhe.context.MyPortal;
import com.itcalf.renhe.context.archives.MyContactArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.relationship.AdvancedSearchIndexActivityTwo;
import com.itcalf.renhe.context.relationship.NearbyActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.eventbusbean.RefreshNewFriendListEvent;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author chan
 * @createtime 2014-11-4
 * @功能说明 新的朋友页面
 */

public class NewFriendsAct extends BaseActivity {
    private ListView newfriendList;
    private TextView newfriendEmptytip;
    private LinearLayout loadingLL;
    private RelativeLayout nearbyRl;
    private RelativeLayout guessInterestRl;
    private NewFriendAdapter sAdapter;
    private List<NewFriendsInfo> contactslist;

    private View mHeadView;//头部
    private LinearLayout searchBox;
    private TextView nearbyContactsTv, guessInterestTv;
    private LinearLayout identificationCardLl;//名片栏（含下划线）
    private RelativeLayout identificationCardRl;
    private ImageView identCardNumb;//识别中的名片数
    private View mFooterView;
    private View divideline;
    private ImageView nearbyNoticeIv;//附近的人脉小红点
    private int page = 1;
    private String sid = "", adSid = "";
    private boolean isCheckNearby;//用来标示在附近的人脉有小红点的情况下是否点击了附近的人脉，如果有则为true，主要是为了通知人脉列表页的新的好友右侧的小红点消失

    public static final String UPDATE_LISTITEM = "newFriend_upload_list_item";
    public static final String UPDATE_DELETE_LIST_ITEM = "newFriend_delete_upload_list_item";
    private UpdateListItem updatelistitem = null;
    private static final int OCR_CARDS_REQUEST_CODE = 100;//查看识别中名片详情
    private int ID_TASK_CHECK_UPGRADE = TaskManager.getTaskId();

    private CheckUpgradeUtil checkUpgradeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.newfriend_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("查看新的朋友"); // 统计页面
        MobclickAgent.onResume(this);
        checkUpgradeUtil.checkUpgrade();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("查看新的朋友"); // 保证 onPageEnd 在onPause 之前调用,因为
        // onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updatelistitem) {
            unregisterReceiver(updatelistitem);
            updatelistitem = null;
        }
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem add = menu.findItem(R.id.item_invite_friend);
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        add.setIcon(null);
        add.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_invite_friend:
                startActivity(new Intent(NewFriendsAct.this, AdvancedSearchIndexActivityTwo.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        newfriendList = (ListView) findViewById(R.id.newfriend_list);
        newfriendEmptytip = (TextView) findViewById(R.id.newfriend_emptytip);
        newfriendEmptytip.setVisibility(View.GONE);

        mHeadView = LayoutInflater.from(this).inflate(R.layout.newfriends_list_head, null);
        searchBox = (LinearLayout) mHeadView.findViewById(R.id.search_box_Ll);
        nearbyContactsTv = (TextView) mHeadView.findViewById(R.id.nearby_contacts_tv);
        guessInterestTv = (TextView) mHeadView.findViewById(R.id.guess_interest_tv);
        identificationCardLl = (LinearLayout) mHeadView.findViewById(R.id.identification_card_Ll);
        identificationCardRl = (RelativeLayout) mHeadView.findViewById(R.id.identification_card_Rl);
        identCardNumb = (ImageView) mHeadView.findViewById(R.id.new_vcard_numb);

        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        divideline = mFooterView.findViewById(R.id.divideline);
        divideline.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.GONE);

        loadingLL = (LinearLayout) findViewById(R.id.loadingLL);
        nearbyNoticeIv = (ImageView) mHeadView.findViewById(R.id.nearby_notice_iv);
        nearbyRl = (RelativeLayout) mHeadView.findViewById(R.id.nearby_rl);
        guessInterestRl = (RelativeLayout) mHeadView.findViewById(R.id.guess_interest_rl);
    }

    @Override
    protected void initData() {
        super.initData();
        checkUpgradeUtil = new CheckUpgradeUtil(this);
        updatelistitem = new UpdateListItem();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_LISTITEM);
        intentFilter.addAction(UPDATE_DELETE_LIST_ITEM);
        registerReceiver(updatelistitem, intentFilter);

        setTextValue(R.id.title_txt, "新的好友");
        contactslist = new ArrayList<>();
        sAdapter = new NewFriendAdapter(this, contactslist);
        newfriendList.addHeaderView(mHeadView, null, false);
        newfriendList.addFooterView(mFooterView, null, false);
        newfriendList.setAdapter(sAdapter);

        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        sid = userInfo.getSid();
        adSid = userInfo.getAdSId();
        loadingLL.setVisibility(View.VISIBLE);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.NEARBY_HAS_NEW, false, true)) {
            initNearByView(true);
        }
        new getNewFriendslistTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid, "" + page);
    }

    @Override
    protected void initListener() {
        super.initListener();
        //注册EventBus
        EventBus.getDefault().register(this);
        searchBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewFriendsAct.this, getResources().getString(R.string.newFriend_search));
                startActivity(MyHomeArchivesActivity.class, new Intent(NewFriendsAct.this, SearchForContactsActivity.class));
            }
        });
        //附近的人脉
        nearbyContactsTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewFriendsAct.this, getResources().getString(R.string.newFriend_nearby));
                startActivity(MyHomeArchivesActivity.class, new Intent(NewFriendsAct.this, NearbyActivity.class));
                initNearByView(false);
            }
        });
        nearbyRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nearbyContactsTv.performClick();
            }
        });
        //猜你感兴趣
        guessInterestTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewFriendsAct.this, getResources().getString(R.string.newFriend_guessInterest));
                startActivity(MyHomeArchivesActivity.class, new Intent(NewFriendsAct.this, GuessInterestActivity.class));
            }
        });
        //猜你感兴趣
        guessInterestRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                guessInterestTv.performClick();
            }
        });
        //识别中的名片
        identificationCardRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewFriendsAct.this, getString(R.string.ocl_cards_see_detail));
                Intent intent = new Intent(NewFriendsAct.this, OcrCardsListActivity.class);
                startHlActivityForResult(intent, OCR_CARDS_REQUEST_CODE);
            }
        });

        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFooterView(1);
                page = page + 1;
                new getNewFriendslistTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid,
                        "" + page);
            }
        });

        newfriendList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewFriendsInfo item = (NewFriendsInfo) newfriendList.getAdapter().getItem(position);
                if (item != null) {
                    NFuserInfo ct = item.getUserInfo();
                    if (ct == null)
                        return;
                    if (position > 0) {
                        //因为addHead，所以position增加了1
                        position = position - 1;
                        //名片，手机通讯录
                        if (ct.getType() == 1 || ct.getType() == 2) {
                            Intent intent = new Intent(NewFriendsAct.this, MyContactArchivesActivity.class);
                            intent.putExtra("from", "newFriendsAct");
                            intent.putExtra(MyContactArchivesActivity.FLAG_INTENT_TYPE, ct.getType());
                            intent.putExtra(MyContactArchivesActivity.FLAG_INTENT_BIZID, ct.getBizId());
                            intent.putExtra(MyContactArchivesActivity.FLAG_INTENT_BIZTYPE, ct.getBizType());
                            intent.putExtra(MyContactArchivesActivity.FLAG_INTENT_DELETE_ID, item.getId());
                            intent.putExtra("position", position);
                            startHlActivity(intent);
                        } else {
                            //人和网会员
                            if (item.getStatus() == 1) {
                                //已添加
                                Intent intent = new Intent(NewFriendsAct.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                                intent.putExtra("name", ct.getName());
                                intent.putExtra("position", position);
                                startHlActivity(intent);
                            } else if (item.getStatus() == 0) {
                                //接受
                                Intent intent = new Intent(NewFriendsAct.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                                intent.putExtra("from", item.getFromType());
                                intent.putExtra("addfriend_from", "newFriend");
                                intent.putExtra("position", position);
                                startHlActivity(intent);
                            } else if (item.getStatus() == 4) {
                                //添加
                                Intent intent = new Intent(NewFriendsAct.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                                intent.putExtra("from", item.getFromType());
                                intent.putExtra("addfriend_from", "newFriend");
                                intent.putExtra("position", position);
                                startHlActivity(intent);
                            } else {
                                //拒绝，等待验证
                                Intent intent = new Intent(NewFriendsAct.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, "" + ct.getSid());
                                intent.putExtra("name", ct.getName());
                                startHlActivity(intent);
                            }
                        }
                    }
                }
            }
        });

        newfriendList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == 0)
                    return true;
                MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(NewFriendsAct.this);
                materialDialog.showSelectList(R.array.delete_item).itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                NewFriendsInfo item = (NewFriendsInfo) newfriendList.getAdapter().getItem(position);
                                Map<String, Object> reqParams = new HashMap<>();
                                reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                                reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
                                reqParams.put("friendId", item.getId());
                                try {
                                    OkHttpClientManager.postAsyn(Constants.Http.DELETE_CARD_MOBILE_FRIEND, reqParams, MessageBoardOperation.class, new OkHttpClientManager.ResultCallback() {
                                        @Override
                                        public void onError(Request request, Exception e) {
                                            ToastUtil.showConnectError(NewFriendsAct.this);
                                        }

                                        @Override
                                        public void onResponse(Object response) {
                                            MessageBoardOperation result = (MessageBoardOperation) response;
                                            if (null != result) {
                                                switch (result.getState()) {
                                                    case 1:
                                                        sAdapter.deleteItem(position - 1);
                                                        ToastUtil.showToast(NewFriendsAct.this, "删除成功");
                                                        break;
                                                    default:
                                                        ToastUtil.showToast(NewFriendsAct.this, "删除失败");
                                                        break;
                                                }
                                            }
                                        }
                                    }, this.getClass().getSimpleName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
                materialDialog.show();
                return true;
            }
        });
    }

    /**
     * eventBus 接收发送消息成功之后的回调
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshNewFriendListEvent event) {
        Logger.e("event bus onEventMainThread 刷新新的好友列表");
        if (null != contactslist) {
            contactslist.clear();
            page = 1;
            loadingLL.setVisibility(View.VISIBLE);
            new getNewFriendslistTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid,
                    "" + page);
        }

    }

    /**
     * @author chan
     * @createtime 2014-11-4
     * @功能说明 获取的新的朋友
     */
    class getNewFriendslistTask extends AsyncTask<String, Void, NewFriendsListBean> {

        @Override
        protected NewFriendsListBean doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("page", params[2]);
            page = Integer.parseInt(params[2]);
            try {
                NewFriendsListBean mb = (NewFriendsListBean) HttpUtil.doHttpRequest(Constants.Http.GETNEWFRIENDS_LIST, reqParams,
                        NewFriendsListBean.class, null);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(NewFriendsListBean result) {
            super.onPostExecute(result);
            handleNewFriendTaskResult(result, true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 != NetworkUtil.hasNetworkConnection(NewFriendsAct.this)) {
            } else {
                ToastUtil.showNetworkError(NewFriendsAct.this);
                loadingLL.setVisibility(View.GONE);
            }

        }
    }

    /**
     * 处理获取到的返回结果
     *
     * @param result
     * @param hasFadeProgressBar 是否存在加载progressbar
     */
    private void handleNewFriendTaskResult(NewFriendsListBean result, boolean hasFadeProgressBar) {
        loadingLL.setVisibility(View.GONE);
        if (result != null) {
            switch (result.getState()) {
                case 1:
                    if (result.isFlag()) {
                        identificationCardLl.setVisibility(View.VISIBLE);
                        identCardNumb.setVisibility(View.VISIBLE);
                    }
                    List<NewFriendsInfo> nf = result.getNewFriendList();
                    if (null != nf && nf.size() > 0) {
                        // 把最大一条的id保存
                        for (int i = 0; i < nf.size(); i++) {
                            NewFriendsInfo ct = new NewFriendsInfo();
                            ct.setId(nf.get(i).getId());
                            ct.setReadState(nf.get(i).isReadState());
                            ct.setStatus(nf.get(i).getStatus());
                            ct.setInviteId(nf.get(i).getInviteId());
                            ct.setInviteType(nf.get(i).getInviteType());
                            ct.setPurpose(nf.get(i).getPurpose());
                            ct.setFromContent(nf.get(i).getFromContent());
                            ct.setFromType(nf.get(i).getFromType());
                            ct.setUserInfo(nf.get(i).getUserInfo());
                            ct.setRedId(nf.get(i).getRedId());
                            contactslist.add(ct);
                        }
                        if (page == 1 && nf.size() < 10) {
                        } else {
                            if (nf.size() < 20)
                                toggleFooterView(3);
                            else
                                toggleFooterView(2);
                        }
                    } else {
                        if (page == 1) {
                        } else {
                            toggleFooterView(3);
                        }
                    }
                    sAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        } else {
            ToastUtil.showErrorToast(NewFriendsAct.this, getString(R.string.connect_server_error));
            return;
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

    /**
     * 初始化附近的人脉tab的view
     *
     * @param showNotice 是否需要显示小红点提醒
     */
    private void initNearByView(boolean showNotice) {
        if (showNotice) {
            nearbyNoticeIv.setVisibility(View.VISIBLE);
        } else {
            if (nearbyNoticeIv.getVisibility() == View.VISIBLE) {
                nearbyNoticeIv.setVisibility(View.GONE);
                SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.NEARBY_HAS_NEW, false, true);
                isCheckNearby = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OCR_CARDS_REQUEST_CODE:
                    identCardNumb.setVisibility(View.GONE);
                    break;
            }

        } else if (resultCode == Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (RenheApplication.getInstance().getLogin() == 0) {
            startHlActivity(new Intent(this, MyPortal.class));
        } else {
            if (isCheckNearby)
                setResult(RESULT_OK);
            super.onBackPressed();
        }
    }

    //广播，更新列表
    class UpdateListItem extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(UPDATE_LISTITEM)) {
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
            } else if (intent.getAction().equals(UPDATE_DELETE_LIST_ITEM)) {
                int position = intent.getIntExtra("position", 0);
                contactslist.remove(position);
                sAdapter.notifyDataSetChanged();
            }
        }
    }
}
