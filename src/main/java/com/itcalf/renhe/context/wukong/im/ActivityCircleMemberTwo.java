package com.itcalf.renhe.context.wukong.im;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Member;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ChatRemindItem;
import com.itcalf.renhe.bean.ContactItem;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.PinnedSectionListView;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;

public class ActivityCircleMemberTwo extends BaseActivity implements com.itcalf.renhe.http.Callback {
    private ListView listView;
    private FastScrollAdapterforContact adapter;
    private ClearableEditText keyword_edt;
    private SearchContactsSideBar sideBar;
    private TextView mLetterTxt;
    private Handler mNotifHandler;

    private List<Member> data = new ArrayList<Member>(); //存储上级传递过来的用户
    private TreeMap<String, List<Member>> mapFormat = new TreeMap<String, List<Member>>(); //用户关键字搜索
    private RelativeLayout rootRl;
    private FadeUitl fadeUitl;
    private String userConversationName; // 用户名
    private String circleName = ""; // 圈名
    private String circleJoinType = ""; // 圈子加入类型
    private String imConversationId; // 圈子会话id
    private boolean isJurisdiction; // 删除权限
    private boolean isCircleMember; // 用户判断是否是通过圈子搜索进入
    private Member circleMasterInfo; // 用于存储圈主信息

    private int MEMBERADD = 200; // 添加联系人
    private String[] joinTypeStr = {"", "所有人可以加入", "需要审批才可以加入", "所有人都不可以加入"};
    private List<HlContactRenheMember> mReceiverList = new ArrayList<>(); // 存储回调时添加的成员
    private static final StringBuffer deleteCircleStr = new StringBuffer(
            ",-7:您没有踢人权限,-6:圈主不能退出群,-5:被踢人不再此圈子中,-4:用户IM id不能为空,-3:会话Id为空,-2:服器内部错误,1:请求成功,");
    private static final StringBuffer inviteCircleStr = new StringBuffer(
            ",1:请求成功,2:已发出加入邀请申请,-1:很抱歉，您的权限不足！,-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:被邀请的圈子成员的im openid数组不能为空,-5:被邀请的圈子成员的im openid数组数据中包含了还不是和聊会员的数据,-6:圈子的成员数量已超过最大的限制，无法加入,");
    private static final StringBuffer invitationJoinCircleStr = new StringBuffer(
            ",1:请求成功，而且圈子为可直接加入的圈子,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,-1:很抱歉，您的权限不足！,-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:被邀请的圈子成员的im openid数组数组不能为空,-5:被邀请的圈子成员的im openid数组数据中包含了还不是和聊会员的数据,-6:您没有权限新增成员,-7:圈子的成员数量已超过最大的限制，无法加入,");
    private List<Member> memberList;
    private MenuItem saveItem;
    private Dialog mAlertDialog;
    private String detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		new ActivityTemplate().doInActivity(this, R.layout.chat_remind_activity);
        new ActivityTemplate().doInActivity(this, R.layout.search_contacts_new);
        //		setTitle("选择提醒的人");
        setTextValue(1, "圈子成员");
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        keyword_edt = (ClearableEditText) findViewById(R.id.keyword_edt);//输入拼音首字母查询
        listView = (ListView) findViewById(R.id.contacts_list);//联系人列表
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);//字母列表
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);//正中显示的首字母
        sideBar.setTextView(mLetterTxt);
        adapter = new FastScrollAdapterforContact(this, R.layout.chat_remind_list_item, R.id.title_txt);//成员列表显示的适配器
        listView.setAdapter(adapter);
        sideBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initListener() {
        super.initListener();
        keyword_edt.addTextChangedListener(tbxEdit_TextChanged);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRemindItem item = adapter.getItem(position);
                if (item.type == ChatRemindItem.ITEM) {
                    Member member = item.getMember();
                    if (null != member) {
                        Intent intent = new Intent(ActivityCircleMemberTwo.this, MyHomeArchivesActivity.class);
                        intent.putExtra("name", member.user().nickname());
                        intent.putExtra("openId", member.user().openId());
                        intent.putExtra("from", Constants.ADDFRIENDTYPE[7]);
                        intent.putExtra("flag", true);
                        intent.putExtra("circleName", circleName);//圈名，为了在添加好友时提示“我是来自XX 圈子的XXX”
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                }
            }
        });
        if (isJurisdiction) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ChatRemindItem item = adapter.getItem(position);
                    if (isJurisdiction && item.type == ChatRemindItem.ITEM) {
                        Member member = item.getMember();
                        if (null != member && member.roleType() != member.roleType().MASTER
                                && member.user().openId() != RenheApplication.getInstance().getUserInfo().getImId()) {
                            createCustomDialog(ActivityCircleMemberTwo.this, position);
                        }
                    }
                    return true;
                }
            });
        }

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SearchContactsSideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int section = adapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int positon = adapter.getPositionForSection(section);
                    listView.setSelection(positon);
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        saveItem = menu.findItem(R.id.item_save);
        saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        saveItem.setTitle("添加成员");
        saveItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save:
                addMember();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initData() {
        super.initData();
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        detail = getIntent().getStringExtra("detail");//判断是否从圈子资料页面进入
        circleName = getIntent().getStringExtra("cirlcleName");//圈名
        circleJoinType = getIntent().getStringExtra("circleJoinType") != null ? joinTypeStr[Integer.parseInt(getIntent().getStringExtra("circleJoinType"))] : null;
        imConversationId = getIntent().getStringExtra("imConversationId");//圈子会话id
        userConversationName = getIntent().getStringExtra("userConversationName");//用户名
        isCircleMember = getIntent().getBooleanExtra("isCircleMember", false);// 用户判断是否是通过圈子搜索进入
        isJurisdiction = getIntent().getBooleanExtra("jurisdiction", false);//删除权限（是否是圈主，只有圈主有删除权限）
        circleMasterInfo = (Member) getIntent().getSerializableExtra("memberInfo");//圈主信息
        //获取圈子成员
        getCircleMember();
        mNotifHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    // 关键字搜索
                    case 0:
                        String keyword = (String) msg.obj;
                        populateContacts(keyword);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 当为 圈主或为 圈子成员
     */
    private void getCircleMember() {
        //获取圈子成员
        IMEngine.getIMService(ConversationService.class).listMembers(new Callback<List<Member>>() {
            @Override
            public void onException(String arg0, String arg1) {
                if (fadeUitl.isFadeShow(rootRl))
                    fadeUitl.removeFade(rootRl);
                ToastUtil.showToast(ActivityCircleMemberTwo.this, R.string.load_circle_members_error);
            }

            public void onProgress(List<Member> arg0, int arg1) {
            }

            public void onSuccess(List<Member> members) {
                for (Member menber : members) {
                    if (menber.roleType() == menber.roleType().MASTER) {
                        circleMasterInfo = menber;
                    }
                }
                if (fadeUitl.isFadeShow(rootRl))
                    fadeUitl.removeFade(rootRl);
                if (null != saveItem) {
                    //判断是否从圈子资料页面进入 如果是就隐藏actionbar上的添加成员按钮
                    if (!TextUtils.isEmpty(detail) && detail.equals("fromActivityDetail")) {
                        saveItem.setVisible(false);
                    } else {
                        saveItem.setVisible(true);
                    }
                }
                memberList = members;
                handleMember(members);
            }

        }, imConversationId, 0, Integer.MAX_VALUE);
    }

    private void handleMember(List<Member> members) {
        if (null != mapFormat) {
            mapFormat.clear();
        }
        for (Member member : members) {
            String key = PinyinUtil.cn2FirstSpell(member.user().nicknamePinyin()).toUpperCase().substring(0, 1);
            if (mapFormat.get(key) == null) {
                List<Member> memberList = new ArrayList<Member>();
                mapFormat.put(key, memberList);
            }
            List<Member> memberList = mapFormat.get(key);
            memberList.add(member);
            mapFormat.put(key, memberList);
            data.add(member);
        }
        populateContacts(null);
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            Message m = new Message();
            m.obj = keyword_edt.getText().toString();
            m.what = 0;
            mNotifHandler.sendMessage(m);
        }
    };

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
            if (!TextUtils.isEmpty(s.toString())) {
                keyword_edt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        getResources().getDrawable(R.drawable.clearbtn_selected), null);
                keyword_edt.setCompoundDrawablePadding(1);
            } else {
                keyword_edt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handler.postDelayed(run, 0);
        }

    };

    /**
     * 查询联系人，支持拼音简写、字母查询
     *
     * @param keyword
     */

    private void populateContacts(String keyword) {
        adapter.clear();
        if (null != mapFormat && !mapFormat.isEmpty()) {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = mapFormat.size() + 1;//+1是因为圈主是额外添加的item
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
                int sectionPosition = 0, listPosition = 0;
                //圈主
                //判断圈主
                if (null != circleMasterInfo) {
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, "圈主");
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);
                    ChatRemindItem circleMasterItem = new ChatRemindItem(ChatRemindItem.ITEM, circleMasterInfo.user().nickname());
                    circleMasterItem.sectionPosition = sectionPosition;
                    circleMasterItem.listPosition = listPosition++;
                    circleMasterItem.setMember(circleMasterInfo);
                    adapter.add(circleMasterItem);
                    sectionPosition++;
                }

                // 圈子列表加载
                Set<Map.Entry<String, List<Member>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<Member>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<Member>> entry = (Map.Entry<String, List<Member>>) it.next();
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);

                    List<Member> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        Member member = memberList.get(j);
                        ChatRemindItem item2 = new ChatRemindItem(ChatRemindItem.ITEM, member.user().nickname());
                        item2.sectionPosition = sectionPosition;
                        item2.listPosition = listPosition++;
                        item2.setMember(member);
                        adapter.add(item2);
                    }
                    sectionPosition++;
                }
                adapter.notifyDataSetChanged();
            } else {
                Map<String, List<Member>> mResultsMap = new TreeMap<String, List<Member>>();// 添加首字母
                Set<Map.Entry<String, List<Member>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<Member>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<Member>> entry = (Map.Entry<String, List<Member>>) it.next();
                    List<Member> memberList = entry.getValue();
                    List<Member> resultList = new ArrayList<Member>();
                    if (null != memberList && !memberList.isEmpty()) {
                        for (int j = 0; j < memberList.size(); j++) {
                            Member member = memberList.get(j);
                            if (null != keyword && null != member.user().nickname()
                                    && (member.user().nickname().toUpperCase().startsWith(keyword.toUpperCase()) || PinyinUtil
                                    .cn2FirstSpell(member.user().nickname()).startsWith(keyword.toUpperCase()))) {
                                resultList.add(member);
                                mResultsMap.put(entry.getKey(), resultList);
                            }
                        }
                    }
                }
                adapter.prepareSections(mResultsMap.size());
                listView.setAdapter(adapter);
                // 联系人列表加载
                int sectionPosition = 0, listPosition = 0;
                Set<Map.Entry<String, List<Member>>> resultSet = mResultsMap.entrySet();
                Iterator<Map.Entry<String, List<Member>>> resultIt = resultSet.iterator();
                while (resultIt.hasNext()) {
                    Map.Entry<String, List<Member>> entry = (Map.Entry<String, List<Member>>) resultIt.next();
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);
                    List<Member> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        Member member = memberList.get(j);
                        ChatRemindItem item = new ChatRemindItem(ChatRemindItem.ITEM, member.user().nickname());
                        item.sectionPosition = sectionPosition;
                        item.listPosition = listPosition++;
                        item.setMember(member);
                        adapter.add(item);
                    }
                    sectionPosition++;
                }
                // adapter.notifyDataSetChanged();
            }
        } else {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = 1;
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
            }
        }
        if (null != mapFormat && !mapFormat.isEmpty()) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 管理员邀请人入圈子
     */
    private void invitationJoinCircle(final int[] invitationMember, final String name) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().invitationJoinCircle(params[0],
                            params[1], imConversationId, invitationMember, ActivityCircleMemberTwo.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        mReceiverList.clear();
                        RenheIMUtil.dismissProgressDialog();
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleMemberTwo.this,
                                getResult(invitationJoinCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleMemberTwo.this, R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 普通成员邀请某人加入圈子
     */
    private void inviteCircle(final int[] invitationMember, final String name) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().inviteCircle(params[0],
                            params[1], imConversationId, invitationMember, ActivityCircleMemberTwo.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        addMembers(invitationMember, name);
                    } else {
                        ToastUtil.showToast(ActivityCircleMemberTwo.this, getResult(inviteCircleStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ActivityCircleMemberTwo.this, R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * IM添加成员
     */
    private void addMembers(final int[] memberId, final String name) {
        final List<MemberInfo> array1 = new ArrayList<MemberInfo>();
        Long[] memberIds = new Long[memberId.length];
        for (int i = 0; i < memberId.length; i++) {
            //			for (int j = 0; j < mReceiverList.size(); j++) {
            //				if (memberId[i] == mReceiverList.get(j).getImId()) {
            //					Member memberInfo = new Member();
            //					memberInfo.setAvatar(mReceiverList.get(j).getContactface());
            //					memberInfo.setNickName(mReceiverList.get(j).getName());
            //					memberInfo.setOpenId(mReceiverList.get(j).getImId());
            //					memberInfo.setPinyin(PinyinUtil.cn2Spell(mReceiverList.get(j).getName()));
            //					array1.add(memberInfo);
            //				}
            //			}
            memberIds[i] = Long.parseLong(memberId[i] + "");
        }
        com.alibaba.wukong.im.Message message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(userConversationName + "邀请 " + name + " 加入了圈子");
        IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> arg0) {
                getCircleMember();
                //				adapter.notifyDataSetChanged();
                invitationJoinCircle(memberId, name);
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {
            }

            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleMemberTwo.this, "加入圈子失败.code:" + arg0 + " reason:" + arg0,
                        Toast.LENGTH_SHORT).show();
            }
        }, imConversationId + "", message, memberIds);
    }

    /**
     * 踢人
     */
    public void deleteCircle(final int[] imMemberId, final Long[] openIds, final String removeName) {
        RenheIMUtil.showProgressDialog(this, R.string.handling);
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().deleteCircle(params[0],
                            params[1], imConversationId, imMemberId, ActivityCircleMemberTwo.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        removeMembers(openIds, removeName);
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleMemberTwo.this, getResult(deleteCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleMemberTwo.this, R.string.service_exception);
                }

            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 移出会话成员
     */
    private void removeMembers(final Long[] openIds, String removeName) {
        com.alibaba.wukong.im.Message message = null;
        //成员退出或被移出圈子，不再发送全员提示消息
//		message = IMEngine.getIMService(MessageBuilder.class)
//				.buildTextMessage(userConversationName + "将" + removeName + "移出了" + circleName + "圈子");
        IMEngine.getIMService(ConversationService.class).removeMembers(new Callback<List<Long>>() {
            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleMemberTwo.this, "删除失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {
            }

            @Override
            public void onSuccess(List<Long> arg0) {
                for (int j = 0; j < openIds.length; j++) {
                    for (int i = 0; i < memberList.size(); i++) {
                        if (memberList.get(i).user().openId() ==openIds[j]) {
                            memberList.remove(i);
                        }
                    }
                }
                handleMember(memberList);
                ToastUtil.showToast(ActivityCircleMemberTwo.this, "删除成功");
                RenheIMUtil.dismissProgressDialog();
                if (null != memberList && memberList.size() <= 1) {//只剩一个人时，自动解散圈子
                    Intent intent = new Intent();
                    setResult(88, intent);
                    finish();
                    overridePendingTransition(0, R.anim.out_to_right);
                }
            }
        }, imConversationId, message, openIds);
    }

    private String getResult(StringBuffer strs, Integer code) {
        int index = strs.indexOf(String.valueOf("," + code + ":"));
        if (index < 0) {
            return "state:" + code;
        }
        return strs.substring(index + 2 + String.valueOf(code).length(),
                strs.indexOf(",", index + 2 + String.valueOf(code).length()));
    }

    public void addMember() {
        if (circleJoinType != null && !circleJoinType.equals("所有人都不可以加入") && null != memberList) {
            String[] memberOpenIdArray = new String[memberList.size()];
            for (int i = 0; i < memberList.size(); i++) {
                memberOpenIdArray[i] = memberList.get(i).user().openId() + "";
            }
            Intent intent = new Intent(ActivityCircleMemberTwo.this, ActivityCircleContacts.class);
            intent.putExtra("memberOpenIdArray", memberOpenIdArray);
            startActivityForResult(intent, MEMBERADD);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            ToastUtil.showToast(ActivityCircleMemberTwo.this, circleJoinType == null ? "圈子加入类型获取失败" : "此圈子不允许其他人加入");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MEMBERADD && data != null) {
            String name = "";
            mReceiverList = (List<HlContactRenheMember>) data.getSerializableExtra("contacts");
            if (mReceiverList.size() > 0) {
                int[] invitationImMemberIds = new int[mReceiverList.size()];
                for (int i = 0; i < mReceiverList.size(); i++) {
                    invitationImMemberIds[i] = mReceiverList.get(i).getImId();
                    if (i == mReceiverList.size() - 1) {
                        name = name + mReceiverList.get(i).getName();
                    } else {
                        name = name + mReceiverList.get(i).getName() + ",";
                    }
                }
                RenheIMUtil.showProgressDialog(ActivityCircleMemberTwo.this, R.string.loading);
                if (isJurisdiction)
                    addMembers(invitationImMemberIds, name);
                else
                    inviteCircle(invitationImMemberIds, name);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class CircleMemberAdapter extends ArrayAdapter<ChatRemindItem> implements PinnedSectionListView.PinnedSectionListAdapter {

        public CircleMemberAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        protected void prepareSections(int sectionsNumber) {
        }

        // 列表添加项
        protected void onSectionAdded(ChatRemindItem section, int sectionPosition) {
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (view != null) {
                ImageView iv_avator = (ImageView) view.findViewById(R.id.iv_avator);//成员头像
                TextView tx_name = (TextView) view.findViewById(R.id.tx_name);//成员姓名
                TextView job_txt = (TextView) view.findViewById(R.id.job_txt);//职务
                TextView company_txt = (TextView) view.findViewById(R.id.company_txt);//公司
                LinearLayout contentLl = (LinearLayout) view.findViewById(R.id.content_ll);//item选项
                LinearLayout titleLl = (LinearLayout) view.findViewById(R.id.title_ll);//不同组的分隔线
                View contactDivider = view.findViewById(R.id.contact_divider);//item间的分隔线

                ChatRemindItem item = getItem(position);
                if (item.type == ChatRemindItem.SECTION) {
                    titleLl.setVisibility(View.VISIBLE);
                    contentLl.setVisibility(View.GONE);
                    contactDivider.setVisibility(View.GONE);
                } else {
                    titleLl.setVisibility(View.GONE);
                    contentLl.setVisibility(View.VISIBLE);
                    tx_name.setText(item.getMember().user().nickname());
                    String job = item.getMember().user().extension("title");
                    String company = item.getMember().user().extension("company");
                    if (!TextUtils.isEmpty(job)) {
                        job_txt.setVisibility(View.VISIBLE);
                        job_txt.setText(job);
                    } else {
                        job_txt.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(company)) {
                        company_txt.setVisibility(View.VISIBLE);
                        company_txt.setText(company);
                    } else {
                        company_txt.setVisibility(View.GONE);
                    }
                    ImageLoader.getInstance().displayImage(item.getMember().user().avatar(), iv_avator, CacheManager.circleImageOptions);
                    if (position < getCount() - 1) {
                        ChatRemindItem tmpItem = getItem(position + 1);
                        if (tmpItem.type == ContactItem.SECTION)
                            contactDivider.setVisibility(View.GONE);
                        else {
                            contactDivider.setVisibility(View.VISIBLE);
                        }
                    } else {
                        contactDivider.setVisibility(View.GONE);
                    }
                }
            }
            return view;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == ChatRemindItem.SECTION;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }
    }

    public class FastScrollAdapterforContact extends CircleMemberAdapter implements SectionIndexer {
        ChatRemindItem[] sections;

        public FastScrollAdapterforContact(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public void prepareSections(int sectionsNumber) {
            sections = new ChatRemindItem[sectionsNumber];
        }

        @Override
        public void onSectionAdded(ChatRemindItem section, int sectionPosition) {
            sections[sectionPosition] = section;
        }

        @Override
        public ChatRemindItem[] getSections() {
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
                if (tag.equals("★")) {
                    return 0;
                }
                for (int i = 0; i < sections.length; i++) {
                    if (null != sections[i] && null != sections[i].text && sections[i].text.equals(tag))
                        return i;
                }
            }
            return -1;
        }
    }

    public void createCustomDialog(final Context context, final int position) {
        final ChatRemindItem deleteItem = (ChatRemindItem) listView.getAdapter().getItem(position);
        if (deleteItem != null && deleteItem.type != ChatRemindItem.SECTION) {
            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(ActivityCircleMemberTwo.this);
            materialDialog.showSelectList(R.array.conversation_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    switch (which) {
                        case 0:
                            if (null != mAlertDialog) {
                                mAlertDialog.dismiss();
                            }
                            Long[] l = {deleteItem.getMember().user().openId()};
                            int[] i = {Integer.parseInt(String.valueOf(deleteItem.getMember().user().openId()))};
                            deleteCircle(i, l, deleteItem.getMember().user().nickname());
                            break;
                        case 1:
                            break;
                        default:
                            break;
                    }
                }
            });
            materialDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (circleMasterInfo != null) {
            List<MemberInfo> circleMemberInfoList = new ArrayList<>();
            MemberInfo circleMasterMemberInfo = new MemberInfo();
            circleMasterMemberInfo.setNickName(circleMasterInfo.user().nickname());
            circleMasterMemberInfo.setAvatar(circleMasterInfo.user().avatar());
            circleMasterMemberInfo.setOpenId(circleMasterInfo.user().openId());
            circleMasterMemberInfo.setPinyin(circleMasterInfo.user().nicknamePinyin());
            circleMasterMemberInfo.setMaster(true);
            circleMemberInfoList.add(circleMasterMemberInfo);
            if (null != memberList) {
                for (int i = 0; i < memberList.size(); i++) {
                    Member member = memberList.get(i);
                    if (member.roleType() != member.roleType().MASTER) {
                        MemberInfo memberInfo = new MemberInfo();
                        memberInfo.setNickName(member.user().nickname());
                        memberInfo.setAvatar(member.user().avatar());
                        memberInfo.setOpenId(member.user().openId());
                        memberInfo.setPinyin(member.user().nicknamePinyin());
                        circleMemberInfoList.add(memberInfo);
                    }
                }
            }
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("contacts", (Serializable) circleMemberInfoList);
            intent.putExtras(bundle);
            setResult(77, intent);
            overridePendingTransition(0, R.anim.out_to_right);
            super.onBackPressed();
        } else {
            finish();
            overridePendingTransition(0, R.anim.out_to_right);
        }
    }
}
