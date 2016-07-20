package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.FindConnectionsListitemAdapter;
import com.itcalf.renhe.bean.ChatLog;
import com.itcalf.renhe.bean.CircleList;
import com.itcalf.renhe.bean.HlContactCardMember;
import com.itcalf.renhe.bean.HlContactContactMember;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.bean.MemberCircle;
import com.itcalf.renhe.bean.MemberList;
import com.itcalf.renhe.bean.SearchGlobalBean;
import com.itcalf.renhe.bean.SearchRecommendedBean;
import com.itcalf.renhe.bean.SearchRenmai;
import com.itcalf.renhe.context.archives.MyContactArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.more.AccountLimitUpgradeActivity;
import com.itcalf.renhe.context.relationship.AdvancedSearchActivity;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.context.wukong.im.ChatMainActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.eventbusbean.NotifyListRefreshWithPositionEvent;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.WriteLogThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FindMoreConnectionsAct extends BaseActivity {

    private TextView titleTxt;
    private ListView findconnectionsItemLv;
    private View mFooterView;
    private RelativeLayout rootRl;

    private String keyword = "";
    private List<SearchGlobalBean> moreList;
    private FindConnectionsListitemAdapter fcliAdapter = null;
    String sid = "", adSid = "";
    private FadeUitl fadeUitl;
    private int mStart = 1;//页码page
    private int mCount = 20;
    private int type;
    private boolean hideFooter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.find_more_connections_list);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (type == 1) {
            MenuItem add = menu.findItem(R.id.item_invite_friend);
            add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            add.setIcon(null);
            add.setTitle("高级搜索");
            add.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_invite_friend:
                startActivity(new Intent(FindMoreConnectionsAct.this, AdvancedSearchActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_result_list_more_search_click), 0, "", null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        titleTxt = (TextView) findViewById(R.id.title_txt);
        titleTxt.setVisibility(View.GONE);
        findconnectionsItemLv = (ListView) findViewById(R.id.findconnections_item_listview);
        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        View divideLine = mFooterView.findViewById(R.id.divideline);
        divideLine.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();
        String key = this.getIntent().getExtras().getString("key");
        setTextValue(1, TextUtils.isEmpty(key) ? "查看更多" : key);
        keyword = this.getIntent().getExtras().getString("keyword");

        if (getString(R.string.search_contacts_tip).equals(key)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    moreList = searchLocalData(keyword);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (moreList != null && moreList.size() > 0) {
                                fcliAdapter = new FindConnectionsListitemAdapter(FindMoreConnectionsAct.this, moreList, keyword);
                                findconnectionsItemLv.setAdapter(fcliAdapter);
                            }
                        }
                    });
                }
            }).start();
        } else {
            fadeUitl = new FadeUitl(this, "加载中...");
            fadeUitl.addFade(rootRl);
            hideFooter = true;
            moreList = new ArrayList<>();
            //搜索更多人脉
            UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
            sid = userInfo.getSid();
            adSid = userInfo.getAdSId();
            if (getString(R.string.search_new_contacts_tip).equals(key)) {
                type = 1;
//                invalidateOptionsMenu();
            } else if (getString(R.string.search_network_business_tip).equals(key)) {
                type = 2;
            } else if (getString(R.string.search_circle_tip).equals(key)) {
                type = 0;
            }
            //通过接口去搜索
            new getGlobalSearchMembersTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid, keyword, "" + mStart,
                    "" + mCount, "" + type);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        //注册EventBus
        EventBus.getDefault().register(this);
        findconnectionsItemLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                SearchGlobalBean item = moreList.get(arg2);
                int type = item.getType();
                if (type == 0) {
                    SearchRecommendedBean recommend = item.getSearchRecommended();
                    Intent intent = new Intent(FindMoreConnectionsAct.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, recommend.getSid());
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[6]);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else if (type == 1) {
                    HlContacts contact = item.getContact();
                    Intent intent = new Intent();
                    if (contact.getType() == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE || contact.getType() == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                        intent.setClass(FindMoreConnectionsAct.this, MyContactArchivesActivity.class);
                        intent.putExtra("contact", contact);
                    } else {
                        intent.setClass(FindMoreConnectionsAct.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, contact.getHlContactRenheMember().getSid());
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else if (type == 11) {
                    CircleList cl = item.getCircleList();
                    Intent intent = new Intent(FindMoreConnectionsAct.this, ChatMainActivity.class);
                    intent.putExtra("conversation", cl.getConversation());
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else if (type == 2) {
                    MemberList ml = item.getMemberList();
                    Intent intent = new Intent(FindMoreConnectionsAct.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, ml.getSid());
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[2]);
                    intent.putExtra("addfriend_from", "renmaiSearchResultMore");
                    intent.putExtra("position", arg2);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    //统计点击事件//写入本地文件
                    new WriteLogThread(FindMoreConnectionsAct.this, "5.408.1",
                            new String[]{"1", "" + ml.getPosition(), ml.getSid()}).start();
                } else if (type == 3) {
                    //人脉圈
                    MemberCircle mc = item.getMemberCircleList();
                    String objectId = mc.getId();
                    Intent intent = new Intent(FindMoreConnectionsAct.this, TwitterShowMessageBoardActivity.class);
                    intent.putExtra("sid", mc.getSid());
                    intent.putExtra("objectId", objectId);
                    intent.putExtra("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
                    intent.putExtra("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    //统计点击事件
                    new WriteLogThread(FindMoreConnectionsAct.this, "5.408.1",
                            new String[]{"3", "" + mc.getPosition(), objectId}).start();
                } else if (type == 4) {
                    ////聊天记录
                    ChatLog chatLog = item.getChatLog();
                    Intent intent = new Intent(FindMoreConnectionsAct.this, ChatMainActivity.class);
                    intent.putExtra("conversation", chatLog.getConversation());
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else if (type == 5) {
                    //圈子
                    CircleList cl = item.getCircleList();
                    Intent i = new Intent();
                    i.putExtra("circleId", "" + cl.getId());
                    i.setClass(FindMoreConnectionsAct.this, ActivityCircleDetail.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    new WriteLogThread(FindMoreConnectionsAct.this, "5.408.1",
                            new String[]{"2", "" + cl.getPosition(), "" + cl.getId()}).start();
                }
            }
        });

        mFooterView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mStart++;
                hideFooter = false;
                new getGlobalSearchMembersTask().executeOnExecutor(Executors.newCachedThreadPool(), sid, adSid, keyword,
                        "" + mStart, "" + mCount, "" + type);
            }
        });
    }

    public List<SearchGlobalBean> searchLocalData(String keyword) {
        //获取搜索数据 1.本地联系人
        Contact[] cts;
        List<SearchGlobalBean> sgbList = new ArrayList<>();
        try {
            //从本地数据库取到的数据源
            List<HlContacts> hlLocalContactsList = new ArrayList<>();
            List<HlContactRenheMember> hlLocalContactRenheMemberList = new ArrayList<>();
            List<HlContactContactMember> hlLocalContactContactMemberList = new ArrayList<>();
            List<HlContactCardMember> hlLocalContactCardMemberList = new ArrayList<>();
            HlContactsUtils.getLimitedHlContacts(hlLocalContactsList, hlLocalContactRenheMemberList,
                    hlLocalContactContactMemberList, hlLocalContactCardMemberList, keyword, Integer.MAX_VALUE);
//            //利用Sql 语句 直接 模糊查询
//            cts = RenheApplication.getInstance().getContactCommand()
//                    .getSearchContact(RenheApplication.getInstance().getUserInfo().getSid(), keyword, false);
            if (hlLocalContactsList.size() > 0) {
                for (HlContacts ct : hlLocalContactsList) {
                    SearchGlobalBean sgb = new SearchGlobalBean();
                    sgb.setType(1);
                    sgb.setIsTitle(false);
                    sgb.setIsFooter(false);
                    sgb.setContact(ct);
                    sgbList.add(sgb);
                }
            }
            if (sgbList.size() > 0) {
                return sgbList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    class getGlobalSearchMembersTask extends AsyncTask<String, Void, SearchRenmai> {
        String keyword = "";
        int type;

        @Override
        protected SearchRenmai doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("text", params[2]);
            reqParams.put("page", params[3]);
            reqParams.put("size", params[4]);
            reqParams.put("type", params[5]);
            keyword = params[2];
            type = Integer.parseInt(params[5]);
            try {
                return (SearchRenmai) HttpUtil.doHttpRequest(Constants.Http.SEARCH_RELATIONSHIPANDCIRCLE_V2, reqParams,
                        SearchRenmai.class, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SearchRenmai result) {
            super.onPostExecute(result);

            if (fadeUitl != null && fadeUitl.isFadeShow(rootRl))
                fadeUitl.removeFade(rootRl);
            if (!hideFooter) {
                toggleFooterView(false);
            }
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        if (type == 1) {
                            // 人脉
                            MemberList[] nf = result.getMemberList();
                            if (null != nf && nf.length > 0) {
                                if (nf.length == mCount) {
                                    mFooterView.setVisibility(View.VISIBLE);
                                } else {
                                    mFooterView.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < nf.length; i++) {
                                    SearchGlobalBean sgb = new SearchGlobalBean();
                                    MemberList srb = new MemberList();
                                    srb.setSid(nf[i].getSid());
                                    srb.setName(nf[i].getName());
                                    srb.setUserFace(nf[i].getUserFace());
                                    srb.setCurTitle(nf[i].getCurTitle());
                                    srb.setExTitle(nf[i].getExTitle());
                                    srb.setCurCompany(nf[i].getCurCompany());
                                    srb.setExCompany(nf[i].getExCompany());
                                    srb.setAccountType(nf[i].getAccountType());
                                    srb.setLocation(nf[i].getLocation());
                                    srb.setSchoolName(nf[i].getSchoolName());
                                    srb.setRealname(nf[i].isRealname());
                                    srb.setPreferred(nf[i].getPreferred());//我能提供
                                    srb.setPosition(i);
                                    sgb.setType(2);
                                    srb.setIsInvite(nf[i].isInvite());
                                    srb.setIsConnection(nf[i].isConnection());
                                    srb.setBeInvitedInfo(nf[i].getBeInvitedInfo());
                                    srb.setUserInfo(nf[i].getUserInfo());
                                    srb.setIsReceived(false);
                                    sgb.setMemberList(srb);
                                    moreList.add(sgb);
                                }
                            } else {
                            }
                        } else if (type == 0) {
                            //圈子
                            CircleList[] cl = result.getCircleList();
                            if (null != cl && cl.length > 0) {
                                if (cl.length == mCount) {
                                    mFooterView.setVisibility(View.VISIBLE);
                                } else {
                                    mFooterView.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < cl.length; i++) {
                                    SearchGlobalBean sgb = new SearchGlobalBean();
                                    CircleList srb = new CircleList();
                                    srb.setId(cl[i].getId());
                                    srb.setImConversationId(cl[i].getImConversationId());
                                    srb.setCreatorMemberId(cl[i].getCreatorMemberId());
                                    srb.setName(cl[i].getName());
                                    srb.setNote(cl[i].getNote());
                                    srb.setJoinType(cl[i].getJoinType());
                                    srb.setAvatar(cl[i].getAvatar());
                                    srb.setMemberCount(cl[i].getMemberCount());
                                    srb.setMemberMaxCount(cl[i].getMemberMaxCount());
                                    srb.setMemberCountAboveMax(cl[i].isMemberCountAboveMax());
                                    srb.setMemberExists(cl[i].isMemberExists());
                                    srb.setRequestExists(cl[i].isRequestExists());
                                    srb.setPosition(i);
                                    sgb.setType(5);
                                    sgb.setCircleList(srb);
                                    moreList.add(sgb);
                                }
                            } else {
                            }
                        } else if (type == 2) {
                            //人脉圈
                            MemberCircle[] mc = result.getMemberCircleList();
                            if (null != mc && mc.length > 0) {
                                if (mc.length == mCount) {
                                    mFooterView.setVisibility(View.VISIBLE);
                                } else {
                                    mFooterView.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < mc.length; i++) {
                                    SearchGlobalBean sgb = new SearchGlobalBean();
                                    MemberCircle srb = new MemberCircle();
                                    srb.setId(mc[i].getId());
                                    srb.setSid(mc[i].getSid());
                                    srb.setName(mc[i].getName());
                                    srb.setCurTitle(mc[i].getCurTitle());
                                    srb.setCurCompany(mc[i].getCurCompany());
                                    srb.setCreatedDate(mc[i].getCreatedDate());
                                    srb.setAccountType(mc[i].getAccountType());
                                    srb.setRealname(mc[i].isRealname());
                                    srb.setUserFace(mc[i].getUserFace());
                                    srb.setContent(mc[i].getContent());
                                    srb.setPosition(i);

                                    sgb.setType(3);
                                    sgb.setMemberCircleList(srb);
                                    moreList.add(sgb);
                                }
                            } else {
                            }
                        }
                        break;
                    case -4:
                        if (type == 1) {
                            MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(FindMoreConnectionsAct.this);
                            materialDialogsUtil.getNotitleBuilder(R.string.search_result_num_limit_error, R.string.upgrade_now,
                                    R.string.hint_iknow).callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    Intent i = new Intent(FindMoreConnectionsAct.this, AccountLimitUpgradeActivity.class);
                                    i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[2]);
                                    startActivity(i);
                                    finish();
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    //和聊统计
                                    String content = "5.158.1" + LoggerFileUtil.getConstantInfo(FindMoreConnectionsAct.this)
                                            + "|3";
                                    LoggerFileUtil.writeFile(content, true);
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                }
                            });
                            materialDialogsUtil.show();
                            //和聊统计
                            new WriteLogThread(FindMoreConnectionsAct.this, "5.158", new String[]{"3"}).start();
                        }
                        break;
                    default:
                        break;
                }
            } else {
                mStart = (mStart--) <= 1 ? 1 : mStart;
                return;
            }
            if (fcliAdapter != null) {
                fcliAdapter.notifyDataSetChanged();
            } else {
                fcliAdapter = new FindConnectionsListitemAdapter(FindMoreConnectionsAct.this, moreList, keyword);
                findconnectionsItemLv.addFooterView(mFooterView, null, false);
                findconnectionsItemLv.setAdapter(fcliAdapter);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (hideFooter) {
                mFooterView.setVisibility(View.GONE);
            } else {
                toggleFooterView(true);
            }
        }
    }

    private void toggleFooterView(boolean isShow) {
        if (isShow) {
            ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("加载中...");
            mFooterView.findViewById(R.id.waitPb).setVisibility(View.VISIBLE);
        } else {
            ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("查看更多");
            mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
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
     * eventBus 添加好友请求发送成功之后，用于通知界面刷新
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NotifyListRefreshWithPositionEvent event) {
        if (event.getType() == NotifyListRefreshWithPositionEvent.RENMAI_SEARCH_MORE) {
            if (null != moreList && moreList.size() > event.getPosition()) {
                if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ADD) {
                    moreList.get(event.getPosition()).getMemberList().setIsInvite(true);//列表item状态改为等待验证
                } else if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ACCEPT) {
                    moreList.get(event.getPosition()).getMemberList().setIsReceived(true);//列表item状态改为等待验证
                }
                fcliAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }
}
