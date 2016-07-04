package com.itcalf.renhe.context.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.MailBoxExpandableListAdapter;
import com.itcalf.renhe.adapter.MailBoxListAdapter.ViewHolder;
import com.itcalf.renhe.bean.ContactResultByMailBox;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.RenheMemberInfo;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.SideBar;
import com.itcalf.renhe.view.SideBar.OnTouchingLetterChangedListener;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @author Chan
 *         邮箱联系人
 */
public class MailBoxList extends BaseActivity {
    /****
     * 导入界面
     ****/
    private LinearLayout mobileMailImportLl;
    private Button importBtn;
    /*****
     * 返回的通讯录列表
     ****/
    private RelativeLayout mobilemail_list;
    private ExpandableListView LVmaillist;
    private SideBar letterListView;
    private TextView mLetterTxt;
    //底部
    private LinearLayout check_Ll;
    private TextView checkAll_Tv;
    private TextView AKeySend_Tv;
    //顶部操作提示，仅第一次进入提示，点击消失
    private TextView contacts_top_tip;

    private Handler handler;
    private OverlayThread overlayThread;

    public static final String UPDATE_LISTITEM = "mailbox_upload_list_item";
    private UpdateListItem updatelistitem = null;
    // 联系人数据
    private Map<String, List<MailBoxContact>> mContactsMap;
    private Handler mNotifHandler;
    private IContactCommand contactCommand;
    private long saveTime = 0l;
    private long space = 3 * 24 * 60 * 60;

    public static String HL_FRIENDS = "和聊好友";
    public static String HL_NO_FRIENDS = "添加好友";
    /**
     * 和聊会员--好友List
     **/
    List<MailBoxContact> membersList = new ArrayList<>();
    /**
     * 和聊会员--非好友List
     **/
    List<MailBoxContact> addFriendList = new ArrayList<>();

    private List<String> group;
    private List<List<MailBoxContact>> cList;
    private MailBoxExpandableListAdapter mAdapter;
    private boolean isAllChecked = false;//默认未全选

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.mailbox_expandable_listview);
    }

    @Override
    protected void findView() {
        super.findView();
        mobileMailImportLl = (LinearLayout) findViewById(R.id.mobilemail_import);
        mobileMailImportLl.setVisibility(View.GONE);
        importBtn = (Button) findViewById(R.id.import_Btn);

        mobilemail_list = (RelativeLayout) findViewById(R.id.mobilemail_list);
        mobilemail_list.setVisibility(View.VISIBLE);
        LVmaillist = (ExpandableListView) findViewById(R.id.LVmaillist);
        letterListView = (SideBar) findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);

        check_Ll = (LinearLayout) findViewById(R.id.check_Ll);
        checkAll_Tv = (TextView) findViewById(R.id.checkAll_Tv);
        AKeySend_Tv = (TextView) findViewById(R.id.AKeySend_Tv);

        contacts_top_tip = (TextView) findViewById(R.id.contacts_top_tip);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "邮箱通讯录");

        updatelistitem = new UpdateListItem();
        IntentFilter intentFilter = new IntentFilter(UPDATE_LISTITEM);
        registerReceiver(updatelistitem, intentFilter);

        contactCommand = RenheApplication.getInstance().getContactCommand();
        mContactsMap = new TreeMap<>();

        group = new ArrayList<>();
        cList = new ArrayList<>();
        LVmaillist.setGroupIndicator(null);
        mAdapter = new MailBoxExpandableListAdapter(MailBoxList.this, group, cList);
        LVmaillist.setAdapter(mAdapter);
//        try {
//            // 根据用户email去查本地有没有缓存的上次时间
//            saveTime = contactCommand.getEmailContactSaveTimeByEmail(RenheApplication.getInstance().getUserInfo().getSid());
//        } catch (Exception e) {
//            saveTime = 0l;
//            e.printStackTrace();
//        }
//        long time = System.currentTimeMillis() / 1000;//秒
//        long timeDif = time - saveTime;
//        //上次时间
//        //&& timeDif > 0 && timeDif < space
//        if (saveTime > 0) {
//            localSearch();
//        } else {
        membersList.clear();
        new AsyncQueryMailBoxTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId());
//        }

        handler = new Handler();
        overlayThread = new OverlayThread();

        mNotifHandler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        populateContacts();
                        break;
                    case 0:
                        RenheIMUtil.dismissProgressDialog();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();

        contacts_top_tip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contacts_top_tip.setVisibility(View.GONE);
                RenheApplication.getInstance().getHlEditor().putBoolean(getString(R.string.sharedperferences_email_contacts_first_tip), false);
                RenheApplication.getInstance().getHlEditor().commit();
            }
        });

        letterListView.setTextView(mLetterTxt);
        // 设置右侧触摸监听
        letterListView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (group != null && group.size() > 0) {
                    //找到此元素的所在位置
                    if (s.equals("#")) {
                        LVmaillist.setSelectedGroup(0);
                    } else {
                        for (int i = 0; i < group.size(); i++) {
                            if (group.get(i).equals(s)) {
                                LVmaillist.setSelectedGroup(i);
                                mLetterTxt.setText(group.get(i));
                                mLetterTxt.setVisibility(View.VISIBLE);
                                handler.removeCallbacks(overlayThread);
                                //延迟一秒后执行，让overlay为不可见
                                handler.postDelayed(overlayThread, 2000);
                            }
                        }
                    }
                }
            }
        });

        LVmaillist.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return !(groupPosition == 0 && mAdapter.getGroup(0).toString().contains(HL_FRIENDS));
            }
        });

        LVmaillist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                MailBoxContact cv = (MailBoxContact) mAdapter.getChild(groupPosition, childPosition);
                if (cv != null) {
                    boolean isrenhe = cv.isRenheMember();
                    if (isrenhe) {
                        boolean isSelf = cv.getRenheMemberInfo().isSelf();
                        if (isSelf) {
                            Intent intent = new Intent(MailBoxList.this, MyHomeArchivesActivity.class);
                            intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                    "" + RenheApplication.getInstance().getUserInfo().getSid());
                            intent.putExtra("position", childPosition);
                            startActivity(intent);
                        } else {
                            boolean isAdded = cv.getRenheMemberInfo().isConnection();
                            //是否已经是人和网好友
                            if (isAdded) {
                                Intent intent2 = new Intent(MailBoxList.this, MyHomeArchivesActivity.class);
                                intent2.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                        "" + cv.getRenheMemberInfo().getMemberSId());
                                intent2.putExtra("name", cv.getName());
                                intent2.putExtra("from", Constants.ADDFRIENDTYPE[8]);
                                intent2.putExtra("position", childPosition);
                                startActivity(intent2);
                            } else {
                                Intent intent = new Intent(MailBoxList.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                        "" + cv.getRenheMemberInfo().getMemberSId());
                                intent.putExtra("position", childPosition);
                                intent.putExtra("from", Constants.ADDFRIENDTYPE[8]);
                                intent.putExtra("addfriend_from", "email");
                                startActivity(intent);
                            }
                        }
                    } else {
                        check_Ll.setVisibility(View.VISIBLE);
                        if (isAllChecked) {
                            isAllChecked = false;
                            checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_unsel, 0, 0, 0);
                        }
                        if (cv.getSelect() == 1)
                            cv.setSelect(2);//选中
                        else
                            cv.setSelect(1);
                        mAdapter.notifyDataSetChanged();
                        getSelectCount();
                    }
                    return true;
                }
                return false;
            }
        });

        importBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(MailBoxList.this, "import_email_first_click");
                startActivity(new Intent(MailBoxList.this, MailBoxWebView.class));
                finish();
            }
        });

        //列表全选
        checkAll_Tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllChecked) {
                    isAllChecked = false;
                    checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_unsel, 0, 0, 0);
                    for (int i = 0; i < group.size(); i++) {
                        if (!group.get(i).equals(HL_FRIENDS) && !group.get(i).equals(HL_NO_FRIENDS)) {
                            List<MailBoxContact> emailList = cList.get(i);
                            if (null != emailList && emailList.size() > 0) {
                                for (MailBoxContact mailBoxContact : emailList) {
                                    mailBoxContact.setSelect(2);
                                }
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    checkAll_Tv.setText("全选");
                } else {
                    isAllChecked = true;
                    checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_sel, 0, 0, 0);
                    for (int i = 0; i < group.size(); i++) {
                        if (!group.get(i).equals(HL_FRIENDS) && !group.get(i).equals(HL_NO_FRIENDS)) {
                            List<MailBoxContact> emailList = cList.get(i);
                            if (null != emailList && emailList.size() > 0) {
                                for (MailBoxContact mailBoxContact : emailList) {
                                    mailBoxContact.setSelect(1);
                                }
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    getSelectCount();
                }
            }
        });

        //一键发送
        AKeySend_Tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //上传的ids
                String ids = "";
                for (int i = 0; i < group.size(); i++) {
                    if (!group.get(i).contains(HL_FRIENDS) && !group.get(i).equals(HL_NO_FRIENDS)) {
                        List<MailBoxContact> emailList = cList.get(i);
                        if (null != emailList && emailList.size() > 0) {
                            for (MailBoxContact mailBoxContact : emailList) {
                                if (mailBoxContact.getSelect() == 1) {
                                    ids = ids + mailBoxContact.getContactId() + ",";
                                }
                            }
                        }
                    }
                }
                if (!TextUtils.isEmpty(ids) && ids.endsWith(",")) {
                    ids = ids.substring(0, ids.length() - 1);
                    inviteMailContact(ids);
                }
            }
        });
    }

    //统计选中的个数
    private void getSelectCount() {
        if (null != group && null != cList && group.size() > 0 && cList.size() > 0) {
            int count = 0;
            for (int i = 0; i < group.size(); i++) {
                if (!group.get(i).contains(HL_FRIENDS) && !group.get(i).equals(HL_NO_FRIENDS)) {
                    List<MailBoxContact> emailList = cList.get(i);
                    if (null != emailList && emailList.size() > 0) {
                        for (MailBoxContact mailBoxContact : emailList) {
                            if (mailBoxContact.getSelect() == 1) {
                                count++;
                            }
                        }
                    }
                }
            }
            if (count > 0) {
                checkAll_Tv.setText("全选（" + count + "）");
                AKeySend_Tv.setEnabled(true);
            } else {
                checkAll_Tv.setText("全选");
                AKeySend_Tv.setEnabled(false);
            }
        }
    }

    //设置overlay不可见
    private class OverlayThread implements Runnable {
        @Override
        public void run() {
            mLetterTxt.setVisibility(View.GONE);
        }
    }

    /**
     * 加载email好友，查询本地数据库
     */
    private void localSearch() {
        Runnable loadCacheRun = new Runnable() {
            @Override
            public void run() {
                try {
                    membersList.clear();
                    mContactsMap.clear();

                    group.clear();
                    cList.clear();

                    List<MailBoxContact> contactResultlist;
                    contactResultlist = contactCommand.getAllEmailContact(RenheApplication.getInstance().getUserInfo().getSid());
                    if (null != contactResultlist && contactResultlist.size() > 0) {
                        for (int i = 0; i < contactResultlist.size(); i++) {
                            String n;
                            //已经是人和网会员
                            if (contactResultlist.get(i).isRenheMember()) {
                                //判断和聊会员是否已经是好友
                                RenheMemberInfo member = contactResultlist.get(i).getRenheMemberInfo();
                                if (null != member) {
                                    if (member.isConnection()) {
                                        membersList.add(contactResultlist.get(i));//好友
                                    } else {
                                        addFriendList.add(contactResultlist.get(i));//非好友会员
                                    }
                                }
                            } else {
                                String namePinyin = PinyinUtil.cn2FirstSpellWithout(contactResultlist.get(i).getName());
                                if (namePinyin.length() > 0) {
                                    n = namePinyin.substring(0, 1).toUpperCase();
                                } else {
                                    n = "*";
                                }
                                List<MailBoxContact> ctList = mContactsMap.get(n);
                                if (null == ctList) {
                                    ctList = new ArrayList<>();
                                }
                                ctList.add(contactResultlist.get(i));
                                mContactsMap.put(n, ctList);
                            }
                        }
                        mNotifHandler.sendEmptyMessage(1);
                    } else {
                        mNotifHandler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    mNotifHandler.sendEmptyMessage(0);
                    System.out.println(e);
                }
            }
        };
        new Thread(loadCacheRun).start();
        RenheIMUtil.showProgressDialog(MailBoxList.this, R.string.contact_info_loading, true);
    }

    private void populateContacts() {
        RenheIMUtil.dismissProgressDialog();
        //默认导入
        boolean isShowTopTip = RenheApplication.getInstance().getHlSharedPreferences().getBoolean(getString(R.string.sharedperferences_email_contacts_first_tip), true);
        if (isShowTopTip) {
            contacts_top_tip.setVisibility(View.VISIBLE);
        } else {
            contacts_top_tip.setVisibility(View.GONE);
        }

        if (null != membersList && membersList.size() > 0) {
            //加载好友会员
            group.add(HL_FRIENDS + "(" + membersList.size() + ")");
            cList.add(membersList);
        }
        if (null != addFriendList && addFriendList.size() > 0) {
            //加载好友会员
            group.add(HL_NO_FRIENDS);
            cList.add(addFriendList);
        }
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            //加载非会员
            Set keySet = mContactsMap.keySet();
            for (Object key : keySet) {
                group.add(key.toString());
                cList.add(mContactsMap.get(key));
            }
        }
        if (group.size() > 0) {
            mAdapter.notifyDataSetChanged();
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                if (mAdapter.getGroup(i).toString().contains(HL_FRIENDS)) {
                    LVmaillist.collapseGroup(i);
                } else {
                    LVmaillist.expandGroup(i);
                }
            }
        }
    }

    /**
     * 获取服务器端的通讯录
     */
    private class AsyncQueryMailBoxTask extends AsyncTask<String, Void, ContactResultByMailBox> {
        @Override
        protected ContactResultByMailBox doInBackground(String... params) {
            try {
                Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                return (ContactResultByMailBox) HttpUtil.doHttpRequest(Constants.Http.GET_EMAIL_CONTACTS,
                        reqParams, ContactResultByMailBox.class, null);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RenheIMUtil.showProgressDialog(MailBoxList.this, R.string.contact_info_loading, true);
        }

        @Override
        protected void onPostExecute(ContactResultByMailBox result) {
            super.onPostExecute(result);
            if (null != result) {
                if (1 == result.getState()) {
                    membersList = result.getFriendlist();//好友会员
                    addFriendList = result.getNofriendlist();//非好友会员
                    List<MailBoxContact> contactResult = result.getNomemberlist();//非会员
                    if ((null != membersList && membersList.size() > 0) || (null != addFriendList && addFriendList.size() > 0) || contactResult.size() > 0) {
                        mobileMailImportLl.setVisibility(View.GONE);
                        mobilemail_list.setVisibility(View.VISIBLE);
                        //非会员按字母排序
                        for (int i = 0; i < contactResult.size(); i++) {
                            String n = MailBoxList.getAlpha(contactResult.get(i));
                            List<MailBoxContact> ctList = mContactsMap.get(n);
                            if (null == ctList) {
                                ctList = new ArrayList<>();
                            }
                            contactResult.get(i).setSelect(2);//未选状态
                            ctList.add(contactResult.get(i));
                            mContactsMap.put(n, ctList);
                        }
                        populateContacts();
                        //保存到本地
//                        List<MailBoxContact> allContacts = new ArrayList<>();
//                        if (null != membersList && membersList.size() > 0)
//                            allContacts.addAll(membersList);
//                        if (null != addFriendList && addFriendList.size() > 0)
//                            allContacts.addAll(addFriendList);
//                        if (contactResult.size() > 0)
//                            allContacts.addAll(contactResult);
//                        if (allContacts.size() > 0) {
//                            long now = System.currentTimeMillis() / 1000;//秒
//                            try {
//                                contactCommand.saveEmailContactList(allContacts,
//                                        RenheApplication.getInstance().getUserInfo().getSid(), "" + now);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                    } else {
                        mobileMailImportLl.setVisibility(View.VISIBLE);
                        mobilemail_list.setVisibility(View.GONE);
                    }
                } else {
                    ToastUtil.showErrorToast(MailBoxList.this, R.string.fetch_contacts_failed);
                    mobileMailImportLl.setVisibility(View.GONE);
                    mobilemail_list.setVisibility(View.VISIBLE);
                }
            } else {
                ToastUtil.showNetworkError(MailBoxList.this);
            }
            RenheIMUtil.dismissProgressDialog();
        }
    }

    /**
     * 邀请接口
     */
    public void inviteMailContact(final String ids) {
        RenheIMUtil.showProgressDialog(MailBoxList.this, R.string.request_handling);
        //发送一键发送请求
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("ids", ids);
        try {
            OkHttpClientManager.postAsyn(Constants.Http.A_KEY_INVITE_EMAIL_CONTACTS, reqParams, InviteFriendsResult.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showConnectError(MailBoxList.this);
                }

                @Override
                public void onResponse(Object response) {
                    RenheIMUtil.dismissProgressDialog();
                    InviteFriendsResult result = (InviteFriendsResult) response;
                    if (null != result) {
                        switch (result.getState()) {
                            case 1:
//                                startActivity(new Intent(MailBoxList.this, InvitedSuccessActivity.class).putExtra("inviteNumb", result.getNum()).putExtra("from", "email"));
//                                for (int i = 0; i < chooseList.size(); i++) {
//                                    chooseList.get(i).setIsInvited();
//                                }
                                mAdapter.notifyDataSetChanged();
                                ToastUtil.showToast(MailBoxList.this, "您已成功发送" + result.getNum() + "封邀请邮件");
                                break;
                            case -2:
                                ToastUtil.showErrorToast(MailBoxList.this, result.getErrorInfo());
                                break;
                        }
                    }
                }
            }, getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class UpdateListItem extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(UPDATE_LISTITEM)) {
                int position = intent.getExtras().getInt("position");//为和聊会员 group：0 的position
                if (null != cList && null != cList.get(0) && null != cList.get(0).get(position))
                    cList.get(0).get(position).getRenheMemberInfo().setInvite(true);
//                contactResultlist.get(position).getRenheMemberInfo().setInvite(true);
                mAdapter.notifyDataSetChanged();
                View convertView = LVmaillist.getChildAt(position);
                if (null != convertView) {
                    ViewHolder vh = (ViewHolder) convertView.getTag();
                    vh.add_btn = (Button) convertView.findViewById(R.id.add_btn);
                    vh.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
                    vh.add_btn.setVisibility(View.GONE);
                    vh.added_txt.setVisibility(View.VISIBLE);
                    vh.added_txt.setText("已邀请");
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem add = menu.findItem(R.id.item_add);
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        add.setIcon(R.drawable.add_email);
        add.setTitle("导入");
        add.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                startActivity(new Intent(MailBoxList.this, MailBoxWebView.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
     * 获得汉语拼音首字母 （区分人和网会员与否）
     */
    public static String getAlpha(MailBoxContact contact) {
        if (contact == null) {
            return "";
        } else {
//            //人和网会员
//            if (contact.isRenheMember()) {
//                return "#";
//            } else {
            String nameStr = contact.getName();
            if (nameStr == null) {
                return "*";
            }

            if (nameStr.trim().length() == 0) {
                return "*";
            }
            String str = PinyinUtil.cn2FirstSpellWithout(nameStr);
            if (str.trim().length() == 0) {
                return "*";
            }
            char c = str.trim().substring(0, 1).charAt(0);
            // 正则表达式，判断首字母是否是英文字母
            Pattern pattern = Pattern.compile("^[A-Za-z]+$");
            if (pattern.matcher(c + "").matches()) {
                return (c + "").toUpperCase();
            } else {
                return "*";
            }
        }
//        }
    }
}
