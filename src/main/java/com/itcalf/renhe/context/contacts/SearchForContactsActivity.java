package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Member;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.SearchForContactAdapter;
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
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.eventbusbean.NotifyListRefreshWithPositionEvent;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.WriteLogThread;
import com.itcalf.renhe.view.TipBox;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * description :搜索人脉
 * Created by Chans Renhenet
 * 2015/8/14
 */
public class SearchForContactsActivity extends BaseActivity {

    //标题栏
    private ImageButton searchImgBtn;
    private EditText keywordEdt;
    private TextView searchTxt;
    private ImageView seniorBtn;

    private LinearLayout guideSearchLl;
    private RelativeLayout advancedSearchLl;
    private ListView searchContactsLv;
    private RelativeLayout findConnectionsRl;
    private TextView noneResult;
    //新注册用户第一次进入显示
    private TipBox tipBox;

    private RelativeLayout mFooterView;

    /**
     * 判断数据是否加载完整，加载完成则高级搜索按钮可点*
     */
    private boolean isclick = false;

    //搜索的列表
    private List<SearchGlobalBean> localList = new ArrayList<>();
    private List<SearchGlobalBean> searchList = new ArrayList<>();
    private SearchForContactAdapter listAdapter;

    private String searchHintStr;
    private boolean isRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.find_connections);
    }

    @Override
    protected void findView() {
        super.findView();
        searchImgBtn = (ImageButton) findViewById(R.id.search_imgbtn);
        keywordEdt = (EditText) findViewById(R.id.keyword_edt);
        searchTxt = (TextView) findViewById(R.id.search_txt);
        searchTxt.setText("");
        searchTxt.setVisibility(View.VISIBLE);
        seniorBtn = (ImageView) findViewById(R.id.senior_btn);
        seniorBtn.setVisibility(View.VISIBLE);
        searchHintStr = TextSize.getInstance().getSearchPlaceholder();
        if (!TextUtils.isEmpty(searchHintStr))
            keywordEdt.setHint("" + searchHintStr);

        SharedPreferences msp2 = getSharedPreferences("regiser_guide_setting_info", 0);
        isRegister = msp2.getBoolean("regiser_search" + RenheApplication.getInstance().getUserInfo().getSid(), false);

        findConnectionsRl = (RelativeLayout) findViewById(R.id.find_connections_rl);
        guideSearchLl = (LinearLayout) findViewById(R.id.guide_search_ll);
        advancedSearchLl = (RelativeLayout) findViewById(R.id.advanced_search_ll);
        searchContactsLv = (ListView) findViewById(R.id.search_contacts_lv);
        mFooterView = (RelativeLayout) findViewById(R.id.footer_layout);
        noneResult = (TextView) findViewById(R.id.noneResult);
    }

    @Override
    protected void initData() {
        super.initData();
        //初始化
        listAdapter = new SearchForContactAdapter(SearchForContactsActivity.this, searchList);
        searchContactsLv.setAdapter(listAdapter);

        new getSeniorSearchPrivilegeTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId());
    }

    @Override
    protected void initListener() {
        super.initListener();
        //注册EventBus
        EventBus.getDefault().register(this);
        actionbarListener();
        searchContactsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchGlobalBean item = (SearchGlobalBean) searchContactsLv.getAdapter().getItem(i);
                int type = item.getType();
                boolean isTitle = item.isTitle();
                boolean isFooter = item.isFooter();
                if (isTitle) {
                    //没有点击效果
                } else if (isFooter) {
                    Intent intent = new Intent(SearchForContactsActivity.this, FindMoreConnectionsAct.class);
                    String key = "";
                    Map<String, String> statisticsMap = new HashMap<>();
                    switch (type) {
                        case 1:
                            key = getString(R.string.search_contacts_tip);
                            statisticsMap.put("type", "1");
                            break;
                        case 2:
                            key = getString(R.string.search_new_contacts_tip);
                            statisticsMap.put("type", "2");
                            break;
                        case 3:
                            key = getString(R.string.search_network_business_tip);
                            statisticsMap.put("type", "3");
                            break;
                        case 4:
                            key = getString(R.string.search_chat_record_tip);
                            break;
                        case 5:
                            key = getString(R.string.search_circle_tip);
                            statisticsMap.put("type", "4");
                            break;
                        case 11:
                            key = getString(R.string.search_circle_contacts_tip);
                            break;
                        case 0:
                            key = getString(R.string.search_recommend_tip);
                            break;
                    }
                    intent.putExtra("keyword", getKeyword());
                    intent.putExtra("key", key);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_result_more_renmai_click), 0, "", statisticsMap);
                } else {
                    //推荐
                    if (type == 0) {
                        SearchRecommendedBean recommend = item.getSearchRecommended();
                        Intent intent = new Intent(SearchForContactsActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, recommend.getSid());
                        intent.putExtra("from", Constants.ADDFRIENDTYPE[6]);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //联系人
                    else if (type == 1) {
                        HlContacts contact = item.getContact();
                        Intent intent = new Intent();
                        if (contact.getType() == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE || contact.getType() == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                            intent.setClass(SearchForContactsActivity.this, MyContactArchivesActivity.class);
                            intent.putExtra("contact", contact);
                        } else {
                            intent.setClass(SearchForContactsActivity.this, MyHomeArchivesActivity.class);
                            intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, contact.getHlContactRenheMember().getSid());
                        }
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //圈子联系人
                    else if (type == 11) {
                        CircleList cl = item.getCircleList();
                        Intent intent = new Intent(SearchForContactsActivity.this, ChatMainActivity.class);
                        intent.putExtra("conversation", cl.getConversation());
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //新的人脉
                    else if (type == 2) {
                        MemberList memberList = item.getMemberList();
                        Intent intent = new Intent(SearchForContactsActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, memberList.getSid());
                        intent.putExtra("from", Constants.ADDFRIENDTYPE[2]);
                        intent.putExtra("addfriend_from", "renmaiSearchResult");
                        intent.putExtra("position", i);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //人脉圈
                    else if (type == 3) {
                        MemberCircle mc = item.getMemberCircleList();
                        String objectId = mc.getId();
                        Intent intent = new Intent(SearchForContactsActivity.this, TwitterShowMessageBoardActivity.class);
                        intent.putExtra("sid", mc.getSid());
                        intent.putExtra("objectId", objectId);
                        intent.putExtra("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
                        intent.putExtra("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //聊天记录
                    else if (type == 4) {
                        ChatLog chatLog = item.getChatLog();
                        Intent intent = new Intent(SearchForContactsActivity.this, ChatMainActivity.class);
                        intent.putExtra("conversation", chatLog.getConversation());
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    //圈子
                    else if (type == 5) {
                        CircleList cl = item.getCircleList();
                        Intent intent = new Intent();
                        intent.putExtra("circleId", "" + cl.getId());
                        intent.setClass(SearchForContactsActivity.this, ActivityCircleDetail.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                }
            }
        });

        advancedSearchLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchForContactsActivity.this, AdvancedSearchActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_result_more_search_click), 0, "", null);
            }
        });
    }

    protected void actionbarListener() {

        searchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchForContactsActivity.this, AdvancedSearchActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                MobclickAgent.onEvent(SearchForContactsActivity.this, "search_senior_click");
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_more_search_click), 0, "", null);
            }
        });

        keywordEdt.addTextChangedListener(new TextWatcher() {
            Timer timer = new Timer();
            long DELAY = 500; // in ms

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //搜索前先把数据源情空
                if (null != localList && localList.size() > 0)
                    localList.clear();
                if (null != searchList && searchList.size() > 0)
                    searchList.clear();
                if (TextUtils.isEmpty(keywordEdt.getText().toString())) {
                    showInitial();
                } else {
                    timer.cancel();
                    timer = new Timer();
                    System.out.println("搜索start:" + System.currentTimeMillis());
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //获取相关的本地数据
                            localList = searchLocalData();
                            //显示到界面上
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    populateSearchData(keywordEdt.getText().toString());
                                    StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_search_process_click), 0, "", null);
                                }
                            });
                        }
                    }, DELAY);
                }
            }
        });

        searchTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(keywordEdt.getText().toString())) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    populateSearchData(keywordEdt.getText().toString());
                }
            }
        });

        seniorBtn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             //是否有高级搜索权限
                                             if (isclick) {
                                                 startActivity(new Intent(SearchForContactsActivity.this, AdvancedSearchActivity.class));
                                                 overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                                 MobclickAgent.onEvent(SearchForContactsActivity.this, "search_senior_click");
                                             } else {
                                                 MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(SearchForContactsActivity.this);
                                                 materialDialogsUtil
                                                         .getNotitleBuilder(R.string.advance_search_auth_error, R.string.upgrade_now, R.string.hint_iknow)
                                                         .callback(new MaterialDialog.ButtonCallback() {
                                                             @Override
                                                             public void onPositive(MaterialDialog dialog) {
                                                                 Intent i = new Intent(SearchForContactsActivity.this, AccountLimitUpgradeActivity.class);
                                                                 i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[3]);
                                                                 startActivity(i);
                                                                 overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                                                 //和聊统计
                                                                 new WriteLogThread(SearchForContactsActivity.this, "5.158.1", new String[]{"4"}).start();
                                                             }

                                                             @Override
                                                             public void onNeutral(MaterialDialog dialog) {
                                                             }

                                                             @Override
                                                             public void onNegative(MaterialDialog dialog) {
                                                             }
                                                         });
                                                 materialDialogsUtil.show();
                                             }
                                         }
                                     }

        );
    }

    /**
     * 判断界面是否加载完成
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isRegister) {
            tipBox = new TipBox(SearchForContactsActivity.this, 3, new TipBox.OnItemClickListener() {
                @Override
                public void onItemClick() {

                }
            });
            tipBox.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    SharedPreferences msp2 = getSharedPreferences("regiser_guide_setting_info", 0);
                    SharedPreferences.Editor editor2 = msp2.edit();
                    isRegister = false;
                    editor2.putBoolean("regiser_search" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                    editor2.commit();
                }
            });
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            if (null != tipBox.getContentView())
                tipBox.showAtLocation(tipBox.getContentView(), Gravity.RIGHT | Gravity.TOP, searchTxt.getWidth() / 2,
                        getSupportActionBar().getHeight() + statusBarHeight - 15);
        }
    }

    /**
     * 显示初始界面
     */
    public void showInitial() {
        listAdapter.notifyDataSetChanged();
        noneResult.setVisibility(View.GONE);
        advancedSearchLl.setVisibility(View.GONE);
        searchContactsLv.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);

        guideSearchLl.setVisibility(View.VISIBLE);
    }

    /**
     * 搜索本地数据
     */
    public List<SearchGlobalBean> searchLocalData() {
        //获取搜索数据 1.本地联系人
        List<SearchGlobalBean> sgbList = new ArrayList<>();
        try {
            //从本地数据库取到的数据源
            List<HlContacts> hlLocalContactsList = new ArrayList<>();
            List<HlContactRenheMember> hlLocalContactRenheMemberList = new ArrayList<>();
            List<HlContactContactMember> hlLocalContactContactMemberList = new ArrayList<>();
            List<HlContactCardMember> hlLocalContactCardMemberList = new ArrayList<>();
            HlContactsUtils.getLimitedHlContacts(hlLocalContactsList, hlLocalContactRenheMemberList,
                    hlLocalContactContactMemberList, hlLocalContactCardMemberList, keywordEdt.getText().toString(), 3);
//            //利用Sql 语句 直接 模糊查询
//            Contact[] cts = RenheApplication.getInstance().getContactCommand()
//                    .getSearchContact(RenheApplication.getInstance().getUserInfo().getSid(), keywordEdt.getText().toString(), true);
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

    /**
     * 获取相关条件的搜索结果
     *
     * @param keyword
     */
    public void populateSearchData(String keyword) {
        guideSearchLl.setVisibility(View.GONE);
        advancedSearchLl.setVisibility(View.VISIBLE);
        searchContactsLv.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.VISIBLE);
        //1 本地联系人处理
        if (null != localList && localList.size() > 0) {
            searchList.addAll(0, AddMyLocalContactsList(localList));
            listAdapter.notifyDataSetChanged();
        }
        System.out.println("local搜索end:" + System.currentTimeMillis());

        //1.1 搜索im端圈子的成员（暂时不做）
        //			searchCircleMemberList(keyword, sgbList);

        //2.搜服务端数据
        new getGlobalSearchMembersTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), keyword);
    }

    //加载本地搜索的数据---我的联系人
    private List<SearchGlobalBean> AddMyLocalContactsList(List<SearchGlobalBean> localList) {
        if (localList == null || localList.size() == 0)
            return null;
        List<SearchGlobalBean> localMyContacts = new ArrayList<>();
        //添加
        SearchGlobalBean sgb1 = new SearchGlobalBean();
        sgb1.setIsTitle(true);
        sgb1.setIsFooter(false);
        sgb1.setType(1);
        sgb1.setTypeName("我的人脉");
        localMyContacts.add(sgb1);
        if (localList.size() >= 3) {
            localMyContacts.add(localList.get(0));
            localMyContacts.add(localList.get(1));
            localList.get(2).setIsShowDivideLine(true);
            localMyContacts.add(localList.get(2));
            //add 底部加载更多
            SearchGlobalBean sgb2 = new SearchGlobalBean();
            sgb2.setIsTitle(false);
            sgb2.setIsFooter(true);
            sgb2.setType(1);
            sgb2.setTypeName("查看更多人脉");
            localMyContacts.add(sgb2);
        } else {
            if (localList.size() > 0)
                localList.get(localList.size() - 1).setIsShowDivideLine(true);
            localMyContacts.addAll(localList);
        }
        return localMyContacts;

    }

    /**
     * 获取是否有高级搜索权限
     */
    class getSeniorSearchPrivilegeTask extends AsyncTask<String, Void, MessageBoardOperation> {

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            try {
                return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.SEARCH_ADVANCED_PRIVILEGE, reqParams,
                        MessageBoardOperation.class, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isclick = false;
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        isclick = true;
                        break;
                    case -5:
                        isclick = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * @author Chan
     * @description 获取全局搜索列表
     * @date 2015-4-23
     */
    class getGlobalSearchMembersTask extends AsyncTask<String, Void, SearchRenmai> {
        String keyword = "";

        @Override
        protected SearchRenmai doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("text", params[2]);
            reqParams.put("page", 1);
            reqParams.put("size", 4);
            keyword = params[2];
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
            if (searchList == null) {
                return;
            }
            if (TextUtils.isEmpty(keywordEdt.getText().toString())) {
                if (null != localList && localList.size() > 0)
                    localList.clear();
                if (null != searchList && searchList.size() > 0)
                    searchList.clear();
                showInitial();
                return;
            }
            if (result != null) {
                //清除数据重新加载
                searchList.clear();
                if (null != AddMyLocalContactsList(localList))
                    searchList.addAll(AddMyLocalContactsList(localList));

                switch (result.getState()) {
                    case 1:
                        // 人脉
                        List<SearchGlobalBean> ctList = new ArrayList<>();
                        MemberList[] nf = result.getMemberList();
                        if (null != nf && nf.length > 0) {
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
                                srb.setIsInvite(nf[i].isInvite());
                                srb.setIsConnection(nf[i].isConnection());
                                srb.setBeInvitedInfo(nf[i].getBeInvitedInfo());
                                srb.setUserInfo(nf[i].getUserInfo());
                                srb.setIsReceived(false);
                                sgb.setType(2);
                                sgb.setMemberList(srb);
                                sgb.setIsTitle(false);
                                sgb.setIsFooter(false);
                                ctList.add(sgb);
                            }
                            if (ctList.size() > 0) {
                                SearchGlobalBean sgb1 = new SearchGlobalBean();
                                sgb1.setIsTitle(true);
                                sgb1.setIsFooter(false);
                                sgb1.setType(2);
                                sgb1.setTypeName("发现人脉");
                                searchList.add(sgb1);
                                if (ctList.size() > 3) {
                                    searchList.add(ctList.get(0));
                                    searchList.add(ctList.get(1));
                                    ctList.get(2).setIsShowDivideLine(true);
                                    searchList.add(ctList.get(2));
                                    //add 底部加载更多
                                    SearchGlobalBean sgb2 = new SearchGlobalBean();
                                    sgb2.setIsTitle(false);
                                    sgb2.setIsFooter(true);
                                    sgb2.setType(2);
                                    sgb2.setTypeName("发现更多人脉");
                                    searchList.add(sgb2);
                                } else {
                                    ctList.get(ctList.size() - 1).setIsShowDivideLine(true);
                                    searchList.addAll(ctList);
                                }
                            }
                        }

                        //圈子
                        List<SearchGlobalBean> circleList = new ArrayList<>();
                        CircleList[] cl = result.getCircleList();
                        if (null != cl && cl.length > 0) {
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
                                sgb.setIsTitle(false);
                                sgb.setIsFooter(false);
                                sgb.setCircleList(srb);
                                circleList.add(sgb);
                            }

                            if (circleList.size() > 0) {
                                SearchGlobalBean sgb1 = new SearchGlobalBean();
                                sgb1.setIsTitle(true);
                                sgb1.setIsFooter(false);
                                sgb1.setType(5);
                                sgb1.setTypeName("圈子");
                                searchList.add(sgb1);
                                if (circleList.size() > 3) {
                                    searchList.add(circleList.get(0));
                                    searchList.add(circleList.get(1));
                                    circleList.get(2).setIsShowDivideLine(true);
                                    searchList.add(circleList.get(2));
                                    //add 底部加载更多
                                    SearchGlobalBean sgb2 = new SearchGlobalBean();
                                    sgb2.setIsTitle(false);
                                    sgb2.setIsFooter(true);
                                    sgb2.setType(5);
                                    sgb2.setTypeName("查看更多圈子");
                                    searchList.add(sgb2);
                                } else {
                                    circleList.get(circleList.size() - 1).setIsShowDivideLine(true);
                                    searchList.addAll(circleList);
                                }
                            }
                        }

                        //人脉圈
                        List<SearchGlobalBean> mcList = new ArrayList<>();
                        MemberCircle[] mc = result.getMemberCircleList();
                        if (null != mc && mc.length > 0) {
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
                                sgb.setIsTitle(false);
                                sgb.setIsFooter(false);
                                sgb.setMemberCircleList(srb);
                                mcList.add(sgb);
                            }
                            if (mcList.size() > 0) {
                                SearchGlobalBean sgb1 = new SearchGlobalBean();
                                sgb1.setIsTitle(true);
                                sgb1.setIsFooter(false);
                                sgb1.setType(3);
                                sgb1.setTypeName("人脉圈商机");
                                searchList.add(sgb1);
                                if (mcList.size() > 3) {
                                    searchList.add(mcList.get(0));
                                    searchList.add(mcList.get(1));
                                    mcList.get(2).setIsShowDivideLine(true);
                                    searchList.add(mcList.get(2));
                                    //add 底部加载更多
                                    SearchGlobalBean sgb2 = new SearchGlobalBean();
                                    sgb2.setIsTitle(false);
                                    sgb2.setIsFooter(true);
                                    sgb2.setType(3);
                                    sgb2.setTypeName("查看更多人脉圈商机");
                                    searchList.add(sgb2);
                                } else {
                                    mcList.get(mcList.size() - 1).setIsShowDivideLine(true);
                                    searchList.addAll(mcList);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            advancedSearchLl.setVisibility(View.VISIBLE);
            searchContactsLv.setVisibility(View.VISIBLE);
            mFooterView.setVisibility(View.GONE);
            //刷新ui
            if (searchList.size() > 0) {
                noneResult.setVisibility(View.GONE);
            } else {
                noneResult.setVisibility(View.VISIBLE);
            }
            if (listAdapter != null)
                listAdapter.notifyDataSetChanged();
            System.out.println("搜索界面加载end:" + System.currentTimeMillis());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            advancedSearchLl.setVisibility(View.GONE);
            searchContactsLv.setVisibility(View.GONE);
            mFooterView.setVisibility(View.VISIBLE);
        }

    }

    /*************************************************************************************************************/
    void addSgbList(List<SearchGlobalBean> sgbList) {
        if (null != sgbList && null != localList && sgbList.size() > 0) {
            localList.addAll(sgbList);
        }
        //获取本地的聊天记录
        getLocalChatLog();
        //获取服务端返回的数据（新的人脉，圈子，人脉圈商机）
        new getGlobalSearchMembersTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId(),
                keywordEdt.getText().toString());
    }

    /**
     * 搜索圈子成员匹配，
     *
     * @param keyWord
     * @param searchGlobalBeans 之前搜索出来的本地联系人，叠加
     */
    private int index = 0;
    private int count;
    private List<Conversation> conversationList = new ArrayList<>();
    private String finalKeyWord;

    public void searchCircleMemberList(String keyWord, final List<SearchGlobalBean> searchGlobalBeans) {
        if (!TextUtils.isEmpty(keyWord)) {
            keyWord = keyWord.toUpperCase();//转化为大写
        }
        //1.获取我所在的所有圈子
        finalKeyWord = keyWord;
        IMEngine.getIMService(ConversationService.class).listConversations(new Callback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations == null || conversations.isEmpty()) {
                } else {
                    index = 0;
                    count = conversations.size();
                    conversationList.addAll(conversations);
                    getCircleMembers(searchGlobalBeans);
                }
            }

            @Override
            public void onException(String code, String reason) {
                if (Constants.DEBUG_MODE) {
                    Toast.makeText(SearchForContactsActivity.this, "加载会话列表失败.code:" + code + " reason:" + reason,
                            Toast.LENGTH_SHORT).show();
                }
                addSgbList(searchGlobalBeans);
            }

            @Override
            public void onProgress(List<Conversation> conversations, int i) {
            }
        }, Integer.MAX_VALUE, Conversation.ConversationType.GROUP);
    }

    /**
     * 获取我所在的所有圈子 相关的成员
     */
    public void getCircleMembers(final List<SearchGlobalBean> searchGlobalBeans) {

        IMEngine.getIMService(ConversationService.class).listMembers(new Callback<List<Member>>() {
            @Override
            public void onException(String arg0, String arg1) {
                Toast.makeText(SearchForContactsActivity.this, "加载会话成员失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT)
                        .show();
                index++;
                if (index < count) {
                    getCircleMembers(searchGlobalBeans);
                } else {
                    addSgbList(searchGlobalBeans);
                }
            }

            public void onProgress(List<Member> arg0, int arg1) {
            }

            public void onSuccess(List<Member> memberList) {
                //获取所有成员，遍历里面的成员name是否包含关键字key
                if (null != memberList && memberList.size() > 0) {
                    List<String> nameList = new ArrayList<>();
                    for (Member member : memberList) {
                        //匹配条件控制
                        if (member.user().nickname().contains(finalKeyWord)
                                || member.user().nicknamePinyin().toUpperCase().startsWith(finalKeyWord)) {
                            nameList.add(member.user().nickname());
                        }
                    }
                    if (nameList.size() > 0) {
                        CircleList result = new CircleList();
                        result.setConversation(conversationList.get(index));
                        result.setImConversationId(conversationList.get(index).conversationId());
                        result.setAvatar(conversationList.get(index).icon());
                        result.setName(conversationList.get(index).title());
                        result.setMemberCount(conversationList.get(index).totalMembers());
                        result.setListSearchMembers(nameList);
                        SearchGlobalBean sgb = new SearchGlobalBean();
                        sgb.setType(11);
                        sgb.setCircleList(result);
                        searchGlobalBeans.add(sgb);
                    }
                }
                index++;
                if (index < count) {
                    getCircleMembers(searchGlobalBeans);
                } else {
                    addSgbList(searchGlobalBeans);
                }
            }

        }, conversationList.get(index).conversationId(), 0, Integer.MAX_VALUE);
    }

    private int chatIndex;
    private int chatCount;
    private List<Conversation> myConversations = new ArrayList<>();
    private List<SearchGlobalBean> chatLogList = new ArrayList<>();

    /**
     * 获取本地的聊天记录
     */
    public void getLocalChatLog() {
        //获取所有的聊天
        IMEngine.getIMService(ConversationService.class).listConversations(new Callback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations != null && !conversations.isEmpty()) {
                    for (Conversation conversation : conversations) {
                        if (conversation.status() == Conversation.ConversationStatus.NORMAL
                        /*|| conversation.status() == Conversation.ConversationStatus.HIDE*/) {
                            myConversations.add(conversation);
                        }
                    }
                    if (myConversations.size() > 0) {
                        chatIndex = 0;
                        chatCount = myConversations.size();
                        getChatLog();
                    }
                }
            }

            @Override
            public void onException(String code, String reason) {
                if (Constants.DEBUG_MODE) {
                    Toast.makeText(SearchForContactsActivity.this, "加载会话列表失败.code:" + code + " reason:" + reason,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgress(List<Conversation> conversations, int i) {
            }
        }, Integer.MAX_VALUE, Conversation.ConversationType.CHAT | Conversation.ConversationType.GROUP);
    }

    /**
     * 遍历获取所有圈子的聊天记录
     */
    public void getChatLog() {
        // 获取会话消息
        myConversations.get(chatIndex).listPreviousMessages(null, Integer.MAX_VALUE, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && messages.size() > 0) {
                    List<Message> messageList = new ArrayList<>();
                    for (Message msg : messages) {
                        //匹配条件控制
                        if (msg.messageContent().type() == MessageContent.MessageContentType.TEXT) {
                            String context = ((MessageContent.TextContent) msg.messageContent()).text();
                            if (context.contains(finalKeyWord)) {
                                messageList.add(msg);
                            }
                        }
                    }
                    if (messageList.size() > 0) {
                        ChatLog cl = new ChatLog();
                        cl.setConversation(myConversations.get(chatIndex));
                        cl.setMessage(messageList);
                        SearchGlobalBean sgb = new SearchGlobalBean();
                        sgb.setType(4);
                        sgb.setChatLog(cl);
                        sgb.setIsTitle(false);
                        sgb.setIsFooter(false);
                        chatLogList.add(sgb);
                    }
                } else {
                    chatIndex++;
                    if (chatIndex < chatCount) {
                        getChatLog();
                    } else {
                        if (null != chatLogList && chatLogList.size() > 0) {
                            SearchGlobalBean sgb1 = new SearchGlobalBean();
                            sgb1.setIsTitle(true);
                            sgb1.setIsFooter(false);
                            sgb1.setType(4);
                            sgb1.setTypeName("聊天记录");
                            searchList.add(sgb1);
                            if (chatLogList.size() > 3) {
                                searchList.add(chatLogList.get(0));
                                searchList.add(chatLogList.get(1));
                                searchList.add(chatLogList.get(2));
                                //add 底部加载更多
                                SearchGlobalBean sgb2 = new SearchGlobalBean();
                                sgb2.setIsTitle(false);
                                sgb2.setIsFooter(true);
                                sgb2.setType(4);
                                sgb2.setTypeName("查看更多聊天记录");
                                searchList.add(sgb2);
                            } else {
                                searchList.addAll(chatLogList);
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onException(String code, String reason) {
                chatIndex++;
                if (chatIndex < chatCount) {
                    getChatLog();
                } else {
                    if (null != chatLogList && chatLogList.size() > 0) {
                        SearchGlobalBean sgb1 = new SearchGlobalBean();
                        sgb1.setIsTitle(true);
                        sgb1.setIsFooter(false);
                        sgb1.setType(4);
                        sgb1.setTypeName("聊天记录");
                        searchList.add(sgb1);
                        if (chatLogList.size() > 3) {
                            searchList.add(chatLogList.get(0));
                            searchList.add(chatLogList.get(1));
                            searchList.add(chatLogList.get(2));
                            //add 底部加载更多
                            SearchGlobalBean sgb2 = new SearchGlobalBean();
                            sgb2.setIsTitle(false);
                            sgb2.setIsFooter(true);
                            sgb2.setType(4);
                            sgb2.setTypeName("查看更多聊天记录");
                            searchList.add(sgb2);
                        } else {
                            searchList.addAll(chatLogList);
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onProgress(List<Message> messages, int i) {
            }

        });
    }

    /**
     * 获取搜索的关键字
     *
     * @return
     */
    public String getKeyword() {
        return keywordEdt.getText().toString().trim();
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        if (event.getType() == NotifyListRefreshWithPositionEvent.RENMAI_SEARCH) {
            if (null != searchList && searchList.size() > event.getPosition()) {
                if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ADD) {
                    searchList.get(event.getPosition()).getMemberList().setIsInvite(true);//列表item状态改为等待验证
                } else if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ACCEPT) {
                    searchList.get(event.getPosition()).getMemberList().setIsReceived(true);//列表item状态改为等待验证
                }
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
        if (searchList != null) {
            if (searchList.size() > 0)
                searchList.clear();
            searchList = null;
        }
        if (localList != null) {
            if (localList.size() > 0)
                localList.clear();
            localList = null;
        }

        if (tipBox != null) {
            tipBox.dismiss();
        }

    }
}
