package com.itcalf.renhe.context.relationship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.google.gson.Gson;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.MemberList;
import com.itcalf.renhe.bean.SearchRelationship;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.innermsg.ReceiveFriend;
import com.itcalf.renhe.context.more.AccountLimitUpgradeActivity;
import com.itcalf.renhe.context.relationship.SeniorSearchRelationshipTask.IDataBack;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.ReceiveAddFriend;
import com.itcalf.renhe.eventbusbean.NotifyListRefreshWithPositionEvent;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
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
 * Title: SearchResultActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-4-11 下午5:07:31 <br>
 *
 * @author wangning
 */
public class SearchResultActivity extends BaseActivity {

    private ListView mRelationshipList;
    private View mFooterView;
    private TextView keywordTv;
    private RelativeLayout noResultLayout;
    private List<Map<String, Object>> mData;
    private SimpleAdapter mSimpleAdapter;
    private int mStart = 1;//页码page
    private int mCount = 20;
    private String keyword = "";
    private int cityCode = -1;
    private int industryCode = -1;
    private String company = "";
    private String job = "";
    private String name = "";
    private RelativeLayout nowifiLayout;
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private String searchJson = "";
    private boolean iscluding = true;//是否进行结果点击事件的统计
    private CheckUpgradeUtil checkUpgradeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.relationship_search_result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("人脉搜索结果"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("人脉搜结果"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        mRelationshipList = (ListView) findViewById(R.id.relationship_list);
        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        mFooterView.setVisibility(View.GONE);
        keywordTv = (TextView) findViewById(R.id.keyword_tv);
        noResultLayout = (RelativeLayout) findViewById(R.id.noresult);
        nowifiLayout = (RelativeLayout) findViewById(R.id.nowifi_rl);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "人脉搜索结果");
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        initIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initIntent(intent);
    }

    private void initIntent(Intent intent) {
        iscluding = intent.getBooleanExtra("excluding", true);
        String key = intent.getStringExtra("keyword");
        cityCode = intent.getIntExtra("citycode", -1);
        industryCode = intent.getIntExtra("industryCode", -1);
        String area = intent.getStringExtra("area");
        String industry = intent.getStringExtra("industry");
        String intent_company = intent.getStringExtra("company");
        String intent_job = intent.getStringExtra("job");
        String intent_name = intent.getStringExtra("name");
        StringBuffer keywordTvSb = new StringBuffer();
        if (null != key && !key.equals("")) {
            keyword = key;
            keywordTvSb.append("\"" + keyword + "\" ");
        }
        if (null != area && !area.equals("") && !area.equals(AdvancedSearchActivity.ALL_AREA_STRING)) {
            keywordTvSb.append("\"" + area + "\" ");
        }
        if (null != industry && !industry.equals("") && !industry.equals(AdvancedSearchActivity.ALL_INDUSTRY_STRING)
                && !industry.equals(AdvancedSearchActivity.ALL_INDUSTRY_STRING2)) {
            keywordTvSb.append("\"" + industry + "\" ");
        }
        if (null != intent_company && !intent_company.equals("")) {
            company = intent_company;
            keywordTvSb.append("\"" + company + "\" ");
        }
        if (null != intent_job && !intent_job.equals("")) {
            job = intent_job;
            keywordTvSb.append("\"" + job + "\" ");
        }
        //名字
        if (null != intent_name && !intent_name.equals("")) {
            name = intent_name;
            keywordTvSb.append("\"" + name + "\" ");
        }
        keywordTv.setText(keywordTvSb);
        invalidateOptionsMenu();
        mData = new ArrayList<Map<String, Object>>();
        mSimpleAdapter = new ImageUpdateAdapter(this, mData, R.layout.relationship_search_list_item, mFrom, mTo);
        mRelationshipList.addFooterView(mFooterView, null, false);
        mRelationshipList.setAdapter(mSimpleAdapter);
        if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                .getBoolean("fastscroll", false)) {
            mRelationshipList.setFastScrollEnabled(true);
        }
        initSearch(keyword, cityCode, industryCode, company, job, name, true);
        Map<String, String> params = new HashMap<String, String>();
        params.put("keyword", keyword);
        params.put("citycode", "" + cityCode);
        params.put("industryCode", "" + industryCode);
        params.put("company", company);
        params.put("job", job);
        params.put("name", name);
        Gson gson = new Gson();
        searchJson = gson.toJson(params);
        checkUpgradeUtil = new CheckUpgradeUtil(this);
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
    protected void initListener() {
        super.initListener();
        //注册EventBus
        EventBus.getDefault().register(this);
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //				mStart += mCount;
                mStart++;
                initSearch(keyword, cityCode, industryCode, company, job, name, false);
            }
        });
        mRelationshipList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mData.size() && null != mData.get(position)) {
                    Intent intent = new Intent(SearchResultActivity.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, (String) mData.get(position).get("sid"));
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[2]);
                    intent.putExtra("addfriend_from", "advanceSearchResult");
                    intent.putExtra("position", position);
                    startHlActivity(intent);
                }
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
            case 2:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.request_handling).cancelable(false)
                        .build();
            default:
                return null;
        }
    }

    private void initSearch(String keyword, int cityCode, int industryCode, String company, String job, String name,
                            final boolean hideFooter) {
        new SeniorSearchRelationshipTask(this, new IDataBack() {

            @Override
            public void onPre() {

                if (hideFooter) {
                    mFooterView.setVisibility(View.GONE);
                } else {
                    toggleFooterView(true);
                }
            }

            @Override
            public void onPost(List<Map<String, Object>> result) {
                if (hideFooter) {
                    fadeUitl.removeFade(rootRl);
                } else {
                    toggleFooterView(false);
                }
                if (null != result) {
                    nowifiLayout.setVisibility(View.GONE);
                    mRelationshipList.setVisibility(View.VISIBLE);
                    noResultLayout.setVisibility(View.GONE);
                    if (result.size() == mCount) {
                        mFooterView.setVisibility(View.VISIBLE);
                    } else {
                        mFooterView.setVisibility(View.GONE);
                    }
                    mData.addAll(result);
                    mSimpleAdapter.notifyDataSetChanged();
                } else {
                    nowifiLayout.setVisibility(View.GONE);
                    mRelationshipList.setVisibility(View.GONE);
                    noResultLayout.setVisibility(View.VISIBLE);
                    //					mStart = (mStart -= mCount) == 0 ? 0 : mStart;
                    mStart = (mStart--) <= 1 ? 1 : mStart;
                }
                if (NetworkUtil.hasNetworkConnection(SearchResultActivity.this) == -1) {
                    nowifiLayout.setVisibility(View.VISIBLE);
                    mRelationshipList.setVisibility(View.GONE);
                    noResultLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPostExtra(SearchRelationship result) {
                if (hideFooter) {
                    fadeUitl.removeFade(rootRl);
                } else {
                    toggleFooterView(false);
                }
                if (-4 == result.getState()) {
                    int limit = result.getLimit();
                    //弹框提示
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(SearchResultActivity.this);
                    materialDialogsUtil
                            .getNotitleBuilder(R.string.search_result_num_limit_error, R.string.upgrade_now, R.string.hint_iknow)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    Intent i = new Intent(SearchResultActivity.this, AccountLimitUpgradeActivity.class);
                                    i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[0]);
                                    startActivity(i);
                                    finish();
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                    //和聊统计
                                    String content = "5.158.1" + LoggerFileUtil.getConstantInfo(SearchResultActivity.this) + "|3";
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
                } else {
                    nowifiLayout.setVisibility(View.GONE);
                    mRelationshipList.setVisibility(View.GONE);
                    noResultLayout.setVisibility(View.VISIBLE);
                }
            }

        }).executeOnExecutor(Executors.newCachedThreadPool(), keyword, cityCode, industryCode, company, job, name, mStart,
                mCount);
    }

    private String[] mFrom = new String[]{"headImage", "nameTv", "titleTv", "infoTv", "rightImage"};
    private int[] mTo = new int[]{R.id.headImage, R.id.nameTv, R.id.titleTv, R.id.infoTv, R.id.rightImage};

    class ImageUpdateAdapter extends SimpleAdapter {

        public ImageUpdateAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            if (convertView != null && position < mData.size()) {
                final String picPath = (String) mData.get(position).get("avatar_path");
                if (null != picPath && !"".equals(picPath)) {

                    ImageView imageView = (ImageView) convertView.findViewById(mTo[0]);
                    if (null != imageView) {
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        try {
                            imageLoader.displayImage(picPath, (ImageView) imageView, CacheManager.options,
                                    CacheManager.animateFirstDisplayListener);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ImageView imageView = (ImageView) convertView.findViewById(mTo[0]);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
                }
                ImageView rightIv = (ImageView) convertView.findViewById(R.id.rightImage);
                //关系几层不显示
                //				if (mData.get(position).get(mFrom[4]) != null) {
                //					rightIv.setVisibility(View.VISIBLE);
                //				} else {
                rightIv.setVisibility(View.GONE);
                //				}
                ImageView vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
                ImageView realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);
                Button acceptBt = (Button) convertView.findViewById(R.id.accept_btn);
                TextView addedTv = (TextView) convertView.findViewById(R.id.added_txt);
                Object accountObject = mData.get(position).get("accountType");
                Object realNameObject = mData.get(position).get("isRealName");
                int accountType = 0;
                boolean isRealName = false;
                if (null != accountObject) {
                    accountType = (Integer) mData.get(position).get("accountType");
                    ;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
                }
                if (null != realNameObject) {
                    isRealName = (Boolean) mData.get(position).get("isRealName");//是否是实名认证的会员
                }
                switch (accountType) {
                    case 0:
                        vipIv.setVisibility(View.GONE);
                        break;
                    case 1:
                        vipIv.setVisibility(View.VISIBLE);
                        //					vipIv.setImageResource(R.drawable.vip_1);
                        vipIv.setImageResource(R.drawable.archive_vip_1);
                        break;
                    case 2:
                        vipIv.setVisibility(View.VISIBLE);
                        //					vipIv.setImageResource(R.drawable.vip_2);
                        vipIv.setImageResource(R.drawable.archive_vip_2);
                        break;
                    case 3:
                        vipIv.setVisibility(View.VISIBLE);
                        //					vipIv.setImageResource(R.drawable.vip_3);
                        vipIv.setImageResource(R.drawable.archive_vip_3);
                        break;

                    default:
                        break;
                }
                if (isRealName && accountType <= 0) {
                    realNameIv.setVisibility(View.VISIBLE);
                    //					realNameIv.setImageResource(R.drawable.realname);
                    realNameIv.setImageResource(R.drawable.archive_realname);
                } else {
                    realNameIv.setVisibility(View.GONE);
                }

                boolean isConnection = false;
                boolean isReceived = false;
                boolean isInvite = false;
                int inviteId = 0, inviteType = 0;
                MemberList.BeInvitedInfo beInvitedInfo = null;
                Object isConnectionObject = mData.get(position).get("isConnection");
                Object isReceivedObject = mData.get(position).get("isReceived");
                Object isInviteObject = mData.get(position).get("isInvite");
                Object beInvitedInfoObject = mData.get(position).get("beInvitedInfo");
                if (null != isConnectionObject) {
                    isConnection = (Boolean) isConnectionObject;
                }
                if (null != isReceivedObject) {
                    isReceived = (Boolean) isReceivedObject;
                }
                if (null != isInviteObject) {
                    isInvite = (Boolean) isInviteObject;
                }
                if (null != beInvitedInfoObject) {
                    beInvitedInfo = (MemberList.BeInvitedInfo) beInvitedInfoObject;
                    inviteId = beInvitedInfo.getInviteId();
                    inviteType = beInvitedInfo.getInviteType();
                }
                // 按钮类型
                int status = 4;
                if (isConnection) {
                    status = 1;
                } else if (isReceived) {
                    status = 6;//已添加
                } else if (isInvite) {
                    status = 3;
                } else if (null != beInvitedInfo && beInvitedInfo.isBeInvited()) {
                    status = 0;
                } else if ((mData.get(position).get("sid")).equals(RenheApplication.getInstance().getUserInfo().getSid())) {
                    status = 5;
                }
                switch (status) {
                    case 0:// 0代表未处理，可以接受邀请；
                        acceptBt.setVisibility(View.VISIBLE);
                        acceptBt.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                        ColorStateList csl = getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                        acceptBt.setTextColor(csl);
                        acceptBt.setText("接受");
                        addedTv.setVisibility(View.GONE);
                        break;
                    case 1:// 1代表已处理，已接受邀请；
                        acceptBt.setVisibility(View.VISIBLE);
                        acceptBt.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                        ColorStateList chatCsl = getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                        acceptBt.setTextColor(chatCsl);
                        acceptBt.setText("聊天");
                        addedTv.setVisibility(View.GONE);
                        break;
                    case 2:// 2代表已处理，已拒绝邀请
                        acceptBt.setVisibility(View.GONE);
                        addedTv.setVisibility(View.VISIBLE);
                        addedTv.setText("已拒绝");
                        break;
                    case 3:// 3代表已添加，等待验证
                        acceptBt.setVisibility(View.GONE);
                        addedTv.setVisibility(View.VISIBLE);
                        addedTv.setText("已邀请");
                        break;
                    case 4:// 4代表 添加
                        acceptBt.setVisibility(View.VISIBLE);
                        acceptBt.setBackgroundResource(R.drawable.btn_bp3_rectangle_shape);
                        ColorStateList csl1 = getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                        acceptBt.setTextColor(csl1);
                        acceptBt.setText("添加");
                        addedTv.setVisibility(View.GONE);
                        break;
                    case 5:// 5代表自己
                        acceptBt.setVisibility(View.GONE);
                        addedTv.setVisibility(View.GONE);
                        break;
                    case 6:// 已添加
                        acceptBt.setVisibility(View.GONE);
                        addedTv.setVisibility(View.VISIBLE);
                        addedTv.setText("已添加");
                        break;
                    default:
                        break;
                }
                final int finalStatus = status;
                final int finalInviteId = inviteId;
                final int finalInviteType = inviteType;
                int imId = 0;
                if (null != mData.get(position).get("imId")) {
                    imId = (int) mData.get(position).get("imId");
                }
                final int finalImId = imId;
                acceptBt.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (finalStatus == 0) {//接受邀请
                            acceptAction(position, finalInviteId, finalInviteType);
                            MobclickAgent.onEvent(SearchResultActivity.this, getResources().getString(R.string.newFriend_item_received));
                        } else if (finalStatus == 4) {//去添加
                            inviteAction(position);
                            MobclickAgent.onEvent(SearchResultActivity.this, getResources().getString(R.string.newFriend_item_add));
                        } else if (finalStatus == 1) {//去聊天
                            createConversation(finalImId, mData.get(position).get("nameTv").toString(), picPath);
                        }
                    }
                });
            }
            return convertView;
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {

        }
    }

    //接受好友邀请
    void acceptAction(final int position, int inviteId, int inviteType) {
        new ReceiveFriend(SearchResultActivity.this) {
            @Override
            public void doPre() {
                showDialog(2);
            }

            @Override
            public void doPost(ReceiveAddFriend result) {
                removeDialog(2);
                if (result == null) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, SearchResultActivity.this.getString(R.string.connect_server_error));
                } else if (result.getState() == 1) {
                    mData.get(position).put("isReceived", true);
                    mSimpleAdapter.notifyDataSetChanged();
                    //通知人脉列表更新
                    new ContactsUtil(SearchResultActivity.this).SyncContacts();
                    checkUpgradeUtil.checkUpgrade();//检测是否要提醒升级的弹框
                } else if (result.getState() == -1) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, R.string.lack_of_privilege);
                } else if (result.getState() == -2) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, R.string.sorry_of_unknow_exception);
                } else if (result.getState() == -3) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, "邀请序号不存在！");
                } else if (result.getState() == -4) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, "邀请类型不存在！");
                } else if (result.getState() == -5) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, "接受类型不存在！");
                } else if (result.getState() == -6) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, R.string.no_permission_do);
                } else if (result.getState() == -7) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, "您已经通过该请求了！");
                } else if (result.getState() == -8) {
                    ToastUtil.showErrorToast(SearchResultActivity.this, "您已经拒绝过该请求！");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), "" + inviteId, "" + inviteType, "true");
    }

    //去添加
    void inviteAction(final int position) {
        Intent i = new Intent(SearchResultActivity.this, AddFriendAct.class);
        i.putExtra("mSid", mData.get(position).get("sid").toString());
        i.putExtra("friendName", mData.get(position).get("nameTv").toString());
        i.putExtra("addfriend_from", "advanceSearchResult");
        i.putExtra("position", position);
        startActivity(i);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * eventBus 添加好友请求发送成功之后，用于通知界面刷新
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NotifyListRefreshWithPositionEvent event) {
        if (event.getType() == NotifyListRefreshWithPositionEvent.ADVANCE_SEARCH) {
            if (null != mData && mData.size() > event.getPosition()) {
                if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ADD) {
                    mData.get(event.getPosition()).put("isInvite", true);//列表item状态改为等待验证
                } else if (event.getMode() == NotifyListRefreshWithPositionEvent.MODE_ACCEPT) {
                    mData.get(event.getPosition()).put("isReceived", true);//列表item状态改为等待验证
                }
                mSimpleAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 创建IM会话
     */
    private void createConversation(int imId, String name, final String userFace) {
        if (imId <= 0)
            return;
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(SearchResultActivity.this, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(name);
        com.alibaba.wukong.im.Message message = null; // 创建会话发送的系统消息,可以不设置
        int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                ConversationListUtil.updateChatConversationExtension(conversation, title.toString(), userFace);
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Intent intent = new Intent(SearchResultActivity.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                intent.putExtra("userName", title.toString());
                intent.putExtra("userFace", userFace);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Toast.makeText(SearchResultActivity.this, "创建会话失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), userFace, message, convType, Long.parseLong(imId + ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }
}
