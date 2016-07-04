package com.itcalf.renhe.context.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.MobileMailListAdapter;
import com.itcalf.renhe.adapter.MobileMailListAdapter.ViewHolder;
import com.itcalf.renhe.bean.ContactsReturn;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.utils.CheckPermissionUtil;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.SideBar;
import com.itcalf.renhe.view.SideBar.OnTouchingLetterChangedListener;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @author chan
 * @createtime 2015-1-6
 * @功能说明 手机联系人 服务器端返回的列表
 */
public class MobileMailList extends BaseActivity {
    /****
     * 导入界面
     ****/
    private LinearLayout mobilemail_import;
    private Button importBtn;
    private LinearLayout no_permission;
    /*****
     * 返回的通讯录列表
     ****/
    private RelativeLayout mobilemail_list;
    private ListView LVmaillist;
    private SideBar letterListView;
    private TextView mLetterTxt;
    private LinearLayout check_Ll;
    private TextView checkAll_Tv;
    private TextView AKeySend_Tv;
    //顶部操作提示，仅第一次进入提示，点击消失
    private RelativeLayout contacts_top_tip_Rl;
    private ImageView contacts_top_tip_close;
    private boolean isShowTopTip = false;

    private BaseAdapter adapter;
    private HashMap<String, Integer> alphaIndexer;//存放存在的汉语拼音首字母和与之对应的列表位置
    private String[] sections;//存放存在的汉语拼音首字母
    private Handler handler;
    private OverlayThread overlayThread;
    private List<ContactResult> contactResultlist;
    private int maxId = 0;
    private String pageEndChar = " ";//分页加载最后一条数据的拼音首字母
    private int totalNumb = 0;
    public static final String UPDATE_LISTITEM = "mobilemail_upload_list_item";
    private UpdateListItem updatelistitem = null;
    private Map<String, List<ContactResult>> mContactsMap;
    private Handler mNotifHandler;
    private IContactCommand contactCommand;
    /**
     * 会员list
     **/
    List<ContactResult> membersList = new ArrayList<ContactsReturn.ContactResult>();
    private SyncMobileContactsReceiver uploadReceiver = null;
    private boolean isAllChecked = false;//默认未全选

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.mobile_maillist);
    }

    @Override
    protected void findView() {
        super.findView();

        no_permission = (LinearLayout) findViewById(R.id.no_permission);
        no_permission.setVisibility(View.GONE);
        mobilemail_import = (LinearLayout) findViewById(R.id.mobilemail_import);
        mobilemail_list = (RelativeLayout) findViewById(R.id.mobilemail_list);
        importBtn = (Button) findViewById(R.id.import_Btn);

        LVmaillist = (ListView) findViewById(R.id.LVmaillist);
        letterListView = (SideBar) findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        mobilemail_import.setVisibility(View.VISIBLE);
        mobilemail_list.setVisibility(View.GONE);

        check_Ll = (LinearLayout) findViewById(R.id.check_Ll);
        checkAll_Tv = (TextView) findViewById(R.id.checkAll_Tv);
        AKeySend_Tv = (TextView) findViewById(R.id.AKeySend_Tv);

        contacts_top_tip_Rl = (RelativeLayout) findViewById(R.id.contacts_top_tip_Rl);
        contacts_top_tip_close = (ImageView) findViewById(R.id.contacts_top_tip_close);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "手机通讯录");
        mobilemail_import.setVisibility(View.GONE);

        updatelistitem = new UpdateListItem();
        IntentFilter intentFilter = new IntentFilter(UPDATE_LISTITEM);
        registerReceiver(updatelistitem, intentFilter);

        uploadReceiver = new SyncMobileContactsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BroadCastAction.UPLOAD_MOBILE_CONTACTS_ACTION);
        filter.addAction(Constants.BroadCastAction.UPLOAD_MOBILE_CONTACTS_FAILED_ACTION);
        registerReceiver(uploadReceiver, filter);

        mContactsMap = new TreeMap<>();
        contactResultlist = new ArrayList<>();
        adapter = new MobileMailListAdapter(MobileMailList.this, contactResultlist, pageEndChar);
        LVmaillist.setAdapter(adapter);

        contactCommand = RenheApplication.getInstance().getContactCommand();
//        try {
//            // 根据用户email去查本地有没有缓存的最大的maxid
//            maxId = contactCommand.getMobileContactMaxidByEmail(RenheApplication.getInstance().getUserInfo().getSid());
//        } catch (Exception e) {
//            maxId = 0;
//            e.printStackTrace();
//        }
//        if (maxId != 0) {
//            localSearch();
//        } else {
        membersList.clear();
        RenheIMUtil.showProgressDialog(MobileMailList.this, R.string.contact_info_loading, true);
        new AsyncQueryMobileMailTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId(),
                "" + maxId);
//        }
        alphaIndexer = new HashMap<>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        mNotifHandler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        populateContacts();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();

        contacts_top_tip_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contacts_top_tip_Rl.setVisibility(View.GONE);
                RenheApplication.getInstance().getHlEditor().putBoolean(getString(R.string.sharedperferences_mobile_contacts_first_tip), false);
                RenheApplication.getInstance().getHlEditor().commit();
            }
        });

        importBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //先去检查权限
                if (CheckPermissionUtil.isHasMailListPermission(RenheApplication.getInstance())
                        && CheckPermissionUtil.isAllowedMailListPermission(RenheApplication.getInstance())) {
                    RenheIMUtil.showProgressDialog(MobileMailList.this, R.string.loading);
                    SharedPreferences sp = getSharedPreferences(
                            "last_upload_mobile_time" + RenheApplication.getInstance().getUserInfo().getSid(), 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isAuthImport", true);
                    editor.commit();
                    new ContactsUtil(MobileMailList.this).SyncMobileContacts();
                    MobclickAgent.onEvent(MobileMailList.this, "import_mobile_first_click");
                } else {
                    MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(MobileMailList.this);
                    materialDialog.showStacked(R.string.no_permission_tip, R.string.contactspermission_guide, R.string.set_permission,
                            R.string.cancel_permission).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                        }
                    });
                    materialDialog.show();
                }
            }
        });

        letterListView.setTextView(mLetterTxt);
        // 设置右侧触摸监听
        letterListView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (alphaIndexer.get(s) != null) {
                    int position = alphaIndexer.get(s);
                    LVmaillist.setSelection(position);
                    mLetterTxt.setText(sections[position]);
                    mLetterTxt.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(overlayThread);
                    //延迟一秒后执行，让overlay为不可见
                    handler.postDelayed(overlayThread, 2000);
                }
            }
        });

        LVmaillist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactResult cv = (ContactResult) adapter.getItem(position);
                if (cv != null) {
                    boolean isrenhe = cv.isRenheMember();
                    if (isrenhe) {
                        boolean isSelf = cv.getRenheMemberInfo().isSelf();
                        if (isSelf) {
                            Intent intent = new Intent(MobileMailList.this, MyHomeArchivesActivity.class);
                            intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                    "" + RenheApplication.getInstance().getUserInfo().getSid());
                            intent.putExtra("position", position);
                            startActivity(intent);
                        } else {
                            boolean isAdded = cv.getRenheMemberInfo().isConnection();
                            //是否已经是人和网好友
                            if (isAdded) {
                                Intent intent2 = new Intent(MobileMailList.this, MyHomeArchivesActivity.class);
                                intent2.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                        "" + cv.getRenheMemberInfo().getMemberSId());
                                intent2.putExtra("name", cv.getName());
                                intent2.putExtra("from", Constants.ADDFRIENDTYPE[9]);
                                intent2.putExtra("position", position);
                                startActivity(intent2);
                            } else {
                                Intent intent = new Intent(MobileMailList.this, MyHomeArchivesActivity.class);
                                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                        "" + cv.getRenheMemberInfo().getMemberSId());
                                intent.putExtra("position", position);
                                intent.putExtra("from", Constants.ADDFRIENDTYPE[9]);
                                intent.putExtra("addfriend_from", "mobile");
                                startActivity(intent);
                            }
                        }
                    } else {
                        check_Ll.setVisibility(View.VISIBLE);
                        //显示选择框，单击则进行选择
                        int state = contactResultlist.get(position).getSelectState();
                        if (state == 1) {
                            contactResultlist.get(position).setSelectState(2);
                        } else if (state == 2) {
                            contactResultlist.get(position).setSelectState(1);
                        }
                        adapter.notifyDataSetChanged();
                        getSelectCount(contactResultlist);
                        //如果是全选状态，点击取消全选
                        if (isAllChecked) {
                            isAllChecked = false;
                            checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_unsel, 0, 0, 0);
                        }
                    }
                }
            }
        });

        //列表全选
        checkAll_Tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllChecked) {
                    isAllChecked = false;
                    checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_unsel, 0, 0, 0);
                    for (int i = 0; i < contactResultlist.size(); i++) {
                        contactResultlist.get(i).setSelectState(2);
                    }
                    adapter.notifyDataSetChanged();
                    checkAll_Tv.setText("全选");
                } else {
                    isAllChecked = true;
                    checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_sel, 0, 0, 0);
                    for (int i = 0; i < contactResultlist.size(); i++) {
                        contactResultlist.get(i).setSelectState(1);
                    }
                    adapter.notifyDataSetChanged();
                    getSelectCount(contactResultlist);
                }
            }
        });

        //一键发送
        AKeySend_Tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //上传的ids
                String ids = "";
                for (int i = 0; i < contactResultlist.size(); i++) {
                    if (contactResultlist.get(i).getSelectState() == 1) {
                        ids = ids + contactResultlist.get(i).getContactId() + ",";
                    }
                }
                if (!TextUtils.isEmpty(ids) && ids.endsWith(",")) {
                    ids = ids.substring(0, ids.length() - 1);
                    RenheIMUtil.showProgressDialog(MobileMailList.this, R.string.request_handling);
                    //发送一键发送请求
                    Map<String, Object> reqParams = new HashMap<>();
                    reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                    reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
                    reqParams.put("ids", ids);
                    try {
                        OkHttpClientManager.postAsyn(Constants.Http.A_KEY_INVITE_MOBILE_CONTACTS, reqParams, InviteFriendsResult.class, new OkHttpClientManager.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                RenheIMUtil.dismissProgressDialog();
                                ToastUtil.showConnectError(MobileMailList.this);
                            }

                            @Override
                            public void onResponse(Object response) {
                                RenheIMUtil.dismissProgressDialog();
                                InviteFriendsResult result = (InviteFriendsResult) response;
                                if (null != result) {
                                    switch (result.getState()) {
                                        case 1:
//                                            startActivity(new Intent(MobileMailList.this, InvitedSuccessActivity.class).putExtra("inviteNumb", result.getNum()).putExtra("from", "mobile"));
                                            ToastUtil.showToast(MobileMailList.this, "您已成功发送" + result.getNum() + "条邀请短信!");
                                            break;
                                        case -2:
                                            ToastUtil.showErrorToast(MobileMailList.this, result.getErrorInfo());
                                            break;
                                    }
                                }
                            }
                        }, getClass().getSimpleName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //统计选中的个数
    private void getSelectCount(List<ContactResult> contactResultlist) {
        if (null != contactResultlist && contactResultlist.size() > 0) {
            int count = 0;
            for (int i = 0; i < contactResultlist.size(); i++) {
                if (contactResultlist.get(i).getSelectState() == 1) {
                    count++;
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
     * 加载mobile好友，查询本地数据库
     */
    private void localSearch() {
        RenheIMUtil.showProgressDialog(MobileMailList.this, R.string.contact_info_loading, true);
        Runnable loadCacheRun = new Runnable() {
            @Override
            public void run() {
                try {
                    membersList.clear();
                    mContactsMap.clear();
                    List<ContactResult> contactResultlist;
                    contactResultlist = contactCommand.getAllMobileContact(RenheApplication.getInstance().getUserInfo().getSid());
                    if (null != contactResultlist && contactResultlist.size() > 0) {
                        for (int i = 0; i < contactResultlist.size(); i++) {
                            String n = "";
                            //已经是人和网会员
                            if (contactResultlist.get(i).isRenheMember()) {
                                n = "#";
                                membersList.add(contactResultlist.get(i));
                            } else {
                                String namePinyin = PinyinUtil.cn2FirstSpell(contactResultlist.get(i).getName());
                                if (null != namePinyin && namePinyin.length() > 0) {
                                    n = namePinyin.substring(0, 1).toUpperCase();
                                }
                                List<ContactResult> ctList = mContactsMap.get(n);
                                if (null == ctList) {
                                    ctList = new ArrayList<>();
                                }
                                contactResultlist.get(i).setSelectState(2);
                                ctList.add(contactResultlist.get(i));
                                mContactsMap.put(n, ctList);
                            }
                        }
                    }
                    mNotifHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        };
        new Thread(loadCacheRun).start();
        mobilemail_import.setVisibility(View.GONE);
        mobilemail_list.setVisibility(View.VISIBLE);
    }

    private void populateContacts() {
        RenheIMUtil.dismissProgressDialog();
        //默认导入
        isShowTopTip = RenheApplication.getInstance().getHlSharedPreferences().getBoolean(
                getString(R.string.sharedperferences_mobile_contacts_first_tip), true);
        if (isShowTopTip) {
            contacts_top_tip_Rl.setVisibility(View.VISIBLE);
        } else {
            contacts_top_tip_Rl.setVisibility(View.GONE);
        }
        if ((null != membersList && membersList.size() > 0) || (null != mContactsMap && !mContactsMap.isEmpty())) {
            //加载会员
            if (membersList != null && membersList.size() > 0) {
                contactResultlist.addAll(membersList);
            }

            // 联系人列表加载
            Set<Entry<String, List<ContactResult>>> set = mContactsMap.entrySet();
            Iterator<Entry<String, List<ContactResult>>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<java.lang.String, java.util.List<ContactResult>> entry = it.next();
                List<ContactResult> contactsList = entry.getValue();
                contactResultlist.addAll(contactsList);
            }

            sections = new String[contactResultlist.size()];
            for (int i = 0; i < contactResultlist.size(); i++) {
                //当前汉语拼音首字母
                String currentStr = MobileMailList.getAlpha(contactResultlist.get(i));
                //上一个汉语拼音首字母，如果不存在为“ ”
                String previewStr = (i - 1) >= 0 ? MobileMailList.getAlpha(contactResultlist.get(i - 1)) : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = MobileMailList.getAlpha(contactResultlist.get(i));
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            mobilemail_import.setVisibility(View.VISIBLE);
            mobilemail_list.setVisibility(View.GONE);
            ToastUtil.showToast(MobileMailList.this, R.string.mobile_contacts_import_filter_tip);
        }
    }

    /**
     * 获取服务器端的通讯录
     */
    private class AsyncQueryMobileMailTask extends AsyncTask<String, Void, ContactsReturn> {

        @Override
        protected ContactsReturn doInBackground(String... params) {
            try {
                Map<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                reqParams.put("maxId", params[2]);
                ContactsReturn info = (ContactsReturn) HttpUtil.doHttpRequest(Constants.Http.GET_IMPORT_CONTACTS, reqParams,
                        ContactsReturn.class, null);
                return info;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ContactsReturn result) {
            super.onPostExecute(result);
            if (null != result) {
                if (1 == result.getState()) {
                    maxId = result.getMaxId();
                    //获取maxId,如果为0，表示服务器端无数据，显示导入界面，否则，显示列表界面
                    if (maxId == 0) {
                        RenheIMUtil.dismissProgressDialog();
                        mobilemail_import.setVisibility(View.VISIBLE);
                        mobilemail_list.setVisibility(View.GONE);
                    } else {
                        //同步手机联系
                        new ContactsUtil(MobileMailList.this).SyncMobileContacts();
                        mobilemail_import.setVisibility(View.GONE);
                        mobilemail_list.setVisibility(View.VISIBLE);
                        totalNumb = result.getCount();

                        List<ContactResult> contactResult = result.getContactResult();
                        if (contactResult.size() > 0) {
                            for (int i = 0; i < contactResult.size(); i++) {
                                if (contactResult.get(i).isRenheMember()) {
                                    membersList.add(contactResult.get(i));
                                } else {
                                    String n = MobileMailList.getAlpha(contactResult.get(i));
                                    List<ContactResult> ctList = mContactsMap.get(n);
                                    if (null == ctList) {
                                        ctList = new ArrayList<>();
                                    }
                                    ctList.add(contactResult.get(i));
                                    mContactsMap.put(n, ctList);
                                }
                                contactResult.get(i).setSelectState(2);
                            }
//                            //存本地数据库
//                            try {
//                                contactCommand.saveMobileContactList(contactResult,
//                                        RenheApplication.getInstance().getUserInfo().getSid(), maxId);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            if (contactResult.size() < totalNumb) {
                                new AsyncQueryMobileMailTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                        RenheApplication.getInstance().getUserInfo().getSid(),
                                        RenheApplication.getInstance().getUserInfo().getAdSId(), "" + maxId);
                            } else {
                                populateContacts();
                            }
                        } else {
                            populateContacts();
                        }
                    }
                } else {
                    ToastUtil.showErrorToast(MobileMailList.this, "通讯录获取失败");
                    mobilemail_import.setVisibility(View.GONE);
                    mobilemail_list.setVisibility(View.VISIBLE);
                    RenheIMUtil.dismissProgressDialog();
                }
            } else {
                ToastUtil.showNetworkError(MobileMailList.this);
                RenheIMUtil.dismissProgressDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updatelistitem) {
            unregisterReceiver(updatelistitem);
            updatelistitem = null;
        }
        if (null != uploadReceiver) {
            unregisterReceiver(uploadReceiver);
            uploadReceiver = null;
        }
    }

    /**
     * 获得汉语拼音首字母 （区分人和网会员与否）
     */
    public static String getAlpha(ContactResult contact) {
        if (contact == null) {
            return "";
        } else {
            //人和网会员
            if (contact.isRenheMember()) {
                return "#";
            } else {
                String nameStr = contact.getName();
                if (nameStr == null) {
                    return "*";
                }

                if (nameStr.trim().length() == 0) {
                    return "*";
                }
                String str = PinyinUtil.cn2FirstSpell(nameStr);
                if (str == null || str.trim().length() == 0) {
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
        }
    }

    //广播
    class UpdateListItem extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(UPDATE_LISTITEM)) {
                int position = intent.getExtras().getInt("position");
                contactResultlist.get(position).getRenheMemberInfo().setInvite(true);
                adapter.notifyDataSetChanged();
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

    //上传成功之后的处理
    class SyncMobileContactsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BroadCastAction.UPLOAD_MOBILE_CONTACTS_ACTION)) {
                RenheIMUtil.dismissProgressDialog();
                mobilemail_import.setVisibility(View.GONE);
                mobilemail_list.setVisibility(View.VISIBLE);
                maxId = 0;
                membersList.clear();
                RenheIMUtil.showProgressDialog(MobileMailList.this, R.string.contact_info_loading);
                new AsyncQueryMobileMailTask().executeOnExecutor(Executors.newCachedThreadPool(),
                        RenheApplication.getInstance().getUserInfo().getSid(),
                        RenheApplication.getInstance().getUserInfo().getAdSId(), "" + maxId);
                //发广播通知 限额页面刷新
                Intent i = new Intent(Constants.BroadCastAction.UPDATE_ACCOUNTLIMIT_ACTION);
                sendBroadcast(i);
            } else if (intent.getAction().equals(Constants.BroadCastAction.UPLOAD_MOBILE_CONTACTS_FAILED_ACTION)) {
                RenheIMUtil.dismissProgressDialog();
            }
        }
    }
}
