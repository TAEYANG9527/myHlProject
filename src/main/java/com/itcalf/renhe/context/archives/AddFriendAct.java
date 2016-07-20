package com.itcalf.renhe.context.archives;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.contacts.GuessInterestActivity;
import com.itcalf.renhe.context.contacts.MailBoxList;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.contacts.NewFriendsAct;
import com.itcalf.renhe.context.luckymoneyaddfriend.AddFriendLuckyMoneySendActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.AddFriend;
import com.itcalf.renhe.eventbusbean.NotifyListRefreshWithPositionEvent;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;


import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executors;

/**
 * @author chan
 * @createtime 2014-11-4
 * @功能说明 添加好友验证
 */
public class AddFriendAct extends BaseActivity {
    private TextView titleTxt;
    private EditText inviteWord;
    private ListView addreasonLv;
    private LinearLayout addLuckyLl;
    private String mSid = "";
    private String friendName = "";
    private String message = "";
    private String reason = "";
    private int position = -1;
    private String fromClass = "";//界面跳转
    String[] data = new String[]{"提供职业发展机会", "寻找潜在的合作机会", "推广产品和服务", "讨论新产品，新技术，交流行业资讯", "结实真心朋友", "其他"};
    //好友添加来源
    private int isFrom = 0;
    private CheckUpgradeUtil checkUpgradeUtil;

    private static final int REQUEST_CODE_ADD_LUCKY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.addfriend);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(88);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                return true;
            case R.id.item_send:
                addFriend();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem send = menu.findItem(R.id.item_send);
        send.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        send.setIcon(null);
        send.setVisible(true);
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    protected void findView() {
        super.findView();
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        inviteWord = (com.itcalf.renhe.view.EditText) findViewById(R.id.inviteWord);
        addreasonLv = (ListView) findViewById(R.id.addreasonLv);
        addLuckyLl = (LinearLayout) findViewById(R.id.add_red_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        Bundle b = this.getIntent().getExtras();
        mSid = b.getString("mSid");
        friendName = b.getString("friendName");
        position = b.getInt("position", -1);
        fromClass = b.getString("addfriend_from");
        isFrom = getIntent().getIntExtra("from", 0);
        if (!TextUtils.isEmpty(friendName)) {
            setTextValue(R.id.title_txt, "邀请" + friendName + "成为好友");
            titleTxt.setText("你想对" + friendName + "说的话");
        }
        titleTxt.setFocusable(true);
        if (RenheApplication.getInstance().getUserInfo() != null) {
            String company = RenheApplication.getInstance().getUserInfo().getCompany();
            String position = RenheApplication.getInstance().getUserInfo().getTitle();
            String name = RenheApplication.getInstance().getUserInfo().getName();
            company = TextUtils.isEmpty(company) ? "" : company;
            position = TextUtils.isEmpty(position) ? "" : position;
            name = TextUtils.isEmpty(name) ? "" : name;
            String circleName = getIntent().getStringExtra("circleName");
            if (!TextUtils.isEmpty(circleName)) {
                inviteWord.setText("我是来自 " + circleName + " 圈子的" + name);
            } else {
                inviteWord.setText(R.string.add_friend_tip);
            }
        }
        inviteWord.setSelection(inviteWord.length());

        AddreasonAdapter aaCheckedTextViewAdapter = new AddreasonAdapter();
        // ArrayAdapter<String> aaCheckedTextViewAdapter = new
        // ArrayAdapter<String>(
        // this, android.R.layout.simple_list_item_checked, data);
        // // 设置CheckedTextView的样式
        // CheckedTextView carriageNumtxt = (CheckedTextView)
        // findViewById(android.R.id.text1);
        // carriageNumtxt.setTextAppearance(this,
        // android.R.attr.textAppearanceLarge);
        addreasonLv.setAdapter(aaCheckedTextViewAdapter);
        addreasonLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        checkUpgradeUtil = new CheckUpgradeUtil(this);
    }

    @Override
    protected void initListener() {
        super.initListener();

        addreasonLv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int i = arg2 + 1;
                reason = "" + i;
            }
        });
        addLuckyLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendAct.this, AddFriendLuckyMoneySendActivity.class);
                intent.putExtra("toMemberSid", mSid);
                intent.putExtra("note", message);
                startHlActivityForResult(intent, REQUEST_CODE_ADD_LUCKY);
            }
        });
    }

    private void addFriend() {
        message = inviteWord.getText().toString().trim();
        new AddFriendTask(AddFriendAct.this, new AddFriendTask.IAddFriendCallBack() {
            @Override
            public void onPre() {
                showDialog(3);
            }

            @Override
            public void doPost(AddFriend result) {
                removeDialog(3);
                if (result == null) {
                    ToastUtil.showErrorToast(AddFriendAct.this, getString(R.string.connect_server_error));
                    return;
                } else if (result.getState() == 1) {
                    if (position != -1 && "mobile".equals(fromClass)) {
                        // 通知手机联系人界面更新
                        Intent intent = new Intent(MobileMailList.UPDATE_LISTITEM);
                        intent.putExtra("position", position);
                        sendBroadcast(intent);
                        checkUpgradeUtil.checkUpgrade();//检测是否要提醒升级的弹框
                    } else if (position != -1 && "email".equals(fromClass)) {
                        // 通知邮箱通讯录界面更新
                        Intent intent = new Intent(MailBoxList.UPDATE_LISTITEM);
                        intent.putExtra("position", position);
                        sendBroadcast(intent);
                    } else if (position != -1 && "newFriend".equals(fromClass)) {
                        // 通知新的好友界面更新
                        Intent intent = new Intent(NewFriendsAct.UPDATE_LISTITEM);
                        intent.putExtra("position", position);
                        sendBroadcast(intent);
                    } else if (position != -1 && "guessInterest".equals(fromClass)) {
                        // 通知猜你感兴趣界面更新
                        Intent intent = new Intent(GuessInterestActivity.UPDATE_LIST_ITEM);
                        intent.putExtra("position", position);
                        sendBroadcast(intent);
                    } else if (position != -1 && "advanceSearchResult".equals(fromClass)) {//来自高级搜索的结果页面
                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.ADVANCE_SEARCH, position, NotifyListRefreshWithPositionEvent.MODE_ADD));
                    } else if (position != -1 && "renmaiSearchResultMore".equals(fromClass)) {//来自人脉搜索，发现人脉查看更多的页面
                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.RENMAI_SEARCH_MORE, position, NotifyListRefreshWithPositionEvent.MODE_ADD));
                    } else if (position != -1 && "renmaiSearchResult".equals(fromClass)) {//来自人脉搜索的页面
                        EventBus.getDefault().post(new NotifyListRefreshWithPositionEvent(NotifyListRefreshWithPositionEvent.RENMAI_SEARCH, position, NotifyListRefreshWithPositionEvent.MODE_ADD));
                    }
                    setResult(99);
                    finish();
                    ToastUtil.showToast(AddFriendAct.this, R.string.success_friend_request);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), mSid, message, reason, "" + isFrom);
    }

    class AddreasonAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int arg0) {
            return data[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(AddFriendAct.this);
                // 使用android自带的android.R.layout.simple_list_item_single_choice
                convertView = (View) inflater.inflate(android.R.layout.simple_list_item_checked, null);
                // 设置背景颜色
                convertView.setBackgroundColor(Color.WHITE);
            }
            CheckedTextView carriageNumtxt = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            // 设置CheckedTextView的样式
            carriageNumtxt.setSingleLine(true);
            carriageNumtxt.setTypeface(Constants.APP_TYPEFACE);
            carriageNumtxt.setTextSize(15f);
            carriageNumtxt.setTextColor(getResources().getColor(R.color.C1));
            carriageNumtxt.setText(data[arg0]);
            carriageNumtxt.setCheckMarkDrawable(getResources().getDrawable(R.drawable.selector_check_box));
            carriageNumtxt.setHeight(DensityUtil.dip2px(AddFriendAct.this, 44));
            carriageNumtxt.setPadding(0, 0, 30, 0);
            return convertView;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 3:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.add_friend_sending).cancelable(false)
                        .build();
            default:
                return null;
        }
    }

    // 监控返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
            setResult(88);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_LUCKY:
                    addFriend();
                    break;
                default:
                    break;
            }
        }
    }
}
